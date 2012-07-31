/**
 * File: $HeadURL$
 * Revision: $Rev$
 * Last modified: $Date$
 * Last modified by: $Author$
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

package org.rdfhdt.hdt.hdt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.ModifiableDictionary;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.header.HeaderFactory;
import org.rdfhdt.hdt.iterator.DictionaryTranslateIterator;
import org.rdfhdt.hdt.iterator.IteratorTripleString;
import org.rdfhdt.hdt.listener.IntermediateListener;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.ModifiableTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.Triples;
import org.rdfhdt.hdt.triples.TriplesFactory;
import org.rdfhdt.hdt.util.StopWatch;

/**
 * @author mario.arias
 *
 */
public class HDTRW implements ModifiableHDT {
	HDTSpecification spec;
	
	Header header;
	ModifiableDictionary dictionary;
	ModifiableTriples triples;
	
	String baseUri=null;
	boolean isOrganized=false;
	
	protected HDTRW(HDTSpecification spec) {
		this.spec = spec;
		dictionary = DictionaryFactory.createModifiableDictionary(spec);
		triples = TriplesFactory.createModifiableTriples(spec);
	}
	
	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#getHeader()
	 */
	@Override
	public Header getHeader() {
		return header;
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#getDictionary()
	 */
	@Override
	public Dictionary getDictionary() {
		return dictionary;
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#getTriples()
	 */
	@Override
	public Triples getTriples() {
		return triples;
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#saveToHDT(java.io.OutputStream, hdt.listener.ProgressListener)
	 */
	@Override
	public void saveToHDT(OutputStream output, ProgressListener listener) throws IOException {
		ControlInformation ci = new ControlInformation();
		IntermediateListener iListener = new IntermediateListener(listener);
		
		ci.clear();
		ci.setHeader(true);
		header.save(output, ci, iListener);
		
		ci.clear();
		ci.setDictionary(true);
		dictionary.save(output, ci, iListener);
		
		ci.clear();
		ci.setTriples(true);
		triples.save(output, ci, iListener);
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#saveToHDT(java.lang.String, hdt.listener.ProgressListener)
	 */
	@Override
	public void saveToHDT(String fileName, ProgressListener listener) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
		//OutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		saveToHDT(out, listener);
		out.close();
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#size()
	 */
	@Override
	public long size() {
		return dictionary.size()+triples.size();
	}


	/* (non-Javadoc)
	 * @see hdt.hdt.HDT#search(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IteratorTripleString search(CharSequence subject, CharSequence predicate, CharSequence object) {

		// Conversion from TripleString to TripleID
		TripleID triple = new TripleID(
				dictionary.stringToId(subject, TripleComponentRole.SUBJECT),
				dictionary.stringToId(predicate, TripleComponentRole.PREDICATE),
				dictionary.stringToId(object, TripleComponentRole.OBJECT)
			);

		return new DictionaryTranslateIterator(triples.search(triple), dictionary);
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.ModifiableHDT#insert(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public void insert(CharSequence subject, CharSequence predicate, CharSequence object) {
		this.triples.insert(
				dictionary.insert(subject, TripleComponentRole.SUBJECT),
				dictionary.insert(predicate, TripleComponentRole.PREDICATE),
				dictionary.insert(object, TripleComponentRole.OBJECT)
				);
	}
	
	/* (non-Javadoc)
	 * @see hdt.hdt.ModifiableHDT#insert(hdt.triples.TripleString[])
	 */
	@Override
	public void insert(IteratorTripleString newTriples) {
		while(newTriples.hasNext()) {
			TripleString triple = newTriples.next();
			this.insert(triple.getSubject(), triple.getPredicate(), triple.getObject());
		}
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.ModifiableHDT#remove(hdt.triples.TripleString[])
	 */
	@Override
	public void remove(IteratorTripleString triples) {
		// FIXME: Removing from triples is easy, but how to know if the dictionary entry should be removed??
		while(triples.hasNext()) {
			TripleString triple = triples.next();
			this.remove(triple.getSubject(), triple.getPredicate(), triple.getObject());
		}
	}

	/* (non-Javadoc)
	 * @see hdt.hdt.ModifiableHDT#remove(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public void remove(CharSequence subject, CharSequence predicate, CharSequence object) {
		// FIXME: Removing from triples is easy, but how to know if the dictionary entry should be removed??
		triples.remove(dictionary.tripleStringtoTripleID(new TripleString(subject, predicate, object)));
	}

	@Override
	public void clear() {
		dictionary.clear();
		triples.clear();
	}

	@Override
	public void loadFromModifiableHDT(ModifiableHDT modHdt, ProgressListener listener) {
		this.loadFromHDT(modHdt, listener);
	}

	@Override
	public void loadFromHDT(InputStream input, ProgressListener listener) throws IOException {
		ControlInformation ci = new ControlInformation();
		IntermediateListener iListener = new IntermediateListener(listener);
		
		// Load header
		ci.clear();
		ci.load(input);
		iListener.setRange(0, 5);
		header = HeaderFactory.createHeader(ci);
		header.load(input, ci, iListener);
		
		// Load dictionary
		ci.clear();
		ci.load(input);
		iListener.setRange(5, 60);
		dictionary.clear();
		dictionary.load(input, ci, iListener);
		
		// Load Triples
		ci.clear();
		ci.load(input);
		iListener.setRange(60, 100);
		triples.clear();
		triples.load(input, ci, iListener);	
	}

	@Override
	public void loadFromHDT(String fileName, ProgressListener listener) throws IOException {
//		InputStream in = new GZIPInputStream(new FileInputStream(fileName));
		InputStream in = new BufferedInputStream(new FileInputStream(fileName));
		loadFromHDT(in, listener);
		in.close();
	}

	@Override
	public void loadFromHDT(HDT hdt, ProgressListener listener) {
		Dictionary otherDict = hdt.getDictionary();
		Triples otherTriples = hdt.getTriples();
		
		dictionary.clear();
		dictionary.load(otherDict, listener);
		
		triples.clear();
		triples.load(otherTriples, listener);
	}

	@Override
	public void close() throws IOException {
		dictionary.close();
		triples.close();
	}

	@Override
	public String getBaseURI() {
		return baseUri;
	}

	@Override
	public void reorganize(ProgressListener listener) {
		if(!isOrganized) {
			// Reorganize dictionary and update IDs on Triples accordingly.
			StopWatch reorgStp = new StopWatch();
			dictionary.reorganize(triples);
			System.out.println("Dictionary reorganized in "+reorgStp.stopAndShow());

			// Reorganize triples.
			String orderStr = spec.get("triples.component.order");
			if(orderStr==null) {
				orderStr = "SPO";
			}
			
			// Sort and remove duplicates.
			StopWatch sortDupTime = new StopWatch();
			triples.sort(TripleComponentOrder.valueOf(orderStr), listener);
			triples.removeDuplicates(listener);
			System.out.println("Sort triples and remove duplicates: "+sortDupTime.stopAndShow());
			
			isOrganized=true;
		}
	}

	@Override
	public void loadOrCreateIndex(ProgressListener listener) {
		// TODO: Do some kind of index to enhance queries.
	}

}
