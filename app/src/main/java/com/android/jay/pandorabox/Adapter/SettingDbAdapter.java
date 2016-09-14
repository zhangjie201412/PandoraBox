package com.android.jay.pandorabox.Adapter;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by H151136 on 9/14/2016.
 */
public class SettingDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_INDEX = "index";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_KEEP = "keep";
    public static final String KEY_TIME = "time";

    private static final String TAG = "Pandora.SettingAdapter";
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "DB.SETTING";
}
