package com.kunfei.bookshelf.view.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.bean.MyFindKindGroupBean;
import com.kunfei.bookshelf.widget.flowlayout.TagAdapter;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class FindFlowAdapter extends TagAdapter<MyFindKindGroupBean> {
    public FindFlowAdapter() {
        super(new ArrayList<MyFindKindGroupBean>());
    }

    public interface OnItemClickListener{
        void itemClick(View v, MyFindKindGroupBean findKindGroupBean);
    }
    private FindFlowAdapter.OnItemClickListener onItemClickListener;

    public OnItemClickListener getListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public View getView(com.kunfei.bookshelf.widget.flowlayout.FlowLayout parent, int position, MyFindKindGroupBean findKindGroupBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_flow_find_item,
                parent, false);

        tv.setText(findKindGroupBean.getGroupName());

        Random myRandom = new Random();
        int ranColor = 0xff000000 | myRandom.nextInt(0x00ffffff);
        //tv.setBackgroundColor(ranColor);


        GradientDrawable bgShape = (GradientDrawable)tv.getBackground();
        bgShape.setStroke(1, ranColor);

/*
         int roundRadius = 15; // 8px not dp

        int fillColor = Color.parseColor("#DFDFE0");
         bgShape.setColor(fillColor);
         bgShape.setCornerRadius(roundRadius);

*/
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != onItemClickListener){
                    onItemClickListener.itemClick(v,findKindGroupBean);
                }
            }
        });
        return tv;
    }



    public MyFindKindGroupBean getItemData(int position){
        return mTagDatas.get(position);
    }

    public int getDataSize(){
        return mTagDatas.size();
    }
}