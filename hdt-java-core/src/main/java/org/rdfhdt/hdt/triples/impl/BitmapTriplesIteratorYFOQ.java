/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/BitmapTriplesIteratorYFOQ.java $
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
import org.rdfhdt.hdt.compact.sequence.DeflateIntegerIterator;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * 
 * Iterates over all Y components of a BitmapTriples. 
 * i.e. In SPO it would iterate over all appearances of a predicate ?P?
 * 
 * @author mario.arias
 *
 */
public class BitmapTriplesIteratorYFOQ implements IteratorTripleID {
	protected BitmapTriples triples; // access to the TripleIDs and PredicateIndex
		protected TripleID pattern, returnTriple;
		protected int patY; // pattern of the search
		
		protected AdjacencyList adjY, adjZ; // adjacency list of Predicates (Y) and Objects (Z). That is, adjY contains all predicates for each subject. AdjZ contains all objects for each pair (subject, predicate)
		protected long posY, posZ; // current position of the predicate (posY), object (posZ). 
		protected long prevZ, nextZ, maxZ; //boundaries
		protected int x, y, z; // current solution, S=X, P=Y and O=Z
		
		protected long numOccurrences, numOccurrence;
		
		DeflateIntegerIterator index;
		
		protected BitmapTriplesIteratorYFOQ() { }
		
		protected BitmapTriplesIteratorYFOQ(BitmapTriples triples, TripleID pattern) {
			this.triples = triples;
			this.pattern = new TripleID();
			this.returnTriple = new TripleID();
			newSearch(pattern);
		}
		
		protected void newSearch(TripleID pattern) {
			this.pattern.assign(pattern);
			
			TripleOrderConvert.swapComponentOrder(this.pattern, TripleComponentOrder.SPO, triples.order);
			patY = this.pattern.getPredicate();
			if(patY==0) {
				throw new IllegalArgumentException("This structure is not meant to process this pattern");
			}
			
			adjY = new AdjacencyList(triples.seqY, triples.bitmapY);
			adjZ = new AdjacencyList(triples.seqZ, triples.bitmapZ);
			
			findRange();
			goToStart();
		}
		
		protected void findRange() {
			numOccurrences = triples.predicateIndex.getNumOcurrences(patY);
			maxZ = triples.adjZ.getNumberOfElements();
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
			return posZ<maxZ && (numOccurrence<numOccurrences) || posZ<=nextZ;
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
				
				x = (int) adjY.findListIndex(posY)+1;  // get the next subject (X)
				y = (int) adjY.get(posY);  // get the next predicate (Y)
			}
			z = (int) adjZ.get(posZ); // get the next object (Z)
			posZ++;	// increase the position where the next object can be found.
		
			updateOutput(); // set the components (subject,predicate,object) of the returned triple
			
			return returnTriple; // return the triple as solution
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return numOccurrence>1 || posZ>=prevZ;
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#previous()
		 */
		@Override
		public TripleID previous() {
			if(posZ<=prevZ) {
				numOccurrence--;
				posY = triples.predicateIndex.getOccurrence(patY, numOccurrence);

				prevZ = adjZ.find(posY);
				posZ = nextZ = adjZ.last(posY); 
				
				x = (int) adjY.findListIndex(posY)+1;
				y = (int) adjY.get(posY);
	 			z = (int) adjZ.get(posZ);
			} else {
				z = (int) adjZ.get(posZ);
				posZ--;
			}
			
			updateOutput();

			return returnTriple;
		}

		/* 
		 * load the first solution and position the next pointers
		 */
		@Override
		public void goToStart() {
			numOccurrence = 1;
			posY = triples.predicateIndex.getOccurrence(patY, numOccurrence); // get the position of the first occurrence of the predicate in AdjY
			
			posZ = prevZ = adjZ.find(posY); // current position of the object, associated to the list posY
			nextZ = adjZ.last(posY); // update nextZ, storing in which position (in adjZ) ends the list of objects associated with the current subject,predicate  
			
			x = (int) adjY.findListIndex(posY)+1; // get the next subject (X)
			y = (int) adjY.get(posY); // get the next predicate (Y)
	        z = (int) adjZ.get(posZ); // get the next object (Z)
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#estimatedNumResults()
		 */
		@Override
		public long estimatedNumResults() {
			return triples.predicateCount.get(patY-1);
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#numResultEstimation()
		 */
		@Override
		public ResultEstimationType numResultEstimation() {
		    return ResultEstimationType.UNKNOWN;
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#canGoTo()
		 */
		@Override
		public boolean canGoTo() {
			return false;
		}

		/* (non-Javadoc)
		 * @see hdt.iterator.IteratorTripleID#goTo(int)
		 */
		@Override
		public void goTo(long pos) {
			if(!canGoTo()) {
				throw new IllegalAccessError("Cannot goto on this bitmaptriples pattern");
			}
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
			if(posZ>nextZ) {
				long tempNumOccurrence = numOccurrence+1;
				long tempposY = triples.predicateIndex.getOccurrence(patY, tempNumOccurrence);
				return adjZ.find(tempposY); 
			}
			else
				return posZ;
		}
		@Override
		public long getPreviousTriplePosition() {
			return posZ-1;
		}
}
