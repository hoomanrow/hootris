package com.hoosuite.hootris;

import android.graphics.Canvas;


public class HootrisBorder {

	
private int sqp; // size of squares	

	
public HootrisBorder(Main main) {
	
	sqp = Main.w / 10;
}


public void draw(Canvas canvas) {
	
	int i, c;
	
	// draw horizontal squares
	for (i = 0; i < Main.w / sqp; i++) {
		c = (int)Math.floor(Math.random() * HooShape.MAX_SHAPE);
		canvas.drawBitmap(Shapes.shinyRoundRect((int)sqp, (int)sqp, HooShape.colours[c][0], HooShape.colours[c][1], 4, true), i * sqp, 0, null);
		c = (int)Math.floor(Math.random() * HooShape.MAX_SHAPE);
		canvas.drawBitmap(Shapes.shinyRoundRect((int)sqp, (int)sqp, HooShape.colours[c][0], HooShape.colours[c][1], 4, true), i * sqp, Main.h - sqp, null);			
	}
	// draw vertical squares
	for (i = 0; i < Main.h / sqp; i++) {			
		c = (int)Math.floor(Math.random() * HooShape.MAX_SHAPE);
		canvas.drawBitmap(Shapes.shinyRoundRect((int)sqp, (int)sqp, HooShape.colours[c][0], HooShape.colours[c][1], 4, true), 0, i * sqp, null);
		c = (int)Math.floor(Math.random() * HooShape.MAX_SHAPE);
		canvas.drawBitmap(Shapes.shinyRoundRect((int)sqp, (int)sqp, HooShape.colours[c][0], HooShape.colours[c][1], 4, true), Main.w - sqp, i * sqp, null);			
	}
}

	
}
