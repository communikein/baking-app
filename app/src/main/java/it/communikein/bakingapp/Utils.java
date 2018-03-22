package it.communikein.bakingapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

public class Utils {

    public static Drawable getDrawableColored(int drawableResource, int colorResource, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResource);
        int color = ContextCompat.getColor(context, colorResource);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);

        drawable.setColorFilter(colorFilter);
        return drawable;
    }

}
