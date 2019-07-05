package com.kunfei.bookshelf.presenter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.SearchHistoryBeanDao;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.SearchBookModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.presenter.contract.MySearchBookContract;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MySearchBookPresenter extends BasePresenterImpl<MySearchBookContract.View> implements MySearchBookContract.Presenter {
    private static final int BOOK = 2;

    private long startThisSearchTime;
    private String durSearchKey;

    private List<BookShelfBean> bookShelfS = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架


    private List<FindKindBean> list = new ArrayList<FindKindBean>();//用来存放发现

    private SearchBookModel searchBookModel;

    private int page = 1;

    private String url = "";

    private  String tag = "";

    private Boolean kindHasMore = true;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MySearchBookPresenter(Context context) {
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> booAll = BookshelfHelp.getAllBook();
            e.onNext(booAll == null ? new ArrayList<>() : booAll);
            e.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        bookShelfS.addAll(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });

        //搜索监听
        SearchBookModel.OnSearchListener onSearchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                mView.refreshSearchBook();
            }

            @Override
            public void refreshFinish(Boolean value) {
                mView.refreshFinish(value);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                mView.loadMoreFinish(value);
            }


            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                mView.loadMoreSearchBook(value);
            }

            @Override
            public void searchBookError(Throwable throwable) {
                mView.searchBookError(throwable);
            }

            @Override
            public int getItemCount() {
                return mView.getSearchBookAdapter().getItemCount();
            }
        };
        //搜索引擎初始化
        searchBookModel = new SearchBookModel(onSearchListener);
    }

    public void insertSearchHistory(String content) {
        final int type = MySearchBookPresenter.BOOK;
        //final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create((ObservableOnSubscribe<SearchHistoryBean>) e -> {
            List<SearchHistoryBean> data = DbHelper.getDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.eq(content))
                    .limit(1)
                    .build().list();
            SearchHistoryBean searchHistoryBean;
            if (null != data && data.size() > 0) {
                searchHistoryBean = data.get(0);
                searchHistoryBean.setDate(System.currentTimeMillis());
                DbHelper.getDaoSession().getSearchHistoryBeanDao().update(searchHistoryBean);
            } else {
                searchHistoryBean = new SearchHistoryBean(type, content, System.currentTimeMillis());
                DbHelper.getDaoSession().getSearchHistoryBeanDao().insert(searchHistoryBean);
            }
            e.onNext(searchHistoryBean);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<SearchHistoryBean>() {
                    @Override
                    public void onNext(SearchHistoryBean value) {
                        mView.insertSearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }





    @Override
    public int getPage() {
        return searchBookModel.getPage();
    }

    @Override
    public void initPage() {
        searchBookModel.setPage(0);
    }

    @Override
    public void initKindPage(String url, String tag) {
        this.page = 1;
        this.url = url;
        this.tag = tag;
        this.startThisSearchTime = System.currentTimeMillis();
    }

    @Override
    public void toSearchBooks(String key, Boolean fromError) {
        if (key != null) {
            durSearchKey = key;
            startThisSearchTime = System.currentTimeMillis();
            searchBookModel.setSearchTime(startThisSearchTime);
            searchBookModel.searchReNew();
        }
        searchBookModel.search(durSearchKey, startThisSearchTime, bookShelfS, fromError);
    }

    @Override
    public String getUrl() {
        return url;
    }


    //
    @Override
    public void toKindSearch(){

        kindHasMore = true;
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> temp = DbHelper.getDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (temp == null)
                temp = new ArrayList<>();
            e.onNext(temp);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        bookShelfS.addAll(value);
                        final long tempTime = startThisSearchTime;
                        kindSearch(url,tag,tempTime);
                        // mView.startRefreshAnim();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }
                });
    }


    public  void  kindSearch(String url, String tag,long searchTime) {

        if(kindHasMore) {

            WebBookModel.getInstance().findBook(url, page, tag)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<List<SearchBookBean>>() {
                        @Override
                        public void onNext(List<SearchBookBean> value) {
                            if (searchTime == startThisSearchTime) {

                                for (SearchBookBean temp : value) {
                                    for (BookShelfBean bookShelfBean : bookShelfS) {
                                        if (temp.getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                                            temp.setIsCurrentSource(true);
                                            break;
                                        }
                                    }
                                }

                                if (page == 1) {
                                    mView.refreshKindBook(value);
                                    mView.refreshKindFinish(value.size() <= 0);
                                } else {
                                    mView.loadMoreKindBook(value);
                                }
                                page++;
                            }
                        }


                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //mView.searchBookError();
                            kindHasMore = false;
                            kindSearch(url, tag, searchTime);
                        }
                    });
        }else {//到头了

            mView.refreshKindFinish(true);
            mView.loadMoreKindFinish(true);
        }
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void stopSearch() {
        searchBookModel.stopSearch();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        searchBookModel.onDestroy();
    }



    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SOURCE_LIST_CHANGE)})
    public void sourceListChange(Boolean change) {

        searchBookModel.initSearchEngineS(BookSourceManager.getSelectedBookSource());
    }

}
