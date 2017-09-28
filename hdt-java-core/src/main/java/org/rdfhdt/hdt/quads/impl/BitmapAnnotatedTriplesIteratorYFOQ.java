package org.rdfhdt.hdt.quads.impl;

import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIteratorYFOQ;
import org.rdfhdt.hdt.triples.impl.TripleOrderConvert;

public class BitmapAnnotatedTriplesIteratorYFOQ extends BitmapTriplesIteratorYFOQ {
	
	// resolves ?P?? queries
	
	private ArrayList<Bitmap> bitmapsGraph; // graph bitmaps of all triples
	private long posG; // current position in the graph bitmap of this triple
	private int g; // g is variable
	
	public BitmapAnnotatedTriplesIteratorYFOQ(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		this.triples = triples;
		this.pattern = new QuadID();
		this.returnTriple = new QuadID();
		this.bitmapsGraph = graphs.getBitmaps();
		newSearch(pattern);
	}

	@Override
	public void goToStart() {
		super.goToStart();
		posG = bitmapsGraph.get((int) posZ).selectNext1(0);
	}
	
	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
		if(posZ>nextZ) {  // if, with the current position of the object (posZ), we have reached the next list of objects (starting in nexZ), then we should update the associated predicate (Y) and, potentially, also the associated subject (X)
			numOccurrence++; // increase the number of occurrence of the predicate
			posY = triples.predicateIndex.getOccurrence(patY, numOccurrence); // get the position of the next occurrence of the predicate in AdjY 
			
			posZ = prevZ = adjZ.find(posY); // current position of the object, associated to the list posY
			nextZ = adjZ.last(posY);  // update nextZ, storing in which position (in adjZ) ends the list of objects associated with the current subject,predicate  
			
			posG = bitmapsGraph.get((int) posZ).selectNext1(0);
			
			x = (int) adjY.findListIndex(posY)+1;  // get the next subject (X)
			y = (int) adjY.get(posY);  // get the next predicate (Y)
		}
		
		g = (int) posG + 1;
		z = (int) adjZ.get(posZ); // get the next object (Z)
		
		posG = bitmapsGraph.get((int) posZ).selectNext1(posG + 1); // get next graph position for this triple
		if(posG == -1) { // there is no further graph for this triple?
			posZ++;	// increase the position where the next object can be found.
			if(hasNext()) { // is there another solution?
				posG = bitmapsGraph.get((int) posZ).selectNext1(0); // get first graph of triple
			}
		}
	
		updateOutput(); // set the components (subject,predicate,object) of the returned triple
		
		return returnTriple; // return the triple as solution
	}
	
	@Override
	protected void updateOutput() {
		((QuadID) returnTriple).setAll(x, y, z, g);
		TripleOrderConvert.swapComponentOrder(returnTriple, triples.order, TripleComponentOrder.SPO);
	}
	
	@Override
	public long getNextTriplePosition() {
		throw new NotImplementedException();
	}
	
	@Override
	public long getPreviousTriplePosition() {
		throw new NotImplementedException();
	}
	
	@Override
	public boolean hasPrevious() {
		throw new NotImplementedException();
	}

	@Override
	public TripleID previous() {
		throw new NotImplementedException();
	}
}
