package com.android.jay.pandorabox;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by H151136 on 9/13/2016.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ImageView mSettings;

    private long mTime = 0;
    private boolean mIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        mNavigationView = (NavigationView)findViewById(R.id.nv_drawer);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mSettings = (ImageView)findViewById(R.id.iv_settings);
        mTime = 0;

        mSettings.setOnClickListener(this);
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
                    Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
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

            default:
                break;
        }
    }
}
