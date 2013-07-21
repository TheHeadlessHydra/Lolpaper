// *****************************************************************************
//***************************
/*
This is the main service that handles the live wallpaper engine. 
*/
//***************************
// *****************************************************************************

package hyoma.app.lollivewallpaper;



import java.io.IOException;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import hyoma.app.lollivewallpaper.AnimationSystem.Idle;
import hyoma.app.lollivewallpaper.AnimationSystem.Key;
import hyoma.app.lollivewallpaper.StartLolpaperActivity;
import hyoma.app.lollivewallpaper.AnimationSystem;

public class LolpaperService extends WallpaperService {
	Bitmap wallpaperBG; // Holds current bg wallpaper
	WallpaperManager wallpaperManager; // Holds current wallpaper manager
	Drawable wallpaperDrawable; // holds current wallpaper drawable 
	AnimationSystem anim;
	
	@Override
	public Engine onCreateEngine() {
		try {
			anim = new AnimationSystem(getApplicationContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		anim.initiate();
		// Call garbage collector to avoid running out of memory!
		// Constant loading and changing of backgrounds will cause out of memory issues 
		// since it is happening too fast for the garbage collector to handle it.
		System.gc();
		
		// Obtain the current wallpaper background
		wallpaperManager = WallpaperManager.getInstance(this);
		wallpaperDrawable = wallpaperManager.getDrawable();
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
	    
		return new LolpaperEngine();
	}

	private class LolpaperEngine extends Engine { 
		
		// private variables
		private int updateTimer; 
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		private boolean visible = true;
		private boolean touchEnabled;
		private int animationCount = 0;
		Bitmap animFrame;

		public LolpaperEngine() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LolpaperService.this);
			updateTimer = Integer.parseInt(prefs.getString("updatetimer", "20"));
			handler.post(drawRunner);
		}

		// Get the next animation in the frame.
		private void nextFrame(){
			// *****************************************************************************************************
			//String nameOfFrame = "chibimord_frame" + animationCount; // HARD CODED STRING - NEEDS TO BE STANDARDIZED
			//String nameOfFrame = "key_sit_key_sleep_" + animationCount; // HARD CODED STRING - NEEDS TO BE STANDARDIZED
			anim.nextFrame();
			Key base = anim.getBase();
			if (base == null){
				String errorMsg = "ERROR: base is null";
				throw new Error(errorMsg);
			}
			Idle main = base.idleMain;
			if (main == null){
				String errorMsg = "ERROR: idle main is null";
				throw new Error(errorMsg);
			}
			String name = main.name;
			if (name == null){
				String errorMsg = "ERROR: name is null";
				throw new Error(errorMsg);
			}
				
			String nameOfFrame = anim.getBase().idleMain.name + "_" + animationCount;
			animationCount++;
			if(animationCount > 0){
				animationCount = 0;
			}
			int identifier = 0;
			identifier = getResources().getIdentifier(nameOfFrame,"drawable", "hyoma.app.lollivewallpaper");
			if (identifier == 0){
				String errorMsg = "ERROR: Animation frames missing or corrupted";
				throw new Error(errorMsg);
			}
			animFrame = BitmapFactory.decodeResource(getResources(), identifier);
		}
		
		// Called to inform you of the wallpaper becoming visible or hidden. 
		// It is very important that a wallpaper only use CPU while it is visible..
		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		// This is called immediately before a surface is being destroyed. 
		// After returning from this call, you should no longer try to access this surface.
		// If you have a rendering thread that directly accesses the surface,
		// you must ensure that thread is no longer touching the Surface before returning from this function.
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.visible = false;
			handler.removeCallbacks(drawRunner);
			anim.cleanExecutor();
		}

