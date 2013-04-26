package hyoma.app.lollivewallpaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SetWallpaperActivity extends Activity {
	// I am assuming that this class remains alive for the duration of the live wallpapers lifecycle.
	// I am storing the height/width values into it so that, in the preview, the user can choose where
	// to place the image, and the once the preview dies and the real wallpaper is being set, the values chosen
	// in the preview remain and gets placed there. 
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

          Measuredwidth = size.x;
          Measuredheight = size.y; 
        }else{
          Display d = w.getDefaultDisplay(); 
          Measuredwidth = d.getWidth(); 
          Measuredheight = d.getHeight(); 
        }		
		// ------------------------------------
	    
		super.onCreate(savedInstanceState); // This activity itself creates the light grey atmosphere. Must be a property of the activity theme. 
		setContentView(R.layout.main); // the main.xml in res/layout/ is what will set any menu/buttons on start of app.
	}

	public void onButton1Click(View view) {
		// This is called when button is clicked. It does all the work of creating the background. 
		Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, MyWallpaperService.class));
		startActivity(intent);
	}



}