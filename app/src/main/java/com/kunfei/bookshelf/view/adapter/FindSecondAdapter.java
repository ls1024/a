package com.kunfei.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.widget.flowlayout.FlowLayout;
import com.kunfei.bookshelf.widget.flowlayout.TagAdapter;

import java.util.ArrayList;

public class FindSecondAdapter extends TagAdapter<FindKindBean> {

    private String url = "";

    public FindSecondAdapter() {
        super(new ArrayList<FindKindBean>());
    }

    public interface OnItemClickListener{
        void itemClick(View v, FindKindBean findKindBean);
    }
    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public View getView(FlowLayout parent, int position, final FindKindBean findKindBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_flow_find_item,
                parent, false);
        tv.setText(findKindBean.getKindName());

        if(findKindBean.getKindUrl().equals(url)){

            tv.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_flow_source_item_selected));
        }
        //Random myRandom = new Random();
        //int ranColor = 0xff000000 | myRandom.nextInt(0x00ffffff);
        //tv.setBackgroundColor(ranColor);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != onItemClickListener){
                    onItemClickListener.itemClick(v,findKindBean);
                }
            }
        });
        return tv;
    }

    public FindKindBean getItemData(int position){
        return mTagDatas.get(position);
    }

    public int getDataSize(){
        return mTagDatas.size();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
