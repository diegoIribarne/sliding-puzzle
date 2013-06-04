package com.example.slidingpuzzle;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
	private GridView imageGrid;
	private Object puzzleImageSource;
	private boolean solved = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle);
		
		// Disable all animations
		getWindow().setWindowAnimations(0);
		
		// Get grid size/difficulty, default 4x4
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
	    GRID_SIZE = prefs.getInt("gridSize", 4);
	       
		// get image ID/path from intent
		Integer imageID = getIntent().getIntExtra("image", 0);
		String imagePath = getIntent().getStringExtra("image");
		if (imageID != 0) {
			this.puzzleImageSource = imageID;
		}
		else {
			this.puzzleImageSource = imagePath;
		}
		
		try {
			this.imageGrid = createGrid();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LinearLayout gridLayout = (LinearLayout)this.findViewById(R.id.grid_layout);
		
		// if resuming previous game, load previous tile order and number of moves made
		if (savedInstanceState != null && (savedInstanceState.getIntArray("gridOrder") != null) && !prefs.getBoolean("startNewGame", false)) {
			((ImageAdapter) this.imageGrid.getAdapter()).setTileOrder(savedInstanceState.getIntArray("gridOrder"));
			this.numberOfMoves = savedInstanceState.getInt("movesMade", 0);
			this.solved = savedInstanceState.getBoolean("solved", false);
			gridLayout.addView(this.imageGrid);
		}
		// if starting new game, randomize tile order
		else {
			gridLayout.addView(this.imageGrid);
	        countdownAndRandomizePuzzle(3);
		}
		
		this.updateNumberOfMoves();
				
	}
	
	private void countdownAndRandomizePuzzle (final int seconds) {
		if (seconds > 0) {
	        displayCountdownToast(Integer.toString(seconds));
	        
			Handler mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
	            public void run() {
	            	countdownAndRandomizePuzzle(seconds-1);
	            }
	        }, 1000);
		}
		else {
			((ImageAdapter) this.imageGrid.getAdapter()).randomizeTileOrder(this.GRID_SIZE*this.GRID_SIZE);
		}
	}
	
	// displays toast then cancels after .5 seconds
	private void displayCountdownToast (String text) {
		final Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();

	    Handler handler = new Handler();
	        handler.postDelayed(new Runnable() {
	           @Override
	           public void run() {
	               toast.cancel(); 
	           }
	    }, 500);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageGrid != null) {
			outState.putIntArray("gridOrder", ((ImageAdapter) this.imageGrid.getAdapter()).getTileOrder());
			outState.putInt("movesMade", this.numberOfMoves);
			outState.putBoolean("solved", this.solved);
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
	
	private GridView createGrid() throws IOException {
		Bitmap puzzleImage = null;
		
		puzzleImage = loadScaledImage();
		int imageWidth = puzzleImage.getWidth();
		int imageHeight = puzzleImage.getHeight();
		
		GridView grid = new GridView(this);
		grid.setLayoutParams(new GridView.LayoutParams(imageWidth, imageHeight));
		
		grid.setNumColumns(this.GRID_SIZE);
		grid.setColumnWidth(imageWidth/this.GRID_SIZE);
		
		grid.setAdapter(new ImageAdapter(this, this.GRID_SIZE, puzzleImage));
		puzzleImage.recycle();
				
		grid.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	ImageAdapter adapter = (ImageAdapter)parent.getAdapter();
		    	int[] tileOrder = adapter.getTileOrder();
		    	int blankTile = adapter.BLANK_TILE_ID;

		    	if (attemptMove(tileOrder, position, blankTile)) {
		    		adapter.notifyDataSetChanged();
		    	}
		    	
		    	checkPuzzleSolved(tileOrder);
		    }
		});
				
		return grid;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Bitmap loadScaledImage () throws IOException {
		// get screen width and height
		Display display = getWindowManager().getDefaultDisplay();

		int screenWidth;
		int screenHeight;
		int actionBarHeight = 0;

		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) + 80;
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
		int maxImageHeight = screenHeight - actionBarHeight - 10;
		
		// load scaled down version of bitmap
		Bitmap sampleImage = null;
		if (this.puzzleImageSource instanceof Integer) {
			sampleImage = ImageLoader.decodeSampledBitmap(getResources(), 
					(Integer) this.puzzleImageSource, maxImageWidth, maxImageHeight);
		}
		else if (this.puzzleImageSource instanceof String) {
			sampleImage = ImageLoader.decodeSampledBitmap((String) this.puzzleImageSource, 
					maxImageWidth, maxImageHeight);

			int angle = getRotationAngle();
	        if (angle != 0) {
		        Matrix mat = new Matrix();
		        mat.postRotate(angle);
		        sampleImage = Bitmap.createBitmap(sampleImage, 0, 0, sampleImage.getWidth(), sampleImage.getHeight(), mat, true);
	        }
		}
		else {
			this.finish();
		}
		
		int imageWidth = sampleImage.getWidth();
		int imageHeight = sampleImage.getHeight();

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

		imageWidth -= imageWidth % this.GRID_SIZE;
		imageHeight -= imageHeight % this.GRID_SIZE;
		
		return Bitmap.createScaledBitmap(sampleImage, imageWidth, imageHeight, false);
	}
	
	private int getRotationAngle() throws IOException {
		ExifInterface exif = new ExifInterface((String) this.puzzleImageSource);
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		
		int angle = 0;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { angle = 90; } 
		else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) { angle = 180; } 
		else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) { angle = 270; }
		
		return angle;
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
			
			if (!this.solved) {
				this.numberOfMoves++;
				updateNumberOfMoves();
			}
		}
		
		return blankTileFound;
	}

	public boolean checkPuzzleSolved(int[] tileOrder) {
	
		if (this.solved) { return true; }
		
		int numTiles = tileOrder.length;
		for (int i = 0; i < numTiles; i++) {
			if (i != tileOrder[i]) {
				return false;
			}
		}
		
		Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
		this.solved = true;
		
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
 	   	intent.putExtra("startNewGame", true);
		
 	   	if (this.puzzleImageSource instanceof Integer) {
 	   		intent.putExtra("image", (Integer) this.puzzleImageSource);
 	   	}
 	   	else if (this.puzzleImageSource instanceof String) {
 	   		intent.putExtra("image", (String) this.puzzleImageSource);
 	   	}
 	   	
		startActivity(intent);
		this.imageGrid = null;
 	   	this.finish();
	}
}