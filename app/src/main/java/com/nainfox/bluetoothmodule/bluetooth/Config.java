package com.nainfox.bluetoothmodule.bluetooth;

import com.nainfox.bluetoothmodule.R;

import java.io.Serializable;

/**
 * Created by yjk on 2017. 12. 4..
 */

public class Config implements Serializable{
    // Title Bar
    private boolean titleBarBgGradient = true;
    private String titleBarBackgroundColor = "#FF6471"; // 타이틀바 배경
    private int titleBarBackroundRes = R.drawable.title_bar_background;       // 타이틀바 배경 ( drawable )
    private String titleText = "장치연결";
    private String titleTextColor = "#FFFFFF";
    private int titleTextSize = 18;


    // List View Item
    private String itemTextColor = "#FF000000";
    private int itemTextSize = 16;
    private String listViewBackgroundColor = "#22000000";

    // Bottom Cancel Button
    private String bottomButtonText = "취소";
    private String bottomButtonTextColor = "#FF6471";
    private int bottomButtonTextSize = 18;
    private String bottomButtonBackgroundColor = "#FFFFFFFF";


    public String getTitleBarBackgroundColor() {
        return titleBarBackgroundColor;
    }

    public void setTitleBarBackgroundColor(String titleBarBackgroundColor) {
        titleBarBgGradient = false;
        this.titleBarBackgroundColor = titleBarBackgroundColor;
    }

    public boolean isTitleBarBgGradient() {
        return titleBarBgGradient;
    }

    public int getTitleBarBackroundRes() {
        return titleBarBackroundRes;
    }

    public void setTitleBarBackroundRes(int titleBarBackroundRes) {
        titleBarBgGradient = true;
        this.titleBarBackroundRes = titleBarBackroundRes;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(String titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public String getItemTextColor() {
        return itemTextColor;
    }

    public void setItemTextColor(String itemTextColor) {
        this.itemTextColor = itemTextColor;
    }

    public int getItemTextSize() {
        return itemTextSize;
    }

    public void setItemTextSize(int itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    public String getListViewBackgroundColor() {
        return listViewBackgroundColor;
    }

    public void setListViewBackgroundColor(String listViewBackgroundColor) {
        this.listViewBackgroundColor = listViewBackgroundColor;
    }

    public String getBottomButtonText() {
        return bottomButtonText;
    }

    public void setBottomButtonText(String bottomButtonText) {
        this.bottomButtonText = bottomButtonText;
    }

    public String getBottomButtonTextColor() {
        return bottomButtonTextColor;
    }

    public void setBottomButtonTextColor(String bottomButtonTextColor) {
        this.bottomButtonTextColor = bottomButtonTextColor;
    }

    public int getBottomButtonTextSize() {
        return bottomButtonTextSize;
    }

    public void setBottomButtonTextSize(int bottomButtonTextSize) {
        this.bottomButtonTextSize = bottomButtonTextSize;
    }

    public String getBottomButtonBackgroundColor() {
        return bottomButtonBackgroundColor;
    }

    public void setBottomButtonBackgroundColor(String bottomButtonBackgroundColor) {
        this.bottomButtonBackgroundColor = bottomButtonBackgroundColor;
    }
}
