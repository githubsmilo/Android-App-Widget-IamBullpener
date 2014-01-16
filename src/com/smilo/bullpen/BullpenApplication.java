package com.smilo.bullpen;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;

public class BullpenApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .threadPoolSize(1)
            .build();
        ImageLoader.getInstance().init(config);
	}
}
