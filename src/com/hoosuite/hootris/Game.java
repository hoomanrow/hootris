
package com.hoosuite.hootris;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import ui.HooButton;
import ui.HooClick;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Game extends SurfaceView implements SurfaceHolder.Callback {

	
final public static int SQW = 10; // # of squares wide a hootris game is
final public static int SQH = 20; // # of square high a hootris game is
final public static int rowsPerLevel = 10; // # of rows per level
final public static String FILENAME = "game.data";
final private static String TAG = Game.class.getSimpleName();;


public Main main;
public GameThread thread;
public SoundManager sound;

private int w = Main.w, h = Main.h; // screen width & height
private int sqp; // width of a hootris square in pixels
private Bitmap board; // board that contains tetris pieces
private int tx, ty, tw, th; // coordinates and size of where hootris gfx starts
private int ta[][] = new int[SQW][SQH]; // 2D array of squares
private int nextX, nextY, holdX, holdY; // x coordinate of next & hold shape
private int levelX, levelY, rowsX, rowsY, scoreX, scoreY; // coordinates of more stuff
private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

private int level; // current level
private int rows; // # of rows completed
private int speed = 1000; // # of milliseconds to wait before a piece falls down
private int score; // current score
private long moveTime = 0; // time that has ellapsed since piece last moved

private int downX, downY; // coordinates of where user pressed down
private long downTime; // time user clicked on screen
private boolean shapeClicked, shapeMoved, shapeHeld, movedSideways, holdClicked; // flags for user clicking on shape, moved shape, held shape, moved sideways
private int dropX; // x coordinate when drop first began
private final int dropSensitivity = 35; // # of pixels that have to be travelled in 50 milliseconds for shape to drop

private boolean rowAnimate = false; // if set to true, game pauses until animation is complete
private long rowStartTime; // time when row animation started
private long rowTime; // used to keep track of row animation
private int rowAnimateSpeed = 50; // speed of row animation
private int rotateTime = 500; // how quick you should press and unpress for shape to rotate

public boolean gameOver = false; // flag for game over state
private long gameOverTime; // time when game over started
private long gameOverSpeed = 100; // speed of game over animation
private int gameOverPos; // position in game over animation

private HooShape currentPiece, nextPiece, ghostPiece, holdPiece, holdGhostPiece;  // current, next, ghost, held and ghost of held piece

private HooButton buttonPlayAgain, buttonMainMenu; // game over buttons


public Game(Main main) {
	
	super(main);
	this.main = main;
	sound = main.sound;
	getHolder().addCallback(this);
	setFocusable(true);
	initDisplay();
	makeBoard();
	clearBoard();
}


/**
 * restores game state 
 */
public void load() {
	
	FileInputStream f;
	InputStreamReader i;
	int cx, cy;
	int n;
	
	try {
		f = main.openFileInput(FILENAME);
		i = new InputStreamReader(f);
		i.read(); // ignore ghost control in case user has changed it
		Main.highScore = i.read();
		level = i.read();
		rows = i.read();
		score = i.read();
		speed = i.read();
		n = i.read();
		if (n < HooShape.MAX_SHAPE) {
			currentPiece = new HooShape(n);
			ghostPiece = new HooShape(n);
			ghostPiece.isGhost = true;
		} else { currentPiece = ghostPiece = null; }
		n = i.read();
		nextPiece = n >= HooShape.MAX_SHAPE ? null : new HooShape(n);
		n = i.read();
		if (n < HooShape.MAX_SHAPE) {
			holdPiece = new HooShape(n);
			holdGhostPiece = new HooShape(n);
			holdGhostPiece.isGhost = true;
		} else { holdPiece = holdGhostPiece = null; }
		if (currentPiece != null) {
			currentPiece.sqx = i.read();
			currentPiece.sqy = i.read();
			currentPiece.rotateTo(ta, i.read());
		} else {
			i.read(); i.read(); i.read();
		}
		for (cy = 0; cy < SQH; cy++) {
			for (cx = 0; cx < SQW; cx++) {
				ta[cx][cy] = i.read();
				if (ta[cx][cy] >= HooShape.MAX_SHAPE) { ta[cx][cy] = -1; }
			}
		}
		i.close();
		Log.d(TAG, "Loaded game");
		makeBoard();
	} catch(IOException e) { Log.d(TAG, "Error loading game!"); }	
}


/**
 * saves game state
 */
public void save() {

	FileOutputStream f;
	OutputStreamWriter w;
	int cx, cy;
	
	try {		
		if (gameOver) { 
			main.deleteFile(FILENAME); 
			f = main.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			w = new OutputStreamWriter(f);
			w.write(Main.ghostControl ? 1 : 0);
			w.write(Main.highScore);
			w.flush();
			w.close();
			Log.d(TAG, "saved game");
		} else {
			f = main.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			w = new OutputStreamWriter(f);
			w.write(Main.ghostControl ? 1 : 0);
			w.write(Main.highScore);
			w.write(level);
			w.write(rows);
			w.write(score);
			w.write(speed);
			w.write(currentPiece == null ? HooShape.MAX_SHAPE : currentPiece.color);
			w.write(nextPiece == null ? HooShape.MAX_SHAPE : nextPiece.color);
			w.write(holdPiece == null ? HooShape.MAX_SHAPE : holdPiece.color);
			if (currentPiece != null) {
				w.write(currentPiece.sqx);
				w.write(currentPiece.sqy);
				w.write(currentPiece.rotation);
			} else {
				w.write((int)0); w.write((int)0); w.write((int)0);
			}
			for (cy = 0; cy < SQH; cy++) {
				for (cx = 0; cx < SQW; cx++) {
					w.write(ta[cx][cy] >= 0 ? ta[cx][cy] : HooShape.MAX_SHAPE);
				}
			}
			w.flush();
			w.close();
			
			Log.d(TAG, "saved game");
		}
	} catch(IOException e) { Log.d(TAG, "error saving game!"); }
}


/// updates the positions/values of everything
public void update(long ellapsed) {

	if (!rowAnimate && !gameOver) { updateGame(ellapsed); }
	else if (rowAnimate) { updateRowAnimation(ellapsed); }
	else { updateGameOver(ellapsed); }
}


/// updates the game
public void updateGame(long ellapsed) {
	
	// create a next piece if doesn't exist
	if (nextPiece == null) { nextPiece = new HooShape((int)Math.floor(Math.random() * HooShape.MAX_SHAPE)); }
	if (currentPiece == null) { 
		createNextPiece(); 
		if (currentPiece.willCollide(ta, 0, 0)) { 
			gameOver = true;
			gameOverTime = gameOverPos = 0;
			save();
			sound.play(sound.gameOver);
		}
	}
	// move piece down one square
	if (moveTime > speed) {
		if (!currentPiece.willCollide(ta, 0, 1)) { currentPiece.sqy++; }
		else {
			// reached the bottom
			currentPiece.attachTo(ta);
			currentPiece = null; 
			checkRows();
		}
		moveTime -= speed;
	}
	// show the ghost piece
	if (currentPiece != null && ghostPiece != null) { 
		ghostPiece.sqx = currentPiece.sqx;
		ghostPiece.sqy = currentPiece.sqy;
		ghostPiece.drop(ta);
	}
	
	moveTime += ellapsed;
}


/// updates row animation
public void updateRowAnimation(long ellapsed) {
	
	int i;
	
	rowTime += ellapsed;
	if (rowTime > rowAnimateSpeed) {
		for (i = 0; i < SQH; i++) {
			if (checkRow(i)) { animateRow(i); }
		}
		rowTime -= rowAnimateSpeed;
	}
	
	if (System.currentTimeMillis() - rowStartTime > rowAnimateSpeed * 5) {
		clearCompletedRows();
		rowAnimate = false;
	}
}

/**
 * @param ellapsed time that has ellapsed
 */
public void updateGameOver(long ellapsed) {
	
	gameOverTime += ellapsed;
	if (gameOverTime > gameOverSpeed && gameOverPos < SQH) {
		animateRow(gameOverPos);
		gameOverPos++;
		gameOverTime -= gameOverSpeed;
	}
}


@Override
protected void onDraw(Canvas canvas) {
	
	int cx;
	int cy;
	String s;
	
	canvas.drawBitmap(board, 0, 0, null);
	
	// draw hootris squares
	for (cy = 0; cy < SQH; cy++) {
		for (cx = 0; cx < SQW; cx++) {
			if (ta[cx][cy] >= 0) { canvas.drawBitmap(HooShape.squares[ta[cx][cy]], tx + cx * sqp, ty + cy * sqp, null); }
		}
	}
	
	// draw currently active piece
	if (currentPiece != null) { 
		currentPiece.draw(canvas, tx + currentPiece.sqx * sqp, ty + currentPiece.sqy * sqp); 
		ghostPiece.draw(canvas, tx + ghostPiece.sqx * sqp, ty + ghostPiece.sqy * sqp);
	}
	// draw next and hold pieces
	if (nextPiece != null) { nextPiece.draw(canvas, nextX + ((4-nextPiece.w)*sqp)/2, nextY+sqp + ((4-nextPiece.h)*sqp)/2); }
	if (holdPiece != null) { holdPiece.draw(canvas, holdX + ((4-holdPiece.w)*sqp)/2, holdY+sqp + ((4-holdPiece.h)*sqp)/2); }
	// draw level, rows and score
	s = String.valueOf(level+1);
	canvas.drawText(s, levelX+(sqp*4-textPaint.measureText(s))/2, levelY+sqp*2, textPaint);	
	s = String.valueOf(rows);
	canvas.drawText(s, levelX+(sqp*4-textPaint.measureText(s))/2, rowsY+sqp*2, textPaint);	
	s = String.valueOf(score);
	canvas.drawText(s, levelX+(sqp*4-textPaint.measureText(s))/2, scoreY+sqp*2, textPaint);	
	
	// game over
	if (gameOver) {
		buttonPlayAgain.draw(canvas);
		buttonMainMenu.draw(canvas);
	}
}


@Override
public boolean onTouchEvent(MotionEvent e) {
	
	int dy;
	int dx;
	
	if (currentPiece == null) return true;
	if (gameOver) { HooButton.touchEvent(e); return(true); }		

	switch(e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (currentPiece != null) {
				downX = dropX = (int)e.getX();
				downY = (int)e.getY();
				downTime = System.currentTimeMillis();
				movedSideways = false;
				// user clicks on hold piece
				if (downX >= holdX && downY >= holdY && downX < holdX + sqp * 4 && downY < holdY + sqp * 5 && !shapeHeld) { holdClicked = true; Log.d(TAG, "!"); }
				// user clicks on playing surface
				else if (downX >= tx && downY >= ty && downX < tx+tw && downY < ty+th) {
					shapeClicked = true;
					shapeMoved = false;
				}
				else { shapeClicked = holdClicked = false; }
			}
			break;
		case MotionEvent.ACTION_UP:
			if (shapeClicked && !shapeMoved && Math.abs(e.getX() - downX) < sqp && Math.abs(e.getY() - downY) < sqp) {
				// user clicks on ghost piece
				if (Main.ghostControl && ghostPiece.clicked(tx, ty, downX, downY) && !currentPiece.clicked(tx, ty, downX, downY)) {
					currentPiece.drop(ta);
					currentPiece.attachTo(ta);
					currentPiece = null;
					checkRows();
				}
				// user wants to rotate the piece
				else if (!movedSideways && System.currentTimeMillis() - downTime < rotateTime) {
					if (!currentPiece.rotate(ta)) { ghostPiece.rotate(ta); }
				}
				shapeClicked = holdClicked = false;
			}
			// user clicks on hold piece
			else if (holdClicked && e.getX() >= holdX && e.getY() >= holdY && e.getX() < holdX + sqp * 4 && e.getY() < holdY + sqp * 5 && !shapeHeld) {
				Log.d(TAG, "@");
				holdCurrentPiece();
				holdClicked = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			dx = (int)e.getX() - downX;
			dy = (int)e.getY() - downY;
			
			if (shapeClicked && (Math.abs(dx) > sqp || dy > sqp)) {
				shapeMoved = true;
				// check if user "swiped" down for a drop
				if (System.currentTimeMillis() - downTime > 25 && !movedSideways) { 
					if (dy > dropSensitivity) {
						// try to make x coordinate go back to what it was when user started "dropping" shape
						if (currentPiece.sqx != dropX && !currentPiece.willCollide(ta, dropX - currentPiece.sqx, 0)) { currentPiece.sqx += dropX - currentPiece.sqx; }
						currentPiece.drop(ta);
						currentPiece.attachTo(ta);
						currentPiece = null;
						checkRows();
						shapeClicked = holdClicked = false;
						break;
					}
					else {
						dropX = currentPiece.sqx;
						downTime = System.currentTimeMillis();
					}
				} 
				// move left
				if (dx < -sqp && !currentPiece.willCollide(ta, -1, 0) && Math.abs(dx) > Math.abs(dy)) { 
					currentPiece.sqx--; 
					downX = (int)e.getX();
					downTime = System.currentTimeMillis();
					movedSideways = true;
				}
				// move right
				if (dx > sqp && !currentPiece.willCollide(ta, 1, 0) && Math.abs(dx) > Math.abs(dy)) { 
					currentPiece.sqx++; 
					downX = (int)e.getX();
					downTime = System.currentTimeMillis();
					movedSideways = true;
				}
				// move down
				if (dy > sqp && !currentPiece.willCollide(ta, 0, 1)) { 
					currentPiece.sqy++; 
					downY += sqp; 
					moveTime = 0; 
					downX = (int)e.getX(); 
				}
				else if (!Main.ghostControl && dy > sqp * 2) {
					currentPiece.attachTo(ta);
					currentPiece = null;
					checkRows();
					shapeClicked = holdClicked = false;
				}
			}
			break;
	}
	
	return true;
}


@Override
public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	// TODO Auto-generated method stub
	
}


@Override
public void surfaceCreated(SurfaceHolder holder) {

	// note: you should never start the same thread twice
	// if you want to run the thread again just start a new one
	Log.d(TAG, "surfaceCreated");
	resume();
}


@Override
public void surfaceDestroyed(SurfaceHolder holder) {

	Log.d(TAG, "surfaceDestroyed");
	stop();
}


/// stops the game and thread
public void stop() {
	
	boolean retry = true;
	
	// this is called a clean shutdown (if you don't do this your app freezes whenever you get a call or something)	
	thread.running = false;
    while (retry) {
        try {
            thread.join();
            retry = thread.running = false;
        } 
        catch (InterruptedException e) { }
	}	
}


// restarts the thread/game
public void resume() {
	
	thread = new GameThread(this);
	thread.running = true;
	thread.start();
}


/// figure out size and coordinates of game
private void initDisplay() {
	
	int sqx = (int)Math.floor(w / 15); // # of squares we need horizontally
	int sqy = (int)Math.floor(h / 21); // # of squares we need vertically
	sqp = sqx > sqy ? sqy : sqx; // use the smallest size as square size
	tx = sqp/2; // x coordate of hootris gfx
	ty = (h - sqp * SQH) / 2; // y coordinate of hootris gfx
	tw = sqp * SQW;
	th = sqp * SQH;
	nextX = w-(int)Math.floor(sqp*4.5);
	nextY = ty;
	holdX = nextX;
	holdY = nextY + (sqp * 6);
	levelX = rowsX = scoreX = holdX;
	levelY = nextY + (sqp * 12);
	rowsY = levelY + (int)(sqp * 2.666);
	scoreY = levelY + (int)(sqp * 5.333);
	
	HooShape.sqp = sqp; // initialize squares
	HooShape.initSquares();
}


/// creates the game board
private void makeBoard() {
	
	Canvas cBoard;
	int l = level % HooShape.MAX_SHAPE;
	
	// board and main play area
	board = Shapes.shinyRoundRect(w, h, HooShape.colours[l][0], HooShape.colours[l][1], sqp, false); // board background colour
	cBoard = new Canvas(board);
	cBoard.drawBitmap(Shapes.rect(tw, th, Color.BLACK), tx, ty, null); // add black inside of board (11 x 21 squares)
	// next piece area
	cBoard.drawBitmap(Shapes.rect(sqp * 4, sqp * 5, Color.BLACK), nextX, nextY, null);
	textPaint.setColor(0xffffffff);
	textPaint.setTextSize(sqp);
	cBoard.drawText("NEXT", nextX+(sqp*4-textPaint.measureText("NEXT"))/2, nextY+sqp, textPaint);
	// hold piece area
	cBoard.drawBitmap(Shapes.rect(sqp * 4, sqp * 5, Color.BLACK), holdX, holdY, null);
	cBoard.drawText("HOLD", holdX+(sqp*4-textPaint.measureText("HOLD"))/2, holdY+sqp, textPaint);
	// level, rows, and score area
	cBoard.drawBitmap(Shapes.rect(sqp * 4, sqp * 8, Color.BLACK), levelX, levelY, null);
	cBoard.drawText("LEVEL", levelX+(sqp*4-textPaint.measureText("LEVEL"))/2, levelY+sqp, textPaint);
	cBoard.drawText("ROWS", rowsX+(sqp*4-textPaint.measureText("ROWS"))/2, rowsY+sqp, textPaint);
	cBoard.drawText("SCORE", scoreX+(sqp*4-textPaint.measureText("SCORE"))/2, scoreY+sqp, textPaint);
	// game over buttons
	HooButton.clear();
	buttonPlayAgain = new HooButton("Play Again", tx+sqp, ty + sqp*2, sqp*(SQW-2), sqp*2, playAgainClick);
	buttonMainMenu = new HooButton("Main Menu", tx+sqp, ty + sqp*5, sqp*(SQW-2), sqp*2, mainMenuClick);
}


/// checks to see if a row was completed
private boolean checkRow(int n) {
	
	int cx;
	boolean rowCompleted = true; 
	
	for (cx = 0; cx < SQW && rowCompleted; cx++) {
		if (ta[cx][n] < 0) { rowCompleted = false; }
	}
	return rowCompleted;
}


/// checks to see if any rows were completed
private void checkRows() {

	int i, nRows = 0;
	
	for (i = 0; i < SQH; i++) {
		if (checkRow(i)) { nRows++; }
	}
	
	if (nRows > 0) { 
		rowStartTime = System.currentTimeMillis();
		rowTime = 0;
		rowAnimate = true; 	
	}
	
	switch (nRows) {
		case 0: sound.play(sound.drop); break; 
		case 1: sound.play(sound.oneRow); break;
		case 2: sound.play(sound.twoRows); break;
		case 3: sound.play(sound.threeRows); break;
		case 4: sound.play(sound.fourRows); break;	
	}	
}


/// animates a row
private void animateRow(int row) {
	
	int i;
	
	for (i = 0; i < SQW; i++) {
		ta[i][row] = (int)Math.floor(Math.random() * HooShape.MAX_SHAPE);
	}
}


/// clears all completed rows
private void clearCompletedRows() {
	
	int row = SQH-1;
	int cx;
	int cy;
	int oldLevel = level;
	int oldRows = rows;
	int nRows;
	
	while (row >= 0) {
		if (checkRow(row)) {
			// move every row above down one square
			for (cy = row; cy > 0; cy--) {
				for (cx = 0; cx < SQW; cx++) {
					ta[cx][cy] = ta[cx][cy-1];
				}
			}
			for (cx = 0; cx < SQW; cx++) {
				ta[cx][0] = -1;
			}
			rows++;
			level = (int)Math.floor(rows / rowsPerLevel);
		}
		else { row--; }
	}
	
	if (level != oldLevel) {
		makeBoard();
		speed *= 0.85;
	}
	
	nRows = (rows - oldRows);
	score += nRows * nRows * 100;
	if (score > Main.highScore) { Main.highScore = score; }
}


// clear the board
private void clearBoard() {
	
	int cx;
	int cy;
	
	for (cx = 0; cx < SQW; cx++) {
		for (cy = 0; cy < SQH; cy++) {
			ta[cx][cy] = -1;
		}
	}
}


/// holds the current piece
private void holdCurrentPiece() {
	
	HooShape temp = currentPiece;
	HooShape tempGhost = ghostPiece;
	
	currentPiece = holdPiece;
	ghostPiece = holdGhostPiece;
	holdPiece = temp;
	holdGhostPiece = tempGhost;
	if (currentPiece == null) { createNextPiece(); }
	currentPiece.sqx = 4;
	currentPiece.sqy = 0;
	
	shapeHeld = true;
}


/// creates next piece
private void createNextPiece() {
	
	currentPiece = nextPiece; 
	ghostPiece = new HooShape(currentPiece.color);
	ghostPiece.isGhost = true;
	nextPiece = null;
	moveTime = 0;
	shapeClicked = shapeMoved = shapeHeld = false;
}


/*** Buttons ***/
private HooClick playAgainClick = new HooClick() { public void onClick() {
	
	stop();
	Main.game = new Game(main);
	main.setContentView(Main.game);
} };
	
private HooClick mainMenuClick = new HooClick() { public void onClick() {
	
	stop();
	main.setContentView(Main.landing);
} };
	
	
}

	















