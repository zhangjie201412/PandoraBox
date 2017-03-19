package com.android.jay.pandorabox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jay.pandorabox.Item.SettingItem;
import com.android.jay.pandorabox.Utils.Utils;
import com.android.jay.pandorabox.ble.BtleListener;
import com.android.jay.pandorabox.ble.BtleManager;
import com.android.jay.pandorabox.view.MyProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by H151136 on 9/13/2016.
 */
@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Pandora";

    private static final String KEY_CMD = "cmd";
    private static final String KEY_RATIO = "ratio";
    private static final String KEY_TEMP = "temp";
    private static final String KEY_TIME = "time";

    private static final String SAVE_PATH = Environment.getExternalStorageDirectory() + "/PandoraBox/";
    private static final String LOG_FILE = "current.log";

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
    @ViewInject(R.id.iv_ble_connect)
    private ImageView mBluetoothImageView;
    @ViewInject(R.id.tv_sample_temperature)
    private TextView mSampleTextView;
    @ViewInject(R.id.tv_ble_status)
    private TextView mBleStatusTextView;
    @ViewInject(R.id.bt_start)
    private Button mStartButton;
    @ViewInject(R.id.bt_stop)
    private Button mStopButton;
    @ViewInject(R.id.tv_stage)
    private TextView mStageTextView;
    @ViewInject(R.id.tv_speed)
    private TextView mSpeedTextView;
    @ViewInject(R.id.tv_temperature)
    private TextView mTemperatureTextView;
    @ViewInject(R.id.tv_time)
    private TextView mTimeTextView;

    private Toast mToast;
    private PowerManager.WakeLock mWakeLock;

    private boolean mStart = false;
    private boolean mBluetoothConnected = false;
    private List<SettingItem> mCurrentSettingList;
    private int mCurrentIndex;

    private float mStartTemperature;

    private long mTime = 0;
    private boolean mIsOpen = false;
    private static final String DEVICE_NAME = "CPLB";

    private ProgressDialog mWaitDialog;

    private byte[] mBuffer;
    private int mBufferIndex = 0;
    //chart
    private ArrayList<ILineDataSet> mDataSets;
    private ArrayList<Entry> mVals1 = new ArrayList<>();
    private LineDataSet mDataSet1;
    private ArrayList<Entry> mVals3 = new ArrayList<>();
    private LineDataSet mDataSet3;

    private BtleListener mBtleListener = new BtleListener() {
        @Override
        public void onDeviceScan(String name, String addr) {
            Log.d(TAG, "name = " + name + ", addr = " + addr);
            if (name.startsWith(DEVICE_NAME)) {
                BtleManager.getInstance().connect(addr);
            }
        }

        @Override
        public void onDeviceConnected() {
            Log.d(TAG, "onDeviceConnected");
            mBluetoothImageView.setImageResource(R.mipmap.ic_ble_connected);
            mBleStatusTextView.setText(R.string.ble_connected);
            mBluetoothConnected = true;
            //stop scan
            BtleManager.getInstance().scan(false);
            showToast(getString(R.string.connect_success));
            if(mWaitDialog.isShowing()) {
                mWaitDialog.dismiss();
            }
        }

        @Override
        public void onDeviceDisconnected() {
            Log.d(TAG, "onDeviceDisconnected");
            mBluetoothImageView.setImageResource(R.mipmap.ic_ble_disconnected);
            mBleStatusTextView.setText(R.string.ble_disconnected);
            mBluetoothConnected = false;
            showToast(getString(R.string.ble_disconnected));
        }

        @Override
        public void onDataAvailable(byte[] data) {
            for (int i = 0; i < data.length; i++) {
                mBuffer[mBufferIndex++] = data[i];
                if (data[i] == '}') {
                    byte[] recvByte = Arrays.copyOfRange(mBuffer, 0, mBufferIndex);
                    processData(recvByte);
                    //clear buffer
                    mBufferIndex = 0;
                }
            }
        }
    };

    private void processData(byte[] recv) {
        String recvString = new String(recv);
        try {
            JSONObject jsonObject = new JSONObject(recvString);
            int insideTemp = jsonObject.getInt("inside_temp");
            int sampleTemp = jsonObject.getInt("sample_temp");
            int output1 = jsonObject.getInt("ssr1");
            Log.d(TAG, "insideTemp = " + insideTemp + ", sampleTemp = " + sampleTemp + ", output1 = " + output1);
            if(mStart) {
                updateChart((float) sampleTemp / 1000, output1 / 1000);
                //write log
                String log = String.format("%d\t%d\t%d\n", mVals1.size(), sampleTemp, output1);
                writeLog(log);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTime = 0;
        mSettings.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mExitAppButton.setOnClickListener(this);
        mNavigationView.setItemIconTintList(null);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
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
        mSsr1.setProgress(0);
        mSsr2.setProgress(0);
        mHeater.setProgress(0);
        mLineChart.setDescription(getString(R.string.setting_chart_description));
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBluetoothImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothConnected) {
                    Log.d(TAG, "start connect to ble device");
                    BtleManager.getInstance().scan(true);
                    mWaitDialog.show();
                }
            }
        });
        initChart();
        checkPermissions();
        BtleManager.getInstance().init(this);
        BtleManager.getInstance().register(mBtleListener);
        mBuffer = new byte[128];
        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage(getString(R.string.connecting_wait));
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        //prepare the log file
        createLogFile();
    }

    private void createLogFile() {
        File file = new File(SAVE_PATH + LOG_FILE);
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeLog(String msg) {
        try {
            FileWriter out = new FileWriter(SAVE_PATH + LOG_FILE, true);
            BufferedWriter writer = new BufferedWriter(out);
            writer.write(msg);
            writer.flush();
            writer.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initChart() {
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinValue(0);
        xAxis.setAxisMaxValue(240);
        mLineChart.setVisibleXRange(0, 20);

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setAxisMinValue(-200);
        yAxis.setAxisMaxValue(100);
        YAxis yAxis2 = mLineChart.getAxisRight();
        yAxis2.setAxisMinValue(-200);
        yAxis2.setAxisMaxValue(100);

        mDataSets = new ArrayList<>();
        mDataSet1 = new LineDataSet(mVals1, getString(R.string.sample_temperature_unit));
        mDataSet1.setColor(Color.RED);
        mDataSet1.setCircleColor(Color.RED);
        mDataSet1.setLineWidth(1f);
        mDataSet1.setCircleRadius(0.5f);
        mDataSet1.setDrawCircleHole(false);
        mDataSet1.setValueTextSize(0.0f);
        mDataSet1.setDrawFilled(false);
        mDataSet1.setLineWidth(1f);

        mDataSet3 = new LineDataSet(mVals3, getString(R.string.caculate_temperature_unit));
        mDataSet3.setColor(Color.BLUE);
        mDataSet3.setCircleColor(Color.BLUE);
        mDataSet3.setLineWidth(1f);
        mDataSet3.setCircleRadius(0.5f);
        mDataSet3.setDrawCircleHole(false);
        mDataSet3.setValueTextSize(0.0f);
        mDataSet3.setDrawFilled(false);
        mDataSet3.setLineWidth(1f);
        mDataSets.add(mDataSet1);
        mDataSets.add(mDataSet3);

        Legend legend = mLineChart.getLegend();
        legend.setEnabled(true);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.BLACK);

        LineData data = new LineData(mDataSets);

        mLineChart.setData(data);
        mLineChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        BtleManager.getInstance().disconnect();
        BtleManager.getInstance().unregister();
        BtleManager.getInstance().release();
    }


    private void updateChart(float temp, int output1) {
        float x, y;
        x = (float) (mVals1.size()) / 60.0f;
        y = temp;

        if(mVals1.size() == 0) {
            mStartTemperature = temp;
        }
        mVals1.add(new Entry(x, y));
        mVals3.add(new Entry(x, mStartTemperature
                - x * (mCurrentSettingList.get(mCurrentIndex)).getSpeed()));
        mDataSet1.setValues(mVals1);
        mDataSet3.setValues(mVals3);

        mLineChart.getData().notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
        //update textView
        mSampleTextView.setText(getString(R.string.sample_temperature_title) + " " +
                String.format("%.1f", temp) + getString(R.string.temp_unit));
        mSsr1.setProgress(output1);
    }

    private static final int RC_ROOT = 102;

    @AfterPermissionGranted(RC_ROOT)
    public void checkPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean[] allows = new boolean[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            if (EasyPermissions.hasPermissions(this, permissions[i])) {
                allows[i] = true;
            } else {
                allows[i] = false;
            }
        }
        boolean result = true;
        for (int i = 0; i < permissions.length; i++) {
            result &= allows[i];
        }

        if (!result) {
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
                    HomeActivity.this.finish();
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_HOME);
//                    startActivity(intent);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void sendSettings(SettingItem item) {
        //send setting to box
        Log.d(TAG, "index = " + item.getIndex() + ", speed = " + item.getSpeed()
                + ", keep = " + item.getKeep() + ", time = " + item.getTime());
        mStageTextView.setText(getString(R.string.stage) + " " + (item.getIndex()));
        mSpeedTextView.setText(getString(R.string.speed) + " " + item.getSpeed());
        mTemperatureTextView.setText(getString(R.string.target_temperature) + " " + item.getKeep());
        mTimeTextView.setText(getString(R.string.keep_time) + " " + item.getTime());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_CMD, 1);
            jsonObject.put(KEY_RATIO, (int)(item.getSpeed() * 1000));
            jsonObject.put(KEY_TEMP, (int)(item.getKeep() * 1000));
            jsonObject.put(KEY_TIME, item.getTime());
            BtleManager.getInstance().send(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
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
            case R.id.bt_start:
                if(!mBluetoothConnected) {
                    showToast(getString(R.string.notice_disconnect));
                    return;
                }
                mCurrentSettingList =
                        PandoraApplication.getInstance().getSettingDb().getApply();
                if(mCurrentSettingList.size() == 0) {
                    showToast(getString(R.string.notice_setting_null));
                    return;
                }
                mCurrentIndex = 0;
//                for(SettingItem item : mCurrentSettingList) {
//                    Log.d(TAG, "index = " + item.getIndex() + ", speed = " + item.getSpeed()
//                            + ", keep = " + item.getKeep() + ", time = " + item.getTime());
//                }
                if(!mStart) {
                    mVals1.clear();
                    mDataSet1.clear();
                    mVals3.clear();
                    mDataSet3.clear();
                    mLineChart.getData().notifyDataChanged();
                    mLineChart.notifyDataSetChanged();
                    mLineChart.invalidate();
                    mStartButton.setEnabled(false);
                    mStart = true;
                    //send first setting to box
                    sendSettings(mCurrentSettingList.get(0));
                }
                break;
            case R.id.bt_stop:
                if(!mBluetoothConnected) {
                    showToast(getString(R.string.notice_disconnect));
                    return;
                }
                if(mStart) {
                    mStartButton.setEnabled(true);
                    mStart = false;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(KEY_CMD, 0);
                    BtleManager.getInstance().send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Pandora");
        if(!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
}
