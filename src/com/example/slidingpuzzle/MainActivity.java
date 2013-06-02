package com.example.slidingpuzzle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	
	private static final int CAMERA_REQUEST_CODE = 1000;
	private static final int GALLERY_REQUEST_CODE = 2000;
	public Uri mCapturedImageURI;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Disable all animations
		getWindow().setWindowAnimations(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		if (item.getItemId() == R.id.action_exit) {
			this.finish();
		}
		*/
		return true;
	}
	
	public void selectIncludedImage(View v) {
		Intent intent = new Intent(this, SelectIncludedImageActivity.class);
		startActivity(intent);
	}
	
	public void selectFromGallery(View v) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(i, GALLERY_REQUEST_CODE); 
	}
	
	public void useCamera(View v) {
        ContentValues values = new ContentValues();  
        values.put(MediaStore.Images.Media.TITLE, "temp.jpg");  
        this.mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.mCapturedImageURI);  
        startActivityForResult(intent, CAMERA_REQUEST_CODE); 
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	    super.onActivityResult(requestCode, resultCode, data); 

	    if (resultCode == RESULT_OK) {
	    	Uri selectedImage = null;
		    
	    	switch(requestCode) { 
		    case GALLERY_REQUEST_CODE:
		        selectedImage = data.getData();
		        break;
		    case CAMERA_REQUEST_CODE:
		    	selectedImage = this.mCapturedImageURI;
		    	break;
		    };
		    
		    String[] filePathColumn = {MediaStore.Images.Media.DATA};
	
	        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	        cursor.moveToFirst();
	
	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	        String filePath = cursor.getString(columnIndex);
	        cursor.close();
	    }
	}
}