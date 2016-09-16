package com.android.jay.pandorabox.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.jay.pandorabox.Item.SettingItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/15.
 */
public class SettingDb {
    private final String INTERNAL_SETTING_NAME = "PANDORA_APPLY";
    private final String TAG = "Pandora.Setting";
    public static final String SETTING_DB_NAME = "setting.db";
    private SQLiteDatabase mDb;

    public SettingDb(Context context) {
        mDb = context.openOrCreateDatabase(SETTING_DB_NAME, Context.MODE_PRIVATE, null);
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + INTERNAL_SETTING_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,speed FLOAT,keep FLOAT,time INTEGER,date TEXT)");
    }

    public void saveItem(String name, SettingItem item) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
            + name
            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,speed FLOAT,keep FLOAT,time INTEGER,date TEXT)");
        mDb.execSQL("insert into _"
            + name
            + " (iindex,speed,keep,time) values(?,?,?,?)",
                new Object[] {item.getIndex(), item.getSpeed(), item.getKeep(), item.getTime()});
    }

    public List<SettingItem> getItems(String name) {
        List<SettingItem> list = new LinkedList<SettingItem>();

        Cursor c = mDb.rawQuery("SELECT * from _" + name + " ORDER BY _id", null);
        while(c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("iindex"));
            float keep = c.getFloat(c.getColumnIndex("keep"));
            float speed = c.getFloat(c.getColumnIndex("speed"));
            int time = c.getInt(c.getColumnIndex("time"));
            String date = c.getString(c.getColumnIndex("date"));
            SettingItem item = new SettingItem(index, speed, keep, time);
            list.add(item);
        }
        c.close();
//        Collections.reverse(list);
        return list;
    }

    public List<String> getTables() {
        List<String> tables = new LinkedList<String>();
        Cursor c = mDb.rawQuery("select name from sqlite_master where type='table' order by name",
                null);
        while (c.moveToNext()) {
            String name = c.getString(0);
            if(name.startsWith("_") && !name.contains(INTERNAL_SETTING_NAME)) {
                tables.add(name.substring(1));
            }
        }
        return tables;
    }

    public void delTable(String name) {
        mDb.execSQL("DROP TABLE IF EXISTS _" + name);
    }

    public void saveApply(List<HashMap<String, String>> data) {
        delTable(INTERNAL_SETTING_NAME);
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + INTERNAL_SETTING_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,speed FLOAT,keep FLOAT,time INTEGER,date TEXT)");
        for(int i = 0; i < data.size(); i++) {
            HashMap<String, String> item = data.get(i);
            int id = Integer.parseInt(item.get("index"));
            float speed = Float.parseFloat(item.get("speed"));
            float keep = Float.parseFloat(item.get("keep"));
            int time = Integer.parseInt(item.get("time"));

            saveItem(INTERNAL_SETTING_NAME, new SettingItem(id, speed, keep, time));
        }
    }

    public void saveFlle(String name, List<HashMap<String, String>> data) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + name
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,speed FLOAT,keep FLOAT,time INTEGER,date TEXT)");
        for(int i = 0; i < data.size(); i++) {
            HashMap<String, String> item = data.get(i);
            int id = Integer.parseInt(item.get("index"));
            float speed = Float.parseFloat(item.get("speed"));
            float keep = Float.parseFloat(item.get("keep"));
            int time = Integer.parseInt(item.get("time"));

            saveItem(name, new SettingItem(id, speed, keep, time));
        }
    }

    public List<SettingItem> getApply() {
        return getItems(INTERNAL_SETTING_NAME);
    }
}
