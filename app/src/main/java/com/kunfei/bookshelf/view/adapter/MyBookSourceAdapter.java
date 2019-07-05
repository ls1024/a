package com.kunfei.bookshelf.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.activity.BookSourceActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;
import com.kunfei.bookshelf.widget.modialog.SelectSourceDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class MyBookSourceAdapter extends RecyclerView.Adapter<MyBookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> dataList;
    private List<BookSourceBean> allDataList;
    private SelectSourceDialog activity;
    private int index;
    private int sort;

    private ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(dataList, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            //activity.saveDate(dataList);
            return true;
        }
    };

    public MyBookSourceAdapter(SelectSourceDialog activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void resetDataS(List<BookSourceBean> bookSourceBeanList) {
        this.dataList = bookSourceBeanList;
        notifyDataSetChanged();
        //activity.upDateSelectAll();
        //activity.upSearchView(dataList.size());
        //activity.upGroupMenu();
    }

    private void setAllDataList(List<BookSourceBean> bookSourceBeanList) {
        this.allDataList = bookSourceBeanList;
        notifyDataSetChanged();
        activity.upDateSelectAll();
    }


    public List<BookSourceBean> getDataList() {
        return dataList;
    }

    public List<BookSourceBean> getSelectDataList() {
        List<BookSourceBean> selectDataS = new ArrayList<>();
        for (BookSourceBean data : dataList) {
            if (data.getEnable()) {
                selectDataS.add(data);
            }
        }
        return selectDataS;
    }

    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_item_book_source, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //holder.itemView.setBackgroundColor(ThemeStore.backgroundColor(activity));
        if (TextUtils.isEmpty(dataList.get(position).getBookSourceGroup())) {
            holder.cbView.setText(dataList.get(position).getBookSourceName());
        } else {
            holder.cbView.setText(String.format("%s (%s)", dataList.get(position).getBookSourceName(), dataList.get(position).getBookSourceGroup()));
        }
        holder.cbView.setChecked(dataList.get(position).getEnable());
        holder.cbView.setOnClickListener((View view) -> {
            dataList.get(position).setEnable(holder.cbView.isChecked());
            activity.saveDate(dataList.get(position));
            activity.upDateSelectAll();
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbView;

        MyViewHolder(View itemView) {
            super(itemView);
            cbView = itemView.findViewById(R.id.cb_book_source);

        }
    }
}
