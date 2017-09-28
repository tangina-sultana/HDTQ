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
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIterator;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIteratorG;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIteratorYFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIteratorYGFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIteratorZFOQ;
import org.rdfhdt.hdt.quads.impl.BitmapAnnotatedTriplesIteratorZGFOQ;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;

public class TripleAnnotator extends BaseAnnotator{
	
	private BitmapType typeBitmap;

	public TripleAnnotator(GraphInformationImpl graphInformation) {
		super(graphInformation);
		this.typeBitmap=BitmapType.TYPE_BITMAP_PLAIN;
	}
	
	public TripleAnnotator(GraphInformationImpl graphInformation, BitmapType typeBitmap) {
		super(graphInformation);
		this.typeBitmap=typeBitmap;
	}

	/**
	 * Load one triple into GraphInformation
	 */
	@Override
	public void load(HashSet<Integer> graphsOfTriple) {
		ArrayList<Bitmap> bitmaps = graphInformation.getBitmaps();
		ModifiableBitmap graphBitmap;
		if (typeBitmap==BitmapType.TYPE_BITMAP_PLAIN){
			graphBitmap = new Bitmap375(graphInformation.getNumberOfGraphs());
		}
		else
			graphBitmap = new BitmapRoaring64();
		
		for(int graph : graphsOfTriple) {
			graphBitmap.set(graph - 1, true);
		}
		
		bitmaps.add(graphBitmap);
	}
	
	/**
	 * Returns the number of bitmaps to load when loading HDT from file
	 * @return number of bitmaps
	 */
	@Override
	protected long getNumberOfBitmapsToLoad() {
		return graphInformation.getNumberOfTriples();
	}

	@Override
	public String getVocabularyMode() {
		return HDTVocabulary.ANNOTATED_TRIPLES;
	}

	@Override
	public Type getType() {
		return Type.ANNOTATED_TRIPLES;
	}
	
	@Override
	public void trimBitmaps() {
		// do nothing
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIterator(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIterator(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorG(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIteratorG(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorYFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIteratorYFOQ(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorYGFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIteratorYGFOQ(bitmapTriples, pattern, graphInformation);
	}

	@Override
	public IteratorTripleID getBitmapGraphIteratorZFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIteratorZFOQ(bitmapTriples, pattern, graphInformation);
	}
	
	@Override
	public IteratorTripleID getBitmapGraphIteratorZGFOQ(BitmapTriples bitmapTriples, QuadID pattern) {
		return new BitmapAnnotatedTriplesIteratorZGFOQ(bitmapTriples, pattern, graphInformation);
	}
}
