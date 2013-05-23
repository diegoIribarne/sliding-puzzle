package com.example.slidingpuzzle;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageAdapter extends BaseAdapter {

	public int BLANK_TILE_ID;

	private Context context;
	private Bitmap[] tileImages;
	private int[] tileOrder;
	private int tileWidth;
	private int tileHeight;
	private int gridSize;

    public ImageAdapter(Context c, int gridSize, Bitmap image) {
        context = c;
        tileImages = new Bitmap[gridSize*gridSize];
        tileOrder = new int[gridSize*gridSize];
        this.gridSize = gridSize;
        
        int x = 0;
        int y = 0;
        tileWidth = image.getWidth()/gridSize;
        tileHeight = image.getHeight()/gridSize;
        
        int column = 0;
        int row = 0;
        
        for (int i = 0; i < (gridSize*gridSize) - 1; i++) {
        	column = i % gridSize;
        	if (column == 0 && i > 0) {
        		row++;
        	}
        	tileImages[i] = Bitmap.createBitmap(image, x + (tileWidth*column), y + (tileHeight*row), tileWidth, tileHeight);
        	tileOrder[i] = i;
        } 
        
        // create blank tile
        Bitmap blankTile = BitmapFactory.decodeResource(context.getResources(), R.drawable.black);
        tileImages[gridSize*gridSize - 1] = Bitmap.createScaledBitmap(blankTile, tileWidth, tileHeight, false);
        BLANK_TILE_ID = gridSize*gridSize - 1;
        tileOrder[gridSize*gridSize - 1] = BLANK_TILE_ID;
        
        
    }

    public int getCount() {
        return tileImages.length;
    }

    public Object getItem(int position) {
        return tileImages[position];
    }

    public long getItemId(int position) {
        return 0;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageView imageView;
    	if (convertView == null) {  // if it's not recycled, initialize some attributes
    		imageView = new ImageView(context);
    		imageView.setLayoutParams(new GridView.LayoutParams(this.tileWidth, this.tileHeight));
    		imageView.setBackgroundResource(R.drawable.border);
        } else {
            imageView = (ImageView) convertView;
        }

    	imageView.setImageBitmap(tileImages[tileOrder[position]]);
    	imageView.setTag(tileOrder[position]);
        return imageView;
    }
    
    public int[] getTileOrder() {
    	return tileOrder;
    }
    
    public void setTileOrder(int[] newOrder) {
    	tileOrder = newOrder;
    	this.notifyDataSetChanged();
    }

    // shuffles tiles using Fisher–Yates shuffle
    public void randomizeTileOrder(int numTiles) {
    	
    	Random randomGenerator = new Random();
    	int randomInt, temp;
    	
    	int[] randomizedOrder = this.tileOrder;
    	
    	for (int i = randomizedOrder.length - 1; i > 0; i--) {
    		randomInt = randomGenerator.nextInt(i);
    		
    		temp = randomizedOrder[i];
    		randomizedOrder[i] = randomizedOrder[randomInt];
    		randomizedOrder[randomInt] = temp;
    	}
    	
    	if (!isSolvable(numTiles)) {
	    	Toast.makeText(this.context, "Made puzzle solvable.", Toast.LENGTH_SHORT).show();
    		if (this.tileOrder[0] == this.BLANK_TILE_ID || this.tileOrder[1] == this.BLANK_TILE_ID) {
    			temp = this.tileOrder[numTiles - 1];
    			this.tileOrder[numTiles - 1] = this.tileOrder[numTiles - 2];
    			this.tileOrder[numTiles - 2] = temp;
    		}
    		else {
    			temp = this.tileOrder[0];
    			this.tileOrder[0] = this.tileOrder[1];
    			this.tileOrder[1] = temp;
    		}
    	}
    	
    	this.tileOrder = randomizedOrder;
    }
    
    public boolean isSolvable(int numTiles) {
		if (gridSize % 2 == 1) {
			return (sumInversions(numTiles) % 2 == 0);
		} 
		else {
			return ((sumInversions(numTiles) + this.gridSize - (this.BLANK_TILE_ID % this.gridSize) + 1) % 2 == 0);
		}
	}

    
    public int sumInversions(int numTiles) {
    	int inversions = 0;
    	  
    	for (int i = 0; i < numTiles; i++) {
    		if (this.tileOrder[i] != this.BLANK_TILE_ID) {
    			for (int j = i; j < numTiles; j++) {
        			if (this.tileOrder[i] > this.tileOrder[j]) {
        				inversions++;
        			}
        		}
    		}
    	}
    	
    	return inversions;
	}

}