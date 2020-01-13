package com.cxk.redpacket.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.cxk.redpacket.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class Utils {

    private static Toast mToast;

    public static void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showToast( int paramInt) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), paramInt, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(paramInt);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void shareAPK(Activity activity){
        PackageInfo packageInfo = getPackageInfo(activity);
        if (packageInfo != null){
            File apkFile = new File(packageInfo.applicationInfo.sourceDir);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider7.getUriForFile(activity, apkFile));
            activity.startActivity(intent);
        }
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            String packageName = getPackageName(context);
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return packageInfo;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    private static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

}
