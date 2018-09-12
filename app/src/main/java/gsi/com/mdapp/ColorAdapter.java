package gsi.com.mdapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;

import java.util.Arrays;

public class ColorAdapter {

    private static final float DARK_FACTOR = 0.25f;
    private static final float BRIGHT_FACTOR = 0.6f;

    public static Drawable getSelectorDrawable(@ColorInt int color, float radius) {
        @ColorInt int defaultColor = color;
        @ColorInt int pressedColor = setDarkerShade(color);

        ShapeDrawable activeDrawable = new ShapeDrawable();
        ShapeDrawable inactiveDrawable = new ShapeDrawable();

        // The corners are ordered top-left, top-right, bottom-right, bottom-left. // For each corner, the array contains 2 values, [X_radius, Y_radius]

        float[] radii = new float[8];
        Arrays.fill(radii, radius);
        inactiveDrawable.setShape(new RoundRectShape(radii, null, null));
        inactiveDrawable.getPaint().setColor(defaultColor);

        activeDrawable.setShape(new RoundRectShape(radii, null, null));
        activeDrawable.getPaint().setColor(pressedColor);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {-android.R.attr.state_enabled}, inactiveDrawable);
        states.addState(new int[] {android.R.attr.state_pressed}, activeDrawable);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return inactiveDrawable;
        } else {
            return states;
        }
    }

    public static Drawable getAdaptiveRippleDrawable(@ColorInt int color, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList pressedColor = ColorStateList.valueOf(setDarkerShade(color));
            Drawable rippleColor = getRippleColor(color, radius);
            return new RippleDrawable(
                    pressedColor,
                    null,
                    rippleColor
            );
        } else {
            return ColorAdapter.getSelectorDrawable(color, radius);
        }
    }

    public static int setDarkerShade(int color) {
        return ColorUtils.blendARGB(color, Color.BLACK, DARK_FACTOR);
    }

    public static int setBrighterShade(int color) {
        return ColorUtils.blendARGB(color, Color.WHITE, BRIGHT_FACTOR);
    }

    @NonNull
    private static Drawable getRippleColor(@ColorInt int color, float radius) {
        float[] radii = new float[8];
        Arrays.fill(radii, radius);
        RoundRectShape rrs = new RoundRectShape(radii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(rrs);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    @NonNull
    public static Drawable getSelectableDrawableFor(@ColorInt int color, float radius) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(
                    new int[]{android.R.attr.state_pressed},
                    new ColorDrawable(lightenOrDarken(color, 0.20D))
            );
            stateListDrawable.addState(
                    new int[]{android.R.attr.state_focused},
                    new ColorDrawable(lightenOrDarken(color, 0.40D))
            );
            stateListDrawable.addState(
                    new int[]{},
                    new ColorDrawable(color)
            );
            return stateListDrawable;
        } else {
            ColorStateList pressedColor = ColorStateList.valueOf(lightenOrDarken(color, 0.2D));
            ColorDrawable defaultColor = new ColorDrawable(color);
            Drawable rippleColor = getRippleColor(color, radius);
            return new RippleDrawable(
                    pressedColor,
                    defaultColor,
                    rippleColor
            );
        }
    }

    private static int lightenOrDarken(@ColorInt int color, double fraction) {
        if (canLighten(color, fraction)) {
            return lighten(color, fraction);
        } else {
            return darken(color, fraction);
        }
    }

    private static int lighten(@ColorInt int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = lightenColor(red, fraction);
        green = lightenColor(green, fraction);
        blue = lightenColor(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    private static int darken(@ColorInt int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static boolean canLighten(@ColorInt int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return canLightenComponent(red, fraction)
                && canLightenComponent(green, fraction)
                && canLightenComponent(blue, fraction);
    }

    private static boolean canLightenComponent(int colorComponent, double fraction) {
        int red = Color.red(colorComponent);
        int green = Color.green(colorComponent);
        int blue = Color.blue(colorComponent);
        return red + (red * fraction) < 255
                && green + (green * fraction) < 255
                && blue + (blue * fraction) < 255;
    }

    private static int darkenColor(@ColorInt int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(@ColorInt int color, double fraction) {
        return (int) Math.min(color + (color * fraction), 255);
    }
}
