package org.rdfhdt.hdt.quads.impl;

import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIteratorZFOQ;
import org.rdfhdt.hdt.triples.impl.TripleOrderConvert;

public class BitmapAnnotatedGraphsIteratorZFOQ extends BitmapTriplesIteratorZFOQ{

	// resolves ?PO?, ??O? queries
	
	private ArrayList<Bitmap> bitmapsGraph; // one bitmap per graph
	private int numberOfGraphs;
	private long posG; // the current graph bitmap
	private int g; // g is variable
	private boolean updateXYZ = true;
	
	public BitmapAnnotatedGraphsIteratorZFOQ(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
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
		if(hasNext()) {
			posG = getNextGraphPosition(0);
		}
	}
	
	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
		if(updateXYZ) {
		    long posY = adjIndex.get(posIndex); // get the position of the next occurrence of the predicate in AdjY
	
		    z = patZ!=0 ? patZ : (int)adjIndex.findListIndex(posIndex)+1; //get the next object (z) as the number of list in adIndex corresponding to posIndex
		    y = patY!=0 ? patY : (int) adjY.get(posY); // get the next predicate (y) as the element in adjY stores in position posY
		    x = (int) adjY.findListIndex(posY)+1; //get the next subject (X) as the number of list in adjY corresponding to posY
		    
		    updateXYZ = false;
		}
	    g = (int) posG + 1;
	    
	    posG = getNextGraphPosition((int) posG + 1); // get the next graph position for the current triple
	    if(posG == numberOfGraphs) { // there are no further graphs for this triple
	    	posIndex++; // increase the position of the next occurrence of the predicate
	    	if(hasNext()) {
	    		updateXYZ = true;
	    		posG = getNextGraphPosition(0);
	    	}
	    }

	    updateOutput(); // set the components (subject,predicate,object) of the returned triple
	    return returnTriple; // return the triple as solution
	}
	
	private int getNextGraphPosition(int pos) {
		int nextTriplePos = (int) getNextTriplePosition();
		while(pos < numberOfGraphs && !bitmapsGraph.get(pos).access(nextTriplePos)) {
			pos++;
		}
		return pos;
	}
	
	@Override
	protected void updateOutput() {
		((QuadID) returnTriple).setAll(x, y, z, g);
		TripleOrderConvert.swapComponentOrder(returnTriple, triples.order, TripleComponentOrder.SPO);
	}
	
	@Override
	public ResultEstimationType numResultEstimation() {
	    return ResultEstimationType.EQUAL_OR_MORE_THAN;
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
	public void goTo(long pos) {
		throw new NotImplementedException();
	}
}
