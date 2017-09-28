/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/BitmapTriplesIteratorZFOQ.java $
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
public class BitmapTriplesIteratorZFOQ implements IteratorTripleID {
	protected BitmapTriples triples; // access to the TripleIDs
	protected TripleID pattern, returnTriple;
	
	protected AdjacencyList adjY, adjIndex; // adjacency list of Predicates (Y)
	protected long posIndex, minIndex, maxIndex; //boundaries
	protected int x, y, z; // current solution, S=X, P=Y and O=Z
	
	protected int patY, patZ; // patterns of the search
	
	protected BitmapTriplesIteratorZFOQ() { }
	
	public BitmapTriplesIteratorZFOQ(BitmapTriples triples, TripleID pattern) {
		this.triples = triples;
		this.pattern = new TripleID();
		this.returnTriple = new TripleID();
		newSearch(pattern);
	}
	
	protected void newSearch(TripleID pattern) {
		this.pattern.assign(pattern);
		
		TripleOrderConvert.swapComponentOrder(this.pattern, TripleComponentOrder.SPO, triples.order);
		patZ = this.pattern.getObject();
		if(patZ==0 && (patY!=0 || this.pattern.getSubject()!=0)) {
			throw new IllegalArgumentException("This structure is not meant to process this pattern");
		}
		
	    patY = this.pattern.getPredicate();
		
		adjY = triples.adjY;
		adjIndex = triples.adjIndex; // adjIndex has the list of positions in adY 
		
		findRange(); // get the boundaries where the solution for the given object can be found
		goToStart(); // load the first solution and position the next pointers
	}
	
	/*
	 * Get the predicate associated to a given position in the object index
	 */
	private long getY(long index) {
		return adjY.get(adjIndex.get(index));
	}
	
	protected void findRange() {
		if(patZ==0) { //if the object is not provided (usually it is in this iterator)
			minIndex = 0;
			maxIndex = adjIndex.getNumberOfElements();
			return;
		}
		minIndex = adjIndex.find(patZ-1); //find the position of the first occurrence of the object
		maxIndex = adjIndex.last(patZ-1); //find the position of the last ocurrence of the object

		if(patY!=0) { // if the predicate is provided then we do a binary search to search for such predicate
			while (minIndex <= maxIndex) {
				long mid = (minIndex + maxIndex) / 2;
				long predicate=getY(mid);  //get predicate at mid position in the object index     

				if (patY > predicate) {
					minIndex = mid + 1;
				} else if (patY < predicate) {
					maxIndex = mid - 1;
				} else { // the predicate has been found, now we have to find the min and max limits (the predicate P is repeated for each PO occurrence in the triples)
					// Binary Search to find left boundary
					long left=minIndex;
					long right=mid;
					long pos=0;

					while(left<=right) {
						pos = (left+right)/2;

						predicate = getY(pos);

						if(predicate!=patY) {
							left = pos+1;
						} else {
							right = pos-1;
						}
					}
					minIndex = predicate==patY ? pos : pos+1;

					// Binary Search to find right boundary
					left = mid;
					right= maxIndex;

					while(left<=right) {
						pos = (left+right)/2;
						predicate = getY(pos);

						if(predicate!=patY) {
							right = pos-1;
						} else {
							left = pos+1;
						}
					}
					maxIndex = predicate==patY ? pos : pos-1;

					break;
				}
			}
		}
	}
	
	protected void updateOutput() {
		returnTriple.setAll(x, y, z);
		TripleOrderConvert.swapComponentOrder(returnTriple, triples.order, TripleComponentOrder.SPO);
	}
	
	/* 
	 * Check if there are more solution
	 */
	@Override
	public boolean hasNext() {
		return posIndex<=maxIndex;
	}
	
	/* 
	 * Get the next solution
	 */
	@Override
	public TripleID next() {
	    long posY = adjIndex.get(posIndex); // get the position of the next occurrence of the predicate in AdjY

	    z = patZ!=0 ? patZ : (int)adjIndex.findListIndex(posIndex)+1; //get the next object (z) as the number of list in adIndex corresponding to posIndex
	    y = patY!=0 ? patY : (int) adjY.get(posY); // get the next predicate (y) as the element in adjY stores in position posY
	    x = (int) adjY.findListIndex(posY)+1; //get the next subject (X) as the number of list in adjY corresponding to posY

	    posIndex++; // increase the position of the next occurrence of the predicate

	    updateOutput(); // set the components (subject,predicate,object) of the returned triple
	    return returnTriple; // return the triple as solution
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return posIndex>minIndex;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		posIndex--;

		long posY = adjIndex.get(posIndex);

		z = patZ!=0 ? patZ : (int)adjIndex.findListIndex(posIndex)+1;
		y = patY!=0 ? patY : (int) adjY.get(posY);
		x = (int) adjY.findListIndex(posY)+1;

		updateOutput();
		return returnTriple;
	}

	/* 
	 * load the first solution and position the next pointers
	 */
	@Override
	public void goToStart() {
		posIndex = minIndex;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#estimatedNumResults()
	 */
	@Override
	public long estimatedNumResults() {
		return maxIndex-minIndex+1;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
	    return ResultEstimationType.EXACT;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return true;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#goTo(int)
	 */
	@Override
	public void goTo(long pos) {
		if(pos>maxIndex-minIndex || pos<0) {
			throw new IndexOutOfBoundsException();
		}
		posIndex = minIndex+pos;
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
		long ret =0;
		try {
			ret = triples.adjZ.find(adjIndex.get(posIndex),patZ);
		} catch (NotFoundException e) {
		}
				
		return ret;
	}
	@Override
	public long getPreviousTriplePosition() {
		long ret =0;
		try {
			ret = triples.adjZ.find(adjIndex.get(posIndex-1),patZ);
		} catch (NotFoundException e) {
		}
				
		return ret;
	}
}
