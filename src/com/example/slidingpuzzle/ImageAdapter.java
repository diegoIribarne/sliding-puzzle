package com.example.slidingpuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	public int BLANK_TILE_ID;

	private Context context;
	private Bitmap[] tileImages;
	private int[] tileOrder;
	private int tileWidth;
	private int tileHeight;

    public ImageAdapter(Context c, int gridSize, Bitmap image) {
        context = c;
        tileImages = new Bitmap[gridSize*gridSize];
        tileOrder = new int[gridSize*gridSize];
        
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
}