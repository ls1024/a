package com.kunfei.bookshelf.widget.my_page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.SharedPreferencesUtil;
import com.kunfei.bookshelf.utils.bar.ImmersionBar;
import com.kunfei.bookshelf.view.activity.MyReadBookActivity;
import com.kunfei.bookshelf.widget.my_page.animation.CoverPageAnim;
import com.kunfei.bookshelf.widget.my_page.animation.HorizonPageAnim;
import com.kunfei.bookshelf.widget.my_page.animation.NonePageAnim;
import com.kunfei.bookshelf.widget.my_page.animation.PageAnimation;
import com.kunfei.bookshelf.widget.my_page.animation.ScrollPageAnim;
import com.kunfei.bookshelf.widget.my_page.animation.SimulationPageAnim;
import com.kunfei.bookshelf.widget.my_page.animation.SlidePageAnim;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kunfei.bookshelf.utils.ScreenUtils.getDisplayMetrics;


/**
 * 绘制页面显示内容的类
 */
public class PageView extends View {

    private MyReadBookActivity activity;

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高
    private int statusBarHeight = 0; //状态栏高度

    private boolean actionFromEdge = false;
    // 初始化参数
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private boolean isPrepare;
    // 动画类
    private PageAnimation mPageAnim;
    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;
    // 动画监听类

    //文字选择画笔
    private Paint mTextSelectPaint = null;
    //文字选择画笔颜色
    private int TextSelectColor = Color.parseColor("#77fadb08");//todo,其实可以做成配置项，哈哈

    private Path mSelectTextPath = new Path();


    //触摸到起始位置
    private int mStartX = 0;
    private int mStartY = 0;

    // 是否发触了长按事件
    private boolean isLongPress = false;

    //第一个选择的文字
    private TxtChar firstSelectTxtChar= null;
    //最后选择的一个文字
    private TxtChar lastSelectTxtChar= null;

    //选择模式
    private Mode mCurrentMode = Mode.Normal;

    //文本高度
    private float textHeight = 0;

    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;

    //是否在移动
    private boolean isMove = false;

    //选择的列
    private List<TxtLine> mSelectLines = new ArrayList<TxtLine>();



    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public void resetScroll() {
            mPageLoader.resetPageOffset();
        }

        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext(int pageOnCur) {
            return PageView.this.hasNextPage(pageOnCur);
        }

        @Override
        public void drawContent(Canvas canvas, float offset) {
            PageView.this.drawContent(canvas, offset);
        }

        @Override
        public void drawBackground(Canvas canvas) {
            PageView.this.drawBackground(canvas);
        }

        @Override
        public void changePage(PageAnimation.Direction direction) {
            mPageLoader.pagingEnd(direction);
        }

