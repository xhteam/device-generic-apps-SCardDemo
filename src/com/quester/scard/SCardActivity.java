package com.quester.scard;

import com.quester.android.platform_library.PcscdManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SCardActivity extends PreferenceActivity implements SCardPcscLite {
	
	private static final int SW_OK = 1;
	private static final int SW_CLA_ERR = 2;
	private static final int SW_INS_ERR = 3;
	private static final int SW_PARAM_ERR = 4;
	private static final int SW_LGTH_ERR = 5;
	private static final int SW_RESPONSE = 6;

	private static final int SERVICE_DELAY = 2000;

	private Preference mIccid;
	private Preference mImsi;
	private Preference mSms;
	private Preference mAdn;
	private ProgressDialog mServiceDialog;
	
	private boolean mConnect = false;
	private boolean mTransmit = false;
	private boolean mStandby = false;
	private boolean mFirstInitDone = false;
	
	private byte[] recv = new byte[128];
	private PcscdManager mPcscdManager;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.master);
		mIccid = findPreference("iccid_info");
		mImsi = findPreference("imsi_info");
		mSms = findPreference("sms_info");
		mAdn = findPreference("adn_info");
		mPcscdManager = new PcscdManager();
		Common.init();
		if (mPcscdManager.startService()) {
			 setProgressDialog();
			 new Thread(new Runnable() {
				 public void run() {
					 try {
						 Thread.sleep(SERVICE_DELAY);
					 } catch (Exception e) {
						 // NA
					 } finally {
						 cancelProgressDialog();
					 }
				 }
			 }).start();
		} else {
			Toast.makeText(this, "Start pcscd service failure!", Toast.LENGTH_LONG).show();
		}
	}

	private void setProgressDialog() {
		if (mServiceDialog == null) {
			mServiceDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
			mServiceDialog.setTitle(R.string.starting);
			mServiceDialog.setMessage(getString(R.string.init_ui));
		}
		mServiceDialog.setCancelable(false);
		mServiceDialog.show();
	}
	
	private void cancelProgressDialog() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mServiceDialog != null) {
					mServiceDialog.dismiss();
					doFirstInit();
				}
			}
		});
	}

	private void doFirstInit() {
		getSCardState();
		selectMF();
		getIccid();
		getImsi();
		mFirstInitDone = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mFirstInitDone) {
			selectMF();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPcscdManager.stopService();
		if (mConnect) {
			if (mTransmit) {
				Common.mgr.scardEndTransaction(SCARD_LEAVE_CARD);
				mTransmit = false;
			}
			Common.mgr.scardDisconnect(SCARD_UNPOWER_CARD);
			mConnect = false;
		} 
	}
	
	private void selectMF() {
		if (mConnect && mTransmit) {
			Common.mgr.scardTransmit(Common.SELECT_MF, Common.SELECT_MF.length, 
					recv, Integer.valueOf(recv.length));
			int ret = parseData();
			if (ret == SW_OK) {
				mStandby = true;
			} else {
				if (ret == SW_RESPONSE) {
					int lgth = recv[1];
					Common.get_response[4] = (byte)lgth;
					Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
							recv, Integer.valueOf(recv.length));
					if (recv[4] == 0x3f && recv[5] == 0x00) {
						mStandby = true;
					}
				}
			}
		}
	}
	
	public int parseData() {
		int ret = 0;
		if (recv[0] == (byte)0x90 && recv[1] == 0x00) {
			ret = SW_OK;
		} else if (recv[0] == 0x6e) {
			ret = SW_CLA_ERR;
		} else if (recv[0] == 0x6d) {
			ret = SW_INS_ERR;
		} else if (recv[0] == 0x6b) {
			ret = SW_PARAM_ERR;
		} else if (recv[0] == 0x6c) {
			ret = SW_LGTH_ERR;
		} else if (recv[0] == (byte)0x9f) {
			ret = SW_RESPONSE;
		}
		return ret;
	}
	
	private void getSCardState() {
		int ret = -1;
		ret = Common.mgr.scardEstablishContext(SCARD_SCOPY_SYSTEM);
		if (ret != SCARD_S_SUCCESS) {
			return;
		}
		ret = Common.mgr.scardIsValidContext();
		if (ret != SCARD_S_SUCCESS) {
			return;
		}
		String[] readers = Common.mgr.scardListReaders(null, SCARD_AUTOALLOCATE);
		if (readers == null) {
			return;
		}
		ret = Common.mgr.scardConnect(readers[0], SCARD_SHARE_SHARED, SCARD_PROTOCOL_ANY);
		if (ret == SCARD_S_SUCCESS) {
			mConnect = true;
		} else {
			return;
		}
		ret = Common.mgr.scardBeginTransaction();
		if (ret == SCARD_S_SUCCESS) {
			mTransmit = true;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		boolean flag = super.onPreferenceTreeClick(preferenceScreen, preference);
		if (mConnect && mTransmit && mStandby) {
			if (preference == mSms) {
				flag = true;
				Common.mgr.scardTransmit(Common.SELECT_TELECOM, Common.SELECT_TELECOM.length, 
						recv, Integer.valueOf(recv.length));
				int ret = parseData();
				if (ret == SW_OK || ret == SW_RESPONSE) {
					boolean selected = true;
					if (ret == SW_RESPONSE) {
						int lgth = recv[1];
						Common.get_response[4] = (byte)lgth;
						Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
								recv, Integer.valueOf(recv.length));
						if (recv[4] != 0x7f || recv[5] != 0x10) {
							selected = false;
						}
					}
					if (selected) {
						Common.mgr.scardTransmit(Common.SELECT_SMS, Common.SELECT_SMS.length, 
								recv, Integer.valueOf(recv.length));
						ret = parseData();
						if (ret == SW_OK || ret == SW_RESPONSE) {
							if (ret == SW_RESPONSE) {
								int lgth = recv[1];
								Common.get_response[4] = (byte)lgth;
								Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
										recv, Integer.valueOf(recv.length));
								if (recv[4] != 0x6f || recv[5] != 0x3c) {
									selected = false;
								} else {
									Common.read_record[4] = recv[lgth-1];
								}
							}
							if (selected) {
								Intent intent = new Intent(SCardActivity.this, SmsActivity.class);
								startActivity(intent);
							}
						}
					}
				}
			} else if (preference == mAdn) {
				flag = true;
				Common.mgr.scardTransmit(Common.SELECT_TELECOM, Common.SELECT_TELECOM.length, 
						recv, Integer.valueOf(recv.length));
				int ret = parseData();
				if (ret == SW_OK || ret == SW_RESPONSE) {
					boolean selected = true;
					if (ret == SW_RESPONSE) {
						int lgth = recv[1];
						Common.get_response[4] = (byte)lgth;
						Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
								recv, Integer.valueOf(recv.length));
						if (recv[4] != 0x7f || recv[5] != 0x10) {
							selected = false;
						}
					}
					if (selected) {
						Common.mgr.scardTransmit(Common.SELECT_ADN, Common.SELECT_ADN.length, 
								recv, Integer.valueOf(recv.length));
						ret = parseData();
						if (ret == SW_OK || ret == SW_RESPONSE) {
							if (ret == SW_RESPONSE) {
								int lgth = recv[1];
								Common.get_response[4] = (byte)lgth;
								Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
										recv, Integer.valueOf(recv.length));
								if (recv[4] != 0x6f || recv[5] != 0x3a) {
									selected = false;
								} else {
									Common.read_record[4] = recv[lgth-1];
									Common.alpha_lgth = (recv[lgth-1] & 0xff) - 14;
								}
							}
							if (selected) {
								Intent intent = new Intent(SCardActivity.this, ContactsActivity.class);
								startActivity(intent);
							}
						}
					}
				}
			}
		}
		return flag;
	}
	
	private void getIccid() {
		if (mConnect && mTransmit && mStandby) {
			Common.mgr.scardTransmit(Common.SELECT_ICCID, Common.SELECT_ICCID.length, 
					recv, Integer.valueOf(recv.length));
			int ret = parseData();
			if (ret == SW_OK || ret == SW_RESPONSE) {
				boolean selected = true;
				if (ret == SW_RESPONSE) {
					int lgth = recv[1];
					Common.get_response[4] = (byte)lgth;
					Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
							recv, Integer.valueOf(recv.length));
					if (recv[4] != 0x2f || recv[5] != (byte)0xe2) {
						selected = false;
					}
				}
				if (selected) {
					Common.mgr.scardTransmit(Common.READ_ICCID, Common.READ_ICCID.length, 
							recv, Integer.valueOf(recv.length));
					mIccid.setSummary(dump_iccid(10));
				}
			}
		}
	}
	
	private void getImsi() {
		if (mConnect && mTransmit && mStandby) {
			Common.mgr.scardTransmit(Common.SELECT_GSM, Common.SELECT_GSM.length, 
					recv, Integer.valueOf(recv.length));
			int ret = parseData();
			if (ret == SW_OK || ret == SW_RESPONSE) {
				boolean selected = true;
				if (ret == SW_RESPONSE) {
					int lgth = recv[1];
					Common.get_response[4] = (byte)lgth;
					Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
							recv, Integer.valueOf(recv.length));
					if (recv[4] != 0x7f || recv[5] != 0x20) {
						selected = false;
					}
				}
				if (selected) {
					Common.mgr.scardTransmit(Common.SELECT_IMSI, Common.SELECT_IMSI.length, 
							recv, Integer.valueOf(recv.length));
					ret = parseData();
					if (ret == SW_OK || ret == SW_RESPONSE) {
						if (ret == SW_RESPONSE) {
							int lgth = recv[1];
							Common.get_response[4] = (byte)lgth;
							Common.mgr.scardTransmit(Common.get_response, Common.get_response.length, 
									recv, Integer.valueOf(recv.length));
							if (recv[4] != 0x6f || recv[5] != 0x07) {
								selected = false;
							}
						}
						if (selected) {
							Common.mgr.scardTransmit(Common.READ_IMSI, Common.READ_IMSI.length, 
									recv, Integer.valueOf(recv.length));
							mImsi.setSummary(dump_imsi(9));
						}
					}
				}
				selectMF();
			}
		}
	}
	
	private String dump_iccid(int lgth) {
		StringBuilder iccid = new StringBuilder();
		int type = 0, count = 0;
		for (int i = 0; i < lgth; i++) {
			int temp = recv[i] & 0x0f;
			iccid.append(String.valueOf(temp));
			if ((++count)%5 == 0) {
				iccid.append(" ");
			}
			temp = (recv[i] & 0xf0) >> 4;
			if (i == 2) {
				type = temp;
			}
			iccid.append(String.valueOf(temp));
			if ((++count)%5 == 0) {
				iccid.append(" ");
			}
		}
		String result = "";
		if (type == 0) {
			result = iccid.toString();
		} else if (type == 1) {
			result = iccid.toString();
		} else if (type == 3) {
			result = iccid.toString();
		} else {
			result = iccid.toString();
		}
		return result;
	}
	
	private String dump_imsi(int lgth) {
		if (recv[0] != 0x08) {
			return getString(R.string.unknown);
		}
		StringBuilder imsi = new StringBuilder();
		int temp = (recv[1] & 0xf0) >> 4;
		imsi.append(String.valueOf(temp));
		for (int i = 2; i < lgth; i++) {
			temp = recv[i] & 0x0f;
			imsi.append(String.valueOf(temp));
			temp = (recv[i] & 0xf0) >> 4;
			imsi.append(String.valueOf(temp));
			if (i == 2 || i == 3) {
				imsi.append(" ");
			}
		}
		return imsi.toString();
	}

}
