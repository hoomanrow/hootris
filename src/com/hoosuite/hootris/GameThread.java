
package com.hoosuite.hootris;

import android.graphics.Canvas;
import android.view.SurfaceHolder;



public class GameThread extends Thread {
	

public Boolean running = false;

	
private SurfaceHolder surfaceHolder;
private Game game;

//time variables
private long lastTime = 0;
private long ellapsed;


public GameThread(Game game) {
	
	super();
	this.game = game;
	this.surfaceHolder = game.getHolder();
}

	
@Override
public void run() {
	
	Canvas canvas;
	long currentTime;
	
	lastTime = System.currentTimeMillis();
	while (running) {
		canvas = null;
		try {
			// lock canvas for exclusive use
			canvas = surfaceHolder.lockCanvas();
			// I think this makes sure we don't access surfaceHolder or canvas twice at the same time
			synchronized (surfaceHolder) {
				currentTime = System.currentTimeMillis();
				ellapsed = currentTime - lastTime;
				game.update(ellapsed);
				game.onDraw(canvas);
				lastTime = currentTime;
			}
		}
		finally {
			surfaceHolder.unlockCanvasAndPost(canvas);			
		}
	}
}


}
