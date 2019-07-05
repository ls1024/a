package com.kunfei.bookshelf.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunfei.basemvplib.BitIntentDataManager;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.bean.MyFindKindGroupBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.presenter.BookDetailPresenter;
import com.kunfei.bookshelf.presenter.MySearchBookPresenter;
import com.kunfei.bookshelf.presenter.contract.MySearchBookContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.view.activity.BookDetailActivity;

import com.kunfei.bookshelf.view.activity.BookSourceActivity;
import com.kunfei.bookshelf.view.activity.MyMainActivity;
import com.kunfei.bookshelf.view.activity.MyReadBookActivity;
import com.kunfei.bookshelf.view.adapter.FindSecondAdapter;

import com.kunfei.bookshelf.view.adapter.KindBookAdapter;
import com.kunfei.bookshelf.view.adapter.MySearchBookAdapter;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.modialog.SelectSourceDialog;
import com.kunfei.bookshelf.widget.my_page.TxtChapter;
import com.kunfei.bookshelf.widget.recycler.refresh.OnLoadMoreListener;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.script.SimpleBindings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

public class MySearchBookFragment extends MBaseFragment<MySearchBookContract.Presenter> implements MySearchBookContract.View {


    @BindView(R.id.resultRecyclerview)
    RecyclerView myRecyclerView;
    @BindView(R.id.fabSearchStop)
    FloatingActionButton fabSearchStop;
    @BindView(R.id.fabShowSecond)
    FloatingActionButton fabShowSecond;
    @BindView(R.id.fabSetting)
    FloatingActionButton fabSetting;
    @BindView(R.id.need_input_keyword)
    TextView need_input_keyword;
    @BindView(R.id.in_book_searching)
    ProgressBar in_book_searching;

    private Activity mActivity;

    //XRecyclerView相关
    //@BindView(R.id.findResultRecyclerview)
    //LoadRecyclerView findResultRecyclerview;


    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;

    private MoDialogHUD moProgressHUD;
    private FindSecondAdapter findSecondAdapter;
    private MyFindKindGroupBean findKindGroupBean;

    private Unbinder unbinder;
    private MySearchBookAdapter searchBookAdapter;
    private KindBookAdapter kindBookAdapter;

    private String searchKey;
    private String searchAuthor = "";

    private boolean mInloading = false;

    private int mPage = 1;

    private int mFirstVisibleItem = 0;

    private  Boolean kindSearch = false;


    private Handler handler = new Handler(Looper.getMainLooper());


    private AnalyzeRule analyzeRule;


    @Override
    public int createLayoutId() {
        return R.layout.my_web_search_books;
    }

    @Override
    protected MySearchBookContract.Presenter initInjector() {
        return new MySearchBookPresenter(getContext());
    }

    @Override
    public void searchBook(String searchKey) {

    }

