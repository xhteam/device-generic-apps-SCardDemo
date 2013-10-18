package com.quester.scard;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

public class BaseListActivity extends ListActivity {
	
	protected String inputStr;
	protected ProgressDialog progress;
	
	protected void showDialog(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.confirm, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.create();
		builder.setCancelable(false);
		builder.show();
	}
	
	protected void showInputDialog(String title, String msg) {
		final EditText edit = new EditText(this);
		edit.setFocusable(true);
		edit.setInputType(InputType.TYPE_CLASS_NUMBER);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setView(edit);
		builder.setPositiveButton(R.string.next, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						inputStr = edit.getText().toString();
						dialog.dismiss();
						doNext();
					}
				});
		builder.setNegativeButton(R.string.cancel, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.create();
		builder.setCancelable(false);
		builder.show();
	}
	
	protected void doNext() {
	}
	
	protected void showProgressDialog() {
		progress = new ProgressDialog(this);
		progress.setTitle(R.string.reading);
		progress.setMessage(getString(R.string.init_ui));
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setCancelable(false);
		progress.show();
	}
	
	protected void dismissProgressDialog() {
		if (progress != null) {
			progress.dismiss();
		}
	}
	
}
