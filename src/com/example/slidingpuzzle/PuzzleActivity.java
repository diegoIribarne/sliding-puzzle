package com.example.slidingpuzzle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleActivity extends Activity   {
	
	public static final String PREFS_NAME = "Prefs";
	private int GRID_SIZE;
	private int numberOfMoves = 0;
	GridView imageGrid;
	int puzzleImageSource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle);
		
		// Disable all animations
		getWindow().setWindowAnimations(0);
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		
	    GRID_SIZE = prefs.getInt("gridSize", 4);
	       
		// load image from intent
		this.puzzleImageSource = getIntent().getIntExtra("image", 0);
		
		this.imageGrid = createGrid(GRID_SIZE);
		LinearLayout gridLayout = (LinearLayout)this.findViewById(R.id.grid_layout);
		
		// if resuming previous game, load previous tile order and number of moves made
		if (savedInstanceState != null && (savedInstanceState.getIntArray("gridOrder") != null) && !prefs.getBoolean("sizeChanged", false)) {
			((ImageAdapter) this.imageGrid.getAdapter()).setTileOrder(savedInstanceState.getIntArray("gridOrder"));
			this.numberOfMoves = savedInstanceState.getInt("movesMade", 0);
		}
		// if starting new game, randomize tile order
		else {
			((ImageAdapter) this.imageGrid.getAdapter()).randomizeTileOrder(this.GRID_SIZE*this.GRID_SIZE);
		}
		
		this.updateNumberOfMoves();
		
		gridLayout.addView(this.imageGrid);
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageGrid != null) {
			outState.putIntArray("gridOrder", ((ImageAdapter) imageGrid.getAdapter()).getTileOrder());
			outState.putInt("movesMade", this.numberOfMoves);
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
				overridePendingTransition(0, 0);
				return true;
	        case R.id.action_difficulty:
	        	openDifficultyDialog();
	            return true;
	        case R.id.action_new_game:
	        	newGame();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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
		int maxImageWidth = screenWidth - 10;
		int maxImageHeight = screenHeight - actionBarHeight - 90;
		/*
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			maxImageHeight -= 70;
		}
		*/
		
		Bitmap puzzleImage = BitmapFactory.decodeResource(this.getResources(), this.puzzleImageSource);
		int imageWidth = puzzleImage.getWidth();
		int imageHeight = puzzleImage.getHeight();
		
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

		puzzleImage = Bitmap.createScaledBitmap(puzzleImage, imageWidth, imageHeight, false);

		GridView grid = new GridView(this);
		grid.setLayoutParams(new GridView.LayoutParams(imageWidth, imageHeight));
		
		grid.setNumColumns(gridSize);
		grid.setColumnWidth(imageWidth/gridSize);
		
		grid.setAdapter(new ImageAdapter(this, gridSize, puzzleImage));
				
		grid.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	ImageAdapter adapter = (ImageAdapter)parent.getAdapter();
		    	int[] tileOrder = adapter.getTileOrder();
		    	int blankTile = adapter.BLANK_TILE_ID;

		    	if (attemptMove(tileOrder, position, blankTile)) {
		    		adapter.notifyDataSetChanged();
		    	}
		    	
		    	if (puzzleSolved(tileOrder)) {
			    	Toast.makeText(view.getContext(), "You win!", Toast.LENGTH_SHORT).show();
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
			
			this.numberOfMoves++;
			updateNumberOfMoves();
		}
		
		return blankTileFound;
	}

	public boolean puzzleSolved(int[] tileOrder) {
	
		int numTiles = tileOrder.length;
		for (int i = 0; i < numTiles; i++) {
			if (i != tileOrder[i]) {
				return false;
			}
		}
		
		return true;
	}

	private void updateNumberOfMoves() {
		TextView textView = (TextView)this.findViewById(R.id.moves_display);
		textView.setText("Moves made: " + this.numberOfMoves);
	}
	
	public void openDifficultyDialog() {
		SharedPreferences settings = getSharedPreferences(PuzzleActivity.PREFS_NAME, 0);
 	   	 	   	
 	   	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 	   	dialog.setTitle("Select Difficulty: ");
 	   	dialog.setSingleChoiceItems(R.array.difficulty_array, settings.getInt("gridSize", 2) - 3, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
 	   	dialog.setPositiveButton("Save", new OnClickListener() {
 	   		public void onClick(DialogInterface dialog, int which) {
 	          //here you can add functions
 	   			ListView lw = ((AlertDialog)dialog).getListView();
 	   			int checkedItem = lw.getCheckedItemPosition();

 	   			changeDifficulty(checkedItem+3);
 	   		} 
 	   	});
 	   dialog.setNegativeButton("Cancel", new OnClickListener() {
	   		public void onClick(DialogInterface dialog, int which) {
	   			dialog.dismiss();
	   		} 
	   	});
 	   
 	   dialog.show();

	}
	
	public void changeDifficulty(int newGridSize) {
		SharedPreferences settings = getSharedPreferences(PuzzleActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
	 	editor.putInt("gridSize", newGridSize);
	 	editor.commit();
	 	   	
	 	if (settings.getInt("gridSize", 0) != this.GRID_SIZE) {
	 		newGame();
	 	}
	}

	private void newGame() {
		Intent intent = new Intent(this, PuzzleActivity.class);
 	   	intent.putExtra("sizeChanged", true);
		intent.putExtra("image", this.puzzleImageSource);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 	   	startActivity(intent);
 	   	this.finish();
 	   	//overridePendingTransition(0, 0);
	}
}