package com.nainfox.bluetoothmodule.bluetooth;

import com.nainfox.bluetoothmodule.R;

import java.io.Serializable;

/**
 * Created by yjk on 2017. 12. 4..
 */

public class Config implements Serializable{

    // title
    private String titleBarColor = "#FFFFFFFF";
    private String titleTextColor = "#FF000000";
    private int titleTextSize = 30;

    // 리스트 아이템
    private String deviceItemTextColor = "#FF000000";
    private int deviceItemTextSize = 30;

    private String cancelButtonBackground = "#FFFFFFFF";
    private String cancelButtonTextColor = "#FF000000";
    private int cancleButtonTextSize = 30;
    private int cancelButtonImage = R.drawable.cancel_btn;


    public String getTitleBarColor() {
        return titleBarColor;
    }

    public void setTitleBarColor(String titleBarColor) {
        this.titleBarColor = titleBarColor;
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

    public String getDeviceItemTextColor() {
        return deviceItemTextColor;
    }

    public void setDeviceItemTextColor(String deviceItemTextColor) {
        this.deviceItemTextColor = deviceItemTextColor;
    }

    public int getDeviceItemTextSize() {
        return deviceItemTextSize;
    }

    public void setDeviceItemTextSize(int deviceItemTextSize) {
        this.deviceItemTextSize = deviceItemTextSize;
    }

    public String getCancelButtonBackground() {
        return cancelButtonBackground;
    }

    public void setCancelButtonBackground(String cancelButtonBackground) {
        this.cancelButtonBackground = cancelButtonBackground;
    }

    public String getCancelButtonTextColor() {
        return cancelButtonTextColor;
    }

    public void setCancelButtonTextColor(String cancelButtonTextColor) {
        this.cancelButtonTextColor = cancelButtonTextColor;
    }

    public int getCancleButtonTextSize() {
        return cancleButtonTextSize;
    }

    public void setCancleButtonTextSize(int cancleButtonTextSize) {
        this.cancleButtonTextSize = cancleButtonTextSize;
    }

    public int getCancelButtonImage() {
        return cancelButtonImage;
    }

    public void setCancelButtonImage(int cancelButtonImage) {
        this.cancelButtonImage = cancelButtonImage;
    }
}
