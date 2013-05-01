package hyoma.app.lollivewallpaper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class SetWallpaperActivity extends Activity {
	// private variables
	private final int SET_WALLPAPER = 1;
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
		// Find the size of the screen here. Allow for both before and after android version 13  by using
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
		// Use startActivityForResult() to notify onActivityResult when the intent is complete
		Intent BGintent = new Intent();
		BGintent.setAction(Intent.ACTION_SET_WALLPAPER);
		startActivityForResult(BGintent,SET_WALLPAPER);
	}
	
	public void onClearClick(View view){
		Bitmap wallpaperBG; 
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
		Drawable wallpaperDrawable = wallpaperManager.getDrawable();
		if (wallpaperDrawable instanceof BitmapDrawable) {
			wallpaperBG = ((BitmapDrawable)wallpaperDrawable).getBitmap();
	    }
		else{
			Bitmap bitmap = Bitmap.createBitmap(wallpaperDrawable.getIntrinsicWidth(), wallpaperDrawable.getIntrinsicHeight(), Config.ARGB_8888);
		    Canvas cv = new Canvas(bitmap); 
		    wallpaperDrawable.setBounds(0, 0, cv.getWidth(), cv.getHeight());
		    wallpaperDrawable.draw(cv);
		    wallpaperBG = bitmap;
		}
		// HACK!!--!!--!! THIS MAY NOT WORK ON ALL DEVICES OR VERSIONS
		// Create an empty byte output stream, put it into an input stream, then use that to setStream()
		// For whatever reason, this resets the wallpaper to exactly how it should be, and fast.
		// The proper method of putting the actual image in takes waaaay too long to compress,
		// which is why it is commented out. This may not work as intended, but there might be a way
		// to manually reset the wallpaper the same way setStream() seems to be doing it. Without a hack.
		// Needs further investigation...
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		//wallpaperBG.compress(CompressFormat.PNG, 100, bos); 
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
		
		try{
			wallpaperManager.setStream(bs);
		} catch(IOException e){
			Toast toast = Toast.makeText(getApplicationContext(), "ERROR: Cannot clear", Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	// When the onSetBGClick() is complete, call the last animation intent that was currently active. 
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the activity that just finished was a set wallpaper call...
		if(requestCode == SET_WALLPAPER){
			Intent mordIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
			mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, MyWallpaperService.class));
			startActivity(mordIntent);
		}
        super.onActivityResult(requestCode, resultCode, data);
    }

}