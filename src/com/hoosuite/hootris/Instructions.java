
package com.hoosuite.hootris;

import ui.HooButton;
import ui.HooClick;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

public class Instructions extends View {

	
private Main main;
private boolean initialized;

private HootrisBorder border;
private HooButton buttonEnable, buttonDisable, buttonExit;
private Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


	
public Instructions(Main main) {
	
	super(main);
	this.main = main;
	setFocusable(true);
}

/**
 * Called when this view should assign a size and position to all of its children. 
 */
@Override
protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

	super.onLayout(changed, left, top, right, bottom);
	HooButton.screenView = this;
	if (!initialized) { init(); }
	HooButton.clear();
	HooButton.buttons.add(buttonExit);
	HooButton.buttons.add(buttonEnable);
}


public void init() {
	
	buttonExit = new HooButton("Exit", 0.2f, 0.8f, 0.6f, 0.08f, okClick);
	buttonEnable = new HooButton("Enable Ghost", 0.2f, 0.7f, 0.6f, 0.08f, toggleClick);
	buttonDisable = new HooButton("Disable Ghost", 0.2f, 0.7f, 0.6f, 0.08f, toggleClick);
	titlePaint.setTextSize(Main.h * 0.05f);
	titlePaint.setColor(Color.WHITE);
	titlePaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
	textPaint.setTextSize(Main.h * 0.03f);
	textPaint.setColor(Color.WHITE);
	border = new HootrisBorder(main);
	initialized = true;	
}


@Override
public void onDraw(Canvas canvas) {
	
	String s;
	
	if (initialized) { 
		s = "CONTROLS";
		canvas.drawText(s, (Main.w-titlePaint.measureText(s))/2, Main.h * 0.2f, titlePaint);
		s = "Tap the game board to rotate.";
		canvas.drawText(s, (Main.w-textPaint.measureText(s))/2, Main.h * 0.3f, textPaint);
		s = "Drag to move.";
		canvas.drawText(s, (Main.w-textPaint.measureText(s))/2, Main.h * 0.35f, textPaint);
		s = "Tap the hold box to hold.";
		canvas.drawText(s, (Main.w-textPaint.measureText(s))/2, Main.h * 0.4f, textPaint);
		s = "Swipe down to drop.";
		canvas.drawText(s, (Main.w-textPaint.measureText(s))/2, Main.h * 0.45f, textPaint);
		if (Main.ghostControl) {
			s = "Touch ghost shape to drop.";
			canvas.drawText(s, (Main.w-textPaint.measureText(s))/2, Main.h * 0.5f, textPaint);
			buttonDisable.draw(canvas);
		}
		else {
			buttonEnable.draw(canvas);
		}
		buttonExit.draw(canvas);
		border.draw(canvas);
	}
}


@Override
public boolean onTouchEvent(MotionEvent e) {
	
	HooButton.touchEvent(e);
	return true;
}

private HooClick toggleClick = new HooClick() { public void onClick() {
	
	Main.ghostControl = !Main.ghostControl;
	invalidate();
} };

private HooClick okClick = new HooClick() { public void onClick() {
	
	main.setContentView(Main.landing);
} };


}
