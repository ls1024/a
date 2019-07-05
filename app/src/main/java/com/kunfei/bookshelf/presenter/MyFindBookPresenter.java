//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.bean.MyFindKindGroupBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.presenter.contract.MyFindBookContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.utils.RxUtils;
import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

public class MyFindBookPresenter extends BasePresenterImpl<MyFindBookContract.View> implements MyFindBookContract.Presenter {
    private Disposable disposable;
    private AnalyzeRule analyzeRule;

    @Override
    public void initData() {
        if (disposable != null) return;

        Single.create((SingleOnSubscribe<List<MyFindKindGroupBean>>) e -> {
            boolean showAllFind = MApplication.getInstance().getConfigPreferences().getBoolean("showAllFind", true);
            List<MyFindKindGroupBean> group = new ArrayList<>();

            List<BookSourceBean> sourceBeans = new ArrayList<>(showAllFind ? BookSourceManager.getAllBookSourceBySerialNumber() : BookSourceManager.getSelectedBookSourceBySerialNumber());
            for (BookSourceBean sourceBean : sourceBeans) {
                try {

                    if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {

                        MyFindKindGroupBean groupBean = new MyFindKindGroupBean();
                        groupBean.setGroupName(sourceBean.getBookSourceName());
                        groupBean.setGroupTag(sourceBean.getBookSourceUrl());
                        group.add(groupBean);


                    }
                } catch (Exception exception) {
                    sourceBean.addGroup("发现规则语法错误");
                    BookSourceManager.addBookSource(sourceBean);
                }
            }
            e.onSuccess(group);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<List<MyFindKindGroupBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(List<MyFindKindGroupBean> recyclerViewData) {
                        mView.updateUI(recyclerViewData);
                        disposable.dispose();
                        disposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        disposable.dispose();
                        disposable = null;
                    }


                });
    }





    @Override
    public void getSecondFind(MyFindKindGroupBean findKindGroupBean) {
        if (disposable != null) return;

        Single.create((SingleOnSubscribe<List<FindKindBean>>) e -> {

            List<FindKindBean> list = new ArrayList<FindKindBean>();
            ACache aCache = ACache.get(mView.getContext(), "findCache");

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

            e.onSuccess(list);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<List<FindKindBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(List<FindKindBean> list) {
                        //mView.updateUI(recyclerViewData);
                        mView.ShowSecond(list,findKindGroupBean.getGroupName());
                        disposable.dispose();
                        disposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        disposable.dispose();
                        disposable = null;
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
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_SOURCE)})
    public void hadAddOrRemoveBook(Object object) {
        initData();
    }
}