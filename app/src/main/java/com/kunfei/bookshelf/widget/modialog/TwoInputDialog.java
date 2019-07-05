package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.views.ATEAutoCompleteTextView;

/**
 * 输入框
 */
public class TwoInputDialog {
    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput1;
    private ATEAutoCompleteTextView etInput2;
    private TextView tvOk;

    private Context context;
    private BaseDialog dialog;

    public static TwoInputDialog builder(Context context) {
        return new TwoInputDialog(context);
    }

    private TwoInputDialog(Context context) {
        this.context = context;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_two_input, null);
        bindView(view);
        dialog = new BaseDialog(context, R.style.alertDialogTheme);
        dialog.setContentView(view);
    }

    public TwoInputDialog setDefaultValue(String defaultValue) {
        if (defaultValue != null) {
            etInput1.setTextSize(2, 16); // 2 --> sp
            etInput1.setText(defaultValue);
            etInput1.setSelectAllOnFocus(true);
        }
        return this;
    }

    public TwoInputDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public TwoInputDialog setAdapterValues(String[] adapterValues) {
        if (adapterValues != null) {
            ArrayAdapter mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, adapterValues);
            etInput1.setAdapter(mAdapter);
        }
        return this;
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = view.findViewById(R.id.tv_title);
        etInput1 = view.findViewById(R.id.et_input1);
        etInput2 = view.findViewById(R.id.et_input2);
        tvOk = view.findViewById(R.id.tv_ok);
    }

    public TwoInputDialog setCallback(Callback callback) {
        tvOk.setOnClickListener(view -> {
            callback.setInputText(etInput1.getText().toString(),etInput2.getText().toString());
            dialog.dismiss();
        });
        return this;
    }

    public void show() {
        dialog.show();
    }

    /**
     * 输入book地址确定
     */
    public interface Callback {
        void setInputText(String inputText1,String inputText2);
    }
}
