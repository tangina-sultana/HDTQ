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

public class BitmapAnnotatedGraphsIteratorYFOQ extends BitmapTriplesIteratorYFOQ {
	
	// resolves ?P?? queries
	
	private ArrayList<Bitmap> bitmapsGraph; // one bitmap per graph
	private int numberOfGraphs;
	private long posG; // the current graph bitmap
	private int g; // g is variable
	
	public BitmapAnnotatedGraphsIteratorYFOQ(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		this.triples = triples;
		this.returnTriple = new QuadID();
		this.pattern = new QuadID();
		this.bitmapsGraph = graphs.getBitmaps();
		this.numberOfGraphs = bitmapsGraph.size();
		newSearch(pattern);
	}
	
	@Override
	public void goToStart() {
		super.goToStart();
		posG = 0;
		while(!bitmapsGraph.get((int) posG).access(posZ)) {
			posG++;
		}
	}
	
	/* 
	 * Check if there are more solution
	 */
	@Override
	public boolean hasNext() {
		return (posZ<maxZ && (numOccurrence<numOccurrences) || posZ<=nextZ && posZ != -1) && numOccurrences != 0;
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
			
			posG = 0;
			while(!bitmapsGraph.get((int) posG).access(posZ)) {
				posG++;
			}
			
			x = (int) adjY.findListIndex(posY)+1;  // get the next subject (X)
			y = (int) adjY.get(posY);  // get the next predicate (Y)
		}
		
		g = (int) posG + 1;
		z = (int) adjZ.get(posZ); // get the next object (Z)
		
		// get next graph position for this triple
		do {
			posG++;
		} while(posG < numberOfGraphs && !bitmapsGraph.get((int) posG).access(posZ));
		 
		if(posG == numberOfGraphs) { // there is no further graph for this triple?
			posZ++;	// increase the position where the next object can be found.
			if(posZ<=nextZ) { // is posZ correct for the next triple? (or do we need to update to a new y?)
				// get first graph of triple
				posG = 0;
				while(!bitmapsGraph.get((int) posG).access(posZ)) {
					posG++;
				} 
			}
		}
	
		updateOutput(); // set the components (subject,predicate,object) of the returned triple
		
		return returnTriple; // return the triple as solution
	}
	
	/*
	 * Set the components (subject,predicate,object) of the returned triple 
	 */
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
