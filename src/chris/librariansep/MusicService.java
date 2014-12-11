package chris.librariansep;

import java.util.ArrayList;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicService extends Service implements
	MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
	MediaPlayer.OnCompletionListener {
	
	private NotificationsManager notificationManager;
	//media player
	private MediaPlayer player;
	//song list
	private ArrayList<Song> songs;
	//current position
	private int songPosn;
	private final IBinder musicBind = new MusicBinder();
	
	private int foregroundID = 3415;
	
	public void onCreate(){
		//create the service
		super.onCreate();
		//initialize position
		songPosn=0;
		this.notificationManager = new NotificationsManager(this.getApplicationContext(), this);
		//create player
		player = new MediaPlayer();
		initMusicPlayer();
	}
	
	public void updateNowPlayingNotification() {
		Notification generatedNotification = this.notificationManager.generateNotification(this.songs.get(songPosn).getName());
		startForeground(foregroundID, generatedNotification);
	}
	
	public void initMusicPlayer() {
		player.setWakeMode(getApplicationContext(),
			  PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}
	
	public void setList(ArrayList<Song> theSongs){
		songs = theSongs;
	}
	
	public class MusicBinder extends Binder {
		
		  MusicService getService() {
			  return MusicService.this;
		  }
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent){
//		  player.stop();
//		  player.release();
		  return false;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		  mp.start();
	}
	
	public void playSong() {
		player.reset();
		//get song
		Song playSong = songs.get(songPosn);
		//get id
		long currSong = playSong.getID();
		Log.d("MUSIC SERVICE", "Attemping to play " + songPosn);
		//set uri

    	String urlPath = "android.resource://" + getPackageName() + "/"
    			+ currSong;
    	Uri trackUri = Uri.parse(urlPath);
		try{
			  player.setDataSource(getApplicationContext(), trackUri);
			}
			catch(Exception e){
			  Log.e("MUSIC SERVICE", "Error setting data source (URI Location: " + trackUri.toString() +")", e);
			}
		player.prepareAsync();
		updateNowPlayingNotification();
	}
	
	public void setSong(int songIndex) {
		Log.d("MUSIC SERVICE", "Song position set to: " + songIndex);
		  songPosn=songIndex;
	}
	
	public int getSongIndex() {
		return this.songPosn;
	}
	
	public MediaPlayer getMediaPlayer() {
		return this.player;
	}

	public void stop() {
		this.player.stop();
    	this.player.release();
		stopForeground(true);
	}
	
	


}
