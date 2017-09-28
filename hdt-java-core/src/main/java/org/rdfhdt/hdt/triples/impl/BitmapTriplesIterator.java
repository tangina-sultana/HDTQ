/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/BitmapTriplesIterator.java $
 * Revision: $Rev: 191 $
 * Last modified: $Date: 2013-03-03 11:41:43 +0000 (dom, 03 mar 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.triples.impl;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * @author mario.arias
 *
 */
public class BitmapTriplesIterator implements IteratorTripleID {

	// NOTE: For simplicity, in our comments we assume a SPO order, that is, S=X, P=Y and O=Z.
	
	protected BitmapTriples triples; // access to the TripleIDs
	protected TripleID pattern;
	protected TripleID returnTriple;
	protected int patX, patY, patZ; // patterns of the search
	
	protected AdjacencyList adjY, adjZ; // adjacency list of Predicates (Y) and Objects (Z). That is, adjY contains all predicates for each subject. AdjZ contains all objects for each pair (subject, predicate)
	protected long posY, posZ; // current position of the predicate (posY), object (posZ). 
	protected long minY, minZ, maxY, maxZ; // boundaries of the solution: predicates are in AdjY between minY and maxY. Objects are in AdjZ between minZ and maxZ  
	protected long nextY, nextZ; // position of the next predicate (nextY) and the next object (nextZ)  
	protected int x, y, z; // current solution, S=X, P=Y and O=Z
	
	protected BitmapTriplesIterator() { }
	
	protected BitmapTriplesIterator(BitmapTriples triples, TripleID pattern) {
		this.triples = triples;
		this.returnTriple = new TripleID();
		this.pattern = new TripleID();
		newSearch(pattern);
	}
	
	public void newSearch(TripleID pattern) {
		this.pattern.assign(pattern);
		
		TripleOrderConvert.swapComponentOrder(this.pattern, TripleComponentOrder.SPO, triples.order);
		patX = this.pattern.getSubject();
		patY = this.pattern.getPredicate();
		patZ = this.pattern.getObject();
		
		adjY = triples.adjY;
		adjZ = triples.adjZ;
		
//		((BitSequence375)triples.bitmapZ).dump();
				
		findRange(); // get the boundaries where the solution for the pattern (patX, patY, patZ) can be found
		goToStart(); // load the first solution and position the next pointers
	}
	
	/*
	 * Set the components (subject,predicate,object) of the returned triple 
	 */
	protected void updateOutput() {
		returnTriple.setAll(x, y, z);
		TripleOrderConvert.swapComponentOrder(returnTriple, triples.order, TripleComponentOrder.SPO);
	}
	
	/*
	 * get the boundaries where the solution for the pattern (patX, patY, patZ) can be found
	 */
	protected void findRange() {
		if(patX!=0) { // the subject is provided in the query
			//
			if(patY!=0) { // the predicate is provided in the query
				try {
					minY = adjY.find(patX-1, patY); // get the position (in the list of predicates, adjY) where it is listed the given predicate (patY) for the given subject (patX) 
					maxY = minY+1; // given that the predicate is provided, the max range of the predicate is just the next position
					if(patZ!=0) { // the object is provided in the query, i.e., (S,P,O).
						minZ = adjZ.find(minY,patZ); // get the position (in the list of objects, adjZ) where it is listed the given object (patZ) for the current subject, predicate (minY)
						maxZ = minZ+1; // given that the object is provided, the max range of the object is just the next position
					} else { // the query is (S,P,?)
						minZ = adjZ.find(minY); // get the initial position (in the list of objects, adjZ) where one can find the objects associated with the current subject, predicate (minY) 
						maxZ = adjZ.last(minY)+1; // get the last position (in the list of objects, adjZ) where one can find the objects associated with the current subject, predicate (minY)
					}
				} catch (NotFoundException e) {
					// Item is not found in the list, thus the solution has NO results.
					minY = minZ = maxY = maxZ = 0;
				}
			} else { // the predicate is not provided in the query. The query is (S,?,?) or (S,?,O).
				minY = adjY.find(patX-1); // get the initial position (in the list of predicates, adjY) where one can find the predicates for the given subject (patX)
				minZ = adjZ.find(minY);  // get the initial position (in the list of objects, adjZ) where one can find the objects associated with the current subject, predicate (minY)
				maxY = adjY.last(patX-1)+1; // get the last position (in the list of predicates, adjY) where one can find the predicates for the given subject (patX)
				maxZ = adjZ.find(maxY); // get the last position (in the list of objects, adjZ) where one can find the objects associated with the current subject, predicate (minY)
			}
			x = patX; // given that the subject is provided in the query, the solution for the subject (X) is set to the subject in the query.
		} else { // the subject is NOT provided in the pattern, i.e., the query is (?,?,?) and retrieves all elements
			minY=0; // the initial position of the predicate is 0 
			minZ=0; // the initial position of the object is 0
			maxY = adjY.getNumberOfElements(); // the max position of the predicate is the total number of elements
			maxZ = adjZ.getNumberOfElements(); // the max position of the object is the total number of elements
		}
	}
	
	/* 
	 * Check if there are more solution
	 */
	@Override
	public boolean hasNext() {
		return posZ<maxZ; // Just check if we have arrived to the maximum position of the objects that resolve the query
	}

	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
		z = (int) adjZ.get(posZ); // get the next object (Z). We just retrieve it from the list of objects (AdjZ) from current position posZ 
		if(posZ==nextZ) { // if, with the current position of the object (posZ), we have reached the next list of objects (starting in nexZ), then we should update the associated predicate (Y) and, potentially, also the associated subject (X)
			posY++; // move to the next position of predicates
			y = (int) adjY.get(posY); // get the next predicate (Y). We just retrieve it from the list of predicates(AdjY) from current position posY
//			nextZ = adjZ.find(posY+1);
			nextZ = adjZ.findNext(nextZ)+1; // update nextZ, storing in which position (in adjZ) ends the list of objects associated with the current subject,predicate  
			
			if(posY==nextY) { // if we have reached the next list of objects (starting in nexZ) we should update the associated predicate (Y) and, potentially, also the associated subject (X)
				x++; // get the next subject (X). Given that subject IDs are correlative, we just increase the ID ++.
//				nextY = adjY.find(x);
				nextY = adjY.findNext(nextY)+1; // update nextY, storing in which position (in AdjY) ends the list of predicates associated with the current subject
			}
		}
		
		posZ++; // increase the position where the next object can be found.
		 
		updateOutput(); // set the components (subject,predicate,object) of the returned triple
		
		return returnTriple; // return the triple as solution
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return posZ>minZ;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		 posZ--;

		 posY = adjZ.findListIndex(posZ);

		 z = (int) adjZ.get(posZ);  
		 y = (int) adjY.get(posY);  
		 x = (int) adjY.findListIndex(posY)+1;

		 nextY = adjY.last(x-1)+1;
		 nextZ = adjZ.last(posY)+1;

		 updateOutput();

		 return returnTriple;
	}

	/* 
	 * load the first solution and position the next pointers
	 */
	
	@Override
	public void goToStart() {
		posZ = minZ; // current position of the object is the initial one, minZ
        posY = adjZ.findListIndex(posZ); // get the ordinal number of the list associated to the object position posZ (that is, posZ corresponds to the 1st list, or 2nd, list, ... etc).   

        z = (int) adjZ.get(posZ); // get the next object (Z). We just retrieve it from the list of objects (AdjZ) from current position posZ
        y = (int) adjY.get(posY); // get the next predicate (Y). We just retrieve it from the list of predicates(AdjY) from current position posY
        x = (int) adjY.findListIndex(posY)+1; // get the next subject (X). We just retrieve it by knowing the ordinal number of the list associated to the predicate position posY (that is, if posY is the 14th list, then the subject is ID=14).

        nextY = adjY.last(x-1)+1; // nextY stores in which position (in AdjY) ends the list of predicates associated with the current subject (X)
        nextZ = adjZ.last(posY)+1; // nextZ stores in which position (in adjZ) ends the list of objects associated with the current subject,predicate (in position posY) 
	}

	
	@Override
	public long estimatedNumResults() {
		return maxZ-minZ; // the results of the query are found in the list of objects (AdjZ) between maxZ and minZ positions.
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
		if(patX!=0 && patY==0 && patZ!=0) {
	        return ResultEstimationType.UP_TO;
	    }
	    return ResultEstimationType.EXACT;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return pattern.isEmpty();
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#goTo(int)
	 */
	@Override
	public void goTo(long pos) {
		if(!canGoTo()) {
			throw new IllegalAccessError("Cannot goto on this bitmaptriples pattern");
		}

		if(pos>=adjZ.getNumberOfElements()) { 
			throw new ArrayIndexOutOfBoundsException("Cannot goTo beyond last triple");
		}

		posZ = pos;
		posY = adjZ.findListIndex(posZ);

		z = (int) adjZ.get(posZ);
		y = (int) adjY.get(posY);
		x = (int) adjY.findListIndex(posY)+1;

		nextY = adjY.last(x-1)+1;
		nextZ = adjZ.last(posY)+1;
	}
	
	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#getOrder()
	 */
	@Override
	public TripleComponentOrder getOrder() {
		return triples.order;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	 public long getNextTriplePosition() {
	 	return posZ;
	 }
	@Override
	 public long getPreviousTriplePosition() {
	 	return posZ-1;
	 }
}