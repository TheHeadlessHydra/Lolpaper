package hyoma.app.lollivewallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class WallpaperChangedReciever extends BroadcastReceiver  {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		String action = intent.getAction();
		if(action.equals("android.intent.action.WALLPAPER_CHANGED")){
			if(StartLolpaperActivity.changeBGButton == true){
				Intent mordIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);  
				mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(ctx, LolpaperService.class));
				mordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Needed to start an intent from outside an activity. 
				ctx.startActivity(mordIntent);
				StartLolpaperActivity.changeBGButton = false;
			}
			else if (StartLolpaperActivity.clearButton == true){
		    	// Reset the position to the centre of the screen. 
		  		SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(ctx); 
		  		SharedPreferences.Editor prefsEditor = defPrefs.edit();
		  			
		  		Float totalHeight = defPrefs.getFloat(StartLolpaperActivity.totalHeight,-1);
		  		Float totalWidth = defPrefs.getFloat(StartLolpaperActivity.totalWidth,-1);
		  		
		  		if(totalHeight != -1 || totalWidth != -1){
		  			prefsEditor.putFloat(StartLolpaperActivity.X, totalWidth/2);
			  		prefsEditor.putFloat(StartLolpaperActivity.Y, totalHeight/2);
		  		}
		  		prefsEditor.commit();
		    	StartLolpaperActivity.clearButton = false;
			}
	    }
	}
};