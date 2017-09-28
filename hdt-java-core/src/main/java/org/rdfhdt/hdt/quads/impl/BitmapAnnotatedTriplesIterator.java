package org.rdfhdt.hdt.quads.impl;

import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIterator;
import org.rdfhdt.hdt.triples.impl.TripleOrderConvert;

public class BitmapAnnotatedTriplesIterator extends BitmapTriplesIterator {

	// resolves ????, S???, SP??, SPO? queries
	
	private ArrayList<Bitmap> bitmapsGraph; // graph bitmaps of all triples
	private long posG; // current position in the graph bitmap of this triple
	private int g; // g is variable
	
	public BitmapAnnotatedTriplesIterator(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		this.triples = triples;
		this.returnTriple = new QuadID();
		this.pattern = new QuadID();
		this.bitmapsGraph = graphs.getBitmaps();
		newSearch(pattern);
	}
	
	@Override
	public void goToStart() {
		super.goToStart();
		
		Bitmap bitmapGraph = bitmapsGraph.get((int) posZ);
		posG = bitmapGraph.selectNext1(0);
	}
	
	@Override
	public long estimatedNumResults() {
		long results = 0;
		for(int i = (int) minZ; i < maxZ; i++) {
			results += bitmapsGraph.get(i).countOnes();
		}
		return results;
	}
	
	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
		z = (int) adjZ.get(posZ); // get the next object (Z). We just retrieve it from the list of objects (AdjZ) from current position posZ
		if(posZ>=nextZ) { // if, with the current position of the object (posZ), we have reached the next list of objects (starting in nexZ), then we should update the associated predicate (Y) and, potentially, also the associated subject (X)
			posY = triples.bitmapZ.rank1(posZ-1);	// move to the next position of predicates
			y = (int) adjY.get(posY); // get the next predicate (Y). We just retrieve it from the list of predicates(AdjY) from current position posY
			nextZ = adjZ.findNext(posZ)+1;	// update nextZ, storing in which position (in adjZ) ends the list of objects associated with the current subject,predicate
			if(posY>=nextY) { // if we have reached the next list of objects (starting in nexZ) we should update the associated predicate (Y) and, potentially, also the associated subject (X)
				x = (int) triples.bitmapY.rank1(posY - 1) + 1;	// get the next subject (X)
				nextY = adjY.findNext(posY)+1;	// update nextY, storing in which position (in AdjY) ends the list of predicates associated with the current subject
			}
		}
		
		g = (int) posG + 1;
		
		// set posG to the next graph of this triple
		posG = bitmapsGraph.get((int) posZ).selectNext1(posG+1);
		if(posG == -1) { // there are no further graphs for this triple
			posZ++;
			if(hasNext()) {
				posG = bitmapsGraph.get((int) posZ).selectNext1(0);
			}
		}
		
		updateOutput(); // set the components (subject,predicate,object,graph) of the returned triple
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
	
	@Override
	public boolean canGoTo() {
		throw new NotImplementedException();
	}
	
	@Override
	public void goTo(long pos) {
		throw new NotImplementedException();
	}
	
}
