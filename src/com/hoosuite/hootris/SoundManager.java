package com.hoosuite.hootris;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;



public class SoundManager {
	
private SoundPool soundPool; 
private AudioManager audioManager;


public int drop;
public int fourRows;
public int threeRows;
public int twoRows;
public int oneRow;
public int gameOver;


public SoundManager(Main main) {
	
	// setup sound classes and volume control
	soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
	audioManager = (AudioManager)main.getSystemService(Context.AUDIO_SERVICE);
	main.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	// initialize sounds
	drop = soundPool.load(main, R.raw.drop, 1);
	fourRows = soundPool.load(main, R.raw.fourrows, 1);
	threeRows = soundPool.load(main, R.raw.threerows, 1);
	twoRows = soundPool.load(main, R.raw.tworows, 1);
	oneRow = soundPool.load(main, R.raw.onerow, 1);
	gameOver = soundPool.load(main, R.raw.gameover, 1);	
}
	

public void play(int index) { 
	
     int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
     soundPool.play(index, volume, volume, 1, 0, 1f); 
}

public void playLooped(int index) { 
	
     int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
     soundPool.play(index, volume, volume, 1, -1, 1f); 
}
	
}