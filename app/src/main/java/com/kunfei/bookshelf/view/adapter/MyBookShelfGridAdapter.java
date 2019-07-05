//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.utils.theme.ThemeStore;
import com.kunfei.bookshelf.view.adapter.base.OnItemClickListenerTwo;
import com.kunfei.bookshelf.widget.BadgeView;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MyBookShelfGridAdapter extends RecyclerView.Adapter<MyBookShelfGridAdapter.MyViewHolder> implements BookShelfAdapter {
    private List<BookShelfBean> books;
    private OnItemClickListenerTwo itemClickListener;
    private String bookshelfPx;
    private Activity activity;
    private HashSet<String> selectList = new HashSet<>();
    private boolean isArrange;


    private ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            int start = srcPosition;
            int end = targetPosition;
            if (start > end) {
                start = targetPosition;
                end = srcPosition;
            }
            notifyItemRangeChanged(start, end - start + 1);
            return true;
        }
    };

    public MyBookShelfGridAdapter(Activity activity) {
        this.activity = activity;
        books = new ArrayList<>();
    }

    @Override
    public void setArrange(boolean isArrange) {
        selectList.clear();
        this.isArrange = isArrange;
        notifyDataSetChanged();
    }

    @Override
    public void selectAll() {
        if (selectList.size() == books.size()) {
            selectList.clear();
        } else {
            for (BookShelfBean bean : books) {
                selectList.add(bean.getNoteUrl());
            }
        }
        notifyDataSetChanged();
        itemClickListener.onClick(null, 0);
    }


    @Override
    public HashSet<String> getSelected() {
        return selectList;
    }

    @Override
    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    @Override
    public void refreshBook(String noteUrl) {
        for (int i = 0; i < books.size(); i++) {
            if (noteUrl.equals(books.get(i).getNoteUrl())) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_item_bookshelf_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        BookShelfBean bookShelfBean = books.get(index);
        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
        holder.tvName.setText(bookInfoBean.getName());



        if (!activity.isFinishing()) {
            if (TextUtils.isEmpty(bookShelfBean.getCustomCoverPath())) {
                Glide.with(activity).load(bookInfoBean.getCoverUrl())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop().placeholder(R.drawable.img_cover_default)).into(holder.ivCover);


            } else if (bookShelfBean.getCustomCoverPath().startsWith("http")) {
                Glide.with(activity).load(bookShelfBean.getCustomCoverPath())
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop().placeholder(R.drawable.img_cover_default))
                        .into(holder.ivCover);
            } else {
                Glide.with(activity).load(new File(bookShelfBean.getCustomCoverPath()))
                        .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .centerCrop().placeholder(R.drawable.img_cover_default)).into(holder.ivCover);
            }
        }




        holder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        holder.tvName.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(view, index);
            }
        });
        if (!"2".equals(bookshelfPx)) {
            holder.flContent.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, index);
                }
                return true;
            });
        } else if (bookShelfBean.getSerialNumber() != index) {
            bookShelfBean.setSerialNumber(index);
            new Thread() {
                public void run() {
                    DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
                }
            }.start();
        }

        Set<String> options  = MApplication.getInstance().getConfigPreferences().getStringSet(activity.getString(R.string.pk_bookshelf_show), null);



        if(options!=null && options.contains("0")) {//显示

            if (bookShelfBean.isLoading()) {
                holder.bvUnread.setVisibility(View.INVISIBLE);
                holder.rotateLoading.setVisibility(View.VISIBLE);
                holder.rotateLoading.start();
            } else {
                holder.bvUnread.setBadgeCount(bookShelfBean.getUnreadChapterNum());
                holder.bvUnread.setHighlight(bookShelfBean.getHasUpdate());
                holder.rotateLoading.setVisibility(View.INVISIBLE);
                holder.rotateLoading.stop();
            }
        }else{
            holder.bvUnread.setVisibility(View.INVISIBLE);
            holder.rotateLoading.setVisibility(View.INVISIBLE);
        }

        if(options!=null && options.contains("1")) {//显示

            holder.mpbDurprogress.setVisibility(View.VISIBLE);
            holder.mpbDurprogress.setMaxProgress(books.get(index).getChapterListSize());
            float speed = books.get(index).getChapterListSize() * 1.0f / 100;

            holder.mpbDurprogress.setSpeed(speed <= 0 ? 1 : speed);
            holder.mpbDurprogress.setDurProgressWithAnim(books.get(index).getDurChapter());
        }else{
            holder.mpbDurprogress.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void setItemClickListener(OnItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public synchronized void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        if (null != newDataS && newDataS.size() > 0) {
            BookshelfHelp.order(newDataS, bookshelfPx);
            books = newDataS;
        } else {
            books.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public List<BookShelfBean> getBooks() {
        return books;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        TextView tvName;
        BadgeView bvUnread;
        RotateLoading rotateLoading;
        MHorProgressBar mpbDurprogress;
        TextView tvNameOnPic;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            bvUnread = itemView.findViewById(R.id.bv_unread);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            mpbDurprogress = (MHorProgressBar) itemView.findViewById(R.id.mpb_durprogress);
            tvNameOnPic = itemView.findViewById(R.id.tv_name_on_pic);


        }
    }
}
