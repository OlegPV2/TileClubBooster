package com.oleg.tileclubbooster;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oleg.tileclubbooster.autoclick.TapAccessibilityService;
import com.oleg.tileclubbooster.constant.PathType;
import com.oleg.tileclubbooster.constant.RequestCode;
import com.oleg.tileclubbooster.util.FileTools;
import com.oleg.tileclubbooster.util.PermissionTools;
import com.oleg.tileclubbooster.util.ToastUtils;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FileTools.defineRootPath(this);
		if (PermissionTools.isShizukuAvailable()) {
			Shizuku.addRequestPermissionResultListener(this);
		}
		checkAllPermissions();
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, OverlayService.class));
		if (PermissionTools.isShizukuAvailable()) {
			Shizuku.removeRequestPermissionResultListener(this);
		}
		super.onDestroy();
	}

	private void checkAllPermissions() {
		// Check overlay permission
		if (checkDrawOverlayPermission()) {
			startOverlayService();
		}
		// Check storage permission
		if (PermissionTools.hasStoragePermission()) {
			checkPermissionToFiles();
		} else {
			showStoragePermissionDialog();
		}
	}

	private void checkPermissionToFiles() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && PermissionTools.isShizukuAvailable()) {
			if (PermissionTools.hasShizukuPermission()) {
				FileTools.specialPathReadType = PathType.SHIZUKU;
			} else {
				PermissionTools.requestShizukuPermission();
			}
		} else if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			showRequestUriPermissionDialog();
		}
	}

	private boolean checkDrawOverlayPermission() {
		/* check if we already  have permission to draw over other apps */
		if (!Settings.canDrawOverlays(this)) {
			/* if not construct intent to request permission */
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + getPackageName()));
			/* request permission via start activity for result */
			startActivityIntent.launch(intent);
			return false;
		}
		return true;
	}

	public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
		ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

		String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (enabledServicesSetting == null)
			return false;

		TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
		colonSplitter.setString(enabledServicesSetting);

		while (colonSplitter.hasNext()) {
			String componentNameString = colonSplitter.next();
			ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

			if (enabledService != null && enabledService.equals(expectedComponentName))
				return true;
		}

		return false;
	}

	private void startOverlayService() {
		Intent intent = new Intent(this, OverlayService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else {
			startService(intent);
		}
	}

	private void showAccessibilityPermission() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_accessibility_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						PermissionTools.requestAccessibilityPermission(this))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
				}).create().show();
	}

	private void showStoragePermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_storage_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						PermissionTools.requestStoragePermission(this))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) ->
						finish()).create().show();
	}

	private void showRequestUriPermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_need_uri_permission_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						FileTools.requestUriPermission(this, FileTools.dataPath))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
				}).create().show();
	}

	ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> this.startOverlayService());

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RequestCode.STORAGE) {
			onStoragePermissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.STORAGE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				onStoragePermissionResult(Environment.isExternalStorageManager());
			}
		} else if (requestCode == RequestCode.DOCUMENT) {
			Uri uri;
			if (data != null && (uri = data.getData()) != null) {
				getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				onDocumentPermissionResult(true);
			} else {
				onDocumentPermissionResult(false);
			}
		}
	}

	protected void onStoragePermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
			checkPermissionToFiles();
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
			showStoragePermissionDialog();
		}
	}

	protected void onDocumentPermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
		}
	}

	@Override
	public void onRequestPermissionResult(int requestCode, int grantResult) {
		if (requestCode == RequestCode.SHIZUKU) {
			if (grantResult == PackageManager.PERMISSION_GRANTED) {
				FileTools.specialPathReadType = PathType.SHIZUKU;
			}
		}
	}
}