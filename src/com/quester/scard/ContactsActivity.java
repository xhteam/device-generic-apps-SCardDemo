package com.quester.scard;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ContactsActivity extends BaseListActivity {
	
	private List<HashMap<String, String>> mContactsList;
	private boolean mCompleted = false;
	private boolean mPause;
	private byte[] recv = new byte[128];
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			dismissProgressDialog();
			switch (msg.what) {
			case Common.DONE:
				if (msg.arg1 == 1) {
					if (mContactsList.size() > 0) {
						showContacts();
					} else {
						showDialog(getString(R.string.adn), getString(R.string.record_empty));
					}
				} else {
					finish();
				}
				break;
			case Common.ACCESS:
				showInputDialog(getString(R.string.pin), getString(R.string.pin_summary));
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void doNext() {
		if (inputStr == null) {
			showInputDialog(getString(R.string.pin), getString(R.string.pin_summary));
			Toast.makeText(this, R.string.pin_format, Toast.LENGTH_SHORT).show();
			return;
		}
		int lgth = inputStr.length();
		if (lgth < 4 || lgth > 8) {
			showInputDialog(getString(R.string.pin), getString(R.string.pin_summary));
			Toast.makeText(this, R.string.pin_format, Toast.LENGTH_SHORT).show();
			return;
		}
		System.arraycopy(inputStr.getBytes(), 0, Common.verify_chv1, 5, lgth);
		Common.mgr.scardTransmit(Common.verify_chv1, Common.verify_chv1.length, 
				recv, Integer.valueOf(recv.length));
		if (recv[0] == (byte)0x90 && recv[1] == 0x00) {
			readRecord();
		} else {
			if (recv[0] == 0x63) {
				int retries = recv[1];
				showInputDialog(getString(R.string.pin), getString(R.string.pin_summary));
				Toast.makeText(this, getString(R.string.pin_err) + retries, 
						Toast.LENGTH_SHORT).show();
			} else if (recv[0] == (byte)0x98) {
				if (recv[1] == 0x40) {
					Toast.makeText(this, R.string.chv_block, Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContactsList = new ArrayList<HashMap<String,String>>();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mPause = false;
		if (!mCompleted) {
			readRecord();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mPause = true;
	}
	
	private void showContacts() {
		SimpleAdapter adapter = new SimpleAdapter(this, 
				mContactsList,
				R.layout.contacts_activity,
				new String[]{"name", "num"},
				new int[]{R.id.contact_name, R.id.contact_num});
		setListAdapter(adapter);
	}
	
	private void readRecord() {
		showProgressDialog();
		new Thread(new Runnable() {
			public void run() {
				try {
					int index = 1;
					while (index <= 250 && !mPause) {
						Common.read_record[2] = (byte)index;
						Common.mgr.scardTransmit(Common.read_record, Common.read_record.length, 
								recv, Integer.valueOf(recv.length));
						int ret = getRecordState();
						if (ret == 0) {
							parseRecord();
						} else {
							if (ret == 1) {
								mHandler.sendMessage(mHandler.obtainMessage(Common.ACCESS));
							} else {
								mHandler.sendMessage(mHandler.obtainMessage(Common.DONE, 1, 1));
							}
							mCompleted = true;
							break;
						}
						if (index++ == 250) {
							mCompleted = true;
							mHandler.sendMessage(mHandler.obtainMessage(Common.DONE, 1, 1));
						}
					}
				} catch (UnsupportedEncodingException e) {
					mHandler.sendMessage(mHandler.obtainMessage(Common.DONE, 0, 0));
				}
			}
		}).start();
	}
	
	private int getRecordState() {
		int ret = 0;
		if (((recv[0] & 0xf0) >> 4) == 9) {
			ret = -1;
			if (recv[0] == (byte)0x98 && recv[1] == 0x04) {
				ret = 1;
			}
		}
		return ret;
	}
	
	private void parseRecord() throws UnsupportedEncodingException {
		if (recv[0] == (byte)0xff) {
			return;
		}
		HashMap<String, String> contact = new HashMap<String, String>();
		
		if (recv[0] == (byte)0x80) {
			int lgth = 0;
			for (int i = 1; i < Common.alpha_lgth; i++) {
				if (recv[i] == (byte)0xff) {
					break;
				}
				lgth++;
			}
			contact.put("name", new String(recv, 1, lgth, "unicode"));
		} else if (recv[0] == (byte)0x81) {
			int data_lgth = (recv[1] & 0xff);
			byte[] data = new byte[data_lgth*2];
			int base = (recv[2] << 7) & 0x7ffe;
			for (int i = 0; i < data_lgth; i++) {
				if ((recv[i+3] >> 7) == 0) {
					data[i*2] = 0x00;
					data[i*2+1] = recv[i+3];
				} else {
					int offset = base + (recv[i+3] & 0x7f);
					data[i*2] = (byte)(offset >> 8);
					data[i*2+1] = (byte)(offset & 0xff);
				}
			}
			contact.put("name", new String(data, "unicode"));
		} else if (recv[0] == (byte)0x82) {
			int data_lgth = (recv[1] & 0xff);
			byte[] data = new byte[data_lgth*2];
			int base = ((recv[2] << 8) & 0xffff) + (recv[3] & 0xff);
			for (int i = 0; i < data.length; i++) {
				if ((recv[i+4] >> 7) == 0) {
					data[i*2] = 0x00;
					data[i*2+1] = recv[i+4];
				} else {
					int offset = base + (recv[i+4] & 0x7f);
					data[i*2] = (byte)(offset >> 8);
					data[i*2+1] = (byte)(offset & 0xff);
				}
			}
			contact.put("name", new String(data, "unicode"));
		} else {
			int data_lgth = 0;
			for (int i = 0; i < Common.alpha_lgth; i++) {
				if (recv[i] == (byte)0xff) {
					break;
				}
				data_lgth++;
			}
			contact.put("name", new String(recv, 0, data_lgth));
		}
		
		StringBuilder sb = new StringBuilder();
		int lgth = (recv[Common.alpha_lgth] & 0xff);
		if (lgth > 1) {
			//recv[Common.alpha_lgth+1]: Type of Number
			for (int i = 0; i < lgth; i++) {
				if (recv[Common.alpha_lgth+2+i] == (byte)0xff) {
					break;
				}
				int temp = recv[Common.alpha_lgth+2+i] & 0x0f;
				if (temp > 9) {
					break;
				}
				sb.append(String.valueOf(temp));
				temp = (recv[Common.alpha_lgth+2+i] & 0xf0) >> 4;
				if (temp > 9) {
					break;
				}
				sb.append(String.valueOf(temp));
			}
		}
		if (sb.length() > 0) {
			contact.put("num", sb.toString());
		} else {
			contact.put("num", "");
		}
		mContactsList.add(contact);
	}
	
}
