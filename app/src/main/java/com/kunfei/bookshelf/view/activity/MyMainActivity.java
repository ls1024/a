//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseTabActivity;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.bean.MyFindKindGroupBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.data.ColorSuggestion;
import com.kunfei.bookshelf.data.ColorWrapper;
import com.kunfei.bookshelf.data.DataHelper;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.help.DataBackup;
import com.kunfei.bookshelf.help.LauncherIcon;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.presenter.MyMainPresenter;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.presenter.contract.MyMainContract;
import com.kunfei.bookshelf.utils.BitmapUtil;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.theme.ATH;
import com.kunfei.bookshelf.view.fragment.BookListFragment;
import com.kunfei.bookshelf.view.fragment.FindBookFragment;
import com.kunfei.bookshelf.view.fragment.MyFindBookFragment;
import com.kunfei.bookshelf.view.fragment.MyBookListFragment;
//import com.kunfei.bookshelf.view.fragment.SearchBookFragment;
import com.kunfei.bookshelf.view.fragment.MySearchBookFragment;
import com.kunfei.bookshelf.widget.modialog.InputDialog;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.kunfei.bookshelf.utils.NetworkUtils.isNetWorkAvailable;


public class MyMainActivity extends BaseTabActivity<MyMainContract.Presenter> implements  MyMainContract.View, AppBarLayout.OnOffsetChangedListener, MyBookListFragment.CallBackValue, MyFindBookFragment.CallBackValue{

    @BindView(R.id.floating_search_view)
    FloatingSearchView mSearchView;
    @BindView(R.id.appbar)
    AppBarLayout mAppBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    CoordinatorLayout aa;
    private Switch swNightTheme;

    private Handler handler = new Handler();


    private final String TAG = "BlankFragment";

    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    private boolean mIsDarkSearchTheme = false;

    //private String mLastQuery = "";

    private int suggestionCount = 10;

    private boolean viewIsList;
    private boolean openBookHiddenFunction;//是否开启书籍隐藏功能

    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;
    private final int requestSource = 14;

    private static String[] mTitles = new String[]{"书架", "搜索列表" ,"发现"};

    private int group;
    private boolean resumed = false;
    private MoDialogHUD moDialogHUD;

    private long exitTime = 0;

    private ActionBarDrawerToggle mDrawerToggle;

    //private boolean isChangeTheme = false;


    private String mLastQuery = "";

