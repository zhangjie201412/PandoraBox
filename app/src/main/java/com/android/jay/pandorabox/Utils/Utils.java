package com.android.jay.pandorabox.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.jay.pandorabox.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/9/15.
 */
public class Utils {
    private boolean mIsFake = true;
    private static final String TAG = "Pandora";

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

    public static void screenCapture(Activity activity, File path) {
        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bitmap = activity.getWindow().getDecorView().getDrawingCache();
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(path);
            if (fileOutputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception:FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException:IOException");
            e.printStackTrace();
        }
    }
}
