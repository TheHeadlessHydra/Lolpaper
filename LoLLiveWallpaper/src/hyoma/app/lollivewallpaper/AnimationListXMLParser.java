// *****************************************************************************
//***************************
/*
	Parser that will take the animation_list.xml file in res/xml and parse it to
	create a final AnimationSystem with appropriately created Base and Keys. 
*/
//***************************
// *****************************************************************************

package hyoma.app.lollivewallpaper;


import hyoma.app.lollivewallpaper.AnimationSystem;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnimationListXMLParser {
	   private static final String ns = null;
	   private AnimationSystem anim = null;
	   
	   public AnimationListXMLParser(AnimationSystem anim){
		   this.anim = anim;
	   }

	    public void parse(InputStream in) throws XmlPullParserException, IOException {
	        try {
	            XmlPullParser parser = Xml.newPullParser();
	            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in, null);
	            parser.nextTag();
	            readAnimations(parser);
	        } finally {
	            in.close();
	        }
	    }
	    
	    // Parses the contents of <animations> root tag. If it encounters <base> or <keys>, hand them
	    // off to their respective methods for processing. Otherwise, skip the tag. Returns a complete
	    // AnimationSystem.
	    private void readAnimations(XmlPullParser parser) throws XmlPullParserException, IOException {
	    	AnimationSystem.Key base = null;
	    	List<AnimationSystem.Key> keyList = new ArrayList<AnimationSystem.Key>();
	    	
	        parser.require(XmlPullParser.START_TAG, ns, "animations");
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String name = parser.getName();
	            // Starts by looking for the entry tag
	            if (name.equals("base")) {
	            	System.out.println ("IN <base>");
	            	if(base != null){throw new IOException("Cannot have more than one base");}
	            	base = readBase(parser);
	            } else if (name.equals("keys")) {
	            	System.out.println ("IN <keys>");
	            	keyList = readKeys(parser);
	            } else {
	                skip(parser);
	            }
	        }
	        
	        // Create the animation system and return it
	        if (base == null){
	        	String errorMsg = "ERROR: Must have at least one base";
				throw new Error(errorMsg);
	        }
	        this.anim.createAnimationSystem(base, keyList);
	    }

	    // Parses the contents of base. If it encounters an idle, idles, or out_transitions tag, hand them
	    // off to their respective methods for processing. Otherwise, skip the tag.
	    private AnimationSystem.Key readBase(XmlPullParser parser) throws XmlPullParserException, IOException {
	        parser.require(XmlPullParser.START_TAG, ns, "base");
			AnimationSystem.Idle idleMain = null;
			List<AnimationSystem.Idle> idleList = null;
			List<AnimationSystem.OutTransition> outTransitionList = null;
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String parseName = parser.getName();
	            if (parseName.equals("idle_main")) {
	            	System.out.println ("IN <idle> main");
	            	if(idleMain != null){throw new IOException("Cannot have more than one main idle state");}
	            	idleMain = readIdleMain(parser);
	            } else if (parseName.equals("idles")) {
	            	System.out.println ("IN <idles>");
	            	idleList = readIdles(parser);
	            } else if (parseName.equals("out_transitions")) {
	            	System.out.println ("IN <out_transitions>");
	            	outTransitionList = readOutTransitions(parser);
	            } else {
	                skip(parser);
	            }
	        }
	        return new AnimationSystem.KeyBuilder().name("base")
	        									   .idleMain(idleMain)
	        									   .idleList(idleList)
	        									   .outTransitionList(outTransitionList)
	        									   .buildKey();
	    }
	    
	   // Processes '<idles>' tag
	    private List<AnimationSystem.Key> readKeys(XmlPullParser parser) throws IOException, XmlPullParserException {
	    	parser.require(XmlPullParser.START_TAG, ns, "keys");
			List<AnimationSystem.Key> keyList = new ArrayList<AnimationSystem.Key>();
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String parseName = parser.getName();
	            if (parseName.equals("key")) {
	            	keyList.add(readKey(parser));
	            } else {
	                skip(parser);
	            }
	        }
	        return keyList;
	    }
	    
	    // Parses the contents of <key>. If it encounters an idle, idles, or out_transitions tag, hand them
	    // off to their respective methods for processing. Otherwise, skip the tag.
	    private AnimationSystem.Key readKey(XmlPullParser parser) throws XmlPullParserException, IOException {
	        parser.require(XmlPullParser.START_TAG, ns, "key");
			AnimationSystem.Idle idleMain = null;
			List<AnimationSystem.Idle> idleList = null;
			List<AnimationSystem.OutTransition> outTransitionList = null;
			String name = parser.getAttributeValue(null, "name");
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String parseName = parser.getName();
	            if (parseName.equals("idle")) {
	            	idleMain = readIdleMain(parser);
	            } else if (parseName.equals("idles")) {
	            	idleList = readIdles(parser);
	            } else if (parseName.equals("out_transitions")) {
	            	outTransitionList = readOutTransitions(parser);
	            } else {
	                skip(parser);
	            }
	        }
	        return new AnimationSystem.KeyBuilder().name(name)
	        									   .idleMain(idleMain)
	        									   .idleList(idleList)
	        									   .outTransitionList(outTransitionList)
	        									   .buildKey();
	    }
	    
	    // LIST TAG FUNCTIIONS ----------------------------------------------------------
	    // Processes '<idles>' tag
	    private List<AnimationSystem.Idle> readIdles(XmlPullParser parser) throws IOException, XmlPullParserException {
	    	parser.require(XmlPullParser.START_TAG, ns, "idles");
			List<AnimationSystem.Idle> idleList = new ArrayList<AnimationSystem.Idle>();
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String parseName = parser.getName();
	            if (parseName.equals("idle")) {
	            	idleList.add(readIdle(parser));
	            } else {
	                skip(parser);
	            }
	        }
	        return idleList;
	    }
	    
	 // Processes '<out_transitions>' tag
	    private List<AnimationSystem.OutTransition> readOutTransitions(XmlPullParser parser) throws IOException, XmlPullParserException {
	    	parser.require(XmlPullParser.START_TAG, ns, "out_transitions");
			List<AnimationSystem.OutTransition> outTransitionList = new ArrayList<AnimationSystem.OutTransition>();
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String parseName = parser.getName();
	            if (parseName.equals("out_transition")) {
	            	outTransitionList.add(readOutTransition(parser));
	            } else {
	                skip(parser);
	            }
	        }
	        return outTransitionList;
	    }
	    
	    // END TAG FUNCTIONS ----------------------------------------------------------------------------------
	    // Processes '<idle_main>' tag
	    private AnimationSystem.Idle readIdleMain(XmlPullParser parser) throws IOException, XmlPullParserException {
	        parser.require(XmlPullParser.START_TAG, ns, "idle_main");
	        String name = parser.getAttributeValue(null, "name");
	        System.out.println ("idle_main name: "+name);
	        String frames = parser.getAttributeValue(null, "frames");
	        System.out.println ("idle_main frames: "+frames);
	        // Parse next tags
	        int next = parser.next();
	        if (next == XmlPullParser.TEXT){
	        	System.err.println ("WARNING: No text required in <idle_main> tag. Skipping.");
	        	next = parser.next();
	        }
	        if (next != XmlPullParser.END_TAG){
	        	String errorMsg = "ERROR: Failure to parse XML. Are you sure you're following the correct format?";
	        	errorMsg += "\nParse format met...: "+next+".  Expected: "+XmlPullParser.END_TAG+", the end tag.";
				throw new Error(errorMsg);
	        }
	        parser.require(XmlPullParser.END_TAG, ns, "idle_main");
	        return new AnimationSystem.Idle(name,Integer.parseInt(frames));
	    }
	 // Processes'<idle>' tag
	    private AnimationSystem.Idle readIdle(XmlPullParser parser) throws IOException, XmlPullParserException {
	        parser.require(XmlPullParser.START_TAG, ns, "idle");
	        String name = parser.getAttributeValue(null, "name");
	        System.out.println ("idle name: "+name);
	        String frames = parser.getAttributeValue(null, "frames");
	        System.out.println ("idle frames: "+frames);
	        // Parse next tags
	        int next = parser.next();
	        if (next == XmlPullParser.TEXT){
	        	System.err.println ("WARNING: No text required in <idle> tag. Skipping.");
	        	next = parser.next();
	        }
	        if (next != XmlPullParser.END_TAG){
	        	String errorMsg = "ERROR: Failure to parse XML. Are you sure you're following the correct format?";
	        	errorMsg += "\nParse format met...: "+next+".  Expected: "+XmlPullParser.END_TAG+", the end tag.";
				throw new Error(errorMsg);
	        }
	        parser.require(XmlPullParser.END_TAG, ns, "idle");
	        
	        return new AnimationSystem.Idle(name,Integer.parseInt(frames));
	    }
	    // Processes '<out_transition>' tag
	    private AnimationSystem.OutTransition readOutTransition(XmlPullParser parser) throws IOException, XmlPullParserException {
	        parser.require(XmlPullParser.START_TAG, ns, "out_transition");
	        String name = parser.getAttributeValue(null, "name");
	        System.out.println ("out_transition name: "+name);
	        String frames = parser.getAttributeValue(null, "frames");
	        System.out.println ("out_transition frames: "+frames);
	        String to = parser.getAttributeValue(null, "to");
	        // Parse next tags
	        int next = parser.next();
	        if (next == XmlPullParser.TEXT){
	        	System.err.println ("WARNING: No text required in <idle> tag. Skipping.");
	        	next = parser.next();
	        }
	        if (next != XmlPullParser.END_TAG){
	        	String errorMsg = "ERROR: Failure to parse XML. Are you sure you're following the correct format?";
	        	errorMsg += "\nParse format met...: "+next+".  Expected: "+XmlPullParser.END_TAG+", the end tag.";
				throw new Error(errorMsg);
	        }
	        parser.require(XmlPullParser.END_TAG, ns, "out_transition");
	        
	        return new AnimationSystem.OutTransition(name,Integer.parseInt(frames),to);
	    }
	    
	    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
	    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
	    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
	    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            throw new IllegalStateException();
	        }
	        int depth = 1;
	        while (depth != 0) {
	            switch (parser.next()) {
	            case XmlPullParser.END_TAG:
	                    depth--;
	                    break;
	            case XmlPullParser.START_TAG:
	                    depth++;
	                    break;
	            }
	        }
	    }
};