    @Override
    protected void initData() {

        searchBookAdapter = new MySearchBookAdapter(this.getActivity());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void setSearchAuthor(String searchAuthor) {
        this.searchAuthor = searchAuthor;
    }


    @Override
    protected void bindView() {

        super.bindView();
        unbinder = ButterKnife.bind(this, view);


        fabSearchStop.setOnClickListener(view -> {
            fabSearchStop.hide();
            fabSetting.show();
            in_book_searching.setVisibility(View.GONE);

            mPresenter.stopSearch();
        });

        fabShowSecond.setOnClickListener(view -> {

            findSecondAdapter.setUrl(mPresenter.getUrl());
            List<FindKindBean> list = new ArrayList<FindKindBean>();
            ACache aCache = ACache.get(getContext(), "findCache");

            BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(findKindGroupBean.getGroupTag());

            try {
                //读取发现规则
                boolean isJsAndCache = sourceBean.getRuleFindUrl().startsWith("<js>");
                String findRule;//这个字符串有可能是js生成的
                String[] kindA;//第一级
                if (isJsAndCache) {
                    findRule = aCache.getAsString(sourceBean.getBookSourceUrl());
                    if (TextUtils.isEmpty(findRule)) {
                        String jsStr = sourceBean.getRuleFindUrl().substring(4, sourceBean.getRuleFindUrl().lastIndexOf("<"));
                        findRule = evalJS(jsStr, sourceBean.getBookSourceUrl()).toString();
                    } else {
                        isJsAndCache = false;
                    }
                } else {
                    findRule = sourceBean.getRuleFindUrl();
                }

                kindA = findRule.split("(&&|\n)+");
                for (String kindB : kindA) {
                    if (kindB.trim().isEmpty()) continue;
                    String kind[] = kindB.split("::");
                    FindKindBean findKindBean = new FindKindBean();
                    findKindBean.setGroup(sourceBean.getBookSourceName());
                    findKindBean.setTag(sourceBean.getBookSourceUrl());
                    findKindBean.setKindName(kind[0]);
                    findKindBean.setKindUrl(kind[1]);
                    list.add(findKindBean);
                }

                if (isJsAndCache) {
                    aCache.put(sourceBean.getBookSourceUrl(), findRule);
                }
            }catch (Exception exception) {
                sourceBean.addGroup("发现规则语法错误");
                BookSourceManager.addBookSource(sourceBean);
            }

            moProgressHUD.showKindList(findKindGroupBean.getGroupName(),list,findSecondAdapter);

            //moProgressHUD.showKindList(findKindGroupBean.getGroupName(),findKindGroupBean.getChildren(),findSecondAdapter);

        });

        fabSetting.setOnClickListener(view -> {

            if (!NetworkUtils.isNetWorkAvailable()) {
                toast(R.string.network_connection_unavailable);
                return;
            }

            SelectSourceDialog.Callback callback = new SelectSourceDialog.Callback() {
                @Override
                public void gotoManageSource() {
                    handler.postDelayed(() -> BookSourceActivity.startThis(getActivity(),14), 200);
                }
                @Override
                public void gotoSearch(){
                    //如果搜索条件不为空则进行搜索

                    Boolean noPicSearch = ReadBookControl.getInstance().getNoPicSearch();
                    searchBookAdapter.setNoPicSearch(noPicSearch);
                    if(!TextUtils.isEmpty(searchKey)) {
                        toSearch();
                    }
                }
            };
            SelectSourceDialog.builder(this.getContext()).setCallback(callback).show();


        });


        fabSearchStop.hide();
        fabSetting.show();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext(),RecyclerView.VERTICAL,false);
        myRecyclerView.setLayoutManager(linearLayoutManager);
        myRecyclerView.setAdapter(searchBookAdapter);


        /*
        searchBookAdapter.setItemClickListener((view, position) -> {
            Intent intent = new Intent(getActivity(), BookDetailActivity.class);
            intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
            intent.putExtra("data", searchBookAdapter.getItemData(position));
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
        });
        */


        xBindView();


        searchBookAdapter.setItemClickListener(new MySearchBookAdapter.OnItemClickListener() {
            @Override
            public void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean) {
                //mPresenter.addBookToShelf(searchBookBean);
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {

                fabSearchStop.hide();
                fabSetting.show();
                mPresenter.stopSearch();



                String dataKey = String.valueOf(System.currentTimeMillis());
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
                intent.putExtra("data_key", dataKey);
                BitIntentDataManager.getInstance().putData(dataKey, searchBookAdapter.getItemData(position));

                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);

                /*
                try {
                    BitIntentDataManager.getInstance().putData(key, searchBookBean.clone());
                } catch (CloneNotSupportedException e) {
                    BitIntentDataManager.getInstance().putData(key, searchBookBean);
                    e.printStackTrace();
                }

                startActivity(intent);
                */
                //startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", getAnalyzeRule());
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    private AnalyzeRule getAnalyzeRule() {
        if (analyzeRule == null) {
            analyzeRule = new AnalyzeRule(null);
        }
        return analyzeRule;
    }

    @Override
    protected void bindEvent() {


        kindBookAdapter.setItemClickListener(new KindBookAdapter.OnItemClickListener() {
            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                String dataKey = String.valueOf(System.currentTimeMillis());
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
                intent.putExtra("data_key", dataKey);
                BitIntentDataManager.getInstance().putData(dataKey, kindBookAdapter.getItem(position));

                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        rfRvSearchBooks.setBaseRefreshListener(() -> {
            mPresenter.initPage();
            mPresenter.toKindSearch();
            startRefreshAnim();
        });
        rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                mPresenter.toKindSearch();
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toKindSearch();
            }
        });
    }


    public void startRefreshAnim() {
        rfRvSearchBooks.startRefresh();
    }

    /**
     * 开始搜索,供MainActivity调用
     */
    public void toSearch() {


        searchBookAdapter.setSearchAuthor("");

        mPresenter.stopSearch();


        kindSearch = false;

        kindBookAdapter.clear();
        searchBookAdapter.clear();

        //进入搜索状态，相关界面改变
        searchShow();

        if (!TextUtils.isEmpty(searchKey)) {
            mPresenter.insertSearchHistory(searchKey);
            //执行搜索请求
            new Handler().postDelayed(() -> {
                mPresenter.initPage();
                //myRecyclerView.startRefresh();
                fabSearchStop.show();
                //fabSetting.hide();
                mPresenter.toSearchBooks(searchKey, false);
            }, 300);
        }
    }

    public void  searchShow(){

       // fragmentThreadlist.setVisibility(View.GONE);
        rfRvSearchBooks.setVisibility(View.GONE);
        // swipeLayout.setVisibility(View.GONE);
        // mLoadingView.setVisibility(View.GONE);


         myRecyclerView.setVisibility(View.VISIBLE);
        fabSearchStop.show();
        //fabSetting.hide();
        fabShowSecond.hide();
        need_input_keyword.setVisibility(View.GONE);
        in_book_searching.setVisibility(View.VISIBLE);

    }


    public void  kindShow(){


        moProgressHUD = new MoDialogHUD(this.getContext());
        findSecondAdapter = new FindSecondAdapter();

        findSecondAdapter.setOnItemClickListener(new FindSecondAdapter.OnItemClickListener() {
            @Override
            public void itemClick(View v, FindKindBean findKindBean) {
                //Toast.makeText(rootView.getContext(), findKindBean.getKindUrl(), Toast.LENGTH_SHORT).show();
                moProgressHUD.dismiss();
                ((MyMainActivity)mActivity).kindSearch(findKindBean.getKindUrl(),findKindBean.getTag(),findKindGroupBean);


            }
        });

        myRecyclerView.setVisibility(View.GONE);
        fabSearchStop.hide();
        //fabSetting.hide();
        fabShowSecond.show();
        need_input_keyword.setVisibility(View.GONE);
        in_book_searching.setVisibility(View.GONE);

        //fragmentThreadlist.setVisibility(View.VISIBLE);
        rfRvSearchBooks.setVisibility(View.VISIBLE);
        //swipeLayout.setVisibility(View.VISIBLE);
        //mLoadingView.setVisibility(View.VISIBLE);



    }

    protected void xBindView() {


        //rfRvSearchBooks.setHasFixedSize(true);
        //rfRvSearchBooks.setLayoutManager(new LinearLayoutManager(mActivity));

        kindBookAdapter = new KindBookAdapter(mActivity);

        rfRvSearchBooks.setRefreshRecyclerViewAdapter(kindBookAdapter, new LinearLayoutManager(mActivity));

        View viewRefreshError = LayoutInflater.from(mActivity).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            kindBookAdapter.replaceAll(null);
            //刷新失败 ，重试
            mPresenter.initPage();
            mPresenter.toKindSearch();
            startRefreshAnim();
        });
       // rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(mActivity).inflate(R.layout.view_searchbook_no_data, null),
         //       viewRefreshError);


        //rfRvSearchBooks.setAdapter(kindBookAdapter);

        //rfRvSearchBooks.scrollToPosition(mFirstVisibleItem);




    }




