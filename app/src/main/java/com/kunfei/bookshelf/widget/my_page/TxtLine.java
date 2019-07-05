package com.kunfei.bookshelf.widget.my_page;

import java.util.List;

public class TxtLine {
    public List<TxtChar> CharsData = null;

    public List<TxtChar> getCharsData() {
        return CharsData;
    }

    public void setCharsData(List<TxtChar> charsData) {
        CharsData = charsData;
    }

    @Override
    public String toString() {
        return "ShowLine [Linedata=" + getLineData() + "]";
    }

    public String getLineData(){
        String linedata = "";
        if(CharsData==null||CharsData.size()==0) return linedata;
        for(TxtChar c:CharsData){
            linedata = linedata+c.chardata;
        }
        return linedata;
    }
}
