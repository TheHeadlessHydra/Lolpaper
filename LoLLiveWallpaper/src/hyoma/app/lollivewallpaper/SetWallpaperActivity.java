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
	static float Measuredwidth = 0;
	static float Measuredheight = 0;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
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