    //搜索作者
    public void authorSearch( ) {

        searchBookAdapter.setSearchAuthor(searchAuthor);

        Boolean noPicSearch = ReadBookControl.getInstance().getNoPicSearch();


        searchBookAdapter.setNoPicSearch(noPicSearch);

        mPresenter.stopSearch();


        kindSearch = false;

        kindBookAdapter.clear();
        searchBookAdapter.clear();

        //进入搜索状态，相关界面改变
        searchShow();

        if (!TextUtils.isEmpty(searchKey)) {
            mPresenter.insertSearchHistory(searchKey);
            //执行搜索请求
            new Handler().postDelayed(() -> {
                mPresenter.initPage();
                //myRecyclerView.startRefresh();
                fabSearchStop.show();
                //fabSetting.hide();
                mPresenter.toSearchBooks(searchKey, false);
            }, 300);
        }


    }




    //来自发现的搜索
    public void kindSearch(String url, String tag, MyFindKindGroupBean selectedfindKindGroupBean){

        mPresenter.stopSearch();

        Boolean noPicSearch = ReadBookControl.getInstance().getNoPicSearch();


        searchBookAdapter.setSearchAuthor("");

        kindSearch = true;

        findKindGroupBean = selectedfindKindGroupBean;

        kindBookAdapter.clear();
        searchBookAdapter.clear();

        kindBookAdapter.setNoPicSearch(noPicSearch);
        kindShow();

        startRefreshAnim();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.initKindPage(url, tag);
                mPresenter.toKindSearch();
            }
        }, 300);





    }

    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        //修改主页面的搜索历史
        ((MyMainActivity)mActivity).reloadSearchHistory();
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> datas) {

    }

    //清空搜索结果
    @Override
    public void refreshSearchBook() {
        searchBookAdapter.clearAll();


    }

    @Override
    public void loadMoreSearchBook(List<SearchBookBean> books) {
        in_book_searching.setVisibility(View.GONE);
        searchBookAdapter.addAll(books, searchKey);
    }




    //整个搜索完成后调用
    @Override
    public void refreshFinish(Boolean isAll) {
        //fabSearchStop.hide();
    }



    @Override
    public void loadMoreFinish(Boolean isAll) {
       // fabSearchStop.hide();
    }

    //多个源搜索后都报错


    @Override
    public void searchBookError(Throwable throwable) {
        if (searchBookAdapter.getItemCount() == 0) {
           // ((TextView) refreshErrorView.findViewById(R.id.tv_error_msg)).setText(throwable.getMessage());
            rfRvSearchBooks.refreshError();
        } else {
            rfRvSearchBooks.loadMoreError();
        }
    }



    //1. 第一次加载数据
    @Override
    public void refreshKindBook(List<SearchBookBean> books) {

        mInloading = false;

        kindBookAdapter.replaceAll(books);
    }


    //2.第一次加载数后，判断是否结束
    @Override
    public void refreshKindFinish(Boolean isAll) {
        rfRvSearchBooks.finishRefresh(isAll, true);
    }

    //3.第一次加载数后，再次加载更多的数据
    @Override
    public void loadMoreKindBook(List<SearchBookBean> books) {

        if (books.size() <= 0) {//m
            loadMoreKindFinish(true);
            return;
        }
        for (SearchBookBean searchBook : kindBookAdapter.getSearchBooks()) {
            if (books.get(0).getName().equals( searchBook.getName()) && books.get(0).getAuthor().equals( searchBook.getAuthor())) {
                loadMoreKindFinish(true);
                return;
            }
        }
        kindBookAdapter.addAll(books);
        loadMoreKindFinish(false);

    }


    //4.结束
    @Override
    public void loadMoreKindFinish(Boolean isAll) {
        fabSearchStop.hide();
        fabSetting.show();
        rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public EditText getEdtContent() {
        return null;
    }

    @Override
    public MySearchBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