		// Called immediately after any structural change. Always called at least once after creation.
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}
	
		// Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper. 
		// Note that the events you receive here are driven by the actual application the user is interacting with, 
		// so if it is slow you will get fewer move events.
		@Override
		public void onTouchEvent(MotionEvent event) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LolpaperService.this);
			touchEnabled = prefs.getBoolean("touch", true);
			
			float fTouchX = event.getX();
			float fTouchY = event.getY();
			float fTotalHeight = prefs.getFloat(StartLolpaperActivity.totalHeight, -1);
			float fTotalWidth = prefs.getFloat(StartLolpaperActivity.totalWidth, -1);
			
			if(fTotalHeight == -1 && fTotalWidth == -1){
				super.onTouchEvent(event);
				return;
			}
			
			nextFrame();
			
			//Reallocate the touch position to be the centre of the image
			float fXSize = animFrame.getWidth();
			float fYSize = animFrame.getHeight();
			float fPositionX = fTouchX - (fXSize/2);
			float fPositionY = fTouchY - (fYSize/2);
			float endOfImageX = fTouchX + fXSize/2;
			float endOfImageY = fTouchY + fYSize/2;
			
			// Allow touch events to occur only if it is enabled in the prefs and is in preview mode, 
			// and the touch event is in the right area.
			if (	fPositionX > 0 && // Too far left
					fPositionY > 0 && // Too far up
					endOfImageX < fTotalWidth && // Too far right
					endOfImageY < fTotalHeight && // Too far down
					fTouchY < (fTotalHeight - fTotalHeight/6) && // Do not go too low, so as not to touch the buttons at the bottom
					//endOfImageX < (fTotalWidth - fTotalWidth/6)   && // Do not go too far right, or cut off image
					touchEnabled && this.isPreview()) {

				// Set the position of the touch in the static holders
				SharedPreferences.Editor prefsEditor = prefs.edit();
				prefsEditor.putFloat(StartLolpaperActivity.X, fTouchX);
				prefsEditor.putFloat(StartLolpaperActivity.Y, fTouchY);
				prefsEditor.commit();
				
				SurfaceHolder holder = getSurfaceHolder();
				Canvas canvas = null;
				try {
					// lockCanvas():
					// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
					// A null is returned if the surface has not been created or otherwise cannot be edited. 
					// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
					canvas = holder.lockCanvas();
					if (canvas != null) {
						// Calculate where the wallpaper should be in relation to the screen. Since
						// homescreens on android affect size of wallpaper, this is the only way
						// to dynamically find out where to position the wallpaper. 
						float wallpaperX = wallpaperBG.getWidth();
						float positionOfWall = (wallpaperX - fTotalWidth)/2;
						
						// Draw the original wallpaper that was there, then on top of it, draw the animation frame. 
						canvas.drawBitmap(wallpaperBG, -positionOfWall, 0, null);
						canvas.drawBitmap(animFrame, fPositionX, fPositionY, null);
					}
				} finally {
					if (canvas != null)
						holder.unlockCanvasAndPost(canvas);
				}
			}
		super.onTouchEvent(event);
		}

		// Called constantly to redraw the next animation. 
		private void draw() {		
			SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(LolpaperService.this);
			updateTimer = Integer.parseInt(defPrefs.getString("updatetimer", "20"));
			
			// This holder holds the image and allows one to change the pixels. 
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			
			nextFrame();
			
			//Reallocate the touch position to be the centre of the image
			float fTouchX = defPrefs.getFloat(StartLolpaperActivity.X, -1);
			float fTouchY = defPrefs.getFloat(StartLolpaperActivity.Y, -1);
			float fXSize = animFrame.getWidth();
			float fYSize = animFrame.getHeight();
			float fPositionX = fTouchX - (fXSize/2);
			float fPositionY = fTouchY - (fYSize/2);
			
			try {
				// lockCanvas():
				// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
				// A null is returned if the surface has not been created or otherwise cannot be edited. 
				// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
				canvas = holder.lockCanvas();
				if (canvas != null) {	
					// Calculate where the wallpaper should be in relation to the screen. Since
					// homescreens on android affect size of wallpaper, this is the only way
					// to dynamically find out where to position the wallpaper. 
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LolpaperService.this);
					float wallpaperX = wallpaperBG.getWidth();
					float fTotalWidth = prefs.getFloat(StartLolpaperActivity.totalWidth, -1);
					float positionOfWall = (wallpaperX - fTotalWidth)/2;
					
					// Draw the original wallpaper that was there, then on top of it, draw the animation frame. 
					canvas.drawBitmap(wallpaperBG, -positionOfWall, 0, null);
					canvas.drawBitmap(animFrame, fPositionX, fPositionY, null);
				}
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, updateTimer);
			}
		}
	}
}