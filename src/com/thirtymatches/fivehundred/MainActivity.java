package com.thirtymatches.fivehundred;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private ProgressBar loading;
	private int width;
	private ArrayList<String> photos;
	private DisplayImageOptions options;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loading = (ProgressBar) findViewById(R.id.activity_main_loading);
		
		// Check the user has accepted the EULA
		checkEULA();
		
		// Device screen size
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;
		
		// Options for displaying an image
		options = new DisplayImageOptions.Builder().cacheInMemory()
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.cacheOnDisc()
				.build();
		
		// Get first batch of photos
		new ReadJSONFeedTask().execute(Constants.DEFAULT_URL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public String readJSONFeed(String URL) {
		StringBuilder builder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		
		try {
			HttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				InputStream content = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Toast.makeText(getApplicationContext(), "Internet Connection Lost :(", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Failed to download data.");
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		
		return builder.toString();
	}
	
	private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... urls) {
			return readJSONFeed(urls[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			loading.setVisibility(View.GONE);
			
			try {
				
				JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
				JSONArray photosArray = object.getJSONArray("photos");
				
				// Parse and save URL of photos
				photos = new ArrayList<String>();
				for(int i=0;i<photosArray.length();i++) {
					JSONObject o = photosArray.getJSONObject(i);
					photos.add(o.getString("image_url"));
				}
				
				// Set ListView adapter
				GridView gridView = (GridView) findViewById(R.id.gridview);
				gridView.setAdapter(new ItemAdapter());
				gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
						intent.putExtra("image_url", photos.get(position));
						startActivity(intent);
					}
				});
				
			} catch(Exception e) {
				Log.e(TAG, "Error!" + e);
			}
		}
		
	}
	
	private class ItemAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return photos.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_list_image, parent, false);
				imageView.setLayoutParams(new GridView.LayoutParams(width/3 - 6, width/3));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView = (ImageView) convertView;
			}
			
			// Load and display image
			ImageLoader.getInstance().displayImage(photos.get(position), imageView, options);

			return imageView;
			
		}
	}
	
	private void checkEULA() {
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		boolean eulaAccepted = preferences.getBoolean("eula_ok", false);
		
		if(!eulaAccepted) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Agreement")
					.setMessage("To comply with 500px\'s License Conditions, I promise not to cache or store any photos or images obtained for more than 24 hours.")
					.setPositiveButton("I Agree",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Editor editor = preferences.edit();
									editor.putBoolean("eula_ok", true);
									editor.commit();
								}
							})
					.setNegativeButton("Exit",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									finish();
								}
							});
			builder.create().show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_abstract:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Abstract");
	    	System.out.println(Constants.URL_FOR_CATEGORY + "Abstract");
	    	return true;
	    case R.id.menu_animals:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Animals");
	        	return true;
	    case R.id.menu_family:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Family");
        	return true;
	    case R.id.menu_fashion:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Fashion");
	    	return true;
	    case R.id.menu_films:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Films");
	    	return true;
	    case R.id.menu_food:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Food");
	    	return true;
	    case R.id.menu_journalism:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Journalism");
	    	return true;
	    case R.id.menu_landscapes:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Landscapes");
	    	return true;
	    case R.id.menu_nature:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Nature");
	    	return true;
	    case R.id.menu_people:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "People");
	    	return true;
	    case R.id.menu_sports:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Sports");
	    	return true;
	    case R.id.menu_street:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Street");
	    	return true;
	    case R.id.menu_transportation:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Transportation");
	    	return true;
	    case R.id.menu_travel:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Travel");
	    	return true;
	    case R.id.menu_underwater:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Underwater");
	    	return true;
	    case R.id.menu_wedding:
	    	new ReadJSONFeedTask().execute(Constants.URL_FOR_CATEGORY + "Wedding");
	    	return true;
	    }    
	    
	    return true;
	}
	
	
}
