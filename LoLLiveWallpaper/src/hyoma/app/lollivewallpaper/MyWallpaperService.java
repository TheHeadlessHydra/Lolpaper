package hyoma.app.lollivewallpaper;



import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;
import hyoma.app.lollivewallpaper.SetWallpaperActivity;

public class MyWallpaperService extends WallpaperService {
	Bitmap wallpaperBG; // Holds current bg wallpaper
	WallpaperManager wallpaperManager; // Holds current wallpaper manager
	Drawable wallpaperDrawable; // holds current wallpaper drawable 
	
	@Override
	public Engine onCreateEngine() {
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
	    
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine { 
		
		// private variables
		private final int updateTimer = 20; 
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

		public MyWallpaperEngine() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
			touchEnabled = prefs.getBoolean("touch", true);
			handler.post(drawRunner);			
		}

		// Get the next animation in the frame.
		private void nextFrame(){
			// *****************************************************************************************************
			String nameOfFrame = "chibimord_frame" + animationCount; // HARD CODED STRING - NEEDS TO BE STANDARDIZED
			animationCount++;
			if(animationCount > 39){
				animationCount = 0;
			}
			BitmapFactory bm = new BitmapFactory(); 
			int identifier = 0;
			identifier = getResources().getIdentifier(nameOfFrame,"drawable", "hyoma.app.lollivewallpaper");
			if (identifier == 0){
				String errorMsg = "ERROR: Animation frames missing or corrupted";
				throw new Error(errorMsg);
			}
			animFrame = bm.decodeResource(getResources(), identifier);
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
		}

		// Called immediately after any structural change. Always called at least once after creation.
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			//SetWallpaperActivity.setWidth(width);
			//SetWallpaperActivity.setHeight(height);
			super.onSurfaceChanged(holder, format, width, height);
		}
	
		// Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper. 
		// Note that the events you receive here are driven by the actual application the user is interacting with, 
		// so if it is slow you will get fewer move events.
		@Override
		public void onTouchEvent(MotionEvent event) {
			float fTouchX = event.getX();
			float fTouchY = event.getY();
			float fTotalHeight = SetWallpaperActivity.getTotalHeight();
			float fTotalWidth = SetWallpaperActivity.getTotalWidth();
			
			nextFrame();
			
			//Reallocate the touch position to be the centre of the image
			float fXSize = animFrame.getWidth();
			float fYSize = animFrame.getHeight();
			float fPositionX = fTouchX - (fXSize/2);
			float fPositionY = fTouchY - (fYSize/2);
			
			// Allow touch events to occur only if it is enabled in the prefs and is in preview mode, 
			// and the touch event is in the right area.
			if (	fPositionY < (fTotalHeight - fTotalHeight/6) && // Do not go too low, so as not to touch the buttons at the bottom
					fPositionY > 0                 			  && // Do not go too high, or cut off top of image
					fPositionX < (fTotalWidth - fTotalWidth/6)   && // Do not go too far right, or cut off image
					touchEnabled && this.isPreview()) {

				// Set the position of the touch in the static holders
				SetWallpaperActivity.setWidth(fPositionX);
				SetWallpaperActivity.setHeight(fPositionY);
				
				SurfaceHolder holder = getSurfaceHolder();
				Canvas canvas = null;
									  	
				try {
					// lockCanvas():
					// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
					// A null is returned if the surface has not been created or otherwise cannot be edited. 
					// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
					canvas = holder.lockCanvas();
					if (canvas != null) {
						// Draw the original wallpaper that was there, then on top of it, draw the animation frame. 
						canvas.drawBitmap(wallpaperBG, -256, 0, null); // I don't know why it needs to be -256 for it to be aligned properly.
						canvas.drawBitmap(animFrame, SetWallpaperActivity.getWidth(), SetWallpaperActivity.getHeight(), null);
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
			// This holder holds the image and allows one to change the pixels. 
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			
			nextFrame();
			
			try {
				// lockCanvas():
				// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
				// A null is returned if the surface has not been created or otherwise cannot be edited. 
				// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
				canvas = holder.lockCanvas();
				if (canvas != null) {	
					// Draw the original wallpaper that was there, then on top of it, draw the animation frame. 
					canvas.drawBitmap(wallpaperBG, -256, 0, null); // I don't know why it needs to be -256 for it to be aligned properly.
					canvas.drawBitmap(animFrame, SetWallpaperActivity.getWidth(), SetWallpaperActivity.getHeight(), null);
					
					// store the location of where the animation is in a preference so that the next time 
					// the app is launched, the location does not change. 
					SharedPreferences prefs = getApplicationContext().getSharedPreferences(SetWallpaperActivity.locationPref, 0);
					SharedPreferences.Editor prefsEditor = prefs.edit();
					prefsEditor.putFloat(SetWallpaperActivity.X, SetWallpaperActivity.getWidth());
					prefsEditor.putFloat(SetWallpaperActivity.Y, SetWallpaperActivity.getHeight());
					prefsEditor.commit();
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