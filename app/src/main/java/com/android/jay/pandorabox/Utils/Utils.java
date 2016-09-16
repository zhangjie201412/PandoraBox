package com.android.jay.pandorabox.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.jay.pandorabox.R;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/9/15.
 */
public class Utils {
    private boolean mIsFake = true;

    public boolean isFake() {
        return mIsFake;
    }

    public static void showAlertDialog(Context context, int resTitle, int resMessage, DialogInterface.OnClickListener listner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(resTitle);
        builder.setMessage(resMessage);
        builder.setPositiveButton(R.string.ok_string, listner);
        builder.create().show();
    }

    public static boolean isValidName(String name) {
        String namePattern = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]{1,10}$";
        boolean result = Pattern.matches(namePattern, name);
        return result;
    }
}
