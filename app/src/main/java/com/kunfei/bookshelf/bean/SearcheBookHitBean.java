package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

@Entity
public class SearcheBookHitBean implements Parcelable, Cloneable {

    private String bookSourceName;//书源名称
    private String tag;
    private String NoteUrl;//书籍页跳转链接

    private String name;
    private String author;

    public SearcheBookHitBean(){

    }

    @Transient
    public static final Creator<SearcheBookHitBean> CREATOR = new Creator<SearcheBookHitBean>() {
        @Override
        public SearcheBookHitBean createFromParcel(Parcel in) {
            return new SearcheBookHitBean(in);
        }

        @Override
        public SearcheBookHitBean[] newArray(int size) {
            return new SearcheBookHitBean[size];
        }
    };


    protected SearcheBookHitBean(Parcel in) {
        name = in.readString();
        tag = in.readString();

        author = in.readString();

        bookSourceName = in.readString();

        NoteUrl = in.readString();

    }


    @Generated(hash = 128862316)
    public SearcheBookHitBean(String bookSourceName, String tag, String NoteUrl, String name,
            String author) {
        this.bookSourceName = bookSourceName;
        this.tag = tag;
        this.NoteUrl = NoteUrl;
        this.name = name;
        this.author = author;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        SearcheBookHitBean searcheBookHitBean = (SearcheBookHitBean) super.clone();
        searcheBookHitBean.bookSourceName = bookSourceName;
        searcheBookHitBean.NoteUrl = NoteUrl;
        searcheBookHitBean.tag = tag;

        return searcheBookHitBean;
    }

    public String getBookSourceName() {
        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getNoteUrl() {
        return NoteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        NoteUrl = noteUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(bookSourceName);
        parcel.writeString(tag);
        parcel.writeString(NoteUrl);
        parcel.writeString(name);
        parcel.writeString(author);
    }
}
