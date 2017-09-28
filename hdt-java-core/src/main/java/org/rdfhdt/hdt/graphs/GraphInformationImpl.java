package org.rdfhdt.hdt.graphs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.HeaderPrivate;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;

public class GraphInformationImpl implements GraphInformation {
	
	public enum NumberOfTriplesAccuracy {
		UP_TO, EXACT
	}
	
	private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	private Annotator annotator;
	private long numberOfGraphs;
	private long numberOfTriples;
	private NumberOfTriplesAccuracy numberOfTriplesAccuracy;
	
	public GraphInformationImpl (HDTOptions spec) {
		annotator = AnnotatorFactory.createAnnotator(this, spec);
	}
	
	public void save(OutputStream output, ProgressListener iListener) throws IOException {
		annotator.save(output, iListener);
	}
	
	/**
	 * Load one triple
	 */
	public void load(HashSet<Integer> graphsOfTriple) {
		annotator.load(graphsOfTriple);
	}
	
	/**
	 * Load from HDT file
	 */
	public void load(InputStream input, ControlInfo ci, ProgressListener iListener) throws IOException {
		ci.clear();
		ci.load(input);
		annotator = AnnotatorFactory.createAnnotator(this, ci.getType());
		annotator.load(input, iListener);
	}
	
	public ArrayList<Bitmap> getBitmaps() {
		return bitmaps;
	}
	
	public Annotator getAnnotator() {
		return annotator;
	}
	
	public void setNumberOfGraphs(long numberOfGraphs) {
		this.numberOfGraphs = numberOfGraphs;
	}
	
	public long getNumberOfGraphs() {
		return numberOfGraphs;
	}
	
	public void setNumberOfTriples(long numberOfTriples, NumberOfTriplesAccuracy accuracy) {
		this.numberOfTriples = numberOfTriples;
		this.numberOfTriplesAccuracy = accuracy;
	}
	
	public long getNumberOfTriples() {
		return numberOfTriples;
	}
	
	/**
	 * When using graph annotator, Bitmaps can be too big (because actual number of triple was unknown), 
	 * trim them to correct size
	 */
	public void trimBitmaps() {
		if(numberOfTriplesAccuracy != NumberOfTriplesAccuracy.EXACT) {
			throw new RuntimeException("Cannot trim bitmaps if number of Triples is not exact!");
		}
		annotator.trimBitmaps();
	}
	
	public long size() {
		long bitmapsSize = 0;
		for(Bitmap bitmap : bitmaps) {
			bitmapsSize += bitmap.getSizeBytes();
		}
		return bitmapsSize;
	}

	public void populateHeader(HeaderPrivate header, String rootNode) {
		header.insert(rootNode, HDTVocabulary.ANNOTATION_MODE, this.getVocabularyMode());
	}
	
	public String getVocabularyMode() {
		return annotator.getVocabularyMode();
	}
	
	public ControlInformation.Type getType() {
		return annotator.getType();
	}
}