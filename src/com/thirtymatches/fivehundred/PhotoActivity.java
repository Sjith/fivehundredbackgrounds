package com.thirtymatches.fivehundred;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class PhotoActivity extends Activity {

	private static final String TAG = "PhotoActivity";

	private ProgressBar loading;
	private ImageView imageView;
	private String imageUrl;
	private DisplayImageOptions options;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);
		
		// Reference to UI elements
		loading = (ProgressBar) findViewById(R.id.activity_photo_loading);
		imageView = (ImageView) findViewById(R.id.activity_photo_photo);
		
		// Options for displaying an image
		options = new DisplayImageOptions.Builder().cacheInMemory()
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.cacheOnDisc().build();		
		
		// Switch to HD
		String originalImageUrl = getIntent().getStringExtra("image_url");
		imageUrl = originalImageUrl.replace("2.jpg", "4.jpg");
		
		new LoadImageTask().execute(imageUrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.photo_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_wallpaper:
	        	setImageAsWallpaper();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void setImageAsWallpaper() {
		try {
			WallpaperManager wpm = WallpaperManager.getInstance(getApplicationContext());
			Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
			wpm.setBitmap(bitmap);
			Toast.makeText(getApplicationContext(), "Image set as wallpaper :) ", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	
	private class LoadImageTask extends AsyncTask<String, Void, Void> {
		
		public Void doInBackground(final String... src ) {
			runOnUiThread(new Runnable() {
			     public void run() {
					ImageLoader.getInstance().displayImage(src[0], imageView, options, new SimpleImageLoadingListener() {
						
						@Override
						public void onLoadingStarted() {
							super.onLoadingStarted();
							loading.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							loading.setVisibility(View.GONE);
						}
					});
			     }
			});
			
			return null;
		}

	}
	
}
