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

public class SmsActivity extends BaseListActivity {
	
	private List<HashMap<String, String>> mSmsList;
	private boolean mCompleted = false;
	private boolean mPause;
	private byte[] recv = new byte[256];
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			dismissProgressDialog();
			switch (msg.what) {
			case Common.DONE:
				if (msg.arg1 == 1) {
					if (mSmsList.size() > 0) {
						showSms();
					} else {
						showDialog(getString(R.string.sms), getString(R.string.record_empty));
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
		mSmsList = new ArrayList<HashMap<String,String>>();
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
	
	private void showSms() {
		SimpleAdapter adapter = new SimpleAdapter(this, 
				mSmsList,
				R.layout.sms_activity,
				new String[]{"num", "txt", "ts"},
				new int[]{R.id.sms_num, R.id.sms_txt, R.id.sms_ts});
		setListAdapter(adapter);
	}
	
	private void readRecord() {
		showProgressDialog();
		new Thread(new Runnable() {
			public void run() {
				try {
					int index = 1;
					while (index <= 50 && !mPause) {
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
						if (index++ == 50) {
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
		int index = 0;
		//recv[0]: Status byte indicates whether this record is used
		index++;
		
		//recv[1]: SMSC(SMS Centre) length
		if (recv[index] == (byte)0xff) {
			return;
		}
		int lgth = recv[index] & 0xff;
		index++;
		
		StringBuilder sb = new StringBuilder();
		//recv[2]~recv[2+lgth]: SMSC(SMS Centre) number
		for (int i = 0; i < lgth; i++) {
			if (i == 0) {
				//recv[2]: TON(type-of-number)
				if (((recv[i+index] & 0x70) >> 4) == 1) {
					sb.append("+");
				}
				continue;
			}
			int temp = recv[i+index] & 0x0f;
			if (temp > 9) {
				break;
			}
			sb.append(temp);
			temp = (recv[i+index] & 0xf0) >> 4;
			if (temp > 9) {
				break;
			}
			sb.append(temp);
		}
		index = index + lgth;
		HashMap<String, String> sms = new HashMap<String, String>();
		sms.put("smsc", sb.toString());
		
		//recv[index]: SMS-DELIVER-TYPE
		index++;
		//recv[index]: Sender number length
		lgth = recv[index] & 0xff;
		if (lgth % 2 == 1) {
			lgth = lgth / 2 + 1;
		} else {
			lgth = lgth / 2;
		}
		index++;
		sb = new StringBuilder();
		//recv[index]: TON(type-of-number)
		if (((recv[index] & 0x70) >> 4) == 1) {
			sb.append("+");
		}
		index++;
		//recv[index]~recv[index+lgth]: Sender number
		for (int i = 0; i < lgth; i++) {
			int temp = recv[i+index] & 0x0f;
			if (temp > 9) {
				break;
			}
			sb.append(temp);
			temp = (recv[i+index] & 0xf0) >> 4;
			if (temp > 9) {
				break;
			}
			sb.append(temp);
		}
		index = index + lgth;
		sms.put("num", sb.toString());
		
		//recv[index]: Protocol identifier, reject this message or not
		index++;
		//recv[index]: DCS(Data Coding Scheme), UCS2 set to 08, 7bit set to 00
		int type = 0;
		if ((recv[index] & 0xff) != 0) {
			type = 8;
		}
		index++;
		//recv[index]~recv[index+7]: Timestamp
		sb = new StringBuilder();
		for (int i = 0; i < 7; i++) {
			int temp = (recv[index+i] & 0x0f)*10 + ((recv[index+i] & 0xf0) >> 4);
			if (i < 6) {
				if (temp < 10) {
					sb.append(0);
				}
				sb.append(temp);
				if (i < 2) {
					sb.append("/");
				} else if (i == 2 || i == 5) {
					sb.append(" ");
				} else {
					sb.append(":");
				}
			} else {
				while (temp >= 24) {
					temp = temp - 24;
				}
				sb.append("ST ");
				if (temp < 10) {
					sb.append(0);
				}
				sb.append(temp);
			}
		}
		index = index + 7;
		sms.put("ts", sb.toString());
		
		//recv[index]: User data length
		lgth = recv[index] & 0xff;
		index++;
		//recv[index]~recv[index+lgth]: User data
		if (type == 0) {
			//7bit
			byte[] _8bit = new byte[lgth + lgth/7];
			int cursor = 0, index_7 = 0, index_8 = 0;
			int prevLeft = 0;
			while (index_7 < lgth) {
				_8bit[index_8] = (byte)((recv[index] << cursor | prevLeft) & 0x7f);
				prevLeft = (recv[index] & 0xff) >> (7 - cursor);
				
				index_8++;
				cursor++;
				
				if (cursor == 7) {
					_8bit[index_8] = (byte)prevLeft;
					
					index_8++;
					
					cursor = 0;
					prevLeft = 0;
				}
				
				index++;
				index_7++;
			}
			sms.put("txt", new String(_8bit));
		} else {
			//UCS2
			sms.put("txt", new String(recv, index, index+lgth, "unicode"));
		}
		mSmsList.add(sms);
	}
	
}
