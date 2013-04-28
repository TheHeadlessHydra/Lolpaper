package hyoma.app.lollivewallpaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SetWallpaperActivity extends Activity {
	// ----- Preferences ------------------------------
	// Preference files
	public static final String locationPref = "location";
	// Preference keys for locationPref
	public static final String X = "x";
	public static final String Y = "y";
	// ------------------------------------------------
	
	// I am assuming that this class remains alive for the duration of the live wallpapers lifecycle.
	// I am storing the height/width values into it so that, in the preview, the user can choose where
	// to place the image, and the once the preview dies and the real wallpaper is being set, the values chosen
	// in the preview remain and gets placed there. 
	static float Totalwidth = 0;
	static float Totalheight = 0;
	static float Measuredwidth = 0;
	static float Measuredheight = 0;

	static public void setWidth(float x){
		Measuredwidth = x;
	}
	static public void setHeight(float y){
		Measuredheight = y;
	}
	static public float getWidth(){
		return Measuredwidth;
	}
	static public float getHeight(){
		return Measuredheight;
	}
	
	static public void setTotalWidth(float x){
		Totalwidth = x;
	}
	static public void setTotalHeight(float y){
		Totalheight = y;
	}
	static public float getTotalWidth(){
		return Totalwidth;
	}
	static public float getTotalHeight(){
		return Totalheight;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		// ------------------------------------
		// Find the size of the screen here. Allow for both before and after version 13 changes by using
		// depricated functions. 
		Point size = new Point();
		WindowManager w = getWindowManager();
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
	      w.getDefaultDisplay().getSize(size);

          setWidth(size.x/2);
          setHeight(size.y/2);
          setTotalWidth(size.x);
          setTotalHeight(size.y);
        }else{
          Display d = w.getDefaultDisplay(); 
          setWidth(d.getWidth()/2); 
          setHeight(d.getHeight()/2); 
          setTotalWidth(d.getWidth());
          setTotalHeight(d.getHeight());
        }		
		// ------------------------------------
	    
	    // If the location of the animation was created earlier, preserve it. 
	    SharedPreferences prefs = getApplicationContext().getSharedPreferences("location", 0);
		if(prefs.contains(X) && prefs.contains(Y)){
			setWidth( prefs.getFloat(X,0) );
			setHeight( prefs.getFloat(Y,0) );
		}
		
		
		super.onCreate(savedInstanceState); // This activity itself creates the light grey atmosphere. Must be a property of the activity theme. 
		setContentView(R.layout.main); // the main.xml in res/layout/ is what will set any menu/buttons on start of app.
	}

	public void oncChibiMordClick(View view) {
		// This is called when button is clicked. It does all the work of creating the background. 
		Intent mordIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, MyWallpaperService.class));
		startActivity(mordIntent);
	}
	
	public void onSetBGClick(View view){
		Intent BGintent = new Intent();
		BGintent.setAction(Intent.ACTION_SET_WALLPAPER);
		startActivity(BGintent);
	}

}