    final static int COUNTS = 2;// 点击次数
    final static long DURATION = 1000;// 规定有效时间
    long[] mHits = new long[COUNTS];


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }



        if (!MApplication.getInstance().isChangeTheme() && preferences.getBoolean(getString(R.string.pk_default_read), false)) {//第一次运行且设置了自动打开最近阅读
            MApplication.getInstance().setChangeTheme(false);
            startReadActivity();
        }
        MApplication.getInstance().setChangeTheme(false);

        // if (savedInstanceState != null) {
        //    resumed = savedInstanceState.getBoolean("resumed");
        //}
        group = preferences.getInt("bookshelfGroup", 0);
        //super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // outState.putBoolean("resumed", resumed);
    }

    private void startReadActivity() {
        Intent intent = new Intent(this, MyReadBookActivity.class);
        intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
        startActivity(intent);
    }

    @Override
    protected MyMainContract.Presenter initInjector() {
        return new MyMainPresenter();
    }


    @Override
    protected void onCreateActivity() {

        setContentView(R.layout.my_activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {

        mPresenter.querySearchHistory("");

        viewIsList = preferences.getBoolean("bookshelfIsList", false);
        openBookHiddenFunction = preferences.getBoolean("openBookHiddenFunction", false);


    }

    @Override
    public void reloadSearchHistory(){
        mPresenter.querySearchHistory("");
    }

    @Override
    protected void bindView() {

        super.bindView();


        mAppBar.addOnOffsetChangedListener(this);

        setupSearchBar();//todo 设置搜索框的各种事件，历史等
        setUpNavigationView();//todo 以后可以自定义图片，和文字，个性化
        // initDrawer();
        initTabLayout();
        upGroup(group);
        moDialogHUD = new MoDialogHUD(this);


        //preferences.getBoolean("bookshelfIsList", false);

        //setFloatingSearchViewNightTheme(isNightTheme());

        updateUI();
    }






    private void changeTabSelect(TabLayout.Tab tab) {
        View view = tab.getCustomView();

        TextView txt_title = view.findViewById(R.id.tabtext);

        txt_title.setTextColor(getResources().getColor(R.color.selectTab));

    }

    private void changeTabNormal(TabLayout.Tab tab) {
        View view = tab.getCustomView();

        TextView txt_title = view.findViewById(R.id.tabtext);

        txt_title.setTextColor(getResources().getColor(R.color.noSelectTab));

    }


    public  void kindSearch(String url, String tag, MyFindKindGroupBean findKindGroupBean){
        mVp.setCurrentItem(1);
        mTlIndicator.getTabAt(1).select();

        String ttag = "android:switcher:"+mVp.getId()+":"+mVp.getCurrentItem();

        FragmentManager fm = getSupportFragmentManager();
        //todo
        MySearchBookFragment fragment = (MySearchBookFragment)fm.findFragmentByTag(ttag);
        fragment.kindSearch(url,tag,findKindGroupBean);

    }


    @Override
    public  void authorSearch(String author){
        mVp.setCurrentItem(1);
        mTlIndicator.getTabAt(1).select();

        String ttag = "android:switcher:"+mVp.getId()+":"+mVp.getCurrentItem();

        FragmentManager fm = getSupportFragmentManager();
        MySearchBookFragment fragment = (MySearchBookFragment)fm.findFragmentByTag(ttag);

        mSearchView.setSearchText(author);

        fragment.setSearchKey(author);
        fragment.setSearchAuthor(author);
        fragment.authorSearch();

    }


    @Override
    public  void keyWordSearch(String keyWord){
        mVp.setCurrentItem(1);
        mTlIndicator.getTabAt(1).select();

        String ttag = "android:switcher:"+mVp.getId()+":"+mVp.getCurrentItem();

        FragmentManager fm = getSupportFragmentManager();
        MySearchBookFragment fragment = (MySearchBookFragment)fm.findFragmentByTag(ttag);

        mSearchView.setSearchText(keyWord);

        fragment.setSearchKey(keyWord);
        fragment.toSearch();

    }


    @Override
    protected List<Fragment> createTabFragments() {

        /*
        MyBookListFragment bookListFragment = new MyBookListFragment();
        MySearchBookFragment searchBookFragment = new MySearchBookFragment();
        MyFindBookFragment findBookFragment = new MyFindBookFragment();

    */


        MyBookListFragment bookListFragment = null;
        MySearchBookFragment searchBookFragment = null;
        MyFindBookFragment findBookFragment = null;

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MyBookListFragment) {
                bookListFragment = (MyBookListFragment) fragment;
            } else if (fragment instanceof MySearchBookFragment) {
                searchBookFragment = (MySearchBookFragment) fragment;
            }else if (fragment instanceof FindBookFragment) {
                findBookFragment = (MyFindBookFragment) fragment;
            }
        }
        if (bookListFragment == null)
            bookListFragment = new MyBookListFragment();
        if (searchBookFragment == null)
            searchBookFragment = new MySearchBookFragment();
        if (findBookFragment == null)
            findBookFragment = new MyFindBookFragment();
        return Arrays.asList(bookListFragment, searchBookFragment, findBookFragment);

    }

    //初始化TabLayout和ViewPager
    private void initTabLayout() {
        //TabLayout使用自定义Item
        for (int i = 0; i < mTlIndicator.getTabCount(); i++) {
            TabLayout.Tab tab = mTlIndicator.getTabAt(i);
            if (tab == null) return;
            if (i == 0) { //设置第一个Item的点击事件(当下标为0时触发)
                tab.setCustomView(tab_icon(mTitles[i], R.drawable.ic_arrow_drop_down));
                View view1 = tab.getCustomView();

                TextView txt_title = view1.findViewById(R.id.tabtext);

                txt_title.setTextColor(getResources().getColor(R.color.selectTab));

                View tabView = (View) Objects.requireNonNull(tab.getCustomView()).getParent();
                tabView.setTag(i);
                tabView.setOnClickListener(view -> {
                    if (tabView.isSelected()) {
                        showBookGroupMenu(view);
                    }
                });
            } else {
                tab.setCustomView(tab_icon(mTitles[i], null));
                View view1 = tab.getCustomView();
                TextView txt_title = view1.findViewById(R.id.tabtext);
                txt_title.setTextColor(getResources().getColor(R.color.noSelectTab));
            }

            View tabView = (View) Objects.requireNonNull(tab.getCustomView()).getParent();
            tabView.setOnLongClickListener(view -> {
                //continuousClick(COUNTS, DURATION);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("openBookHiddenFunction", !openBookHiddenFunction);
                editor.apply();
                openBookHiddenFunction = !openBookHiddenFunction;

                String aaa = "只显示未隐藏的书籍。";
                if(!openBookHiddenFunction){
                    aaa = "显示所有书籍。";
                }


                mPresenter.setHiddenMode(openBookHiddenFunction);//触发列表刷新

                Toast.makeText(this, aaa, Toast.LENGTH_SHORT).show();

                return true;
            });
        }


        mTlIndicator.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                changeTabSelect(tab);

                //设置右边三点的菜单
                if(tab.getPosition()==0){

                    mSearchView.inflateOverflowMenu(R.menu.menu_main_bookshelf);
                }
                if(tab.getPosition()==1){
                    mSearchView.inflateOverflowMenu(R.menu.menu_main_search);
                }
                if(tab.getPosition()==2){
                    mSearchView.inflateOverflowMenu(R.menu.menu_main_find);
                }

                //setFloatingSearchMenuIcon();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                changeTabNormal(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }



    private void continuousClick(int count, long time) {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //为数组最后一位赋值
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
            mHits = new long[COUNTS];//重新初始化数组


            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("openBookHiddenFunction", !openBookHiddenFunction);
            editor.apply();
            openBookHiddenFunction = !openBookHiddenFunction;

            String aaa = "连续点击了2次,开始书籍隐藏模式，长按书籍详情里的书名，隐藏书籍。";
            if(!openBookHiddenFunction){
                aaa = "连续点击了2次,关闭书籍隐藏模式,显示所有书籍。";
            }


            mPresenter.setHiddenMode(openBookHiddenFunction);//触发列表刷新

            Toast.makeText(this, aaa, Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public int getGroup() {
        return group;
    }

    /**
     * 显示分组菜单
     */
    private void showBookGroupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        for (int j = 0; j < getResources().getStringArray(R.array.book_group_array).length; j++) {
            popupMenu.getMenu().add(0, 0, j, getResources().getStringArray(R.array.book_group_array)[j]);
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            upGroup(menuItem.getOrder());
            return true;
        });
        popupMenu.setOnDismissListener(popupMenu1 -> updateTabItemIcon(false));
        popupMenu.show();
        updateTabItemIcon(true);
    }

    private void updateTabItemIcon(boolean showMenu) {
        TabLayout.Tab tab = mTlIndicator.getTabAt(0);
        if (tab == null) return;
        View customView = tab.getCustomView();
        if (customView == null) return;
        ImageView im = customView.findViewById(R.id.tabicon);
        if (showMenu) {
            im.setImageResource(R.drawable.ic_arrow_drop_up);
        } else {
            im.setImageResource(R.drawable.ic_arrow_drop_down);
        }
    }

    private void upGroup(int group) {
        if (this.group != group) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("bookshelfGroup", group);
            editor.apply();
        }
        this.group = group;
        RxBus.get().post(RxBusTag.UPDATE_GROUP, group);
        RxBus.get().post(RxBusTag.REFRESH_BOOK_LIST, false);
        //更换Tab文字
        updateTabItemText(group);

    }

    private void updateTabItemText(int group) {
        TabLayout.Tab tab = mTlIndicator.getTabAt(0);
        if (tab == null) return;
        View customView = tab.getCustomView();
        if (customView == null) return;
        TextView tv = customView.findViewById(R.id.tabtext);
        tv.setText(getResources().getStringArray(R.array.book_group_array)[group]);
    }

    private View tab_icon(String name, Integer iconID) {
        @SuppressLint("InflateParams")
        View tabView = LayoutInflater.from(this).inflate(R.layout.tab_view_icon_right, null);
        TextView tv = tabView.findViewById(R.id.tabtext);
        //tv.setHeight(14);
        tv.setText(name);
        tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
        ImageView im = tabView.findViewById(R.id.tabicon);
        if (iconID != null) {
            im.setVisibility(View.VISIBLE);
            im.setImageResource(iconID);
        } else {
            im.setVisibility(View.GONE);
        }
        return tabView;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        if (swNightTheme != null) {
            swNightTheme.setChecked(isNightTheme());
        }
    }

    /**
     * 沉浸状态栏
     */
    @Override
    public void initImmersionBar() {
        super.initImmersionBar();
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(mTitles);
    }

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, MyMainActivity.class));
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mSearchView.setTranslationY(verticalOffset);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "start onResume~~~");



        setFloatingSearchViewNightTheme(isNightTheme());


    }

    private void setFloatingSearchViewNightTheme(Boolean isNightTheme){

        if(isNightTheme) {
            mIsDarkSearchTheme = true;
            mSearchView.setBackgroundColor(Color.parseColor("#282828"));
            mSearchView.setViewTextColor(Color.parseColor("#e9e9e9"));
            mSearchView.setHintTextColor(Color.parseColor("#e9e9e9"));
            mSearchView.setActionMenuOverflowColor(Color.parseColor("#e9e9e9"));
            mSearchView.setMenuItemIconColor(Color.parseColor("#e9e9e9"));
            mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
            mSearchView.setClearBtnColor(Color.parseColor("#e9e9e9"));
            mSearchView.setDividerColor(Color.parseColor("#BEBEBE"));
        }
        else{
            mIsDarkSearchTheme = false;
            mSearchView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mSearchView.setViewTextColor(Color.parseColor("#787878"));//
            mSearchView.setHintTextColor(Color.parseColor("#787878"));
            mSearchView.setActionMenuOverflowColor(Color.parseColor("#787878"));
            mSearchView.setMenuItemIconColor(Color.parseColor("#787878"));
            mSearchView.setLeftActionIconColor(Color.parseColor("#808080"));
            mSearchView.setClearBtnColor(Color.parseColor("#787878"));
            mSearchView.setDividerColor(Color.parseColor("#F0F0F0"));
        }
    }


    //侧边栏按钮
    private void setUpNavigationView() {
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.my_navigation_header, null);
        navigationView.addHeaderView(headerView);
        ColorStateList colorStateList = getResources().getColorStateList(R.color.navigation_color);
        navigationView.setItemTextColor(colorStateList);
        navigationView.setItemIconTintList(colorStateList);
        Menu drawerMenu = navigationView.getMenu();
        swNightTheme = drawerMenu.findItem(R.id.action_setting).getActionView().findViewById(R.id.sw_night_theme);
        swNightTheme.setChecked(isNightTheme());
        swNightTheme.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                setNightTheme(b);
                MApplication.getInstance().setChangeTheme(true);
                setFloatingSearchViewNightTheme(b);

            }
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                //case R.id.action_main_new:
                //     handler.postDelayed(() -> MainNewActivity.startThis(this), 200);
                //    break;
                case R.id.action_book_source_manage:
                    handler.postDelayed(() -> BookSourceActivity.startThis(this,requestSource), 200);
                    break;
                case R.id.action_replace_rule:
                    handler.postDelayed(() -> ReplaceRuleActivity.startThis(this,null), 200);
                    break;
                case R.id.action_download:
                    handler.postDelayed(() -> DownloadActivity.startThis(this), 200);
                    break;
                case R.id.action_setting:
                    handler.postDelayed(() -> MySettingActivity.startThis(this), 200);
                    break;
                case R.id.action_backup://todo
                    handler.postDelayed(this::backup, 200);
                    break;
                case R.id.action_restore: //todo
                    handler.postDelayed(this::restore, 200);
                    break;


            }
            return true;
        });
    }


    /**
     * 备份
     */
    private void backup() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallback() {
            @Override
            public void onHasPermission() {
                AlertDialog alertDialog = new AlertDialog.Builder(MyMainActivity.this)
                        .setTitle(R.string.backup_confirmation)
                        .setMessage(R.string.backup_message)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.backupData())
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                ATH.setAlertDialogTint(alertDialog);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                MyMainActivity.this.toast(R.string.backup_permission);
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                MyMainActivity.this.toast(R.string.backup_permission);
                PermissionUtils.requestMorePermissions(MyMainActivity.this, permission, BACKUP_RESULT);
            }
        });
    }

    /**
     * 恢复
     */
    private void restore() {
        PermissionUtils.checkMorePermissions(this, MApplication.PerList, new PermissionUtils.PermissionCheckCallback() {
            @Override
            public void onHasPermission() {
                AlertDialog alertDialog = new AlertDialog.Builder(MyMainActivity.this)
                        .setTitle(R.string.restore_confirmation)
                        .setMessage(R.string.restore_message)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.restoreData())
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                ATH.setAlertDialogTint(alertDialog);
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                MyMainActivity.this.toast(R.string.restore_permission);
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                PermissionUtils.requestMorePermissions(MyMainActivity.this, permission, RESTORE_RESULT);
            }
        });
    }


    private void setupSearchBar() {

        //左边按钮点击
        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                //打开抽屉侧滑菜单
                drawer.openDrawer(GravityCompat.START);
                mSearchView.closeMenu(true);

            }

            @Override
            public void onMenuClosed() {
                //Toast.makeText(MainActivity.this, "ddddd", Toast.LENGTH_SHORT).show();
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {



                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.swapSuggestions(DataHelper.getHistory(getContext(), suggestionCount));
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    mSearchView.showProgress();

                    //simulates a query call to a data source
                    //with a new query.
                    DataHelper.findSuggestions(getContext(), newQuery, 5,
                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                @Override
                                public void onResults(List<ColorSuggestion> results) {

                                    //this will swap the data and
                                    //render the collapse/expand animations as necessary
                                    mSearchView.swapSuggestions(results);

                                    //let the users know that the background
                                    //process has completed
                                    mSearchView.hideProgress();
                                }
                            });
                }


                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                mLastQuery = searchSuggestion.getBody();

                //mSearchView.setSearchText(mLastQuery);
                mSearchView.clearFocus();  //可以收起键盘
                mSearchView.clearSuggestions();

                ColorSuggestion colorSuggestion = (ColorSuggestion) searchSuggestion;

                mVp.setCurrentItem(1);
                mTlIndicator.getTabAt(1).select();

                String ttag = "android:switcher:"+mVp.getId()+":"+mVp.getCurrentItem();

                FragmentManager fm = getSupportFragmentManager();
                MySearchBookFragment fragment = (MySearchBookFragment)fm.findFragmentByTag(ttag);
                fragment.setSearchKey(colorSuggestion.getBody().trim());
                fragment.toSearch();


                /*
                DataHelper.findColors(getContext(), colorSuggestion.getBody(),
                        new DataHelper.OnFindColorsListener() {

                            @Override
                            public void onResults(List<ColorWrapper> results) {
                                //show search results
                            }

                        });
                        */
                Log.d(TAG, "onSuggestionClicked()");

                //mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {

                mLastQuery = query;


                mVp.setCurrentItem(1);
                mTlIndicator.getTabAt(1).select();

                String ttag = "android:switcher:"+mVp.getId()+":"+mVp.getCurrentItem();

                FragmentManager fm = getSupportFragmentManager();
                MySearchBookFragment fragment = (MySearchBookFragment)fm.findFragmentByTag(ttag);
                fragment.setSearchKey(query.trim());
                fragment.toSearch();

                Log.d(TAG, "onSearchAction()");


            }

            @Override
            public void onSuggestionDeleteClicked(SearchSuggestion searchSuggestion) {


                mPresenter.cleanSearchHistory(searchSuggestion.getBody());


            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                //show suggestions when search bar gains focus (typically history suggestions)
                mSearchView.swapSuggestions(DataHelper.getHistory(getContext(), suggestionCount));

                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                mSearchView.setSearchBarTitle(getString(R.string.app_name));

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                // mSearchView.setSearchText(searchSuggestion.getBody());

                Log.d(TAG, "onFocusCleared()");
            }
        });


        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                SharedPreferences.Editor editor = preferences.edit();
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_manage_source:
                        //mPresenter.cleanSearchHistory();
                        handler.postDelayed(() -> BookSourceActivity.startThis(MyMainActivity.this,requestSource), 200);
                        break;
                    case R.id.action_clear_history:
                        mPresenter.cleanSearchHistory();
                        break;
                    case R.id.action_add_local:
                        PermissionUtils.checkMorePermissions(getContext(), MApplication.PerList, new PermissionUtils.PermissionCheckCallback() {
                            @Override
                            public void onHasPermission() {
                                startActivity(new Intent(MyMainActivity.this, ImportBookActivity.class));
                            }

                            @Override
                            public void onUserHasAlreadyTurnedDown(String... permission) {
                                MyMainActivity.this.toast(R.string.import_per);
                            }

                            @Override
                            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                                MyMainActivity.this.toast(R.string.please_grant_storage_permission);
                                PermissionUtils.requestMorePermissions(MyMainActivity.this, permission, FILE_SELECT_RESULT);
                            }
                        });
                        break;
                    case R.id.action_add_url:
                        InputDialog.builder(getContext())
                                .setTitle(getString(R.string.add_book_url))
                                .setCallback(inputText -> {
                                    inputText = StringUtils.trim(inputText);
                                    mPresenter.addBookUrl(inputText);
                                }).show();
                        break;
                    case R.id.action_download_all:
                        if (!isNetWorkAvailable())
                            toast(R.string.network_connection_unavailable);
                        else
                            RxBus.get().post(RxBusTag.DOWNLOAD_ALL, 10000);
                        break;
                    case R.id.action_list_grid:
                        editor.putBoolean("bookshelfIsList", !viewIsList);
                        editor.apply();
                        MApplication.getInstance().setChangeTheme(true);
                        recreate();
                        break;
                    case R.id.action_clear_cache:
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.clear_content)
                                .setMessage("是否同时删除已下载的书籍目录？")
                                .setPositiveButton("是", (dialog, which) -> BookshelfHelp.clearCaches(true))
                                .setNegativeButton("否", (dialogInterface, i) -> BookshelfHelp.clearCaches(false))
                                .show();
                        break;
                    case R.id.action_clearBookshelf:
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.clear_bookshelf)
                                .setMessage(R.string.clear_bookshelf_s)
                                .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                })
                                .show();
                        break;
                    //case R.id.action_change_icon:
                    //    LauncherIcon.Change();
                    //     break;
                    case android.R.id.home:
                        if (drawer.isDrawerOpen(GravityCompat.START)
                        ) {
                            drawer.closeDrawers();
                        } else {
                            drawer.openDrawer(GravityCompat.START);
                        }
                        break;
                }

                //just print action
                //Toast.makeText(getContext().getApplicationContext(), item.getTitle(),
                //        Toast.LENGTH_SHORT).show();


            }


        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                ColorSuggestion colorSuggestion = (ColorSuggestion) item;

                String textColor = mIsDarkSearchTheme ? "#ffffff" : "#000000";
                String textLight = mIsDarkSearchTheme ? "#bfbfbf" : "#787878";

                if (colorSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }

                textView.setTextColor(Color.parseColor(textColor));
                String text = colorSuggestion.getBody()
                        .replaceFirst(mSearchView.getQuery(),
                                "<font color=\"" + textLight + "\">" + mSearchView.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }

        });
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> searchHistoryBeanList){

        List<ColorSuggestion> suggestionList = new ArrayList<>();
        ColorSuggestion colorSuggestion;

        //mSearchView.setSearchHint(getString((R.string.app_name)));

        if(searchHistoryBeanList!=null) {//清空浏览记录后要执行查询历史记录，让历史界面上为空
            for (int i = 0; i < searchHistoryBeanList.size(); i++) {
                colorSuggestion = new ColorSuggestion(searchHistoryBeanList.get(i).getContent());
                colorSuggestion.setIsHistory(true);
                suggestionList.add(colorSuggestion);
                if (suggestionList.size() == suggestionCount) {
                    break;
                }
            }
        }
        DataHelper.setsColorSuggestions(suggestionList);

    }

    @Override
    public void deleteSearchHistorySuccess(List<SearchHistoryBean> searchHistoryBeanList){

        List<ColorSuggestion> suggestionList = new ArrayList<>();
        ColorSuggestion colorSuggestion;

        //mSearchView.setSearchHint(getString((R.string.app_name)));

        if(searchHistoryBeanList!=null) {//清空浏览记录后要执行查询历史记录，让历史界面上为空
            for (int i = 0; i < searchHistoryBeanList.size(); i++) {
                colorSuggestion = new ColorSuggestion(searchHistoryBeanList.get(i).getContent());
                colorSuggestion.setIsHistory(true);
                suggestionList.add(colorSuggestion);
                if (suggestionList.size() == suggestionCount) {
                    break;
                }
            }
        }
        DataHelper.setsColorSuggestions(suggestionList);

        mSearchView.swapSuggestions(DataHelper.getHistory(getContext(), suggestionCount));
    }


    @Override
    protected void onDestroy() {
        UpLastChapterModel.destroy();
        super.onDestroy();
    }

    @Override
    public void dismissHUD() {
        moDialogHUD.dismiss();
    }

    public void onRestore(String msg) {
        moDialogHUD.showLoading(msg);
    }


    @Override
    public void recreate(){


        super.recreate();
    }

    @Override
    public void updateUI(){

        String logo_path = preferences.getString(getResources().getString(R.string.pk_logo_path), "");

        GifImageView logoPath =  navigationView.getHeaderView(0).findViewById(R.id.logo_path);

        File f=new File(logo_path);

        if(f.exists()){
            //logoPath.setImageURI(Uri.parse(logo_path));

            try {

                if(logo_path.toLowerCase().endsWith(".gif")) {
                    GifDrawable gifDrawable = new GifDrawable(f);

                    logoPath.setImageDrawable(gifDrawable);
                }else{
                    logoPath.setImageURI(Uri.parse(logo_path));
                }
            }catch (IOException e) {
                e.printStackTrace();
            }


        }else{

            logoPath.setImageResource(R.drawable.ebook);
        }


        String logo_title = preferences.getString(getResources().getString(R.string.pk_logo_title), "");
        String logo_title_align = preferences.getString(getResources().getString(R.string.pk_logo_title_align), "0");

        TextView logoTitle = navigationView.getHeaderView(0).findViewById(R.id.logo_title);

        if(!TextUtils.isEmpty(logo_title)) {
            logoTitle.setText(logo_title);
        }else{
            logoTitle.setText(R.string.read_summary);
        }

        switch (logo_title_align) {
            case "0":
                logoTitle.setGravity(Gravity.LEFT);
                break;
            case "1":
                logoTitle.setGravity(Gravity.CENTER);
                break;

            case "2":
                logoTitle.setGravity(Gravity.RIGHT);
                break;

        }



    }


    @Override
    protected void firstRequest() {
        if (!isRecreate) {
            //versionUpRun();
            requestPermission();
            handler.postDelayed(this::preloadReader, 200);
        }
        handler.postDelayed(() -> UpLastChapterModel.getInstance().startUpdate(), 60 * 1000);

    }

    /**
     * 获取权限
     */
    private void requestPermission() {
        List<String> per = PermissionUtils.checkMorePermissions(this, MApplication.PerList);
        if (per.size() > 0) {
            toast(R.string.get_storage_per);
            PermissionUtils.requestMorePermissions(this, per, MApplication.RESULT__PERMS);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else if (mTlIndicator.getSelectedTabPosition() != 0) {
            Objects.requireNonNull(mTlIndicator.getTabAt(0)).select();
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawers();
                    return true;
                }
                exit();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            if (getCurrentFocus() != null) {
                showSnackBar(mSearchView, "再按一次退出程序");
            }
            exitTime = System.currentTimeMillis();
        } else {
            MApplication.getInstance().setChangeTheme(false);
            DataBackup.getInstance().autoSave();
            finish();
        }
    }


    private void preloadReader() {
        AsyncTask.execute(() -> {
            ReadBookControl.getInstance();
            ChapterContentHelp.getInstance();
        });
    }

    @Override
    public boolean isRecreate() {
        return isRecreate;
    }


}
