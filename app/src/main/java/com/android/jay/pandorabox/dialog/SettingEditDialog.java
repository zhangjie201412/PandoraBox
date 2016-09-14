package com.android.jay.pandorabox.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.jay.pandorabox.R;

/**
 * Created by H151136 on 9/14/2016.
 */
public class SettingEditDialog extends DialogFragment {

    private EditText mSpeedEditText;
    private EditText mKeepEditText;
    private EditText mTimeEditText;
    private float mSpeed = -1;
    private float mKeep = -1;
    private int mTime = -1;
    private OnSettingCompleteListener mListener;

    public interface OnSettingCompleteListener {
        public void onSettingComplete(float speed, float keep, int time);
    }

    public void setNew() {
        setParam(-1, -1, -1);
    }

    public void setParam(float speed, float keep, int time) {
        mSpeed = speed;
        mKeep = keep;
        mTime = time;
    }

    public void setOnCompleteListener(OnSettingCompleteListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_setting_edit, null);

        mSpeedEditText = (EditText)view.findViewById(R.id.dialog_et_speed);
        mKeepEditText = (EditText)view.findViewById(R.id.dialog_et_keep);
        mTimeEditText = (EditText)view.findViewById(R.id.dialog_et_time);

        builder.setView(view).setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(mSpeedEditText.getEditableText().toString().length() <= 0 ||
                        mKeepEditText.getEditableText().toString().length() <= 0 ||
                        mTimeEditText.getEditableText().toString().length() <= 0) {
                    Toast.makeText(getActivity(), R.string.notice_input_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
                float speed = Float.parseFloat(mSpeedEditText.getEditableText().toString());
                float keep = Float.parseFloat(mKeepEditText.getEditableText().toString());
                int time = Integer.parseInt(mTimeEditText.getEditableText().toString());
                if(mListener != null) {
                    mListener.onSettingComplete(speed, keep, time);
                }
            }
        }).setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle(R.string.settings).setIcon(R.mipmap.ic_launcher);
        if(mSpeed > 0) {
            mSpeedEditText.setText("" + mSpeed);
        } else {
            mSpeedEditText.setText("");
        }
        if(mKeep > 0) {
            mKeepEditText.setText("" + mKeep);
        } else {
            mKeepEditText.setText("");
        }
        if(mTime > 0) {
            mTimeEditText.setText("" + mTime);
        } else {
            mTimeEditText.setText("");
        }
        return builder.create();
    }
}
