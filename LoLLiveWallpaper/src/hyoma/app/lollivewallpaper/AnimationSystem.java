// *****************************************************************************
//***************************
/*
	The animation tree that handles the entire animation system. 
*/
//***************************
// *****************************************************************************

package hyoma.app.lollivewallpaper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;

public class AnimationSystem {
	// PRIVATE VARIABLES -------------------------
	private Context currentCtx = null;
	
	private Key base = null;
	private List<Key> keyList = new ArrayList<Key>();
	private boolean isCreated = false; 
	
	private Bitmap currentFrame;
	private int currentFrameNumber;
	private Key currentKey;
	
	private int actionrate;
	private int idlerate;
	
	// variables and runners to handle timings 
	private ScheduledExecutorService scheduleTaskExecutor;
	private final Handler handler = new Handler();
	private boolean updateIdle = false;
	// Runnable that occurs for every 
	private Runnable idleTimer = new Runnable() {
		public void run() {
			updateIdle = true;
			System.out.println ("ITS BEEN 60 SECONDS");
			idlerate += 1;
			System.out.println (Integer.toString(idlerate));
		}
	};
	private boolean updateAction = false;
	private Runnable actionTimer = new Runnable() {
		public void run() {
			updateAction = true;
			System.out.println ("ITS BEEN 120 SECONDS");
			idlerate += 1;
			System.out.println (Integer.toString(idlerate));
		}
	};
	// END PRIVATE VARIABLES -------------------------
	
	
	public AnimationSystem(Context ctx) throws IOException {
		if (ctx == null){
			throw new IOException("ERROR: Could not obtain Context");
		}
		currentCtx = ctx;
		
		// Create parser and pass the animation system. The parser will properly set the base and keylist. 
		try {
			new AnimationListXMLParser(this).parse(ctx.getResources().openRawResource(R.raw.animation_list));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Called to create the animation system. It is only allowed to be called once, and is called from within the parser. 
	public void createAnimationSystem(Key base,  List<Key> keyList) throws IOException{
		if (isCreated){throw new IOException("Cannot create Animation System more than once");}
		this.base = base;
		this.keyList = keyList;
		this.isCreated = true;
	}
	
	public Key getBase(){
		return base;
	}
	public List<Key> getKeyList(){
		return keyList;
	}
	
// Classes to hold information ------------------------
	public static class Idle{
		public final String name;
		public final int frames;
		
		Idle(String name, int frames){
			this.name = name;
			this.frames = frames;
		}
	}
	
	public static class OutTransition{
		public final String name;
		public final int frames;
		public final String to;
		
		OutTransition(String name, int frames, String to){
			this.name = name;
			this.frames = frames;
			this.to = to;
		}
	}
	
	// Builder class for a Key - Used to create default parameters
	public static class KeyBuilder
	{
		private String name; // CANNOT be defaulted.
		private Idle idleMain = null;
		private List<Idle> idleList = null;
		private List<OutTransition> outTransitionList; // CANNOT be defaulted. 

	    public KeyBuilder() { }

	    public Key buildKey()
	    {
	        return new Key(name, idleMain, idleList, outTransitionList);
	    }

	    public KeyBuilder name(String name)
	    {
	        this.name = name;
	        return this;
	    }

	    public KeyBuilder idleMain(Idle idleMain)
	    {
	        this.idleMain = idleMain;
	        return this;
	    }

	    public KeyBuilder idleList(List<Idle> idleList)
	    {
	        this.idleList = idleList;
	        return this;
	    }
	    public KeyBuilder outTransitionList(List<OutTransition> outTransitionList)
	    {
	        this.outTransitionList = outTransitionList;
	        return this;
	    }
	}
	
	public static class Key{
		// public setter variables
		public String name;
		public Idle idleMain;
		public List<Idle> idleList;
		public List<OutTransition> outTransitionList;
		
		// private variables
		private Idle currentIdle;
		
		private Key(String name, Idle idleMain, List<Idle> idleList, List<OutTransition> outTransitionList){
			this.name = name;
			this.idleMain = idleMain;
			this.idleList = idleList;
			this.outTransitionList = outTransitionList;
			this.currentIdle = idleMain;
		}		
		public void setMainIdle(Idle transition){
			this.idleMain = transition;
		}
		public void addIdle(Idle transition){
			this.idleList.add(transition);
		}
		public void addTransition(OutTransition transition){
			this.outTransitionList.add(transition);
		}	
		
		public void setIdle(String name){
			
		}
		public String getFrame(int frame){
			return currentIdle.name+"_"+(Integer.toString(frame));
		}
	}
// Classes to hold information ------------------------

// Animation handling functions
	public void initiate(){
		currentFrameNumber = 0;
		currentKey = base;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(currentCtx);
		actionrate = Integer.parseInt(prefs.getString("actionrate", "120"));
		idlerate = Integer.parseInt(prefs.getString("idlerate", "60"));
		
		// Set current frame
		String frame = currentKey.getFrame(currentFrameNumber);
		int identifier = 0;
		identifier = currentCtx.getResources().getIdentifier(frame,"drawable", "hyoma.app.lollivewallpaper");
		if (identifier == 0){
			String errorMsg = "ERROR: Animation frame missing or corrupted:    "+frame;
			throw new Error(errorMsg);
		}
		currentFrame = BitmapFactory.decodeResource(currentCtx.getResources(), identifier);

		// Start the timers for action and idle animations
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleAtFixedRate(idleTimer, 0, actionrate, TimeUnit.SECONDS);
		scheduleTaskExecutor.scheduleAtFixedRate(actionTimer, 0, idlerate, TimeUnit.SECONDS);
	}

	public void nextFrame(){

	}
	
	public void cleanRunnables(){
		scheduleTaskExecutor.shutdown();
	}
};