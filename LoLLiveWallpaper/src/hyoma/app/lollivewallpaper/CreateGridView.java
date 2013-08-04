// *****************************************************************************
//***************************
/*
This class starts, creates, and handles the list of animations that will be available
to the user.
*/
//***************************
// *****************************************************************************



package hyoma.app.lollivewallpaper;

import hyoma.app.lollivewallpaper.AnimationsImageAdapter;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class CreateGridView {
	// Main static used to reference the current xml file used to create the animation system
	public static String currentXML = "raw/animation_list_riven";
	
	final Activity mainAct;
	CreateGridView(final Activity act){
		mainAct = act;
		GridView gridView;
	
		gridView = (GridView) act.findViewById(R.id.animationview);
	
		// Provides the grids data
		gridView.setAdapter(new AnimationsImageAdapter(act));
	
		gridView.setOnItemClickListener(new OnItemClickListener() {
		     public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	 if(position == 0){ onChibiMordClick(); }
		    	 if(position == 1){ onRivenClick(); }
		     }
		});
	}
	
	public void onChibiMordClick() {
		CreateGridView.currentXML = "raw/animation_list_chibimord";
		Intent mordIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(mainAct, LolpaperService.class));
		mainAct.startActivity(mordIntent);
	}
	public void onRivenClick() {
		CreateGridView.currentXML = "raw/animation_list_riven";
		Intent rivenIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		rivenIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(mainAct, LolpaperService.class));
		mainAct.startActivity(rivenIntent);
	}
	
};