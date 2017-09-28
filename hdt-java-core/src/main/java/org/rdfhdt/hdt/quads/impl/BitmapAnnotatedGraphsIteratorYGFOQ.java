package org.rdfhdt.hdt.quads.impl;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.graphs.GraphInformationImpl;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesIteratorYFOQ;

public class BitmapAnnotatedGraphsIteratorYGFOQ extends BitmapTriplesIteratorYFOQ {
	
	// resolves ?P?G queries
	
	private Bitmap bitmapGraph; // the bitmap of the requested graph
	
	private long posG;
	private boolean updateXYonNextIteration = true;
	
	public BitmapAnnotatedGraphsIteratorYGFOQ(BitmapTriples triples, QuadID pattern, GraphInformationImpl graphs) {
		super();
		
		this.triples = triples;
		this.pattern = new QuadID();
		this.returnTriple = new QuadID();
		this.bitmapGraph = graphs.getBitmaps().get(pattern.getGraph() - 1);
		newSearch(pattern);
	}

	@Override
	public void goToStart() {
		if(this.bitmapGraph == null || numOccurrences == 0) {
			return;
		}
		numOccurrence = 1;
		posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);
		posZ = adjZ.find(posY);
		nextZ = adjZ.last(posY);
		posG = bitmapGraph.selectNext1(posZ);	// find the first triple that appears in given graph
		if(posG>nextZ) {  // if, with the current position of the object (posZ), we have reached the next list of objects (starting in nexZ), then we should update the associated predicate (Y) and, potentially, also the associated subject (X)
			while(numOccurrence < numOccurrences) {
				numOccurrence++;
				posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);
				nextZ = adjZ.last(posY);
				if(nextZ < posG) { // the [posZ,nextZ] "window" is too far to the left?
					continue; // next iteration (moves nextZ to the right)
				}
				posZ = adjZ.find(posY);
				if(posZ > posG) { // the [posZ,nextZ] "window" is too far to the right?
					posG = bitmapGraph.selectNext1(posZ); // update posG to the next candidate
					if(posG == -1) {
						// there are no more solution candidates
						numOccurrences = 0;
						break;
					}
					if(nextZ < posG) { // the [posZ,nextZ] "window" is too far to the left?
						continue; // next iteration (moves "window" to the right)
					}
				}
				break;
			}
			
		}
		posZ = posG; //at this point, posG is within posZ and nextZ, so posG is the next solution
	}
	
	/* 
	 * Check if there are more solution
	 */
	@Override
	public boolean hasNext() {
		return (posZ<maxZ && (numOccurrence<numOccurrences) || posZ<=nextZ && posZ != -1) && numOccurrences != 0 && posZ != -1;
	}
	
	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
		if(updateXYonNextIteration) {
			x = (int) adjY.findListIndex(posY)+1;  // get the next subject (X)
			y = (int) adjY.get(posY);  // get the next predicate (Y)
			updateXYonNextIteration = false;
		}
		
		z = (int) adjZ.get(posZ); // get the next object (Z)
		posG = posZ = bitmapGraph.selectNext1(posZ + 1); // increase the position where the next object can be found.
		
		if(posG>nextZ) {  // if, with the current position of the object (posZ), we have reached the next list of objects (starting in nexZ), then we should update the associated predicate (Y) and, potentially, also the associated subject (X)
			while(numOccurrence < numOccurrences) {
				numOccurrence++;
				posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);
				nextZ = adjZ.last(posY);
				if(nextZ < posG) { // the [posZ,nextZ] "window" is too far to the left?
					continue; // next iteration (moves nextZ to the right)
				}
				posZ = adjZ.find(posY);
				if(posZ > posG) { // the [posZ,nextZ] "window" is too far to the right?
					posG = bitmapGraph.selectNext1(posZ); // update posG to the next candidate
					if(posG == -1) {
						// there are no more solution candidates
						numOccurrences = 0;
						break;
					}
					if(nextZ < posG) { // the [posZ,nextZ] "window" is too far to the left?
						continue; // next iteration (moves "window" to the right)
					}
				}
				break;
			}
			
			posZ = posG; //at this point, posG is within posZ and nextZ, so posG is the next solution
			updateXYonNextIteration = true;
		}
	
		updateOutput(); // set the components (subject,predicate,object) of the returned triple
		
		return returnTriple; // return the triple as solution
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
