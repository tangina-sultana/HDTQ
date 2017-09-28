package org.rdfhdt.hdt.quads.impl;
import java.util.ArrayList;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIteratorYFOQ;

public class BitmapAnnotatedTriplesIteratorYGFOQ extends BitmapTriplesIteratorYFOQ {
	
	// resolves ?P?G queries
	
	private ArrayList<Bitmap> bitmapsGraph; // graph bitmaps of all triples
	private int posG; // the graph from the search
	
	public BitmapAnnotatedTriplesIteratorYGFOQ(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		this.triples = triples;
		this.pattern = new QuadID();
		this.returnTriple = new QuadID();
		this.bitmapsGraph = graphs.getBitmaps();
		this.posG = pattern.getGraph() - 1;
		newSearch(pattern);
	}
	
	@Override
	protected void findRange() {
		super.findRange();
		long posY, posZ, nextZ, posG = -2;
		do {
			if(posG != -2) {	//skip in first iteration
				numOccurrences--;
				if(numOccurrences == 0) {
					break;
				}
			}
			posY = triples.predicateIndex.getOccurrence(patY, numOccurrences);
			posZ = adjZ.find(posY);
			nextZ = adjZ.last(posY);
			posG = selectNext1Graph((int) posZ, (int) nextZ);
		} while(posG > nextZ || posG == -1);
	}
	
	/**
	 * Return the position of the first 1 in the graph bitmaps, in the range [min,max] (both included).
	 * @param min
	 * @param max
	 * @return If there is no 1 in the range, max+1 is returned. If the end of the bitmaps has been reached (there cannot be further 1s in the bitmaps), -1 is returned. 
	 */
	private long selectNext1Graph(int min, int max) {
		if(min == maxZ) {
			return -1;
		}
		if(min > max) {
			return max+1;
		}
		while(!bitmapsGraph.get(min).access(posG)) {
			min++;
			if(min == maxZ) {
				return -1;
			}
			if(min == max+1) {
				return max+1;
			}
		}
		return min;
	}
	
	private long selectPrev1Graph(int pos) {
		while(!bitmapsGraph.get(pos).access(posG)) {
			pos--;
			if(pos == -1) {
				return -1;
			}
		}
		return pos;
	}

	@Override
	public void goToStart() {
		if(this.bitmapsGraph == null || numOccurrences == 0) {
			return;
		}
		numOccurrence = 0;
		do {
			numOccurrence++;
			posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);
			posZ = adjZ.find(posY);
			nextZ = adjZ.last(posY);
			posZ = selectNext1Graph((int) posZ, (int) nextZ);	// find the first triple that appears in given graph
		} while(posZ > nextZ);
		
		nextZ = selectPrev1Graph((int) nextZ);
		
		x = (int) adjY.findListIndex(posY)+1;
		y = (int) adjY.get(posY);
        z = (int) adjZ.get(posZ);
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
			do {
				numOccurrence++;
				posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);
				posZ = adjZ.find(posY);
				nextZ = adjZ.last(posY);
				posZ = selectNext1Graph((int) posZ, (int) nextZ);	// find the first triple that appears in given graph
			} while(posZ > nextZ);
			
			nextZ = selectPrev1Graph((int) nextZ);
			
			x = (int) adjY.findListIndex(posY)+1;  // get the next subject (X)
			y = (int) adjY.get(posY);  // get the next predicate (Y)
		}
		
		z = (int) adjZ.get(posZ); // get the next object (Z)
		posZ = selectNext1Graph((int) posZ + 1, (int) nextZ); // increase the position where the next object can be found.
	
		updateOutput(); // set the components (subject,predicate,object) of the returned triple
		
		return returnTriple; // return the triple as solution
	}
	
	@Override
	public long getNextTriplePosition() {
		if(posZ > nextZ) {
			long tempNumOccurence = numOccurrence;
			long tempPosY = posY;
			long tempPosZ = posZ;
			long tempNextZ = nextZ;
			do {
				tempNumOccurence++;
				tempPosY = triples.predicateIndex.getOccurrence(patY, tempNumOccurence);
				tempPosZ = adjZ.find(tempPosY);
				tempNextZ = adjZ.last(tempPosY);
				tempPosZ = selectNext1Graph((int) tempPosZ, (int) tempNextZ);	// find the first triple that appears in given graph
			} while(tempPosZ > tempNextZ);
			return tempPosZ;
		} else {
			return posZ;
		}
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
