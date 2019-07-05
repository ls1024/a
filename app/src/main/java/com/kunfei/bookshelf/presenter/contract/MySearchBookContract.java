package com.kunfei.bookshelf.presenter.contract;

import android.widget.EditText;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.view.adapter.MySearchBookAdapter;

import java.util.List;

public interface MySearchBookContract {
    interface Presenter extends IPresenter {

        void insertSearchHistory(String content);



        int getPage();
        String getUrl();

        void initPage();

        void toSearchBooks(String key, Boolean fromError);

        void initKindPage(String url, String tag);

        void toKindSearch();

        void stopSearch();

    }

    interface View extends IView {

        void searchBook(String searchKey);

        /**
         * 成功 新增查询记录
         */
        void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

        /**
         * 成功搜索 搜索记录
         */
        void querySearchHistorySuccess(List<SearchHistoryBean> datas);

        /**
         * 首次查询成功 更新UI
         */
        void refreshSearchBook();

        /**
         * 加载更多书籍成功 更新UI
         */
        void loadMoreSearchBook(List<SearchBookBean> books);


        /**
         * 加载更多书籍成功 更新UI
         */
        void loadMoreKindBook(List<SearchBookBean> books);


        /**
         * 刷新成功
         */
        void refreshFinish(Boolean isAll);

        /**
         * 刷新成功
         */
        void refreshKindFinish(Boolean isAll);

        /**
         * 加载成功
         */
        void loadMoreFinish(Boolean isAll);

        /**
         * 加载成功
         */
        void loadMoreKindFinish(Boolean isAll);


        /**
         * 搜索失败
         */
        void searchBookError(Throwable throwable);

        /**
         * 获取搜索内容EditText
         */
        EditText getEdtContent();

        /**
         * @return SearchBookAdapter
         */
        MySearchBookAdapter getSearchBookAdapter();



        //发现相关

        void refreshKindBook(List<SearchBookBean> value);



    }

}
