package com.kunfei.bookshelf.presenter.contract;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.SearchHistoryBean;

import java.util.List;

public interface MyMainContract {

    interface View extends IView {

        void initImmersionBar();

        /**
         * 成功搜索 搜索记录
         */
        void querySearchHistorySuccess(List<SearchHistoryBean> datas);

        /**
         * 删除成功
         * @param datas
         */
        void deleteSearchHistorySuccess(List<SearchHistoryBean> datas);


        void reloadSearchHistory();


        /**
         * 取消弹出框
         */
        void dismissHUD();

        /**
         * 恢复数据
         */
        void onRestore(String msg);

        void recreate();

        void updateUI();

        void authorSearch(String author);

        void keyWordSearch(String keyWord);

        void toast(String msg);

        void toast(int strId);

        int getGroup();
    }

    interface Presenter extends IPresenter {

        void backupData();

        void restoreData();

        void addBookUrl(String bookUrl);

        void clearBookshelf();

        void querySearchHistory(String content);

        void cleanSearchHistory();

        void cleanSearchHistory(String content);

        //void cleanSearchHistory(SearchHistoryBean searchHistoryBean);

        void setHiddenMode( boolean openBookHiddenFunction);
    }

}
