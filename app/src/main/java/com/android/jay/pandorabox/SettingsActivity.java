package com.android.jay.pandorabox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jay.pandorabox.Item.SettingItem;
import com.android.jay.pandorabox.Utils.Utils;
import com.android.jay.pandorabox.dialog.SaveNameDialog;
import com.android.jay.pandorabox.dialog.SettingEditDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 9/13/2016.
 */
@ContentView(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity implements View.OnClickListener,
        SettingEditDialog.OnSettingCompleteListener,
        SaveNameDialog.OnSaveNameCompleteListener {

    private final String TAG = "Pandora.Settings";
    @ViewInject(R.id.bt_add)
    private Button mAdd;
    @ViewInject(R.id.bt_save)
    private Button mSave;
    @ViewInject(R.id.bt_apply)
    private Button mApply;
    @ViewInject(R.id.tv_open)
    private TextView mOpen;
    @ViewInject(R.id.lv_settings)
    private ListView mListView;
    @ViewInject(R.id.chart_setting_preview)
    private LineChart mLineChart;

    private SimpleAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private SettingEditDialog mSettingEditDialog;
    private SaveNameDialog mSaveNameDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdd.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mApply.setOnClickListener(this);
        mOpen.setOnClickListener(this);
        mData = new ArrayList<HashMap<String, String>>();
        mAdapter = new SimpleAdapter(this, mData, R.layout.item_settings,
                new String[]{"index", "speed", "keep", "time"},
                new int[]{R.id.item_id, R.id.item_speed, R.id.item_keep, R.id.item_time});
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item = mData.get(position);
                float speed = Float.parseFloat(item.get("speed"));
                float keep = Float.parseFloat(item.get("keep"));
                int time = Integer.parseInt(item.get("time"));
                mSettingEditDialog.setParam(speed, keep, time);
                mSettingEditDialog.show(getFragmentManager(), "setting");
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Utils.showAlertDialog(SettingsActivity.this, R.string.delete, R.string.sure_to_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(position);
                        setPreview();
                    }
                });
                return true;
            }
        });
        mSettingEditDialog = new SettingEditDialog();
        mSettingEditDialog.setOnCompleteListener(this);
        mSaveNameDialog = new SaveNameDialog();
        mSaveNameDialog.setListner(this);
        mLineChart.setNoDataText("Please add setting and press preview");
        mLineChart.setDescription(getString(R.string.setting_chart_description));
        load_apply();
    }

    public void load_apply() {
        List<SettingItem> settings = PandoraApplication.getInstance().getSettingDb().getApply();
        for(int i = 0; i < settings.size(); i++) {
            addItem(settings.get(i));
        }
        if(settings.size() > 0) {
            setPreview();
        }
    }

    private void setPreview() {
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        ArrayList<Entry> values = new ArrayList<>();

//        for(int i = 0; i < 100; i++) {
//            float val = (float)(Math.random() * 100);
//            values.add(new Entry(i, val));
//        }

        values.add(new Entry(0, 25.0f));
        float lastY = 25.0f;
        float lastX = 0.0f;
        for(int i = 0; i < mData.size(); i++) {
            float x, y;
            SettingItem it = getItem(i);
            y = it.getKeep();
            x = lastX + (lastY - it.getKeep()) / it.getSpeed();
            values.add(new Entry(x, y));
            x += it.getTime();
            values.add(new Entry(x, y));
            lastX = x;
        }

        LineDataSet dataSet;
        if(mLineChart.getData() != null &&
                mLineChart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet)mLineChart.getData().getDataSetByIndex(0);
            dataSet.setValues(values);
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        } else {
            dataSet = new LineDataSet(values, getString(R.string.temperature_unit));
            dataSet.setColor(Color.GREEN);
            dataSet.setCircleColor(Color.GREEN);
            dataSet.setLineWidth(1f);
            dataSet.setCircleRadius(0.5f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(false);
            dataSet.setLineWidth(1f);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet);

            Legend legend = mLineChart.getLegend();
            legend.setEnabled(true);
            legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
            legend.setForm(Legend.LegendForm.SQUARE);
            legend.setTextColor(Color.GREEN);

            LineData data = new LineData(dataSets);

            mLineChart.setData(data);
        }
        mLineChart.animateX(1500);
        mLineChart.invalidate();
    }

    private void addItem(SettingItem item) {
        HashMap<String, String> it = new HashMap<>();
        int no = mData.size() + 1;

        it.put("index", "" + no);
        it.put("speed", "" + item.getSpeed());
        it.put("keep", "" + item.getKeep());
        it.put("time", "" + item.getTime());
        mData.add(it);
        mAdapter.notifyDataSetChanged();
    }

    private void removeItem(int pos) {
        mData.remove(pos);
        for(int i = 0; i < mData.size(); i++) {
            HashMap<String, String> item = mData.get(i);
            item.put("index", "" + (i + 1));
        }
        mAdapter.notifyDataSetChanged();
    }

    private SettingItem getItem(int index) {
        HashMap<String, String> item = mData.get(index);
        int id = Integer.parseInt(item.get("index"));
        float speed = Float.parseFloat(item.get("speed"));
        float keep = Float.parseFloat(item.get("keep"));
        int time = Integer.parseInt(item.get("time"));

        return new SettingItem(id, speed, keep, time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_add:
                mSettingEditDialog.setNew();
                mSettingEditDialog.show(getFragmentManager(), "settings");
                break;
            case R.id.bt_save:
                mSaveNameDialog.show(getFragmentManager(), "save_file");
                break;
            case R.id.bt_apply:
                PandoraApplication.getInstance().getSettingDb().saveApply(mData);
                Toast.makeText(SettingsActivity.this, R.string.notice_apply_done, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_open:
                openFile();
                break;
            default:
                break;
        }
    }

    public void loadFile(String name) {
        List<SettingItem> items = PandoraApplication.getInstance().getSettingDb().getItems(name);
        mData.clear();
        for(int i = 0; i < items.size(); i++) {
            addItem(items.get(i));
        }
        setPreview();
    }

    private int mOpenSelect = 0;
    public void openFile() {
        List<String> items = PandoraApplication.getInstance().getSettingDb().getTables();
        if(items.size() > 0) {
            final String [] selects = new String[items.size()];
            for(int i = 0; i < selects.length; i++) {
                selects[i] = items.get(i);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.open);
            builder.setSingleChoiceItems(selects, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    mOpenSelect = which;
                }
            });
            builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadFile(selects[mOpenSelect]);
                }
            });
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.showAlertDialog(SettingsActivity.this, R.string.delete, R.string.sure_to_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PandoraApplication.getInstance().getSettingDb().delTable(selects[mOpenSelect]);
                            Toast.makeText(SettingsActivity.this, R.string.notice_delete_done, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
            builder.setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        } else {
            Toast.makeText(this, R.string.notice_open_null, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSettingComplete(float speed, float keep, int time) {
        addItem(new SettingItem(-1, speed, keep, time));
        setPreview();
    }

    @Override
    public void onSaveNameComplete(String name) {
        if(mData.size() > 0) {
            PandoraApplication.getInstance().getSettingDb().saveFlle(name, mData);
            Toast.makeText(SettingsActivity.this, R.string.notice_save_done, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsActivity.this, R.string.notice_data_null, Toast.LENGTH_SHORT).show();
        }
    }
}
