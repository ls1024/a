package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class BookExtendBean implements Parcelable,Cloneable{

    @Id
    private String noteUrl;
    private String col1;
    private String col2;
    private String col3;
    private String col4;
    private String col5;
    private String col6;
    private String col7;
    private String col8;
    private String col9;
    private String col10;

    


    public static final Creator<BookExtendBean> CREATOR = new Creator<BookExtendBean>() {
        @Override
        public BookExtendBean createFromParcel(Parcel in) {
            return new BookExtendBean(in);
        }

        @Override
        public BookExtendBean[] newArray(int size) {
            return new BookExtendBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(noteUrl);
        parcel.writeString(col1);
        parcel.writeString(col2);
        parcel.writeString(col2);
        parcel.writeString(col3);
        parcel.writeString(col4);
        parcel.writeString(col5);
        parcel.writeString(col6);
        parcel.writeString(col7);
        parcel.writeString(col8);
        parcel.writeString(col9);
        parcel.writeString(col10);

    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BookExtendBean bookHiddenBean = (BookExtendBean) super.clone();
        bookHiddenBean.noteUrl = noteUrl;
        bookHiddenBean.col1 = col1;
        bookHiddenBean.col2 = col2;
        bookHiddenBean.col3 = col3;
        bookHiddenBean.col4 = col4;
        bookHiddenBean.col5 = col5;
        bookHiddenBean.col6 = col6;
        bookHiddenBean.col7 = col7;
        bookHiddenBean.col8 = col8;
        bookHiddenBean.col9 = col9;
        bookHiddenBean.col10 = col10;


        return bookHiddenBean;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getCol1() {
        return this.col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return this.col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getCol3() {
        return this.col3;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public String getCol4() {
        return this.col4;
    }

    public void setCol4(String col4) {
        this.col4 = col4;
    }

    public String getCol5() {
        return this.col5;
    }

    public void setCol5(String col5) {
        this.col5 = col5;
    }

    public String getCol6() {
        return this.col6;
    }

    public void setCol6(String col6) {
        this.col6 = col6;
    }

    public String getCol7() {
        return this.col7;
    }

    public void setCol7(String col7) {
        this.col7 = col7;
    }

    public String getCol8() {
        return this.col8;
    }

    public void setCol8(String col8) {
        this.col8 = col8;
    }

    public String getCol9() {
        return this.col9;
    }

    public void setCol9(String col9) {
        this.col9 = col9;
    }

    public String getCol10() {
        return this.col10;
    }

    public void setCol10(String col10) {
        this.col10 = col10;
    }

    protected BookExtendBean(Parcel in) {
        noteUrl = in.readString();
        col1 = in.readString();
        col2 = in.readString();
        col3 = in.readString();
        col4 = in.readString();
        col5 = in.readString();
        col6 = in.readString();
        col7 = in.readString();
        col8 = in.readString();
        col9 = in.readString();
        col10 = in.readString();
    }

    @Generated(hash = 41074093)
    public BookExtendBean(String noteUrl, String col1, String col2, String col3, String col4,
            String col5, String col6, String col7, String col8, String col9, String col10) {
        this.noteUrl = noteUrl;
        this.col1 = col1;
        this.col2 = col2;
        this.col3 = col3;
        this.col4 = col4;
        this.col5 = col5;
        this.col6 = col6;
        this.col7 = col7;
        this.col8 = col8;
        this.col9 = col9;
        this.col10 = col10;
    }

    @Generated(hash = 1121560882)
    public BookExtendBean() {
    }



}
