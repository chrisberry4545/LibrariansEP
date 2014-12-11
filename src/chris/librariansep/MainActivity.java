package chris.librariansep;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import chris.carousel.Carousel;
import chris.carousel.CarouselAdapter;
import chris.carousel.CarouselAdapter.OnItemClickListener;
import chris.carousel.CarouselAdapter.OnItemSelectedListener;
import chris.librariansep.MusicService.MusicBinder;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
	OnTouchListener, MediaController.MediaPlayerControl {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private MediaController mc;
    private int[] songs = {
    		R.raw.thepier,
    		R.raw.treadmills,
    		R.raw.murder,
    		R.raw.halcyondays,
    		R.raw.thefair,
    		R.raw.theballadofcarl
    		};
    
    private static MusicService musicSrv;
    private static Intent playIntent;
    private boolean musicBound=false;
    
    //connect to the service
    private ServiceConnection musicConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        mViewPager.setCurrentItem(1);
        mc = new MediaController(this);
    	mc.setMediaPlayer(this);
    	mc.setPrevNextListeners(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				nextSong();
			}
    	}, new OnClickListener() {
			@Override
			public void onClick(View v) {
				previousSong();
			}
    	});
    	mViewPager.setOnTouchListener(this);
    }
    
    	
    
    public ArrayList<Song> generateSongList() {
    	String[] songNames = this.getResources().getStringArray(R.array.songTitles);
    	ArrayList<Song> songList = new ArrayList<Song>();
    	for (int i = 0; i < songs.length; i++) {
    		int songId = songs[i];
    		String name = songNames[i];
    		songList.add(new Song(songId, name));
    	}
    	return songList;
    }
    

    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    private void setUpService() {
    	musicConnection = new ServiceConnection(){
 	       
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
              MusicBinder binder = (MusicBinder)service;
              //get service
              musicSrv = binder.getService();
    	  	    musicSrv.getMediaPlayer().setOnCompletionListener(new OnCompletionListener() {
    	  	    	public void onCompletion(MediaPlayer player) {
    	      	    	decreaseSongIndex();
    	      	    	musicSrv.setSong(musicSrv.getSongIndex());
    	      	    	musicSrv.playSong();
    	      	    	updateCarousel();
    	  	    	}
    	  	    });
              //pass list
              musicSrv.setList(generateSongList());
              musicBound = true;
            }
           
            @Override
            public void onServiceDisconnected(ComponentName name) {
              musicBound = false;
            }
          };
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if(playIntent == null || musicSrv == null){
    		Log.d("LibrariansEPMain", "Play Intent starting...");
    		setUpService();
    		playIntent = new Intent(this, MusicService.class);
    	    startService(playIntent);
    	    this.getApplicationContext().bindService(new Intent(this, MusicService.class), musicConnection, Context.BIND_AUTO_CREATE);
    	    
    	} else {
    		Log.d("LibrariansEPMain", "Play Intent already exists...");
    		musicSrv.getMediaPlayer().setOnCompletionListener(new OnCompletionListener() {
	  	    	public void onCompletion(MediaPlayer player) {
	      	    	decreaseSongIndex();
	      	    	musicSrv.setSong(musicSrv.getSongIndex());
	      	    	musicSrv.playSong();
	      	    	updateCarousel();
	  	    	}
	  	    });
    	}
    }
    
    @Override
    protected void onPause() { 
    	if (musicSrv.getMediaPlayer().isPlaying()) {
        	musicSrv.getMediaPlayer().setOnCompletionListener(new OnCompletionListener() {
    	    	public void onCompletion(MediaPlayer player) {
    	    		if (musicSrv.getSongIndex() != 1) {
              	    	decreaseSongIndex();
              	    	musicSrv.setSong(musicSrv.getSongIndex());
              	    	musicSrv.playSong();
    	      	    	updateCarousel();
    	    		} else {
    	    			stopMusicService();
    	    		}
      	    	}
      	    });
    	} else {
			stopMusicService();
    	}
    	super.onPause();
    }
    
    public void stopMusicService() {
    	try { //TODO:: Should check for exception rather than catching it.
        	if (musicSrv != null) {
            	musicSrv.stop();
        	}
        	this.getApplicationContext().unbindService(this.musicConnection);
    		stopService(playIntent);
    		playIntent = null;
    		musicSrv = null;
    	} catch (Exception e) {
    		
    	}
    }
    
    @Override
    protected void onDestroy() {
//    	stopMusicService();
    	super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkOrientation(newConfig.orientation);
    }
    
    public void checkOrientation(int orientation) {
//    	Toast.makeText(this, orientation + "", Toast.LENGTH_LONG).show();
    	if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	setBackgroundsToLandscape();
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT){
        	setBackgroundsToPortrait();
        }
    }
    
    private void setBackgroundsToLandscape() {
    	setBioBackground();
    }
    
    private void setBackgroundsToPortrait() {
    	setBioBackground();
    }
    
    private void setBioBackground() {
    	setLayoutBackground(R.id.biolayout, R.drawable.bandpic);
    }
    
    private void setLayoutBackground(int layoutID, int drawableID) {
    	View view = findViewById(layoutID);
    	Drawable drawable = this.getResources().getDrawable(drawableID);
    	if (view != null && drawable != null) {
    		view.setBackgroundDrawable(drawable);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment fragment;
        	switch (position) {
        	case 0 : fragment = new BioSectionFragment();
        		break;
        	case 1 : fragment = new TrackSectionFragment();
        		break;
        	case 2 : fragment = new LinkSectionFragment();
        		break;
        	default : fragment = new DummySectionFragment();
        			
        	}
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
	

    private Handler handler = new Handler();
    private void showMediaController() {
		mc.setAnchorView(findViewById(R.id.trackslayout));

	    handler.post(new Runnable() {
	      public void run() {
	    	  mc.setEnabled(true);
	    	  mc.show();
	      }
	    });
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		showMediaController();
		return false;
	}
    
	public void linkClicked(View v) {
		Intent browserIntent;
		String urlString = null;
		switch (v.getId()) {
		case R.id.facebooklogo :
			urlString = getString(R.string.facebooklink);
			break;
		case R.id.youtubelogo :
			urlString = getString(R.string.youtubelink);
			break;
		case R.id.twitterlogo :
			urlString = getString(R.string.twitterlink);
			break;
		case R.id.soundcloudlogo :
			urlString = getString(R.string.soundcloudlink);
			break;
		case R.id.bandcamplogo :
			urlString = getString(R.string.bandcamplink);
			break;
		}
		if (urlString != null) {
			browserIntent =
					new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
			startActivity(browserIntent);
		}
	}
    
    public void handleOnClick(int songId) {
    	Log.d("MainActivity", "Song id is " + songId + " songindex is " + musicSrv.getSongIndex());
    	if (musicSrv != null) {
	  		  musicSrv.setSong(songId);
	  		  musicSrv.playSong();
    	}
		showMediaController();
    }
    
    public void nextSong() {
        decreaseSongIndex();
        handleOnClick(musicSrv.getSongIndex());
    	updateCarousel();
    }
    
    public void previousSong() {
        increaseSongIndex();
        handleOnClick(musicSrv.getSongIndex());
    	updateCarousel();
    }
    
    public void increaseSongIndex() {
    	int songIndex = musicSrv.getSongIndex();
    	if (songIndex + 1 >= songs.length) {
    		songIndex = 0;
    	} else {
    		songIndex++;
    	}
    	musicSrv.setSong(songIndex);
    }
    
    public void decreaseSongIndex() {
    	int songIndex = musicSrv.getSongIndex();
    	if (songIndex - 1 < 0) {
    		songIndex = songs.length - 1;
    	} else {
    		songIndex--;
    	}
    	musicSrv.setSong(songIndex);
    }
    
    public void updateCarousel() {
    	Carousel carousel = (Carousel)findViewById(R.id.carousel);
    	if (carousel != null && carousel.getSelectedItemPosition() != musicSrv.getSongIndex()) {
    		updateCarouselToPosition(musicSrv.getSongIndex());
    	}
    }
    
    public void updateCarouselToPosition(int pos) {
    	Carousel carousel = (Carousel)findViewById(R.id.carousel);
    	carousel.setSelection(pos, true);
    }
    
    public static int getCurrentSongIndex() {
    	return musicSrv.getSongIndex();
    }
    
    /*Music should only start if music isn't already playing and its first call (i.e app start).*/
    public boolean isMusicPlaying() {
    	try { //TODO:: Should test before catching exceptions here.
        	if (musicSrv == null || musicSrv.getMediaPlayer() == null) {
        		return false;
        	}
        	return musicSrv.getMediaPlayer().isPlaying();
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    private boolean firstCall;
    public void setIsFirstCall(boolean fc) {
    	firstCall = fc;
    }
    public boolean isFirstCall() {
    	return firstCall;
    }
    

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.soundcloudtextlink);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class BioSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public BioSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bio, container, false);
            return rootView;
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class TrackSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
    	
    	public TrackSectionFragment() {
    		
    	}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
	            View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
	            
	            Carousel carousel = (Carousel)rootView.findViewById(R.id.carousel);
	            final MainActivity activity = (MainActivity) getActivity();
	            activity.setIsFirstCall(true);
	            carousel.setOnItemClickListener(new OnItemClickListener(){
            	
            	
    			public void onItemClick(CarouselAdapter<?> parent, View view,
    					int position, long id) {		
    				Log.d("CAROUSEL", "Item click.. Position selected: " + position);
    				if (MainActivity.getCurrentSongIndex() != position) {
            			activity.handleOnClick(position); 
            			activity.updateCarouselToPosition(position);
    				}
    			}
            	
            });
            carousel.setOnItemSelectedListener(new OnItemSelectedListener(){

    			public void onItemSelected(CarouselAdapter<?> parent, View view,
    					int position, long id) {
    				Log.d("CAROUSEL", "Item selected.. Position selected: " + position);
    				if (!activity.isFirstCall() || 
    						(activity.isFirstCall() && !activity.isMusicPlaying())) {
        				activity.handleOnClick(position);
    				} else {
    					activity.updateCarousel();
    				}
    				activity.setIsFirstCall(false);
    			}

    			public void onNothingSelected(CarouselAdapter<?> parent) {
    			}
            	
            }
            );
            
            return rootView;
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class LinkSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public LinkSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_links, container, false);
            return rootView;
        }
    }


	@Override
	public boolean canPause() {
		return true;
	}



	@Override
	public boolean canSeekBackward() {
		return true;
	}



	@Override
	public boolean canSeekForward() {
		return true;
	}



	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public int getBufferPercentage() {
		return 0;
	}



	@Override
	public int getCurrentPosition() {
		return musicSrv.getMediaPlayer().getCurrentPosition();
	}



	@Override
	public int getDuration() {
		return musicSrv.getMediaPlayer().getDuration();
	}



	@Override
	public boolean isPlaying() {
		return musicSrv.getMediaPlayer().isPlaying();
	}



	@Override
	public void pause() {
		musicSrv.getMediaPlayer().pause();
	}



	@Override
	public void seekTo(int arg0) {
		musicSrv.getMediaPlayer().seekTo(arg0);
	}



	@Override
	public void start() {
		musicSrv.getMediaPlayer().start();
	}

}
