

package com.hoosuite.hootris;


import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class Main extends Activity {
   

private static final String TAG = Main.class.getSimpleName(); // used for debugging
	

// globals
static public Main main;

// different pages
static public Landing landing;
static public Instructions instructions;
static public Game game;

// screen size
static public int w;
static public int h;

// high score
static public int highScore;
// whether touching ghost piece drops shape
static public boolean ghostControl = false;

// sounds
public SoundManager sound; 

	
@Override
public void onCreate(Bundle savedInstanceState) {
    
	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate");
    // request to turn the title OFF and make it full screen
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    // initialize screen width
    Display display = getWindowManager().getDefaultDisplay();
	w = display.getWidth();
	h = display.getHeight();
	main = this;
	// initialize sounds
	sound = new SoundManager(this);
    // go to landing page
    landing = new Landing(this);
    instructions = new Instructions(this);
	setContentView(landing);
}


/**
 * Checks to see if a game can be resumed
 * @return true if there is a game to resume
 */
public boolean checkResumedGame() {
	
	InputStreamReader i;

	try {
		i = new InputStreamReader(openFileInput(Game.FILENAME));
		ghostControl = i.read() == 1 ? true : false;
		highScore = i.read();
		if (highScore < 0) { highScore = 0; }
		if (i.read() < 0) { return false; }
		i.close();
		return true;
	} catch(IOException e) { }
	return false;
}


@Override
protected void onPause() {
	
	Log.d(TAG, "onPause");
	if (game != null) { game.save(); }
	super.onPause();
}


@Override
public boolean onKeyDown(int keyCode, KeyEvent e) {
	
	Toast toast = Toast.makeText(Main.main.getApplicationContext(), "Game Saved", Toast.LENGTH_SHORT);
	
	// capture back button
	if (keyCode == KeyEvent.KEYCODE_BACK) {
		if (game != null && game.thread.running) {
			game.stop();
			toast.setGravity(Gravity.TOP, 0, (int)Math.floor(Main.h * 0.4));
			toast.show();
			setContentView(landing);
			return true;
		}
	}
	
	return super.onKeyDown(keyCode, e);
}


}