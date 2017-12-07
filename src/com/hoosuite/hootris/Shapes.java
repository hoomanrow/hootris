
package com.hoosuite.hootris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

public class Shapes {


/// creates a simple rectangle and returns it as a bitmap
static public Bitmap rect(int w, int h, int c) {
	
	Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
	Canvas canvas = new Canvas(b);
	Paint paint = new Paint();
	
	paint.setColor(c);
	canvas.drawRect(new Rect(0, 0, w, h), paint);
	
	return b;
}


/**
 * 
 * creates a shiny rectangle
 * 
 * @param w width of rectangle
 * @param h height of rectangle
 * @param cDark darkest colour in rectangle
 * @param cLight lightest colour in rectangle
 * @param radius radius of rectangle
 * @param whether or not to add shine
 * @return bitmap of rectangle
 */
static public Bitmap shinyRoundRect(int w, int h, int cDark, int cLight, int radius, boolean addShine) {
	
	RectF rect = new RectF(0, 0, w, h);
	GradientDrawable gradient = new GradientDrawable(Orientation.TOP_BOTTOM, new int [] { cDark, cLight } );
	int bw = w - (int)radius/2;
	Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	Canvas canvas = new Canvas(b);
	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	// draw dark border
	paint.setColor(cDark);
	canvas.drawRoundRect(rect, radius, radius, paint);
	// draw top/down gradient
	gradient.setBounds(new Rect(2, 2, w-2, h-2));
	gradient.setCornerRadius(radius);
	paint.setColor(cLight);
	gradient.draw(canvas);
	// draw shiny white bubble
	if (addShine) {
		gradient = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] { 0xffffffff, 0x00ffffff } );
		gradient.setCornerRadius(radius * 0.4f);	
		gradient.setBounds(new Rect(w-bw, 3, bw, (int)(h*0.4f)));
		gradient.draw(canvas);
	}
	
	return b;
}
	
	
}
