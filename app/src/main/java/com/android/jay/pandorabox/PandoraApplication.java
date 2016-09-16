package com.android.jay.pandorabox;

import android.app.Application;

import com.android.jay.pandorabox.database.SettingDb;

import org.xutils.x;
/**
 * Created by H151136 on 9/14/2016.
 */
public class PandoraApplication extends Application {
    private static PandoraApplication instance = null;

    private SettingDb mSettingDb;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        instance = this;

        mSettingDb = new SettingDb(this);
    }

    public synchronized SettingDb getSettingDb() {
        if(mSettingDb == null) {
            mSettingDb = new SettingDb(this);
        }
        return mSettingDb;
    }

    public synchronized static PandoraApplication getInstance() {
        return instance;
    }
}
