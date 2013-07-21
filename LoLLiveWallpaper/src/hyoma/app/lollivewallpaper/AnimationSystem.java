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
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

public class AnimationSystem {
// static classes to hold basic animation information ------------------------
		public static class Idle{
			public final String name;
			public final int frames;
			
			Idle(String name, int frames){
				this.name = name;
				this.frames = frames;
			}
			public int getMaxFrame(){
				return this.frames - 1;
			}
			public String getFrame(int frame){
				return this.name+"_"+(Integer.toString(frame));
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
			
			public int getMaxFrame(){
				return this.frames - 1;
			}
			public String getFrame(int frame){
				return this.name+"_"+(Integer.toString(frame));
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
			Random random = new Random();
			
			// private variables
			private int currentIdleNumber = 0;
			
			private Key(String name, Idle idleMain, List<Idle> idleList, List<OutTransition> outTransitionList){
				this.name = name;
				this.idleMain = idleMain;
				this.idleList = idleList;
				this.outTransitionList = outTransitionList;
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
			
			public Idle getIdleMain(){
				return idleMain;
			}
			public String getIdleMainFrame(int frame){
				return idleMain.name+"_"+(Integer.toString(frame));
			}
			public int getIdleMainMaxFrame(){
				return idleMain.frames - 1;
			}
			// Choose a random out transition and return it, or null if there are no out transitions
			public OutTransition chooseRandomTransiton(){
				int START = 0;
				int END = outTransitionList.size();
				if (END == 0){
					return null;
				}
				// Choose random position in list
				int rand = random.nextInt(END-START) + START;
				return outTransitionList.get(rand);
			}
			// Chooses next idle in the list; cycle through list. 
			public Idle getNextIdle(){
				int idleSize = idleList.size();
				if (idleSize == 0)
					return null;
				else if (currentIdleNumber == idleSize-1)
					currentIdleNumber = 0;
				else
					currentIdleNumber++;
				return idleList.get(currentIdleNumber);
			}
		}
// static classes to hold basic animation information ------------------------
		
//  PRIVATE VARIABLES -------------------------------------------------------
	private Context currentCtx = null;
	
	private Key base = null;
	private List<Key> keyList = new ArrayList<Key>();
	private boolean isCreated = false; 
	
	private Key currentKey;								// Store the current Key that is playing
	private Bitmap currentFrame;						// Store bitmap for the current frame
	private int currentFrameNumber;						// Stores the current frame number of the animation
	private int maxFrameNumber;							// Store the maximum frame number allowed for the current animation

	private boolean inIdle = false;						// True when a secondary idle animation is playing.
	private Idle currentIdle = null;					// Holds the secondary idle animation that will play. 
		
	private boolean inTransition = false;  				// True when transition animation is playing, false otherwise. 
	private OutTransition currentTransition = null; 	// Key that will be set after a transition
	
	
	private int actionrate;								// Pref that stores how fast actions should play
	private int idlerate;								// Pref that stores how fast idle animations should play
		
	// variables and runners to handle timings 
	private ScheduledExecutorService scheduleTaskExecutor;
	private boolean updateIdle = false;
	private Runnable idleTimer = new Runnable() {
		public void run() {
			updateIdle = true;
			System.out.println ("Idle Timer");
		}
	};
	private boolean updateAction = false;
	private Runnable actionTimer = new Runnable() {
		public void run() {
			updateAction = true;
			System.out.println ("Action Timer");
		}
	};
	
// Basic construction functions -----------------------------------

	
// Basic construction functions -----------------------------------	
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
// Animation handling functions
	
// PUBLIC FUNCTIONS -----------------------------------------------
	// Call to initiate the animation system
	public void initiate(){
		// Update rates based on pref values
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(currentCtx);
		actionrate = Integer.parseInt(prefs.getString("actionrate", "120"));
		idlerate = Integer.parseInt(prefs.getString("idlerate", "60"));
		
		// Set the current key to base and set the current frame
		setKey(base);
		setFrame();

		// Setup and fire initial timers
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		fireActionTask();
		fireIdleTask();
	}
	// Main call to be made to move on to the next frame
	public Bitmap nextFrame(){
		currentFrameNumber++;
		// Covers base case of only one frame in the animation.
		// If only one frame in animation, show only that one frame, static image.
		if (currentFrameNumber > maxFrameNumber)
			currentFrameNumber = 0;
		
		// Allow seam-less transitions by only allowing transitions at end frames. 
		boolean isEndFrame = currentFrameNumber == maxFrameNumber;
		
		// Start a new action
		if (updateAction && isEndFrame && !inTransition && !inIdle)
			startNewAction();
		// Start a new secondary idle sequence
		else if (updateIdle && isEndFrame && !inTransition && !inIdle)
			startNewIdle();
		// End of transition animation
		if(isEndFrame && inTransition)
			endNewAction();
		// End of secondary idle sequence
		if(isEndFrame && !inTransition && inIdle)
			endNewIdle();
		
		// End of main idle animation. Loop.
		if(isEndFrame && !inTransition && !inIdle)
			currentFrameNumber = 0;
		
		setFrame();
		return currentFrame;
	}
	

// PRIVATE FUNCTIONS -----------------------------------------------
	// Create a new Action Task thread. Make sure only one is alive at a time!
	private void fireActionTask(){
		updateAction = false;
		scheduleTaskExecutor.schedule(actionTimer, actionrate, TimeUnit.SECONDS);
	}
	// Create a new Idle Task thread. Make sure only one is alive at a time!
	private void fireIdleTask(){
		updateIdle = false;
		scheduleTaskExecutor.schedule(idleTimer, idlerate, TimeUnit.SECONDS);
	}
	// Return Key with 'name', or null if it was not found.

	// Search through keyList and return key 'name'
	private Key getKey(String name){
		Key get = null;
		for(int i = 0; i < keyList.size();i++){
			if (keyList.get(i).name == name){
				get = keyList.get(i);
			}
		}
		return get;
	}	
	// Set currentKey and update relevant information
	private void setKey(Key set){
		currentKey = set;
		currentFrameNumber = 0;
		maxFrameNumber = set.getIdleMainMaxFrame();
		inIdle = false;
		currentIdle = null;
		inTransition = false;
		currentTransition = null;
		
	}
	// Set currentTransition and update relevant information
	private void setTrans(OutTransition trans){
		currentKey = null;
		currentFrameNumber = 0;
		maxFrameNumber = trans.getMaxFrame();
		inIdle = false;
		currentIdle = null;
		inTransition = true;
		currentTransition = trans;
	}
	// Set currentIdle and update relevant information
	private void setIdle(Idle idle){
		currentKey = null;
		currentFrameNumber = 0;
		maxFrameNumber = idle.getMaxFrame();
		inIdle = true;
		currentIdle = idle;
		inTransition = false;
		currentTransition = null;
	}
	
	// Sets the bitmap for the current frame based on currentFrameNumber and
	// if its an idleMain, an action transition or an idle sequence. 
	private void setFrame(){
		String frame;
		if (inIdle)
			frame = currentIdle.getFrame(currentFrameNumber);
		else if (inTransition)
			frame = currentTransition.getFrame(currentFrameNumber);
		else
			frame = currentKey.getIdleMainFrame(currentFrameNumber);
		
		int identifier = 0;
		identifier = currentCtx.getResources().getIdentifier(frame,"drawable", "hyoma.app.lollivewallpaper");
		if (identifier == 0){
			String errorMsg = "ERROR: Animation frame missing or corrupted:    "+frame;
			throw new Error(errorMsg);
		}
		currentFrame = BitmapFactory.decodeResource(currentCtx.getResources(), identifier);
	}
	
	// Begin the transition sequence, or reset to base if no transition found.
	// Kills all remaining idle and action threads.
	private void startNewAction(){
		cleanExecutor();
					
		OutTransition trans = currentKey.chooseRandomTransiton();
		if (trans == null){
			setKey(base);
			fireActionTask();
			fireIdleTask();
		}
		else
			setTrans(trans);
	}	
	// Begin Idle sequence, or do nothing if no idle found.
	private void startNewIdle(){
		Idle idle = currentKey.getNextIdle();
		if (idle != null)
			setIdle(idle);
		else{
			// No idle animation available. Do not fire a new idle task. 
			inIdle = false;
			currentIdle = null;
		}
	}
	// End of secondary idle sequence. Switch back to main idle.
	// Fire new idle task.
	private void endNewIdle(){
		setKey(currentKey);
		fireIdleTask();
	}
	// End of transition sequenced. 
	// Kill then reset any idle or action threads that remain. 
	private void endNewAction(){
		// This should never actually do anything, no threads should be running at this time.
		resetExecutor();
		setKey( getKey(currentTransition.to) );
		fireActionTask();
		fireIdleTask();
	}
	
	
	// Shutdown all tasks, do not allow their completion, reset Executor.
	public void resetExecutor(){
		scheduleTaskExecutor.shutdownNow();
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		
	}
	// Shutdown tasks
	public void cleanExecutor(){
		scheduleTaskExecutor.shutdownNow();
	}
};