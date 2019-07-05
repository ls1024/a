//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class KindBookAdapter extends RefreshRecyclerViewAdapter {

    private WeakReference<Activity> activityRef;
    private List<SearchBookBean> searchBooks;
    private OnItemClickListener itemClickListener;
    private Boolean noPicSearch = false;


    public KindBookAdapter(Activity activity) {
        super(true);
        searchBooks = new ArrayList<>();
        this.activityRef = new WeakReference<>(activity);

    }

    public void setNoPicSearch(Boolean noPicSearch) {
        this.noPicSearch = noPicSearch;
    }


    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderImpl(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_web_search_item, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchBookBean searchBook = getItem(position);

        Activity activity = activityRef.get();
        if (!activity.isFinishing()) {

            if(!noPicSearch) {
                Glide.with(activity)
                        .load(searchBooks.get(position).getCoverUrl())
                        .apply(new RequestOptions()
                                .dontAnimate()
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop()
                                .placeholder(R.drawable.img_cover_default))
                        .into(((ViewHolderImpl) holder).cover);
            }
        }


        String title = searchBooks.get(position).getName();

        //title = title.replace(searchKey,"<font color='#1971b9'>"+searchKey+"</font>");
        ((ViewHolderImpl) holder).title.setText(Html.fromHtml(title));


        String author = searchBooks.get(position).getAuthor();
        // author = author.replace(searchKey,"<font color='#1971b9'>"+searchKey+"</font>");

        ((ViewHolderImpl) holder).author.setText(Html.fromHtml(author));
        if(!isEmpty(searchBooks.get(position).getIntroduce())) {
            ((ViewHolderImpl) holder).description.setText("　　" + searchBooks.get(position).getIntroduce());
        }
        //String aa = ((Viewholder) holder).sources.getText().toString();

        String strSrc ="";
        for(int i = 0; i< searchBooks.get(position).getHitList().size();i++){
            strSrc += " " + searchBooks.get(position).getHitList().get(i).getBookSourceName();
        }
        ((ViewHolderImpl) holder).sources.setText("书源(" + searchBooks.get(position).getHitList().size() + "):" + strSrc.substring(1));


        //列表事件
        ((ViewHolderImpl) holder).flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(((ViewHolderImpl) holder).cover, position, searchBooks.get(position));
        });
    }


    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public SearchBookBean getItem(int position) {

        return searchBooks.get(position);
    }



    public void clear(){
        searchBooks.clear();
        notifyDataSetChanged();
    }


    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getICount();
            if (newData.size() > 0) {
                searchBooks.addAll(newData);
            }
            notifyItemInserted(position);
            notifyItemRangeChanged(position, newData.size());
        }
    }

    public void replaceAll(List<SearchBookBean> newData) {
        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }


    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }


    public interface OnItemClickListener {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }


    private  class ViewHolderImpl extends RecyclerView.ViewHolder {
        LinearLayout flContent;
        ImageView cover;
        TextView title;
        TextView author;
        TextView description;
        TextView sources;

        ViewHolderImpl(View itemView) {
            super(itemView);
            flContent = (LinearLayout) itemView.findViewById(R.id.fl_content);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            title = (TextView) itemView.findViewById(R.id.title);
            author = (TextView) itemView.findViewById(R.id.author);
            description = (TextView) itemView.findViewById(R.id.description);
            sources = (TextView) itemView.findViewById(R.id.sources);
        }
    }
}