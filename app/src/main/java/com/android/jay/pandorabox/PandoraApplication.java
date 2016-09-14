package com.android.jay.pandorabox;

import android.app.Application;

import org.xutils.x;
/**
 * Created by H151136 on 9/14/2016.
 */
public class PandoraApplication extends Application {
    private static PandoraApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        instance = this;
    }

    public static PandoraApplication getInstance() {
        return instance;
    }
}
