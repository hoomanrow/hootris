
package ui;


import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.hoosuite.hootris.Shapes;


public class HooButton {
	

final static public int OUT = 0; // normal state
final static public int OVER = 1; // over state
	
static public int screenW; // screen width
static public int screenH; // screen height
static public View screenView; // pointer to view
static public List<HooButton> buttons = new ArrayList<HooButton>(); // array of all active buttons
static public int lastDown = -1; // button user last mouse downed on

// button size	
public int x;
public int y;
public int w;
public int h;
// button style
public int colorDark = 0xff1a2953;
public int colorLight = 0xff95d5df;
public int colorDarkOver = 0xff7b0100;
public int colorLightOver = 0xffff3601;
// caption style
public int colorCaption = 0xFF000000;
public String caption;
public Paint paintCaption = new Paint(Paint.ANTI_ALIAS_FLAG);
// click function/class
public HooClick hooClick;
// current state
public int state = OUT;
// normal and over image
public Bitmap bitmapOut = null;
public Bitmap bitmapOver = null;


/// used for creating a button with absolute positioning
public HooButton(String caption, int x, int y, int w, int h, HooClick hooClick) {
	
	screenW = screenView.getWidth();
	screenH = screenView.getHeight();
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
	this.caption = caption;
	this.hooClick = hooClick;
	buttons.add(this);
}


/// used for creating a button with fraction positioning (e.g. x=0.2, w=0.6 means place button 20% to the left with 60% width)
public HooButton(String caption, float x, float y, float w, float h, HooClick hooClick) {
	
	screenW = screenView.getWidth();
	screenH = screenView.getHeight();
	this.x = (int)(x * screenW);
	this.y = (int)(y * screenH);
	this.w = (int)(w * screenW);
	this.h = (int)(h * screenH);
	this.caption = caption;
	this.hooClick = hooClick;
	buttons.add(this);
}


/**
 * draws all the buttons
 * @param canvas canvas to draw on
 */
static public void drawAll(Canvas canvas) {
	
	int i;
	for (i = 0; i < buttons.size(); i++) {
		buttons.get(i).draw(canvas);
	}
}


/// draws a button
public void draw(Canvas canvas) {

	// make sure buttons have been rendered
	if (bitmapOut == null || bitmapOver == null) { render(); }
	// draw proper bitmap
	if (state == OVER) { canvas.drawBitmap(bitmapOver, x, y, null); }
	else { canvas.drawBitmap(bitmapOut, x, y, null); }
}


/// renders the button
public void render() {
	
	bitmapOut = makeButton(colorDark, colorLight);
	bitmapOver = makeButton(colorDarkOver, colorLightOver);
}


/// checks if user clicked on any button
static public void touchEvent(MotionEvent e) {
	
	int i, selected = -1;
	HooButton b = null;
	
	// see which button event occured above
	for (i = 0; i < buttons.size() && selected < 0; i++) {
		b = buttons.get(i);
		if (e.getX() >= b.x && e.getX() < b.x+b.w && e.getY() >= b.y && e.getY() < b.y+b.h) { selected = i; }
	}
	// cancel any over states if not a button
	if (selected < 0) { 
		if (lastDown >= 0) {
			lastDown = -1;
			for (i = 0; i < buttons.size(); i++) { buttons.get(i).state = OUT; }
			screenView.invalidate();
		}
	}
	// check if it is a mouse down or a mouse up, and perform click if that's what it was
	else if (selected >= 0) {
		if (e.getAction() == MotionEvent.ACTION_UP && selected == lastDown) { 
			b.hooClick.onClick();
			b.state = OUT;
			screenView.invalidate();
		}
		else if (e.getAction() == MotionEvent.ACTION_DOWN) { 
			lastDown = selected; 
			b.state = OVER;
			screenView.invalidate();
		}
	}
}


/// erases all active buttons
static public void clear() {
	
	buttons = new ArrayList<HooButton>();
}


/// given colours, renders button and returns it as a bitmap
private Bitmap makeButton(int cDark, int cLight) {
	
	Bitmap b = Shapes.shinyRoundRect(w, h, cDark, cLight, h/2, true);
	Canvas canvas = new Canvas(b);
	
	// draw text over button
	paintCaption.setColor(colorCaption);
	paintCaption.setTextSize(h*0.6f);
	canvas.drawText(caption, (w-paintCaption.measureText(caption))/2, -paintCaption.getFontMetricsInt().ascent+paintCaption.getFontMetrics().descent, paintCaption);
	
	return b;
}


}
