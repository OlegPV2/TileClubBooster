package com.oleg.tileclubbooster.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.oleg.tileclubbooster.App;
import com.oleg.tileclubbooster.constant.RequestCode;

import rikka.shizuku.Shizuku;

public class PermissionTools {
    private static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";

    public static boolean hasStoragePermission() {
        Context context = App.get();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:"+activity.getPackageName()));
            activity.startActivityForResult(intent, RequestCode.STORAGE);
        } else {
            activity.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE } , RequestCode.STORAGE);
        }
    }

    private static boolean isShizukuInstalled() {
        try {
            App.get().getPackageManager().getPackageInfo(SHIZUKU_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("isShizukuInstalled", e.toString());
        }
        return false;
    }

    public static boolean isShizukuAvailable() {
        return isShizukuInstalled() && Shizuku.pingBinder();
    }

    public static boolean hasShizukuPermission() {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestShizukuPermission() {
        Shizuku.requestPermission(RequestCode.SHIZUKU);
    }

    public static void requestAccessibilityPermission(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
