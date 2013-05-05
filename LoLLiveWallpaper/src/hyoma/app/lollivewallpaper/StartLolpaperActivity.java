// *****************************************************************************
//***************************
/*
This is the launcher activity.
It sets up the main UI and fires the intents that start the live wallpaper . 
*/
//***************************
// *****************************************************************************

package hyoma.app.lollivewallpaper;

import hyoma.app.lollivewallpaper.CreateGridView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class StartLolpaperActivity extends Activity {
	// private variables
	private final int SET_WALLPAPER = 1;
	// ----- Preferences ------------------------------
	public static final String X = "x";
	public static final String Y = "y";
	public static final String TOUCHX = "touchx";
	public static final String TOUCHY = "touchy";
	public static final String totalHeight = "totalheight";
	public static final String totalWidth = "totalwidth";
	
	// Tells the receiver if the 'Clear' button was pressed.
	public static boolean clearButton = false; 
	// Tells the receiver if the 'Change Background' button was pressed.
	public static boolean changeBGButton = false; 
	// ------------------------------------------------

	@SuppressWarnings({ "deprecation", "unused" })
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
		SharedPreferences.Editor prefsEditor = defPrefs.edit();
		// ------------------------------------
		// Find the size of the screen here. Allow for both before and after android version 13  by using
		// Deprecated functions. Do not change location in prefs if there is a stored value already. 
		Float currentX = defPrefs.getFloat(X,-1);
		Float currentY = defPrefs.getFloat(Y,-1);
		
		if(currentX == -1 && currentY == -1){
			Point size = new Point();
			WindowManager w = getWindowManager();
		    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
		      w.getDefaultDisplay().getSize(size);
		      
		      float x = size.x;
		      float y = size.y;
		      prefsEditor.putFloat(X, size.x/2);
		      prefsEditor.putFloat(Y, size.y/2);
		      prefsEditor.putFloat(totalWidth, size.x);
		      prefsEditor.putFloat(totalHeight, size.y);
		      prefsEditor.commit();
		      
	        }else{
	          Display d = w.getDefaultDisplay(); 
	          
	          float x = d.getWidth();
		      float y = d.getHeight();
	          prefsEditor.putFloat(X, d.getWidth()/2);
		      prefsEditor.putFloat(Y, d.getHeight()/2);
		      prefsEditor.putFloat(totalWidth, d.getWidth());
		      prefsEditor.putFloat(totalHeight, d.getHeight());
		      prefsEditor.commit();
	        }	
		}
		// ------------------------------------
		
		// Set the main layout, and create the grid view for the animations. 
		setContentView(R.layout.main); 
		CreateGridView gridView = new CreateGridView(this);
		
		super.onCreate(savedInstanceState); 
	}
	
	public void onSetBGClick(View view){
		// Call garbage collector to avoid running out of memory!
		// Constant loading and changing of backgrounds will cause out of memory issues 
		// since it is happening too fast for the garbage collector to handle it.
		// Call the GB manually in order to avoid these types of errors. 
		System.gc();
				
		// Use startActivityForResult() to notify onActivityResult when the intent is complete
		StartLolpaperActivity.changeBGButton = true;
		
		Intent BGintent = new Intent();
		BGintent.setAction(Intent.ACTION_SET_WALLPAPER);
		//startActivityForResult(BGintent,SET_WALLPAPER);
		startActivity(BGintent);
	}
	
	public void onClearClick(View view){
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
		StartLolpaperActivity.clearButton = true;
		// HACK!!--!!--!! THIS MAY NOT WORK ON ALL DEVICES OR VERSIONS
		// Create an empty byte output stream, put it into an input stream, then use that to setStream()
		// For whatever reason, this resets the wallpaper to exactly how it should be, and fast.
		// The proper method of putting the actual image in takes waaaay too long to compress,
		// which is why it is commented out. This may not work as intended, but there might be a way
		// to manually reset the wallpaper the same way setStream() seems to be doing it. Without a hack.
		// Needs further investigation...
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
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
			mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, LolpaperService.class));
			startActivity(mordIntent);
		}
        super.onActivityResult(requestCode, resultCode, data);
    }

}