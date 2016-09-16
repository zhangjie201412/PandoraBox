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
import com.android.jay.pandorabox.Utils.Utils;

/**
 * Created by H151136 on 9/14/2016.
 */
public class SaveNameDialog extends DialogFragment {

    private EditText mNameEditText;

    public interface OnSaveNameCompleteListener {
        public void onSaveNameComplete(String name);
    }

    private OnSaveNameCompleteListener mListener;

    public void setListner(OnSaveNameCompleteListener listner) {
        mListener = listner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_save, null);

        mNameEditText = (EditText)view.findViewById(R.id.dialog_et_name);

        builder.setView(view).setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = mNameEditText.getEditableText().toString();
                if(name.length() <= 0 ||
                        !Utils.isValidName(name)) {
                    Toast.makeText(getActivity(), R.string.notice_input_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mListener != null) {
                    mListener.onSaveNameComplete(name);
                }
            }
        }).setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle(R.string.settings).setIcon(R.mipmap.ic_launcher);
        return builder.create();
    }
}
