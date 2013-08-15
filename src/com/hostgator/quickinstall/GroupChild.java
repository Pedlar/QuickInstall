package com.hostgator.quickinstall;

import android.graphics.Bitmap;

public class GroupChild {

    private String name;
    private String tag;
    private String iconUrl;
    private Bitmap iconBitmap;
    
    public String getName() {
        return name;
    }
    public void setName(String Name) {
        this.name = Name;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String Tag) {
        this.tag = Tag;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String x) {
        iconUrl = x;
    }
    public Bitmap getIcon() {
        return iconBitmap;
    }
    public void setIcon(Bitmap icon) {
        iconBitmap = icon;
    }
}