package com.example.npuzzle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PuzzleActivity extends Activity   {
	
	private int GRID_SIZE = 4;
	GridView imageGrid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle);
		
		imageGrid = createGrid(GRID_SIZE);
		
		if (savedInstanceState != null) {
			if (savedInstanceState.getIntArray("gridOrder") != null) {
				((ImageAdapter) imageGrid.getAdapter()).setTileOrder(savedInstanceState.getIntArray("gridOrder"));
			}
		}
		
		LinearLayout gridLayout = (LinearLayout)this.findViewById(R.id.grid_layout);
		gridLayout.addView(imageGrid);
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageGrid != null) {
			outState.putIntArray("gridOrder", ((ImageAdapter) imageGrid.getAdapter()).getTileOrder());
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.puzzle, menu);
		return true;
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private GridView createGrid(int gridSize) {
		// get screen width and height
		Display display = getWindowManager().getDefaultDisplay();
		
		int screenWidth;
		int screenHeight;
		int actionBarHeight = 0;
		
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}
		
		// get width and height of screen
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		}
		else {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		
		// set maximum puzzle image width and height
		int maxImageWidth = screenWidth - 50;
		int maxImageHeight = screenHeight - actionBarHeight - 50;
		
		Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic1);
		
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		float maxImageRatio = (float)maxImageWidth/maxImageHeight; 
		float imageRatio = (float)imageWidth/imageHeight;
		
		if (imageWidth > maxImageWidth || imageHeight > maxImageHeight) {
			int newWidth;
			int newHeight;
			
			if (imageRatio > maxImageRatio) {
				// scale based on image width
				newWidth = maxImageWidth;
				newHeight = (int) ((float)maxImageWidth/imageWidth*imageHeight);
			}
			else {
				// scale based on image height
				newHeight = maxImageHeight;
				newWidth = (int) ((float)maxImageHeight/imageHeight*imageWidth);
			}
			
			imageWidth = newWidth;
			imageHeight = newHeight;
		}
		
		imageWidth -= imageWidth % gridSize;
		imageHeight -= imageHeight % gridSize;
		
		image = Bitmap.createScaledBitmap(image, imageWidth, imageHeight, false);

		GridView grid = new GridView(this);
		grid.setLayoutParams(new GridView.LayoutParams(image.getWidth(), image.getHeight()));
		
		grid.setNumColumns(gridSize);
		grid.setColumnWidth(imageWidth/gridSize);
		
		grid.setAdapter(new ImageAdapter(this, gridSize, image));
				
		grid.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	ImageAdapter adapter = (ImageAdapter)parent.getAdapter();
		    	int[] tileOrder = adapter.getTileOrder();
		    	int blankTile = adapter.BLANK_TILE_ID;

		    	Toast.makeText(view.getContext(), Integer.toString(tileOrder[position]), Toast.LENGTH_SHORT).show();
		    	if (attemptMove(tileOrder, position, blankTile)) {
		    		adapter.notifyDataSetChanged();
		    	}
		    }
		});
				
		return grid;
	}
	
	public boolean attemptMove(int[] tileOrder, int selectedTilePosition, int blankTile) {
		
		int swapPosition = selectedTilePosition;
		boolean blankTileFound = false;
		
		// if there is a tile to the left of the selected tile
		if (selectedTilePosition % GRID_SIZE != 0) {
			if (tileOrder[selectedTilePosition-1] == blankTile) {
				swapPosition = selectedTilePosition-1;
				blankTileFound = true;
			}
		}
		// if there is a tile to the right of the selected tile
		if ((selectedTilePosition % GRID_SIZE != GRID_SIZE - 1) && (!blankTileFound)) {
			if (tileOrder[selectedTilePosition+1] == blankTile) {
				swapPosition = selectedTilePosition+1;
				blankTileFound = true;
			}
		}
		// if there is a tile below the selected tile
		if ((selectedTilePosition < GRID_SIZE * (GRID_SIZE - 1)) && (!blankTileFound)) {
			if (tileOrder[selectedTilePosition+GRID_SIZE] == blankTile) {
				swapPosition = selectedTilePosition+GRID_SIZE;
				blankTileFound = true;
			}
		}
		// if there is a tile above the selected tile
		if ((selectedTilePosition >= GRID_SIZE) && (!blankTileFound)) {
			if (tileOrder[selectedTilePosition-GRID_SIZE] == blankTile) {
				swapPosition = selectedTilePosition-GRID_SIZE;
				blankTileFound = true;
			}
		}
		
		if (blankTileFound) {
			int temp = tileOrder[selectedTilePosition];
			tileOrder[selectedTilePosition] = tileOrder[swapPosition];
			tileOrder[swapPosition] = temp;
		}
		
		return blankTileFound;
	}
}