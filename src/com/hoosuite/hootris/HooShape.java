
package com.hoosuite.hootris;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * 
 * @author Hooman Rowshanbin 2010-10-17
 * 
 * shapes used in Hootris
 * as well as all the different colours
 *
 */

public class HooShape {


final static public int MAX_SHAPE = 7; // maximum # of shapes in the game
//different colours for shapes
final static public int colours[][] = { { 0xFF66FFFF, 0xFF003C3C }, { 0xFF66FF00, 0xFF1E4200 } , { 0xFFFF0000, 0xFF420000 }, 
	{ 0xFF0000FF, 0xFF00002B }, { 0xFFFFFF00, 0xFF2B2B00 }, { 0xFFFF00FF, 0xFF2B002B }, {0xFFFFFFFF, 0xFF2B2B2B } };
// shape information
final static private int shapeData[][] = {	{ 	1, 0, 0, 0,
												1, 0, 0, 0,
												1, 0, 0, 0,
												1, 0, 0, 0 },
												
											{	1, 1, 0, 0,
												1, 1, 0, 0,
												0, 0, 0, 0,
												0, 0, 0, 0 },
												
											{	1, 1, 0, 0,
												0, 1, 0, 0,
												0, 1, 0, 0,
												0, 0, 0, 0 },
												
											{	1, 1, 0, 0,
												1, 0, 0, 0,
												1, 0, 0, 0,
												0, 0, 0, 0 },
												
											{	0, 1, 0, 0,
												1, 1, 0, 0,
												1, 0, 0, 0,
												0, 0, 0, 0 },
												
											{	1, 0, 0, 0,
												1, 1, 0, 0,
												0, 1, 0, 0,
												0, 0, 0, 0 },
												
											{	1, 1, 1, 0,
												0, 1, 0, 0,
												0, 0, 0, 0,
												0, 0, 0, 0 } };
// different sizes for each shape
final static private int shapeW[] = { 1, 2, 2, 2, 2, 2, 3 };
final static private int shapeH[] = { 4, 2, 3, 3, 3, 3, 2 };

//different squares (based on colours)
static public Bitmap[] squares = new Bitmap[7];
static public Bitmap[] ghostSquares = new Bitmap[7];
// width of a square in pixels
static public int sqp;

public int sqx, sqy; // shape position (in squares)
public int w, h; // shape size (in squares)
public int color; // shape color
public int rotation; // shape rotation
public int data[][] = new int[4][4]; // contains square data
public boolean isGhost = false; // if true this is a ghost square


/// create a new shape
public HooShape(int n) {
	
	int cx;
	int cy;
	
	color = n;
	w = shapeW[n];
	h = shapeH[n];
	
	for (cy = 0; cy < h; cy++) {
		for (cx = 0; cx < w; cx++) {
			data[cx][cy] = shapeData[color][cy*4+cx];
		}
	}
	sqx = 4;
	sqy = 0;	
}


/// initialized all the different square colours
static public void initSquares() {
	
	int i;
	
	for (i = 0; i < MAX_SHAPE; i++) {
		squares[i] = Shapes.shinyRoundRect(sqp, sqp, colours[i][1], colours[i][0], 4, true);
		ghostSquares[i] = Shapes.shinyRoundRect(sqp, sqp, colours[i][1] & 0x33ffffff, colours[i][0] & 0x33ffffff, 4, false);
	}
}


/// draws the shape
public void draw(Canvas canvas, int x, int y) {
	
	int cx;
	int cy;
	
	for (cx = 0; cx < w; cx++) {
		for (cy = 0; cy < h; cy++) {
			if (data[cx][cy] > 0) canvas.drawBitmap(isGhost ? ghostSquares[color] : squares[color], x+cx*sqp, y+cy*sqp, null);
		}
	}
}


/// checks if shape is about to collide (if it moves) or if it is going to go out of bounds (returns true if there is a collision)
public boolean willCollide(int ta[][], int dx, int dy) {
	
	int newX;
	int newY;
	int cx;
	int cy;
	boolean collided = false;
	
	for (cy = 0; cy < 4 && !collided; cy++) {
		for (cx = 0; cx < 4 && !collided; cx++) {
			newX = sqx + dx + cx;
			newY = sqy + dy + cy;
			if (data[cx][cy] > 0 && (newX < 0 || newY < 0 || newX >= Game.SQW || newY >= Game.SQH || ta[newX][newY] >= 0)) {
				collided = true;
			}
		}
	}
	
	return collided;
}


/// rotates piece, but only if it doesn't collide (returns true if it collided)
public boolean rotate(int ta[][]) {
	
	int oldData[][] = data.clone();
	int rotateData[][] = new int[4][4];
	int rotateW = h;
	int rotateH = w;
	int cy;
	int cx;
	
	for (cy = 0; cy < rotateH; cy++) {
		for (cx = 0; cx < rotateW; cx++) {
			rotateData[cx][cy] = data[cy][h - cx - 1];
	} }
	data = rotateData;
	w = rotateW;
	h = rotateH;
	rotation++;
	// undo rotate if it doesn't work
	if (!isGhost && willCollide(ta, 0, 0)) {
		data = oldData;
		w = rotateH;
		h = rotateW;
		rotation--;
		return true;
	}
	return false;
}


/// rotates shape n times
public void rotateTo(int ta[][], int n) {

	while (n % 4 != rotation % 4) { rotate(ta); }
}


/// given tx, ty and mouse coordinates, checks if user clicked on piece
public boolean clicked(int tx, int ty, int mx, int my) {
	
	return(mx >= tx+sqx*sqp && my >= ty+sqy*sqp && mx < tx+(sqx+w)*sqp && my < ty+(sqy+h)*sqp);
}


/// attaches piece to hootris board
public void attachTo(int ta[][]) {
	
	int cx;
	int cy;
	
	for (cy = 0; cy < 4; cy++) {
		for (cx = 0; cx < 4; cx++) {
			if (data[cx][cy] > 0) { ta[sqx+cx][sqy+cy] = color; }
		}
	}
}


/// drops the piece as far down as possible
public void drop(int ta[][]) {
	
	while (!willCollide(ta, 0, 1)) {
		sqy++;
	}
}
	
	
}





















