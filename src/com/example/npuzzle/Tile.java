package com.example.npuzzle;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Tile implements Parcelable {
	
	private Bitmap tileImage;
	private int tileNumber;
	
	public Tile (Bitmap image, int number) {
		tileImage = image;
		tileNumber = number;
	}

	public Bitmap getBitmap() {
		return tileImage;
	}
	
	public int getTileNumber() {
		return tileNumber;
	}
	
	public void setBitmap(Bitmap image) {
		tileImage = image;
	}
	
	public void setTileNumber(int number) {
		tileNumber = number;
	}
	
	public void writeToParcel(Parcel out, int flags) {
        tileImage.writeToParcel(out, flags);
        out.writeInt(tileNumber);
    }
   
    public static final Parcelable.Creator<Tile> CREATOR = new Parcelable.Creator<Tile>() {
        public Tile createFromParcel(Parcel in) { return new Tile(in); }
        public Tile[] newArray(int size) { return new Tile[size]; }
    };
   
    private Tile (Parcel in) {
    	in.setDataPosition(0); 
    	tileImage = Bitmap.CREATOR.createFromParcel(in);
    	tileNumber = in.readInt();
    }
   
    public int describeContents() { 
    	return 0; 
    }

}
