
package com.hoosuite.hootris;


import ui.HooButton;
import ui.HooClick;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * 
 * @author Hooman Rowshanbin
 * 
 * Landing page for Hootris  
 *
 */


public class Landing extends View {

	
private static final String TAG = Landing.class.getSimpleName(); // used for debugging


private Main main; // pointer to main


private HooButton buttonResume, buttonControls, buttonPlay, buttonQuit;
private Boolean initialized = false; // whether or not buttons have been initialized

private Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
private Paint authorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
private Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG); // paint for highscore
private HootrisBorder border;


public Landing(Main main) {
	
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
	if ((Main.game != null && !Main.game.gameOver) || main.checkResumedGame()) { HooButton.buttons.add(buttonResume); }
	HooButton.buttons.add(buttonPlay);
	HooButton.buttons.add(buttonControls);
	HooButton.buttons.add(buttonQuit);
}


public void init() {
	
	Log.d(TAG, "init");
	buttonResume = new HooButton("Resume Game", 0.2f, 0.5f, 0.6f, 0.08f, resumeClick);
	buttonPlay = new HooButton("New Game", 0.2f, 0.6f, 0.6f, 0.08f, startClick);
	buttonControls = new HooButton("Controls", 0.2f, 0.7f, 0.6f, 0.08f, instructionsClick);
	buttonQuit = new HooButton("Quit", 0.2f, 0.8f, 0.6f, 0.08f, quitClick);
	titlePaint.setTextSize(Main.h * 0.1f);
	titlePaint.setColor(Color.WHITE);
	titlePaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
	authorPaint.setTextSize(Main.h * 0.03f);
	authorPaint.setColor(Color.WHITE);
	scorePaint.setTextSize(Main.h * 0.05f);
	scorePaint.setColor(Color.WHITE);
	border = new HootrisBorder(main);
	initialized = true;
}


@Override
public void onDraw(Canvas canvas) {
	
	String s;
	
	Log.d(TAG, "onDraw initialized = " + String.valueOf(initialized));
	if (initialized) { 
		s = "HOOTRIS";
		canvas.drawText(s, (Main.w-titlePaint.measureText(s))/2, Main.h * 0.2f, titlePaint);
		s = "By: Hooman Rowshanbin";
		canvas.drawText(s, (Main.w-authorPaint.measureText(s))/2, Main.h * 0.25f, authorPaint);
		s = "hoomanr.com";
		canvas.drawText(s, (Main.w-authorPaint.measureText(s))/2, Main.h * 0.28f, authorPaint);
		s = "High Score: " + String.valueOf(Main.highScore);
		canvas.drawText(s, (Main.w-scorePaint.measureText(s))/2, Main.h * 0.4f, scorePaint);
		HooButton.drawAll(canvas);
		border.draw(canvas);
	}
}


@Override
public boolean onTouchEvent(MotionEvent e) {
	
	HooButton.touchEvent(e);
	return true;
}


/*** buttons ***/
private HooClick resumeClick = new HooClick() { public void onClick() {
	
	if (Main.game != null) { main.setContentView(Main.game); }
	else {
		Log.d(TAG, "Trying to load");
		Main.game = new Game(main);
		Main.game.load();
		main.setContentView(Main.game);
	}
} };


private HooClick startClick = new HooClick() { public void onClick() {
	
	Main.game = new Game(main);
	main.setContentView(Main.game);
} };


private HooClick instructionsClick = new HooClick() { public void onClick() {
	
	Log.d(TAG, "Instructions click!");
	main.setContentView(Main.instructions);
} };


private HooClick quitClick = new HooClick() { public void onClick() {

	if (Main.game != null) { Main.game.save(); }
	System.exit(0);
} };

	
}
