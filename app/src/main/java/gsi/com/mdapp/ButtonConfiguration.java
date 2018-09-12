package gsi.com.mdapp;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class ButtonConfiguration {

    @ColorInt
    private int mColor;
    private String mTitle;
    private String mColorHex;

    public ButtonConfiguration(String color, String title) {
        this.mColor = parseColor(color);
        this.mColorHex = color;
        this.mTitle = title;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getColorHex() {
        return mColorHex;
    }

    public void setColorHex(String colorHex) {
        this.mColorHex = colorHex;
    }

    private int parseColor(String color) {
        return Color.parseColor(color);
    }
}
