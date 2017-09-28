package org.rdfhdt.hdt.quads.impl;

import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIterator;

public class BitmapAnnotatedTriplesIteratorG extends BitmapTriplesIterator {

	// resolves ???G, S??G, SP?G, SPOG queries
	
	private ArrayList<Bitmap> bitmapsGraph; // graph bitmaps of all triples
	private int patG; // the graph from the search
	
	public BitmapAnnotatedTriplesIteratorG(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		this.triples = triples;
		this.returnTriple = new QuadID();
		this.pattern = new QuadID();
		this.bitmapsGraph = graphs.getBitmaps();
		this.patG = pattern.getGraph();
		newSearch(pattern);
	}
	
	@Override
	public void goToStart() {
		if(minZ == maxZ) { // no results
			posZ = maxZ;
			return;
		}
		super.goToStart();
	}
	
	/*
	 * get the boundaries where the solution for the pattern (patX, patY, patZ, patG) can be found
	 */
	@Override
	protected void findRange() {
		super.findRange();
		
		//find the first triple in the range which is in the graph of the search
		while(minZ < maxZ && !bitmapsGraph.get((int) minZ).access(patG - 1)) {
			minZ++;
		}
		
		//find the last triple in the range which is in the graph of the search
		while(maxZ > minZ+1 && !bitmapsGraph.get((int) maxZ - 1).access(patG - 1)) {
			maxZ--;
		}
	}
	
	@Override
	public long estimatedNumResults() {
		long results = 0;
		for(int i = (int) minZ; i < maxZ; i++) {
			if(bitmapsGraph.get(i).access(patG - 1)) {
				results++;
			}
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
		do {
			posZ++;
		} while(posZ < maxZ && !bitmapsGraph.get((int) posZ).access(patG - 1));
		
		updateOutput(); // set the components (subject,predicate,object,graph) of the returned triple
		return returnTriple; // return the triple as solution
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
