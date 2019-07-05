//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;import android.text.Html;
import android.text.TextUtils;
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
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.bean.SearcheBookHitBean;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.utils.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class MySearchBookAdapter extends RecyclerView.Adapter {
    private WeakReference<Activity> activityRef;
    private List<SearchBookBean> searchBooks;

    private String searchKey ="";
    private String searchAuthor ="";
    private Boolean noPicSearch = false;

    public String getSearchAuthor() {
        return searchAuthor;
    }

    public void setSearchAuthor(String searchAuthor) {
        this.searchAuthor = searchAuthor;
    }

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }


    private OnItemClickListener itemClickListener;

    public void setNoPicSearch(Boolean noPicSearch) {
        this.noPicSearch = noPicSearch;
    }

    public MySearchBookAdapter(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
        searchBooks = new ArrayList<>();

        noPicSearch = ReadBookControl.getInstance().getNoPicSearch();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_web_search_item, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

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
                        .into(((Viewholder) holder).cover);
            }
        }

        String title = searchBooks.get(position).getName().trim();

        title = title.replace(searchKey,"<font color='#1971b9'>"+searchKey+"</font>");
        ((Viewholder) holder).title.setText(Html.fromHtml(title));


        String author = searchBooks.get(position).getAuthor().trim();
        author = author.replace(searchKey,"<font color='#1971b9'>"+searchKey+"</font>");

        ((Viewholder) holder).author.setText(Html.fromHtml(author));
        if(!isEmpty(searchBooks.get(position).getIntroduce())) {
            ((Viewholder) holder).description.setText(searchBooks.get(position).getIntroduce().trim());
        }
        //String aa = ((Viewholder) holder).sources.getText().toString();

        StringBuilder strSrc = new StringBuilder() ;
        for(int i = 0; i< searchBooks.get(position).getHitList().size();i++){
            strSrc.append(" " + searchBooks.get(position).getHitList().get(i).getBookSourceName());
        }

        ((Viewholder) holder).sources.setText("书源(" + searchBooks.get(position).getHitList().size() + "):" + strSrc.substring(1));

        /*
        int originNum =  searchBooks.get(position).getOriginNum();
        if(originNum==1) {
            ((Viewholder) holder).sources.setText("书源(" + searchBooks.get(position).getHitList().size() + "):" + searchBooks.get(position).getOrigin());
        }else {
            ((Viewholder) holder).sources.setText("书源(" + searchBooks.get(position).getHitList().size() + "):" + searchBooks.get(position).getOriginStr());
        }*/


        //列表事件
        ((Viewholder) holder).flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(((Viewholder) holder).cover, position, searchBooks.get(position));
        });
    }


    @Override
    public int getItemCount() {
        return searchBooks.size();
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        this.searchKey = keyWord;
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            saveData(newDataS);//newDataS 为搜索结果
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();//要加入界面列表的
            if (copyDataS.size() == 0) {//默认情况，在原有数据为空情况下，不做判断加入搜索结果
                //copyDataS.addAll(newDataS);
                for (SearchBookBean newData : newDataS) {
                    //只搜索作者
                    if(!StringUtils.isTrimEmpty(searchAuthor)){
                        if ( newData.getAuthor().contains(keyWord)){

                            copyDataS.add(newData);
                        }

                    }else{
                        if (newData.getName().contains(keyWord) || newData.getAuthor().contains(keyWord)){

                            copyDataS.add(newData);
                        }
                    }
                }



                //sortSearchBooks(copyDataS, keyWord);
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {//遍历搜索结果
                    Boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {//遍历已有数据
                        SearchBookBean searchBook = copyDataS.get(i);
                        //如果书名作者一样则认为是一本书，而源不同而已
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;

                            //对于多个源的书籍的处理 begin
                            List<SearcheBookHitBean> tempHitList = temp.getHitList();
                            searchBook.getHitList().addAll(tempHitList);
                            //对于多个源的书籍的处理 end

                            //desc 处理，找到第一个有介绍的为止
                            if(isEmpty(searchBook.getIntroduce()) && !isEmpty(temp.getIntroduce())){
                                searchBook.setIntroduce(temp.getIntroduce());
                            }

                            searchBook.addOriginUrl(temp.getTag());

                            notifyItemChanged(i);

                            break;
                        }
                    }

                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {//遍历准备添加到结果中的搜索结果

                    //只搜索作者
                    if(!StringUtils.isTrimEmpty(searchAuthor)){

                        if (TextUtils.equals(keyWord, temp.getAuthor())) {//作者名字等于关键字
                            for (int i = 0; i < copyDataS.size(); i++) {
                                SearchBookBean searchBook = copyDataS.get(i);
                                if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                    copyDataS.add(i, temp);
                                    break;
                                }
                            }
                        }
                    }else{

                        if (TextUtils.equals(keyWord, temp.getName())) {//书名等于关键字
                            for (int i = 0; i < copyDataS.size(); i++) {
                                SearchBookBean searchBook = copyDataS.get(i);
                                if (!TextUtils.equals(keyWord, searchBook.getName())) {//书名在结果中不存在则添加
                                    copyDataS.add(i, temp);
                                    break;
                                }
                            }
                        } else if (TextUtils.equals(keyWord, temp.getAuthor())) {//作者名字等于关键字
                            for (int i = 0; i < copyDataS.size(); i++) {
                                SearchBookBean searchBook = copyDataS.get(i);
                                if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                    copyDataS.add(i, temp);
                                    break;
                                }
                            }
                        } else if (temp.getName().contains(keyWord) || temp.getAuthor().contains(keyWord)) {//如果作者名字或者书名包括关键字
                            for (int i = 0; i < copyDataS.size(); i++) {
                                SearchBookBean searchBook = copyDataS.get(i);
                                if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                    copyDataS.add(i, temp);
                                    break;
                                }
                            }
                        } else {
                            //copyDataS.add(temp);
                        }

                    }


                }
            }
            searchBooks = copyDataS;
            Activity activity = activityRef.get();
            if(activity != null) {
                activity.runOnUiThread(this::notifyDataSetChanged);
            }
        }
    }

    public void clearAll() {
        int bookSize = searchBooks.size();
        if (bookSize > 0) {
            try {
                Glide.with(activityRef.get()).onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchBooks.clear();
            notifyItemRangeRemoved(0, bookSize);
        }
    }

    private void saveData(List<SearchBookBean> data) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(data));
    }

    private void sortSearchBooks(List<SearchBookBean> searchBookBeans, String keyWord) {
        try {
            Collections.sort(searchBookBeans, (o1, o2) -> {
                if (TextUtils.equals(keyWord, o1.getName())
                        || TextUtils.equals(keyWord, o1.getAuthor())) {
                    return -1;
                } else if (TextUtils.equals(keyWord, o2.getName())
                        || TextUtils.equals(keyWord, o2.getAuthor())) {
                    return 1;
                } else if (o1.getName().contains(keyWord) || o1.getAuthor().contains(keyWord)) {
                    return -1;
                } else if (o2.getName().contains(keyWord) || o2.getAuthor().contains(keyWord)) {
                    return 1;
                } else {
                    return 0;
                }
            });
        } catch (Exception ignored) {
        }
    }


    public void replaceAll(List<SearchBookBean> newData) {
        try {
            Glide.with(activityRef.get()).onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public SearchBookBean getItemData(int pos) {
        return searchBooks.get(pos);
    }

    class Viewholder extends RecyclerView.ViewHolder {
        LinearLayout flContent;
        ImageView cover;
        TextView title;
        TextView author;
        TextView description;
        TextView sources;
        //TextView tvKind;
        // TextView tvLastest;
        // TextView tvAddShelf;
        // TextView tvOrigin;

        public Viewholder(View itemView) {
            super(itemView);
            flContent = (LinearLayout) itemView.findViewById(R.id.fl_content);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            title = (TextView) itemView.findViewById(R.id.title);
            author = (TextView) itemView.findViewById(R.id.author);
            description = (TextView) itemView.findViewById(R.id.description);
            sources = (TextView) itemView.findViewById(R.id.sources);
            //tvLastest = (TextView) itemView.findViewById(R.id.tv_lastest);
            //tvAddShelf = (TextView) itemView.findViewById(R.id.tv_addshelf);
            //tvKind = (TextView) itemView.findViewById(R.id.tv_kind);
            //tvOrigin = (TextView) itemView.findViewById(R.id.tv_origin);
        }
    }


    public void clear(){

        searchBooks.clear();
        notifyDataSetChanged();
    }
}