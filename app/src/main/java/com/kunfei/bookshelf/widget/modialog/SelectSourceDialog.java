package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.BookSourceBeanDao;
import com.kunfei.bookshelf.dao.SearchBookBeanDao;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.utils.ScreenUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.activity.BookSourceActivity;
import com.kunfei.bookshelf.view.activity.MyMainActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.view.adapter.BookSourceAdapter;
import com.kunfei.bookshelf.view.adapter.ChangeSourceAdapter;
import com.kunfei.bookshelf.view.adapter.MyBookSourceAdapter;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerView;
import com.kunfei.bookshelf.widget.views.ATECheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SelectSourceDialog {
    private Context context;
    private AppCompatImageButton IbSelectAll;
    private AppCompatImageButton IbGroups;
    private TextView manageSource;
    private TextView cancel;
    private TextView ok;
    private ATECheckBox noPicSearch;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MyBookSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;
    private Callback callback;
    private BaseDialog dialog;

    private boolean selectAll = true;
    private SubMenu groupMenu;
    private MenuItem groupItem;
    PopupMenu popup;

    private boolean isSearch;


    public static SelectSourceDialog builder(Context context) {
        return new SelectSourceDialog(context);
    }

    private SelectSourceDialog(Context context) {
        this.context = context;
        compositeDisposable = new CompositeDisposable();
        dialog = new BaseDialog(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.my_dialog_select_source, null);
        bindView(view);
        dialog.setContentView(view);
        initData();
    }

    private void bindView(View view) {

        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        IbSelectAll = view.findViewById(R.id.ib_select_all);
        //IbSelectAll.setVisibility(View.INVISIBLE);
        IbGroups = view.findViewById(R.id.ib_groups);
        //IbGroups.setVisibility(View.INVISIBLE);
        manageSource = view.findViewById(R.id.tv_manage_source);
        ok = view.findViewById(R.id.tv_ok);
        cancel = view.findViewById(R.id.tv_cancel);
        noPicSearch = view.findViewById(R.id.cb_no_pic_search);

        searchView = view.findViewById(R.id.searchView);


        if(ReadBookControl.getInstance().getNoPicSearch()){
            noPicSearch.setChecked(true);
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        //ibtStop.setVisibility(View.INVISIBLE);
        ok.setOnClickListener(v -> {
            dialog.dismiss();
            callback.gotoSearch();
        });

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        IbGroups.setOnClickListener(v -> {
            popup.show();
        });

        IbSelectAll.setOnClickListener(v -> {
            selectAllDataS();
        });




        noPicSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    ReadBookControl.getInstance().setNoPicSearch(true);
                }else{
                    ReadBookControl.getInstance().setNoPicSearch(false);

                }
            }
        });

        manageSource.setOnClickListener(v -> {
            dialog.dismiss();
            callback.gotoManageSource();

        });


        initSearchView();

    }


    public SelectSourceDialog setCallback(SelectSourceDialog.Callback callback) {
        this.callback = callback;
        return this;
    }

    @SuppressLint("InflateParams")
    private void initData() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
        adapter = new MyBookSourceAdapter(this);
        recyclerView.setAdapter(adapter);

        refreshBookSource(false);
        upGroupMenu();


    }


    private void initSearchView() {
        searchView.setQueryHint(context.getString(R.string.search_book_source));
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSearch = !TextUtils.isEmpty(newText);
                refreshBookSource(isSearch);
                return false;
            }
        });
    }


    public void saveDate(BookSourceBean bookSourceBean) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean));
        RxBus.get().post(RxBusTag.SOURCE_LIST_CHANGE, true);
    }

    public void saveDate(List<BookSourceBean> bookSourceBeans) {
        AsyncTask.execute(() -> {
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(bookSourceBeans);
        });

        RxBus.get().post(RxBusTag.SOURCE_LIST_CHANGE, true);
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            if (!bookSourceBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }


    public void upGroupMenu() {

        List<String> groupList = BookSourceManager.getGroupList();
        if (groupList.size() == 0) {
            IbGroups.setVisibility(View.INVISIBLE);
        }else{
            IbGroups.setVisibility(View.VISIBLE);

            //创建弹出式菜单对象（最低版本11）
            popup = new PopupMenu(this.context, IbGroups);//第二个参数是绑定的那个view
            for (String groupName : new ArrayList<>(groupList)) {
                popup.getMenu().add(groupName);
            }

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    searchView.setQuery(item.getTitle(), true);
                    return false;
                }
            });


        }
    }

    private void selectAllDataS() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        saveDate(adapter.getDataList());
        //setResult(RESULT_OK);
    }


    public interface Callback {
        void gotoManageSource();
        void gotoSearch();
    }


    public void refreshBookSource(Boolean isSearch) {
        if (isSearch) {
            String term = "%" + searchView.getQuery() + "%";
            List<BookSourceBean> sourceBeanList = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                            BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                            BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                    .orderRaw(BookSourceManager.getBookSourceSort())
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
            adapter.resetDataS(sourceBeanList);
        } else {
            adapter.resetDataS(BookSourceManager.getAllBookSource());
        }
    }

    public SelectSourceDialog show() {
        dialog.show();
        WindowManager.LayoutParams params = Objects.requireNonNull(dialog.getWindow()).getAttributes();
        params.height = ScreenUtils.getAppSize()[1] - 360;
        dialog.getWindow().setAttributes(params);
        return this;
    }





}
