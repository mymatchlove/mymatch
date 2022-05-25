package com.androidbuts.multispinnerfilter;

/**
 * Created by TCIG_PC_54 on 8/22/2017.
 */

public class SectionDropDownModel {
    boolean isHeader;
    int categoryId;
    int drawable;
    String name;
    String id;

    boolean isSelected;

    public SectionDropDownModel(boolean isHeader, int drawable, String name, String id) {
        this.isHeader = isHeader;
//        this.categoryId = categoryId;
        this.drawable = drawable;
        this.name = name;
        this.id = id;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

//    public int getCategoryId() {
//        return categoryId;
//    }
//
//    public void setCategoryId(int categoryId) {
//        this.categoryId = categoryId;
//    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
