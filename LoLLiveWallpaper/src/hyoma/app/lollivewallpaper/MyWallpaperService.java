package hyoma.app.lollivewallpaper;



import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import hyoma.app.lollivewallpaper.SetWallpaperActivity;
public class MyWallpaperService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine { 
		// private variables
		private int animationCount = 0;
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		private Paint paint = new Paint();
		private boolean visible = true;
		private boolean touchEnabled;

		// Constructor
		public MyWallpaperEngine() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
			touchEnabled = prefs.getBoolean("touch", true);
			paint.setAntiAlias(true);
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(10f);
			handler.post(drawRunner);
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
			SetWallpaperActivity.setWidth(width);
			SetWallpaperActivity.setHeight(height);
			super.onSurfaceChanged(holder, format, width, height);
		}

		// Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper. 
		// Note that the events you receive here are driven by the actual application the user is interacting with, 
		// so if it is slow you will get fewer move events.
		@Override
		public void onTouchEvent(MotionEvent event) {
			// Allows touch events to occur only if it is enabled in the prefs and is in preview mode. 
			if (touchEnabled && this.isPreview()) {

				SetWallpaperActivity.setWidth(event.getX());
				SetWallpaperActivity.setHeight(event.getY());
				SurfaceHolder holder = getSurfaceHolder();
				Canvas canvas = null;
				
				BitmapFactory bm = new BitmapFactory(); 
				String animFrame = "chibimord_frame" + animationCount; 
				animationCount++;
				if(animationCount > 39){
					animationCount = 0;
				}
				int identifier = 0;
				identifier = getResources().getIdentifier(animFrame,"drawable", "hyoma.app.lollivewallpaper");
				if(identifier != 0){
					Bitmap img = bm.decodeResource(getResources(), identifier);  					  	
					try {
						// lockCanvas():
						// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
						// A null is returned if the surface has not been created or otherwise cannot be edited. 
						// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
						canvas = holder.lockCanvas();
						if (canvas != null) {
							canvas.drawColor(Color.BLACK);
							canvas.drawBitmap(img, SetWallpaperActivity.getWidth(), SetWallpaperActivity.getHeight(), null);
						}
					} finally {
						if (canvas != null)
							holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		super.onTouchEvent(event);
		}

		// The main look of the wallpaper. 
		private void draw() {

			// This holder holds the image and allows one to change the pixels. 
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			
			BitmapFactory bm = new BitmapFactory(); 
			String animFrame = "chibimord_frame" + animationCount; 
			animationCount++;
			if(animationCount > 39){
				animationCount = 0;
			}
			int identifier = 0;
			identifier = getResources().getIdentifier(animFrame,"drawable", "hyoma.app.lollivewallpaper");
			if(identifier != 0){
				Bitmap img = bm.decodeResource(getResources(), identifier);				  
				try {
					// lockCanvas():
					// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
					// A null is returned if the surface has not been created or otherwise cannot be edited. 
					// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
					canvas = holder.lockCanvas();
					if (canvas != null) {
						canvas.drawColor(Color.BLACK);
						canvas.drawBitmap(img, SetWallpaperActivity.getWidth(), SetWallpaperActivity.getHeight(), null);
					}
				} finally {
					if (canvas != null)
						holder.unlockCanvasAndPost(canvas);
				}
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 20);
			}
		}
	}
}