package org.rdfhdt.hdt.graphs;

import java.util.ArrayList;
import java.util.HashSet;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.bitmap.Bitmap.BitmapType;
import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.bitmap.BitmapRoaring64;
import org.rdfhdt.hdt.compact.bitmap.ModifiableBitmap;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.ControlInfo.Type;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIterator;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIteratorG;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIteratorYFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIteratorYGFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIteratorZFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedGraphsIteratorZGFOQ;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;

public class GraphAnnotator extends BaseAnnotator {

	private int nextTriplePosition;
	private BitmapType typeBitmap;
	
	public GraphAnnotator(GraphInformationImpl graphInformation) {
		super(graphInformation);
		typeBitmap=BitmapType.TYPE_BITMAP_PLAIN;
	}
	public GraphAnnotator(GraphInformationImpl graphInformation,BitmapType typeBitmap ) {
		super(graphInformation);
		this.typeBitmap=typeBitmap;
	}

	/**
	 * Load one triple.
	 */
	@Override
	public void load(HashSet<Integer> graphsOfTriple) {
		if(!bitmapsAreInitialized()) {
			initializeBitmaps();
		}
		
		ArrayList<Bitmap> bitmaps = graphInformation.getBitmaps();

		for(int graph : graphsOfTriple) {
			((ModifiableBitmap)bitmaps.get(graph - 1)).set(nextTriplePosition, true);
		}
		nextTriplePosition++;
	}
	
	/**
	 * Set up bitmaps, one for each graph
	 * @param graphInformation
	 */
	private void initializeBitmaps() {
		ArrayList<Bitmap> bitmaps = graphInformation.getBitmaps();
		long numberOfGraphs = graphInformation.getNumberOfGraphs();
		for(int i = 0; i < numberOfGraphs; i++) {
			//bitmaps.add(new Bitmap375(graphInformation.getNumberOfTriples()));
			if (typeBitmap==BitmapType.TYPE_BITMAP_PLAIN){
				bitmaps.add(new Bitmap375(graphInformation.getNumberOfTriples()));
			}
			else
				bitmaps.add(new BitmapRoaring64());
		}
		nextTriplePosition = 0;
	}

	/**
	 * Returns true if bitmaps are already initialized
	 */
	private boolean bitmapsAreInitialized() {
		return graphInformation.getBitmaps().size() > 0;
	}
	
	/**
	 * Returns the number of bitmaps to load when loading HDT from file
	 * @return number of bitmaps
	 */
	@Override
	protected long getNumberOfBitmapsToLoad() {
		return graphInformation.getNumberOfGraphs();
	}
	
	/**
	 * Bitmaps can be too big (because actual number of triple was unknown), 
	 * trim them to correct size
	 */
	@Override
	public void trimBitmaps() {
		for(Bitmap bitmap : graphInformation.getBitmaps()) {
			if (bitmap instanceof Bitmap375)
				((Bitmap375)bitmap).trim(graphInformation.getNumberOfTriples());
		}
	}

	@Override
	public String getVocabularyMode() {
		return HDTVocabulary.ANNOTATED_GRAPHS;
	}

	@Override
	public Type getType() {
		return Type.ANNOTATED_GRAPHS;
	}

	@Override
	public IteratorTripleID getBitmapGraphIterator(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedGraphsIterator(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorG(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedGraphsIteratorG(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorYFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedGraphsIteratorYFOQ(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorYGFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedGraphsIteratorYGFOQ(bitmapTriples, pattern, graphInformation);
	}

	@Override
	public IteratorTripleID getBitmapGraphIteratorZFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		 return new BitmapAnnotatedGraphsIteratorZFOQ(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorZGFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		 return new BitmapAnnotatedGraphsIteratorZGFOQ(bitmapTriples, pattern, graphInformation);
	}
}
