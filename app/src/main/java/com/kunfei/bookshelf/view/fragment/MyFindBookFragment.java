package com.kunfei.bookshelf.view.fragment;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.MyFindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.presenter.MyFindBookPresenter;
import com.kunfei.bookshelf.presenter.contract.MyFindBookContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.view.activity.MyMainActivity;
import com.kunfei.bookshelf.view.adapter.FindFlowAdapter;
import com.kunfei.bookshelf.view.adapter.FindSecondAdapter;
import com.kunfei.bookshelf.widget.flowlayout.TagFlowLayout;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

public class MyFindBookFragment extends MBaseFragment<MyFindBookContract.Presenter> implements MyFindBookContract.View {
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.rl_empty_view)
    RelativeLayout rlEmptyView;

    @BindView(R.id.tfl_find)
    TagFlowLayout tflFind;

    private boolean isRecreate;

    //private boolean findIsFlow;

    private CallBackValue callBackValue;

    private Unbinder unbinder;
    private FindFlowAdapter flowAdapter;
    private FindSecondAdapter findSecondAdapter;

    private MoDialogHUD moProgressHUD;

    private Activity mActivity;

    private MyFindKindGroupBean selectedKindGroupBean;





    @Override
    public int createLayoutId() {
        return R.layout.my_fragment_book_find;
    }

    @Override
    protected MyFindBookContract.Presenter initInjector() {
        return new MyFindBookPresenter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBackValue = (MyFindBookFragment.CallBackValue) getActivity();

        mActivity = getActivity();
    }

    @Override
    protected void initData() {
        isRecreate = callBackValue != null && callBackValue.isRecreate();
    }
    @Override
    protected void bindView() {
        super.bindView();
        unbinder = ButterKnife.bind(this, view);

        tflFind.setVisibility(View.VISIBLE);
        flowAdapter = new FindFlowAdapter();
        findSecondAdapter = new FindSecondAdapter();
        tflFind.setAdapter(flowAdapter);

        moProgressHUD = new MoDialogHUD(this.getContext());

        flowAdapter.setOnItemClickListener(new FindFlowAdapter.OnItemClickListener() {
            @Override
            public void itemClick(View v,MyFindKindGroupBean findKindGroupBean) {

                selectedKindGroupBean = findKindGroupBean;

                mPresenter.getSecondFind(findKindGroupBean);
                //moProgressHUD.showKindList(findKindGroupBean.getGroupName(),list,findSecondAdapter);

            }
        });

        findSecondAdapter.setOnItemClickListener(new FindSecondAdapter.OnItemClickListener() {
            @Override
            public void itemClick(View v, FindKindBean findKindBean) {
                //Toast.makeText(rootView.getContext(), findKindBean.getKindUrl(), Toast.LENGTH_SHORT).show();
                moProgressHUD.dismiss();
                //todo
                ((MyMainActivity)mActivity).kindSearch(findKindBean.getKindUrl(),findKindBean.getTag(),selectedKindGroupBean);


            }
        });



        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
        });
    }




    /**
     * 首次逻辑操作
     */
    @Override
    protected void firstRequest() {
        super.firstRequest();
        mPresenter.initData();

    }

    @Override
    public synchronized void updateUI(List<MyFindKindGroupBean> group) {
        if (rlEmptyView == null) return;
        updateFlowUI(group);


    }

    @Override
    public synchronized void ShowSecond(List<FindKindBean> list,String GroupName) {
        if (list == null) return;
        moProgressHUD.showKindList(GroupName,list,findSecondAdapter);


    }

    public MyFindKindGroupBean getSelectedKindGroupBean() {
        return selectedKindGroupBean;
    }

    public void setSelectedKindGroupBean(MyFindKindGroupBean selectedKindGroupBean) {
        this.selectedKindGroupBean = selectedKindGroupBean;
    }

    public  void updateFlowUI(List<MyFindKindGroupBean> group) {
        if (group.size() > 0) {

            flowAdapter.replaceAll(group);

            rlEmptyView.setVisibility(View.GONE);
        } else {
            flowAdapter.clearAll();
            tvEmpty.setText("没有发现，可以在书源里添加。");
            rlEmptyView.setVisibility(View.VISIBLE);
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



    public interface CallBackValue {
        boolean isRecreate();

    }




}
