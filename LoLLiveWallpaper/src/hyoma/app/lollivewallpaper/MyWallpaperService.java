package hyoma.app.lollivewallpaper;


import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import hyoma.app.lollivewallpaper.MyApplication;

public class MyWallpaperService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine {
		private int animationCount = 0;
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}

		};
		private List<MyPoint> circles;
		private Paint paint = new Paint();
		private int width;
		int height;
		private boolean visible = true;
		private int maxNumber;
		private boolean touchEnabled;

		public MyWallpaperEngine() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
			maxNumber = Integer.valueOf(prefs.getString("numberOfCircles", "4"));
			touchEnabled = prefs.getBoolean("touch", false);
			circles = new ArrayList<MyPoint>();
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
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			this.width = width;
			this.height = height;
			super.onSurfaceChanged(holder, format, width, height);
		}

		// Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper. 
		// Note that the events you receive here are driven by the actual application the user is interacting with, 
		// so if it is slow you will get fewer move events.
		@Override
		public void onTouchEvent(MotionEvent event) {
			if (touchEnabled) {

				float x = event.getX();
				float y = event.getY();
				SurfaceHolder holder = getSurfaceHolder();
				Canvas canvas = null;
				try {
					canvas = holder.lockCanvas();
					if (canvas != null) {
						canvas.drawColor(Color.BLACK);
						circles.clear();
						circles.add(new MyPoint(String.valueOf(circles.size() + 1), x, y));
						drawCircles(canvas, circles);
					}
				} finally {
					if (canvas != null)
						holder.unlockCanvasAndPost(canvas);
				}
				super.onTouchEvent(event);
			}
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
				
				
				//AnimationDrawable chibiMordAnimation;
				//ImageView chibiMord = (ImageView) findViewById(R.id.chibi_mord);
				//chibiMord.setBackgroundResource(R.drawable.chibimord);
				//chibiMordAnimation = (AnimationDrawable) chibiMord.getBackground();
				  
				try {
					// lockCanvas():
					// Start editing the pixels in the surface. The returned Canvas can be used to draw into the surface's bitmap. 
					// A null is returned if the surface has not been created or otherwise cannot be edited. 
					// You will usually need to implement Callback.surfaceCreated to find out when the Surface is available for use.
					canvas = holder.lockCanvas();
					if (canvas != null) {
						canvas.drawColor(Color.BLACK);
						canvas.drawBitmap(img,200,200, null);
						/*
						if (circles.size() >= maxNumber) {
							circles.clear();
						}
						int x = (int) (width * Math.random());
						int y = (int) (height * Math.random());
						circles.add(new MyPoint(String.valueOf(circles.size() + 1), x, y));
						drawCircles(canvas, circles);
						*/
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

		// Surface view requires that all elements are drawn completely
		private void drawCircles(Canvas canvas, List<MyPoint> circles) {
			canvas.drawColor(Color.BLACK);
			for (MyPoint point : circles) {
				canvas.drawCircle(point.x, point.y, 20.0f, paint);
			}
		}
	}

}