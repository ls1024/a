package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.AudioMngHelper;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.view.activity.MyReadBookActivity;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyTTSBottomMenu extends FrameLayout {

    //@BindView(R.id.vw_bg)
    //View vwBg;

    @BindView(R.id.tts_volume)
    SeekBar sbTTSVolume;

    @BindView(R.id.tts_pitch)
    SeekBar sbTTSPitch;

    @BindView(R.id.tts_speed)
    SeekBar sbTTSSpeed;

    @BindView(R.id.resetTv)
    TextView resetTv;


    @BindView(R.id.tts_stop)
    ImageView ivStop;

    @BindView(R.id.tts_page_up)
    ImageView ivPageUp;

    @BindView(R.id.tts_prior)
    ImageView ivPrior;

    @BindView(R.id.tts_page_down)
    ImageView ivPageDown;

    @BindView(R.id.tts_next)
    ImageView ivNext;

    @BindView(R.id.tts_filter)
    TextView tvFilter;

    @BindView(R.id.tts_play)
    ImageView ivPlay;

    AudioMngHelper audioMngHelper = new AudioMngHelper(getContext());

    private ReadBookControl readBookControl = ReadBookControl.getInstance();


    private Callback callback;
    private MyReadBookActivity activity;

    public MyTTSBottomMenu(Context context) {
        super(context);
        init(context);
    }

    public MyTTSBottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTTSBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_pop_tts_menu, this);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);

        sbTTSVolume.setMax(100);
        sbTTSVolume.setProgress(audioMngHelper.getSystemCurrentVolume()*10);
        sbTTSVolume.setSecondaryProgress(10);


        sbTTSPitch.setMax(20);
        sbTTSPitch.setProgress(readBookControl.getSpeechPitch());
        sbTTSPitch.setSecondaryProgress(1);

        sbTTSSpeed.setMax(15);
        sbTTSSpeed.setProgress(readBookControl.getSpeechRate());
        sbTTSSpeed.setSecondaryProgress(1);
    }


    public void setNavigationBarHeight(int height) {
        //vwNavigationBar.getLayoutParams().height = height;
    }

    public void setListener(MyReadBookActivity readBookActivity, Callback callback) {
        this.activity = readBookActivity;
        this.callback = callback;
        bindEvent();
    }

    private void bindEvent() {

        //音量调节
        sbTTSVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioMngHelper.setVoice100(seekBar.getProgress());
            }
        });

        //音调调节
        sbTTSPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                readBookControl.setSpeechPitch(seekBar.getProgress());
                if (callback != null) {
                    callback.changeSpeechPitch(readBookControl.getSpeechPitch());
                }
            }

        });

        //语速调节
        sbTTSSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                readBookControl.setSpeechRate(seekBar.getProgress());
                if (callback != null) {
                    callback.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });

        //重置
        resetTv.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                   readBookControl.setSpeechRate(10);
                   readBookControl.setSpeechPitch(10);
                   sbTTSPitch.setProgress(readBookControl.getSpeechPitch());
                   sbTTSSpeed.setProgress(readBookControl.getSpeechRate());
                   if (callback != null) {
                       callback.changeSpeechRate(readBookControl.getSpeechRate());
                   }
               }
        });

        //上一页
        ivPageUp.setOnClickListener(view -> callback.readPrePage());

        //上一句
        ivPrior.setOnClickListener(view -> callback.readPreContent());

        //下一页
        ivPageDown.setOnClickListener(view -> callback.readNextPage());

        //下一句
        ivNext.setOnClickListener(view -> callback.readNextContent());

        //朗读
        ivPlay.setOnClickListener(view -> callback.onMediaButton());

        //停止
        ivStop.setOnClickListener(view -> callback.stopTTS());

        tvFilter.setOnClickListener(view -> callback.setFilter());

    }

    public void setFabReadAloudImage(int id) {
        ivPlay.setImageResource(id);
    }

    public void setReadAloudTimer(boolean visibility) {
        if (visibility) {
            //llReadAloudTimer.setVisibility(VISIBLE);
        } else {
           // llReadAloudTimer.setVisibility(GONE);
        }
    }



    public interface Callback {
        /*
        void pauseTTS();//暂停

        void playTTS();//播放

        void stopTTS();//停止
        */
        void readNextPage();

        void readNextContent();

        void readPrePage();

        void readPreContent();

        void changeSpeechPitch(int speechPitch);

        void changeSpeechRate(int speechRate);

        void stopTTS();//停止

        void setFilter();//设置字符过滤

        void onMediaButton();//播放，暂停共用按钮

        void toast(int id);

        void dismiss();
    }

}
