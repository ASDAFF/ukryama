package ru.redsolution.rosyama;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener,
		DialogClickListener {
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	private static final int OPTION_MENU_PREFERENCE_ID = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	private static final int DIALOG_PHOTO_ID = 0;
	private static final int DIALOG_SEND_ID = 1;
	private static final int DIALOG_GET_ID = 2;

	private Uri requestedUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		for (int id : new int[] { R.id.photo, R.id.send, R.id.get })
			findViewById(id).setOnClickListener(this);
		requestedUri = null;
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}
		// ((Rosyama) getApplication())
		// .setPhoto("/mnt/sdcard/2011-06-02-21-20-24.jpg");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!((Rosyama) getApplication()).hasLogin()) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			finish();
			return;
		}
		enables();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_REQUESTED_URI, requestedUri == null ? null
				: requestedUri.toString());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
			if (resultCode != RESULT_OK)
				requestedUri = null;
			else
				((Rosyama) getApplication()).setPhoto(requestedUri.getPath());
			enables();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_PREFERENCE_ID, 0,
				getString(R.string.preferences)).setIcon(
				android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_PREFERENCE_ID:
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.photo:
			showDialog(DIALOG_PHOTO_ID);
			break;
		case R.id.send:
			showDialog(DIALOG_SEND_ID);
			break;
		case R.id.get:
			showDialog(DIALOG_GET_ID);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_PHOTO_ID:
			return new DialogBuilder(this, this, DIALOG_PHOTO_ID,
					getString(R.string.photo_help)).create();
		case DIALOG_SEND_ID:
			return new DialogBuilder(this, this, DIALOG_SEND_ID,
					getString(R.string.send_help)).create();
		case DIALOG_GET_ID:
			return new DialogBuilder(this, this, DIALOG_GET_ID,
					getString(R.string.get_help)).create();
		default:
			return null;
		}
	}

	private Uri getNextUri(String extention) {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention));
	}

	private void enables() {
		findViewById(R.id.send).setEnabled(
				((Rosyama) getApplication()).hasPhoto());
		findViewById(R.id.send_label).setEnabled(
				((Rosyama) getApplication()).hasPhoto());
		findViewById(R.id.get).setEnabled(((Rosyama) getApplication()).hasId());
		findViewById(R.id.get_label).setEnabled(
				((Rosyama) getApplication()).hasId());
	}

	@Override
	public void onAccept(DialogBuilder dialog) {
		Intent intent;
		switch (dialog.getDialogId()) {
		case DIALOG_PHOTO_ID:
			requestedUri = getNextUri(".jpg");
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		case DIALOG_SEND_ID:
			intent = new Intent(this, Address.class);
			startActivity(intent);
			break;
		case DIALOG_GET_ID:
			if (((Rosyama) getApplication()).pdf()) {
				intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				Uri uri = Uri.fromFile(((Rosyama) getApplication()).getPdf());
				System.out.println(uri);
				intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent,
						getString(R.string.email)));
			} else {
				Toast.makeText(this, getString(R.string.pdf_fail),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialog) {
	}

	@Override
	public void onCancel(DialogBuilder dialog) {
	}
}