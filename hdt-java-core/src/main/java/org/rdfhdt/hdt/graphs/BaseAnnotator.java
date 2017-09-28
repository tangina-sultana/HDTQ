package org.rdfhdt.hdt.graphs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.bitmap.BitmapFactory;
import org.rdfhdt.hdt.listener.ProgressListener;

public abstract class BaseAnnotator implements Annotator{
	protected GraphInformationImpl graphInformation;
	
	public BaseAnnotator(GraphInformationImpl graphInformation) {
		this.graphInformation = graphInformation;
	}
	
	/**
	 * Save to HDT file
	 */
	@Override
	public void save(OutputStream output, ProgressListener iListener) throws IOException {
		ArrayList<Bitmap> bitmaps = graphInformation.getBitmaps();
		for(Bitmap bitmap : bitmaps) {
			bitmap.save(output, iListener);
		}
	}
	
	/**
	 * Load from HDT file
	 */
	@Override
	public void load(InputStream input, ProgressListener iListener) throws IOException {
		ArrayList<Bitmap> bitmaps = this.graphInformation.getBitmaps();
		
		Bitmap bitmap;
		for(long i = 0; i < getNumberOfBitmapsToLoad(); i++) {
			//bitmap = new Bitmap375();
			bitmap = BitmapFactory.createBitmap(input);
			bitmap.load(input, iListener);
			bitmaps.add(bitmap);
		}
	}

	/**
	 * Returns the number of bitmaps to load when loading HDT from file
	 * @return number of bitmaps
	 */
	protected abstract long getNumberOfBitmapsToLoad();
}