        @Override
        public void clickCenter() {
            if (mTouchListener != null) {
                mTouchListener.center();
            }
        }
    };

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        //初始化画笔
        mTextSelectPaint = new Paint();
        mTextSelectPaint.setAntiAlias(true);
        mTextSelectPaint.setTextSize(19);
        mTextSelectPaint.setColor(TextSelectColor);

        //定义长按事件
        setOnLongClickListener(mLongClickListener);
    }

    //长按监听
    private OnLongClickListener mLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (mStartX > 0 && mStartY > 0) {// 说明还没释放，是长按事件
                isLongPress = true;//长按
                TxtChar p = mPageLoader.DetectPressTxtChar(mStartX, mStartY);//找到长按的点
                firstSelectTxtChar = p;//设置开始位置字符
                lastSelectTxtChar = p;//设置结束位置字符
                mCurrentMode = Mode.PressSelectText;//设置模式为长按选择
                mTouchListener.onLongPress();//响应长按事件，供上层调用

            }
            return false;
        }
    };

    public TxtChar getFirstSelectTxtChar() {
        return firstSelectTxtChar;
    }

    public void setFirstSelectTxtChar(TxtChar firstSelectTxtChar) {
        this.firstSelectTxtChar = firstSelectTxtChar;
    }

    public TxtChar getLastSelectTxtChar() {
        return lastSelectTxtChar;
    }

    public void setLastSelectTxtChar(TxtChar lastSelectTxtChar) {
        this.lastSelectTxtChar = lastSelectTxtChar;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mViewWidth = width;
        mViewHeight = height;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(width, height);
        }

    }

    //设置翻页的模式
    void setPageMode(PageAnimation.Mode pageMode, int marginTop, int marginBottom) {
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0 || mPageLoader == null) return;
        if (!readBookControl.getHideStatusBar()) {
            marginTop = marginTop + statusBarHeight;
        }
        switch (pageMode) {
            case SIMULATION:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case COVER:
                mPageAnim = new CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SLIDE:
                mPageAnim = new SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case NONE:
                mPageAnim = new NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SCROLL:
                mPageAnim = new ScrollPageAnim(mViewWidth, mViewHeight, 0,
                        marginTop, marginBottom, this, mPageAnimListener);
                break;
            default:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
    }

    public MyReadBookActivity getActivity() {
        return activity;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public Bitmap getBgBitmap(int pageOnCur) {
        if (mPageAnim == null) return null;
        return mPageAnim.getBgBitmap(pageOnCur);
    }

    public void autoPrevPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.PREV);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.PREV);
        }
    }

    public void autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            ((ScrollPageAnim) mPageAnim).startAnim(PageAnimation.Direction.NEXT);
        } else {
            startHorizonPageAnim(PageAnimation.Direction.NEXT);
        }
    }

    private synchronized void startHorizonPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //结束动画
        mPageAnim.abortAnim();
        if (direction == PageAnimation.Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            //设置方向
            boolean hasNext = hasNextPage(0);

            mPageAnim.setDirection(direction);
            if (!hasNext) {
                ((HorizonPageAnim) mPageAnim).setNoNext(true);
                return;
            }
        } else if (direction == PageAnimation.Direction.PREV) {
            int x = 0;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            mPageAnim.setDirection(direction);
            //设置方向方向
            boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                ((HorizonPageAnim) mPageAnim).setNoNext(true);
                return;
            }
        } else {
            return;
        }
        ((HorizonPageAnim) mPageAnim).setNoNext(false);
        ((HorizonPageAnim) mPageAnim).setCancel(false);
        mPageAnim.startAnim();
    }

    public void drawPage(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
    }

    /**
     * 绘制滚动背景
     */
    public void drawBackground(Canvas canvas) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawBackground(canvas);
        }
    }

    /**
     * 绘制滚动内容
     */
    public void drawContent(Canvas canvas, float offset) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawContent(canvas, offset);
        }
    }

    /**
     * 绘制横翻背景
     */
    public void drawBackground(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
    }

    /**
     * 绘制横翻内容
     * @param pageOnCur 相对当前页的位置
     */
    public void drawContent(int pageOnCur) {
        if (!isPrepare) return;
        if (mPageLoader != null) {
            mPageLoader.drawPage(getBgBitmap(pageOnCur), pageOnCur);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPageAnim instanceof ScrollPageAnim)
            super.onDraw(canvas);
        //绘制动画
        if (mPageAnim != null) {
            mPageAnim.draw(canvas);
        }

        if (mCurrentMode != Mode.Normal && !isRunning() && !isMove) {
            DrawSelectText(canvas);
        }
    }


    private void DrawSelectText(Canvas canvas) {


        if (mCurrentMode == Mode.PressSelectText) {
            DrawPressSelectText(canvas);
        } else if (mCurrentMode == Mode.SelectMoveForward) {
            DrawMoveSelectText(canvas);
        } else if (mCurrentMode == Mode.SelectMoveBack) {
            DrawMoveSelectText(canvas);
        }
    }


    private void DrawPressSelectText(Canvas canvas) {


        if (lastSelectTxtChar != null) {// 找到了选择的字符
            mSelectTextPath.reset();
            mSelectTextPath.moveTo(firstSelectTxtChar.TopLeftPosition.x, firstSelectTxtChar.TopLeftPosition.y);
            mSelectTextPath.lineTo(firstSelectTxtChar.TopRightPosition.x,firstSelectTxtChar.TopRightPosition.y);
            mSelectTextPath.lineTo(firstSelectTxtChar.BottomRightPosition.x, firstSelectTxtChar.BottomRightPosition.y);
            mSelectTextPath.lineTo(firstSelectTxtChar.BottomLeftPosition.x, firstSelectTxtChar.BottomLeftPosition.y);
            canvas.drawPath(mSelectTextPath, mTextSelectPaint);

            //DrawBorderPoint(canvas);

        }
    }


    public String getSelectStr(){

        if(mSelectLines.size()==0){
            return  String.valueOf(firstSelectTxtChar.getChardata());
        }
        StringBuilder sb = new StringBuilder();
        for (TxtLine l : mSelectLines) {
            //Log.e("selectline", l.getLineData() + "");
            sb.append(l.getLineData());
        }

        return sb.toString();
    }


    private void DrawMoveSelectText(Canvas canvas) {
        if (firstSelectTxtChar == null || lastSelectTxtChar == null)
            return;
        GetSelectData();
        DrawSeletLines(canvas);
        //DrawBorderPoint(canvas);
    }

    List<TxtLine> mLinseData = null ;

    private void GetSelectData() {

        mLinseData = mPageLoader.curChapter().txtChapter.getPage(mPageLoader.getCurPagePos()).txtLists;

        Boolean Started = false;
        Boolean Ended = false;

        mSelectLines.clear();

        Log.e("FirstSelectTxtChar",firstSelectTxtChar.toString());
        Log.e("LastSelectTxtChar",lastSelectTxtChar.toString());

        // 找到选择的字符数据，转化为选择的行，然后将行选择背景画出来
        for (TxtLine l : mLinseData) {

            TxtLine selectline = new TxtLine();
            selectline.CharsData = new ArrayList<TxtChar>();

            for (TxtChar c : l.CharsData) {


                if (!Started) {
                    if (c.Index == firstSelectTxtChar.Index) {
                        Started = true;
                        Log.e("找到第一個字符",c.toString());
                        selectline.CharsData.add(c);
                        if (c.Index == lastSelectTxtChar.Index) {
                            Ended = true;
                            Log.e("找到最後一字符",c.toString());
                            break;
                        }
                    }
                } else {

                    if (c.Index == lastSelectTxtChar.Index) {
                        Ended = true;
                        if (!selectline.CharsData.contains(c)) {
                            selectline.CharsData.add(c);

                        }
                        break;
                    } else {
                        selectline.CharsData.add(c);
                        Log.e("找到中間字符",c.toString());
                    }
                }
            }

            Log.e("selectline", selectline.toString() + "");
            mSelectLines.add(selectline);

            if (Started && Ended) {
                break;
            }
        }
    }

    public Mode getmCurrentMode() {
        return mCurrentMode;
    }

    public void setmCurrentMode(Mode mCurrentMode) {
        this.mCurrentMode = mCurrentMode;
    }

    private void DrawSeletLines(Canvas canvas) {
        // DrawRectangleSeletLinesBg(canvas);
        DrawOaleSeletLinesBg(canvas);
    }

    public void clearSelect(){
        firstSelectTxtChar = null;
        lastSelectTxtChar = null;
        mCurrentMode = Mode.Normal;
        mSelectTextPath.reset();
        invalidate();

    }


    //根据当前坐标返回文字
    public TxtChar getCurrentTxtChar(float x, float y) {
        return mPageLoader.DetectPressTxtChar(x, y);
    }

    private void DrawOaleSeletLinesBg(Canvas canvas) {// 绘制椭圆型的选中背景
        for (TxtLine l : mSelectLines) {
            Log.e("selectline", l.getLineData() + "");

            if (l.CharsData != null && l.CharsData.size() > 0) {


                TxtChar fistchar = l.CharsData.get(0);
                TxtChar lastchar = l.CharsData.get(l.CharsData.size() - 1);

                float fw = fistchar.charWidth;
                float lw = lastchar.charWidth;

                RectF rect = new RectF(fistchar.TopLeftPosition.x, fistchar.TopLeftPosition.y,
                        lastchar.TopRightPosition.x, lastchar.BottomRightPosition.y);

                canvas.drawRoundRect(rect, fw / 2,
                        textHeight / 2, mTextSelectPaint);

            }
        }
    }



    @Override
    public void computeScroll() {
        //进行滑动
        if (mPageAnim != null) {
            mPageAnim.scrollAnim();
        }
        super.computeScroll();
    }

    //@SuppressLint("ClickableViewAccessibility")
    //@Override
    public boolean onTouchEventOld(MotionEvent event) {
        super.onTouchEvent(event);

        if (mPageLoader != null) {
            //Paint.FontMetrics fontMetrics = mPageLoader.mTextPaint.getFontMetrics();
            //TextHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);
        }

        int x = (int) event.getX();
        int y = (int) event.getY();

        if (mPageAnim == null) return true;
        if (actionFromEdge) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                actionFromEdge = false;
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getEdgeFlags() != 0 || event.getRawY() < ScreenUtils.dpToPx(5) || event.getRawY() > getDisplayMetrics().heightPixels - ScreenUtils.dpToPx(5)) {
                actionFromEdge = true;
                return true;
            }

            mStartX = x;
            mStartY = y;

            mTouchListener.onTouch();
            mPageAnim.onTouchEvent(event);
        } else {
            mPageAnim.onTouchEvent(event);
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);


        if (mPageLoader != null) {
            Paint.FontMetrics fontMetrics = mPageLoader.mTextPaint.getFontMetrics();
            textHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);
        }

        if (mPageAnim == null) {
            return true;
        }

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        if (actionFromEdge) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                actionFromEdge = false;
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getEdgeFlags() != 0 || event.getRawY() < ScreenUtils.dpToPx(5) || event.getRawY() > getDisplayMetrics().heightPixels - ScreenUtils.dpToPx(5)) {
                    actionFromEdge = true;
                    return true;
                }
                mStartX = x;
                mStartY = y;
                isMove = false;
                isLongPress = false;
                canTouch = mTouchListener.onTouch();
                mPageAnim.onTouchEvent(event);

                mCurrentMode = Mode.Normal;

                mTouchListener.onTouchClearCursor();

                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(mStartX - event.getX()) > slop || Math.abs(mStartY - event.getY()) > slop;
                }

                // 如果滑动了,且不是长按，则进行翻页。
                if (isMove) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(mViewWidth / 4f, mViewHeight / 4f,
                                mViewWidth * 3f / 4, mViewHeight * 3f / 4);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)  ) {
                        if(firstSelectTxtChar==null) {
                            if (mTouchListener != null) {
                                mTouchListener.center();
                            }
                        }else{
                            if(mSelectTextPath!=null){//长安选择删除选中状态
                                if(!isLongPress){
                                    firstSelectTxtChar = null;
                                    mSelectTextPath.reset();
                                    invalidate();
                                }

                                //mTouchListener.onTouchUp();
                            }
                            //清除移动选择状态
                        }
                        return true;
                    }


                    if (!readBookControl.getCanClickTurn()) {
                        return true;
                    }

                    if (mPageAnim instanceof ScrollPageAnim && SharedPreferencesUtil.getBoolean("disableScrollClickTurn", false)) {
                        return true;
                    }
                }

                if(firstSelectTxtChar==null || isMove){//长安选择删除选中状态
                    mPageAnim.onTouchEvent(event);
                }else{
                    if(!isLongPress){
                        firstSelectTxtChar = null;
                        mSelectTextPath.reset();
                        invalidate();
                    }
                }




                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     */
    private boolean hasPrevPage() {
        if (mPageLoader.hasPrev()) {
            return true;
        } else {
            showSnackBar("没有上一页");
            return false;
        }
    }

    /**
     * 判断是否下一页存在
     */
    private boolean hasNextPage(int pageOnCur) {
        if (mPageLoader.hasNext(pageOnCur)) {
            return true;
        } else {
            showSnackBar("没有下一页");
            return false;
        }
    }

    public boolean isRunning() {
        return mPageAnim != null && mPageAnim.isRunning();
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPageAnim != null) {
            mPageAnim.abortAnim();
            mPageAnim.clear();
        }

        mPageLoader = null;
        mPageAnim = null;
    }

    /**
     * 获取 PageLoader
     */
    public PageLoader getPageLoader(MyReadBookActivity activity, BookShelfBean bookShelfBean, PageLoader.Callback callback) {
        this.activity = activity;
        this.statusBarHeight = ImmersionBar.getStatusBarHeight(activity);
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 根据书籍类型，获取具体的加载器
        if (!Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG)) {
            mPageLoader = new PageLoaderNet(this, bookShelfBean, callback);
        } else {
            String fileSuffix = FileHelp.getFileSuffix(bookShelfBean.getNoteUrl());
            if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_EPUB)) {
                mPageLoader = new PageLoaderEpub(this, bookShelfBean, callback);
            } else {
                mPageLoader = new PageLoaderText(this, bookShelfBean, callback);
            }
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public void autoChangeSource() {
        mPageLoader.setStatus(TxtChapter.Status.CHANGE_SOURCE);
        activity.autoChangeSource();
    }

    public void showSnackBar(String msg) {
        activity.showSnackBar(this, msg);
    }

    public interface TouchListener {
        boolean onTouch();

        boolean onTouchClearCursor();

        boolean onLongPress();

        void center();
    }

    public enum Mode {
        Normal, PressSelectText, SelectMoveForward, SelectMoveBack
    }
}
