package com.thirtymatches.fivehundred;

import java.io.File;

import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class FiveHundredBackgroundsApplication extends Application {

	public void onCreate() {
		super.onCreate();
		
		/* Setup Universal Picture Loader */
		File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		        .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75)
		        .denyCacheImageMultipleSizesInMemory()
		        .offOutOfMemoryHandling()
		        .discCacheSize(50 * 1024 * 1024)
		        .discCacheFileCount(100)
		        .discCache(new UnlimitedDiscCache(cacheDir))
		        .build();
		ImageLoader.getInstance().init(config);
	}
	
}
