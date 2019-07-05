package com.kunfei.bookshelf.widget.my_page;

import android.graphics.Point;

/**
 * 可见字符数据封装
 */
public class TxtChar {

    public char chardata ;//字符数据

    public Boolean Selected =false;//当前字符是否被选中

    //记录文字的左上右上左下右下四个点坐标
    public Point TopLeftPosition = null;//左上
    public Point TopRightPosition = null;//右上
    public Point BottomLeftPosition = null;//左下
    public Point BottomRightPosition = null;//右下

    public float charWidth = 0;//字符宽度
    public int Index = 0;//当前字符位置


    public char getChardata() {
        return chardata;
    }

    public void setChardata(char chardata) {
        this.chardata = chardata;
    }

    public Boolean getSelected() {
        return Selected;
    }

    public void setSelected(Boolean selected) {
        Selected = selected;
    }

    public Point getTopLeftPosition() {
        return TopLeftPosition;
    }

    public void setTopLeftPosition(Point topLeftPosition) {
        TopLeftPosition = topLeftPosition;
    }

    public Point getTopRightPosition() {
        return TopRightPosition;
    }

    public void setTopRightPosition(Point topRightPosition) {
        TopRightPosition = topRightPosition;
    }

    public Point getBottomLeftPosition() {
        return BottomLeftPosition;
    }

    public void setBottomLeftPosition(Point bottomLeftPosition) {
        BottomLeftPosition = bottomLeftPosition;
    }

    public Point getBottomRightPosition() {
        return BottomRightPosition;
    }

    public void setBottomRightPosition(Point bottomRightPosition) {
        BottomRightPosition = bottomRightPosition;
    }

    public float getCharWidth() {
        return charWidth;
    }

    public void setCharWidth(float charWidth) {
        this.charWidth = charWidth;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    @Override
    public String toString() {
        return "ShowChar [chardata=" + chardata + ", Selected=" + Selected + ", TopLeftPosition=" + TopLeftPosition
                + ", TopRightPosition=" + TopRightPosition + ", BottomLeftPosition=" + BottomLeftPosition
                + ", BottomRightPosition=" + BottomRightPosition + ", charWidth=" + charWidth + ", Index=" + Index
                + "]";
    }
}
