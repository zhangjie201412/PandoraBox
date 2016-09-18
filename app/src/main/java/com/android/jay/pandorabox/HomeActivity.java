package com.android.jay.pandorabox;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.jay.pandorabox.Utils.Utils;
import com.android.jay.pandorabox.view.MyProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by H151136 on 9/13/2016.
 */
@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.nv_drawer)
    private NavigationView mNavigationView;
    @ViewInject(R.id.drawer)
    private DrawerLayout mDrawerLayout;
    @ViewInject(R.id.iv_settings)
    private ImageView mSettings;
    @ViewInject(R.id.bt_exit_app)
    private Button mExitAppButton;
    @ViewInject(R.id.bt_settings)
    private Button mSettingsButton;
    @ViewInject(R.id.bar_ssr1)
    private MyProgressBar mSsr1;
    @ViewInject(R.id.bar_ssr2)
    private MyProgressBar mSsr2;
    @ViewInject(R.id.bar_heater)
    private MyProgressBar mHeater;
    @ViewInject(R.id.chart_main)
    private LineChart mLineChart;

    private long mTime = 0;
    private boolean mIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTime = 0;
        mSettings.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mExitAppButton.setOnClickListener(this);
        mNavigationView.setItemIconTintList(null);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mIsOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mIsOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mSsr1.setTile(getString(R.string.ssr1));
        mSsr2.setTile(getString(R.string.ssr2));
        mHeater.setTile(getString(R.string.heater));
//        mLineChart.setNoDataText(getString(R.string.main_no_data_text));
        mLineChart.setDescription(getString(R.string.setting_chart_description));
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        checkPermissions();
    }

    private static final int RC_ROOT = 102;
    @AfterPermissionGranted(RC_ROOT)
    public void checkPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean[] allows = new boolean[permissions.length];
        for(int i = 0; i < permissions.length; i++) {
            if(EasyPermissions.hasPermissions(this, permissions[i])) {
                allows[i] = true;
            } else {
                allows[i] = false;
            }
        }
        boolean result = true;
        for(int i = 0; i < permissions.length; i++) {
            result &= allows[i];
        }

        if(!result) {
            EasyPermissions.requestPermissions(this, "Root", RC_ROOT, permissions[0], permissions[1], permissions[2]);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(mNavigationView);
                mIsOpen = false;
                return true;
            } else {
                if ((System.currentTimeMillis() - mTime > 1000)) {
                    Toast.makeText(this, R.string.notice_app_exit, Toast.LENGTH_SHORT).show();
                    mTime = System.currentTimeMillis();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_settings:
                if (!mIsOpen) {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                    mIsOpen = true;
                }
                break;

            case R.id.bt_settings:
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.bt_exit_app:
                Utils.showAlertDialog(this, R.string.exit_app, R.string.sure_to_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.this.finish();
                    }
                });
                break;
            default:
                break;
        }
    }
}
