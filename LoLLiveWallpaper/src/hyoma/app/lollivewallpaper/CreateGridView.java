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
	final Activity mainAct;
	CreateGridView(final Activity act){
		mainAct = act;
		GridView gridView;
	
		gridView = (GridView) act.findViewById(R.id.animationview);
	
		// Provides the grids data
		gridView.setAdapter(new AnimationsImageAdapter(act));
	
		gridView.setOnItemClickListener(new OnItemClickListener() {
		     public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	 if(position == 0){ oncChibiMordClick(); }
		    	 if(position == 1){ oncChibiMordClick(); }
		    	 if(position == 2){ oncChibiMordClick(); }
		     }
		});
	}
	
	public void oncChibiMordClick() {
		Intent mordIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		mordIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(mainAct, LolpaperService.class));
		mainAct.startActivity(mordIntent);
	}
	
};