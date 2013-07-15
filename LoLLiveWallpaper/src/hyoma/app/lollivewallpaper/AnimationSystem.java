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

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class AnimationSystem {
	private Key base = null;
	private List<Key> keyList = new ArrayList<Key>();
	private boolean isCreated = false; 
	
	public AnimationSystem(Context ctx) {

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
	
	// Called to create the animation system. It is only allowed to be called once.
	public void createAnimationSystem(Key base,  List<Key> keyList) throws IOException{
		if (isCreated){throw new IOException("Cannot create Animation System more than once");}
		this.base = base;
		this.keyList = keyList;
		this.isCreated = true;
	}
	
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
		public String name;
		public Idle idleMain;
		public List<Idle> idleList;
		public List<OutTransition> outTransitionList;
		
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
	}
};