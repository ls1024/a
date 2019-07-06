//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.widget.check_box.SmoothCheckBox;
import com.kunfei.bookshelf.widget.views.ATESwitch;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MoreSettingPop extends FrameLayout {
    @BindView(R.id.vw_bg)
    View vwBg;
    @BindView(R.id.hpb_light)
    SeekBar hpbLight;
    @BindView(R.id.scb_follow_sys)
    SmoothCheckBox scbFollowSys;
    @BindView(R.id.ll_follow_sys)
    LinearLayout llFollowSys;
    @BindView(R.id.sb_click_all_next)
    Switch sbClickAllNext;
    @BindView(R.id.sb_click)
    Switch sbClick;
    @BindView(R.id.sb_show_title)
    Switch sbShowTitle;
    @BindView(R.id.sb_showTimeBattery)
    Switch sbShowTimeBattery;
    @BindView(R.id.sb_hideStatusBar)
    Switch sbHideStatusBar;
    @BindView(R.id.ll_hideStatusBar)
    LinearLayout llHideStatusBar;
    @BindView(R.id.ll_showTimeBattery)
    LinearLayout llShowTimeBattery;
    @BindView(R.id.sb_hideNavigationBar)
    Switch sbHideNavigationBar;
    @BindView(R.id.ll_hideNavigationBar)
    LinearLayout llHideNavigationBar;
    @BindView(R.id.sb_showLine)
    Switch sbShowLine;
    @BindView(R.id.llScreenTimeOut)
    LinearLayout llScreenTimeOut;
    @BindView(R.id.tv_screen_time_out)
    TextView tvScreenTimeOut;
    @BindView(R.id.tvJFConvert)
    TextView tvJFConvert;
    @BindView(R.id.llJFConvert)
    LinearLayout llJFConvert;
    @BindView(R.id.tv_screen_direction)
    TextView tvScreenDirection;
    @BindView(R.id.ll_screen_direction)
    LinearLayout llScreenDirection;
    @BindView(R.id.tv_progress_display)
    TextView tvProgressDisplay;
    @BindView(R.id.ll_progress_display)
    LinearLayout llProgressDisplay;
    @BindView(R.id.tv_longpress_setting)
    TextView tvLongpressSetting;
    @BindView(R.id.ll_longpress_setting)
    LinearLayout llLongpressSetting;
    @BindView(R.id.sw_volume_next_page)
    Switch swVolumeNextPage;
    @BindView(R.id.sw_read_aloud_key)
    Switch swReadAloudKey;
    @BindView(R.id.ll_read_aloud_key)
    LinearLayout llReadAloudKey;
    @BindView(R.id.ll_click_all_next)
    LinearLayout llClickAllNext;
    @BindView(R.id.reNavBarColor)
    TextView reNavBarColor;
    @BindView(R.id.reNavBarColor_val)
    TextView reNavBarColorVal;
    @BindView(R.id.llNavigationBarColor)
    LinearLayout llNavigationBarColor;
    @BindView(R.id.sbImmersionStatusBar)
    ATESwitch sbImmersionStatusBar;
    @BindView(R.id.llImmersionStatusBar)
    LinearLayout llImmersionStatusBar;

    private Activity context;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private Callback callback;

    public MoreSettingPop(Context context) {
        super(context);
        init(context);
    }

    public MoreSettingPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MoreSettingPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pop_more_setting, this);
        ButterKnife.bind(this, view);
        vwBg.setOnClickListener(null);
    }

    public void setListener(Activity activity, @NonNull Callback callback) {
        this.context = activity;
        this.callback = callback;
        initData();
        bindEvent();
        initLight();
    }

    private void bindEvent() {
        this.setOnClickListener(view -> this.setVisibility(GONE));


        //亮度调节
        llFollowSys.setOnClickListener(v -> {
            if (scbFollowSys.isChecked()) {
                scbFollowSys.setChecked(false, true);
            } else {
                scbFollowSys.setChecked(true, true);
            }
        });
        scbFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            readBookControl.setLightFollowSys(isChecked);
            if (isChecked) {
                //跟随系统
                hpbLight.setEnabled(false);
                setScreenBrightness();
            } else {
                //不跟随系统
                hpbLight.setEnabled(true);
                setScreenBrightness(readBookControl.getLight());
            }
        });
        hpbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!readBookControl.getLightFollowSys()) {
                    readBookControl.setLight(i);
                    setScreenBrightness(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        sbImmersionStatusBar.setOnCheckedChangeListener(((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setImmersionStatusBar(b);
                callback.upBar();
                RxBus.get().post(RxBusTag.RECREATE, true);
            }
        }));
        sbHideStatusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setHideStatusBar(isChecked);
                callback.recreate();
            }
        });
        sbHideNavigationBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setHideNavigationBar(isChecked);
                initData();
                callback.recreate();
            }
        });
        swVolumeNextPage.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setCanKeyTurn(b);
                upView();
            }
        });
        swReadAloudKey.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                readBookControl.setAloudCanKeyTurn(b);
            }
        });
        sbClick.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setCanClickTurn(isChecked);
                upView();
            }
        });
        sbClickAllNext.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setClickAllNext(isChecked);
            }
        });

        sbShowTitle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowTitle(isChecked);
                callback.refreshPage();
            }
        });
        sbShowTimeBattery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowTimeBattery(isChecked);
                callback.refreshPage();
            }
        });
        sbShowLine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                readBookControl.setShowLine(isChecked);
                callback.refreshPage();
            }
        });
        llScreenTimeOut.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.keep_light))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.screen_time_out), readBookControl.getScreenTimeOut(), (dialogInterface, i) -> {
                        readBookControl.setScreenTimeOut(i);
                        upScreenTimeOut(i);
                        callback.keepScreenOnChange(i);
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        llJFConvert.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.jf_convert))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.convert_s), readBookControl.getTextConvert(), (dialogInterface, i) -> {
                        readBookControl.setTextConvert(i);
                        upFConvert(i);
                        dialogInterface.dismiss();
                        callback.refreshPage();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
        llScreenDirection.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.screen_direction))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.screen_direction_list_title), readBookControl.getScreenDirection(), (dialogInterface, i) -> {
                        readBookControl.setScreenDirection(i);
                        upScreenDirection(i);
                        dialogInterface.dismiss();
                        callback.recreate();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });

        llProgressDisplay.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.progress_display))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.progress_display_list_title), readBookControl.getProgressDisplay(), (dialogInterface, i) -> {
                        readBookControl.setProgressDisplay(i);
                        upProgressDisplay(i);
                        dialogInterface.dismiss();
                        callback.refreshPage();
                    })
                    .create();
            dialog.show();
        });

        llLongpressSetting.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.book_longpress_setting))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.longpress_setting_list_title), readBookControl.getLongPressSetting(), (dialogInterface, i) -> {
                        readBookControl.setLongPressSetting(i);
                        upLongPressSetting(i);
                        dialogInterface.dismiss();
                        //callback.refreshPage();
                    })
                    .create();
            dialog.show();
        });

        llNavigationBarColor.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.re_navigation_bar_color))
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.NavBarColors), readBookControl.getNavBarColor(), (dialogInterface, i) -> {
                        readBookControl.setNavBarColor(i);
                        upNavBarColor(i);
                        dialogInterface.dismiss();
                        callback.recreate();
                    })
                    .create();
            dialog.show();
            ATH.setAlertDialogTint(dialog);
        });
    }

    private void initData() {
        upScreenDirection(readBookControl.getScreenDirection());
        upProgressDisplay(readBookControl.getProgressDisplay());
        upLongPressSetting(readBookControl.getLongPressSetting());
        upScreenTimeOut(readBookControl.getScreenTimeOut());
        upFConvert(readBookControl.getTextConvert());
        upNavBarColor(readBookControl.getNavBarColor());
        sbImmersionStatusBar.setChecked(readBookControl.getImmersionStatusBar());
        swVolumeNextPage.setChecked(readBookControl.getCanKeyTurn());
        swReadAloudKey.setChecked(readBookControl.getAloudCanKeyTurn());
        sbHideStatusBar.setChecked(readBookControl.getHideStatusBar());
        sbHideNavigationBar.setChecked(readBookControl.getHideNavigationBar());
        sbClick.setChecked(readBookControl.getCanClickTurn());
        sbClickAllNext.setChecked(readBookControl.getClickAllNext());
        sbShowTitle.setChecked(readBookControl.getShowTitle());
        sbShowTimeBattery.setChecked(readBookControl.getShowTimeBattery());
        sbShowLine.setChecked(readBookControl.getShowLine());
        upView();
    }

    private void upView() {
        if (readBookControl.getHideStatusBar()) {
            sbShowTimeBattery.setEnabled(true);
        } else {
            sbShowTimeBattery.setEnabled(false);
        }
        if (readBookControl.getCanKeyTurn()) {
            swReadAloudKey.setEnabled(true);
        } else {
            swReadAloudKey.setEnabled(false);
        }
        if (readBookControl.getCanClickTurn()) {
            sbClickAllNext.setEnabled(true);
        } else {
            sbClickAllNext.setEnabled(false);
        }
        if (readBookControl.getHideNavigationBar()) {
            llNavigationBarColor.setEnabled(false);
            reNavBarColorVal.setEnabled(false);
        } else {
            llNavigationBarColor.setEnabled(true);
            reNavBarColorVal.setEnabled(true);
        }
    }

    private void upScreenTimeOut(int screenTimeOut) {
        tvScreenTimeOut.setText(context.getResources().getStringArray(R.array.screen_time_out)[screenTimeOut]);
    }

    private void upFConvert(int fConvert) {
        tvJFConvert.setText(context.getResources().getStringArray(R.array.convert_s)[fConvert]);
    }

    private void upScreenDirection(int screenDirection) {
        String[] screenDirectionListTitle = context.getResources().getStringArray(R.array.screen_direction_list_title);
        if (screenDirection >= screenDirectionListTitle.length) {
            tvScreenDirection.setText(screenDirectionListTitle[0]);
        } else {
            tvScreenDirection.setText(screenDirectionListTitle[screenDirection]);
        }
    }

    private void upProgressDisplay(int progressDisplay) {
        String[] progressDisplayListTitle = context.getResources().getStringArray(R.array.progress_display_list_title);
        if (progressDisplay >= progressDisplayListTitle.length) {
            tvProgressDisplay.setText(progressDisplayListTitle[0]);
        } else {
            tvProgressDisplay.setText(progressDisplayListTitle[progressDisplay]);
        }
    }

    private void upLongPressSetting(int longpresssettiing) {
        String[] longpressSettingListTitle = context.getResources().getStringArray(R.array.longpress_setting_list_title);
        if (longpresssettiing >= longpressSettingListTitle.length) {
            tvLongpressSetting.setText(longpressSettingListTitle[0]);
        } else {
            tvLongpressSetting.setText(longpressSettingListTitle[longpresssettiing]);
        }
    }




    private void upNavBarColor(int nColor) {
        reNavBarColorVal.setText(context.getResources().getStringArray(R.array.NavBarColors)[nColor]);
    }

    public void setScreenBrightness() {
        WindowManager.LayoutParams params = (context).getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        (context).getWindow().setAttributes(params);
    }

    public void setScreenBrightness(int value) {
        if (value < 1) value = 1;
        WindowManager.LayoutParams params = (context).getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        (context).getWindow().setAttributes(params);
    }

    public void initLight() {
        hpbLight.setProgress(readBookControl.getLight());
        scbFollowSys.setChecked(readBookControl.getLightFollowSys());
        if (!readBookControl.getLightFollowSys()) {
            setScreenBrightness(readBookControl.getLight());
        }
    }

    public interface Callback {
        void upBar();

        void keepScreenOnChange(int keepScreenOn);

        void recreate();

        void refreshPage();
    }

}
