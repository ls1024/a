//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.popupwindow;

import android.content.Context;
import android.content.Intent;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.view.activity.MyReadBookActivity;
import com.kunfei.bookshelf.view.activity.ReadStyleActivity;
import com.kunfei.bookshelf.widget.CustomRoundAngleImageView;
import com.kunfei.bookshelf.widget.font.FontSelector;
import com.kunfei.bookshelf.widget.my_page.animation.PageAnimation;
import com.kunfei.bookshelf.widget.style.StyleSelector;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyReadInterfacePop extends FrameLayout {

    //字体大小，字体
    @BindView(R.id.fl_smaller)
    FrameLayout flSmaller;
    @BindView(R.id.tv_dur_textsize)
    TextView tvTextSize;
    @BindView(R.id.fl_bigger)
    FrameLayout flBigger;

    //加粗
    @BindView(R.id.fl_bold_smaller)
    FrameLayout flBoldSmaller;
    @BindView(R.id.tv_dur_bold_size)
    TextView tvBoldSize;
    @BindView(R.id.fl_bold_bigger)
    FrameLayout flBoldBigger;
    //@BindView(R.id.fl_bold)
    //FrameLayout  flBold;
    //@BindView(R.id.tv_bold)
    //TextView  tvBold;
    @BindView(R.id.fl_moreFont)
    FrameLayout flMoreFont;

    //样式相关
    @BindView(R.id.fl_space1)
    FrameLayout flSpace1;
    @BindView(R.id.fl_space2)
    FrameLayout flSpace2;
    @BindView(R.id.fl_space3)
    FrameLayout flSpace3;
    @BindView(R.id.fl_space_none)
    FrameLayout flSpaceNone;
    @BindView(R.id.fl_moreStyle)
    FrameLayout flMoreStyle;

    //配色相关
    @BindView(R.id.fl_bg_0)
    RelativeLayout flBg0;
    @BindView(R.id.tv_0)
    TextView tv0;
    @BindView(R.id.civ_0)
    CustomRoundAngleImageView civ0;
    @BindView(R.id.fl_bg_1)
    RelativeLayout flBg1;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.civ_1)
    CustomRoundAngleImageView civ1;
    @BindView(R.id.fl_bg_2)
    RelativeLayout flBg2;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.civ_2)
    CustomRoundAngleImageView civ2;
    @BindView(R.id.fl_bg_3)
    RelativeLayout flBg3;
    @BindView(R.id.tv_3)
    TextView tv3;
    @BindView(R.id.civ_3)
    CustomRoundAngleImageView civ3;
    @BindView(R.id.fl_moreColor)
    FrameLayout flMoreColor;

    //翻页相关
    @BindView(R.id.fl_page1)
    FrameLayout flPage1;
    @BindView(R.id.fl_page2)
    FrameLayout flPage2;
    @BindView(R.id.fl_page3)
    FrameLayout flPage3;
    @BindView(R.id.fl_morePage)
    FrameLayout flMorePage;


    private MyReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnChangeProListener changeProListener;

    public MyReadInterfacePop(Context context) {
        super(context);
        init(context);
    }

    public MyReadInterfacePop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyReadInterfacePop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_pop_read_interface, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
    }

    public void setListener(MyReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;
        initData();
        bindEvent();
    }

    private void initData() {
        tvTextSize.setText(String.valueOf(readBookControl.getTextSize()));
        setBg();
        updateBg(readBookControl.getTextDrawableIndex());
        updateBoldText(readBookControl.getBoldSize());
        updateStyleDefault(readBookControl.getStyleDefault());
        updatePageMode(readBookControl.getPageMode());
    }

    private void bindEvent() {

        flSmaller.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = readBookControl.getTextSize()-1;
                readBookControl.setTextSize(c);
                changeProListener.upTextSize();
                tvTextSize.setText(String.valueOf(c));
            }
        });

        flBigger.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = readBookControl.getTextSize()+1;
                readBookControl.setTextSize(c);
                changeProListener.upTextSize();
                tvTextSize.setText(String.valueOf(c));
            }
        });

        flBoldSmaller.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                float c = readBookControl.getBoldSize()-0.1f;
                if(c<0) return;
                readBookControl.setBoldSize(c);

               updateBoldText(c);
                changeProListener.refresh();

            }
        });

        flBoldBigger.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float c = readBookControl.getBoldSize()+0.1f;
                readBookControl.setBoldSize(c);
                updateBoldText(c);
                changeProListener.refresh();
            }
        });

        /*
        flBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setTextBold(!readBookControl.getTextBold());
                updateBoldText(readBookControl.getTextBold());
                changeProListener.refresh();
            }
        });
        */

        //选择字体
        flMoreFont.setOnClickListener(view -> {
            List<String> per = PermissionUtils.checkMorePermissions(activity, MApplication.PerList);
            if (per.isEmpty()) {
                new FontSelector(activity, readBookControl.getFontPath())
                        .setListener(new FontSelector.OnThisListener() {
                            @Override
                            public void setDefault() {
                                clearFontPath();
                            }

                            @Override
                            public void setFontPath(String fontPath) {
                                setReadFonts(fontPath);
                            }
                        })
                        .create()
                        .show();
            } else {
                Toast.makeText(activity, "本软件需要存储权限来存储备份书籍信息", Toast.LENGTH_SHORT).show();
                PermissionUtils.requestMorePermissions(activity, per, MApplication.RESULT__PERMS);
            }
        });

        //长按清除字体
        flMoreFont.setOnLongClickListener(view -> {
            clearFontPath();
            activity.toast(R.string.clear_font);
            return true;
        });

        //默认间距1
        flSpace1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setStyleDefault(1);
                updateStyleDefault(1);
                readBookControl.setLineMultiplier(1);
                readBookControl.setParagraphSize(1);
                readBookControl.setPaddingTop(0);
                readBookControl.setPaddingBottom(0);
                readBookControl.setPaddingLeft(20);
                readBookControl.setPaddingRight(20);
                changeProListener.upTextSizeAndMargin();
            }
        });
        //默认间距2
        flSpace2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setStyleDefault(2);
                updateStyleDefault(2);
                readBookControl.setLineMultiplier(1);
                readBookControl.setParagraphSize(2);
                readBookControl.setPaddingTop(0);
                readBookControl.setPaddingBottom(0);
                readBookControl.setPaddingLeft(20);
                readBookControl.setPaddingRight(20);
                changeProListener.upTextSizeAndMargin();
            }
        });

        //默认间距3
        flSpace3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setStyleDefault(3);
                updateStyleDefault(3);
                readBookControl.setLineMultiplier(2);
                readBookControl.setParagraphSize(2);
                readBookControl.setPaddingTop(0);
                readBookControl.setPaddingBottom(0);
                readBookControl.setPaddingLeft(20);
                readBookControl.setPaddingRight(20);
                changeProListener.upTextSizeAndMargin();
            }
        });

        //默认间距4
        flSpaceNone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setStyleDefault(4);
                updateStyleDefault(4);
                readBookControl.setLineMultiplier(0);
                readBookControl.setParagraphSize(0);
                readBookControl.setPaddingTop(0);
                readBookControl.setPaddingBottom(0);
                readBookControl.setPaddingLeft(20);
                readBookControl.setPaddingRight(20);
                changeProListener.upTextSizeAndMargin();
            }
        });

        //自定义样式
        flMoreStyle.setOnClickListener(view -> {
            new StyleSelector(activity, readBookControl.getFontPath())
                    .setListener(new StyleSelector.OnThisListener() {
                        @Override
                        public void setLineMultiplier(float number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setLineMultiplier(number);
                            changeProListener.upTextSize();
                        }

                        @Override
                        public void setParagraphSize(float number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setParagraphSize(number);
                            changeProListener.upTextSize();
                        }

                        @Override
                        public void setPaddingTop(int number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setPaddingTop(number);
                            changeProListener.upMargin();
                        }

                        @Override
                        public void setPaddingBottom(int number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setPaddingBottom(number);
                            changeProListener.upMargin();
                        }

                        @Override
                        public void setPaddingLeft(int number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setPaddingLeft(number);
                            changeProListener.upMargin();
                        }

                        @Override
                        public void setPaddingRight(int number) {
                            readBookControl.setStyleDefault(5);
                            updateStyleDefault(5);
                            readBookControl.setPaddingRight(number);
                            changeProListener.upMargin();
                        }
                        

                    })
                    .create()
                    .show();

        });


        //背景选择
        flBg0.setOnClickListener(v -> {
            updateBg(0);
            changeProListener.bgChange();
        });
        flBg1.setOnClickListener(v -> {
            updateBg(1);
            changeProListener.bgChange();
        });
        flBg2.setOnClickListener(v -> {
            updateBg(2);
            changeProListener.bgChange();
        });
        flBg3.setOnClickListener(v -> {
            updateBg(3);
            changeProListener.bgChange();
        });
        //自定颜色
        flMoreColor.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ReadStyleActivity.class);
            intent.putExtra("index", readBookControl.getTextDrawableIndex());
            activity.startActivity(intent);
        });

        //翻页1
        flPage1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setPageMode(0);
                updatePageMode(0);
                changeProListener.upPageMode();
            }
        });

        //翻页2
        flPage2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setPageMode(1);
                updatePageMode(1);
                changeProListener.upPageMode();
            }
        });

        //翻页3
        flPage3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookControl.setPageMode(2);
                updatePageMode(2);
                changeProListener.upPageMode();
            }
        });


        //自定义翻页
        flMorePage.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity, R.style.alertDialogThemeRead)
                    .setTitle(activity.getString(R.string.page_mode))
                    .setSingleChoiceItems(PageAnimation.Mode.getAllPageMode(), readBookControl.getPageMode(), (dialogInterface, i) -> {
                        readBookControl.setPageMode(i);
                        updatePageMode(i);
                        changeProListener.upPageMode();
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
        });
    }




    //设置字体
    public void setReadFonts(String path) {
        readBookControl.setReadBookFont(path);
        changeProListener.refresh();
    }

    //清除字体
    private void clearFontPath() {
        readBookControl.setReadBookFont(null);
        changeProListener.refresh();
    }

    private void updateBoldText(float boldSize) {
        //tvBold.setSelected(isBold);
        java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.0");
        tvBoldSize.setText(myformat.format(boldSize));
    }

    private void updateStyleDefault(int styleDefault) {
        switch (styleDefault) {
            case 1:
                flSpace1.setSelected(true);
                flSpace2.setSelected(false);
                flSpace3.setSelected(false);
                flSpaceNone.setSelected(false);
                flMoreStyle.setSelected(false);
                break;
            case 2:
                flSpace1.setSelected(false);
                flSpace2.setSelected(true);
                flSpace3.setSelected(false);
                flSpaceNone.setSelected(false);
                flMoreStyle.setSelected(false);
                break;
            case 3:
                flSpace1.setSelected(false);
                flSpace2.setSelected(false);
                flSpace3.setSelected(true);
                flSpaceNone.setSelected(false);
                flMoreStyle.setSelected(false);
                break;
            case 4:
                flSpace1.setSelected(false);
                flSpace2.setSelected(false);
                flSpace3.setSelected(false);
                flSpaceNone.setSelected(true);
                flMoreStyle.setSelected(false);
                break;
            case 5:
                flSpace1.setSelected(false);
                flSpace2.setSelected(false);
                flSpace3.setSelected(false);
                flSpaceNone.setSelected(false);
                flMoreStyle.setSelected(true);
                break;
        }

        //tvBold.setSelected(styleDefault);
    }


    private void updatePageMode(int pageMode) {
        switch (pageMode) {
            case 0:
                flPage1.setSelected(true);
                flPage2.setSelected(false);
                flPage3.setSelected(false);
                flMorePage.setSelected(false);
                break;
            case 1:
                flPage1.setSelected(false);
                flPage2.setSelected(true);
                flPage3.setSelected(false);
                flMorePage.setSelected(false);
                break;
            case 2:
                flPage1.setSelected(false);
                flPage2.setSelected(false);
                flPage3.setSelected(true);
                flMorePage.setSelected(false);
                break;
            case 3:
                flPage1.setSelected(false);
                flPage2.setSelected(false);
                flPage3.setSelected(false);
                flMorePage.setSelected(true);
                break;
        }
    }

    public void setBg() {
        tv0.setTextColor(readBookControl.getTextColor(0));
        tv1.setTextColor(readBookControl.getTextColor(1));
        tv2.setTextColor(readBookControl.getTextColor(2));
        tv3.setTextColor(readBookControl.getTextColor(3));

        civ0.setImageDrawable(readBookControl.getBgDrawable(0, activity, 100, 180));
        civ1.setImageDrawable(readBookControl.getBgDrawable(1, activity, 100, 180));
        civ2.setImageDrawable(readBookControl.getBgDrawable(2, activity, 100, 180));
        civ3.setImageDrawable(readBookControl.getBgDrawable(3, activity, 100, 180));
    }

    private void updateBg(int index) {

        switch (index) {
            case 0:
                flBg0.setSelected(true);
                flBg1.setSelected(false);
                flBg2.setSelected(false);
                flBg3.setSelected(false);
                break;
            case 1:
                flBg0.setSelected(false);
                flBg1.setSelected(true);
                flBg2.setSelected(false);
                flBg3.setSelected(false);
                break;
            case 2:
                flBg0.setSelected(false);
                flBg1.setSelected(false);
                flBg2.setSelected(true);
                flBg3.setSelected(false);
                break;
            case 3:
                flBg0.setSelected(false);
                flBg1.setSelected(false);
                flBg2.setSelected(false);
                flBg3.setSelected(true);
                break;

        }


        /*
        flBg0.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        flBg2.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        flBg2.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        flBg2.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        switch (index) {
            case 0:
                civBgWhite.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 1:
                civBgYellow.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 2:
                civBgGreen.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 3:
                civBgBlue.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 4:
                civBgBlack.setBorderColor(Color.parseColor("#F3B63F"));
                break;
        }
        */
        readBookControl.setTextDrawableIndex(index);
    }

    public interface OnChangeProListener {
        void upPageMode();

        void upTextSize();

        void upMargin();

        void bgChange();

        void refresh();

        void  upTextSizeAndMargin();
    }
}