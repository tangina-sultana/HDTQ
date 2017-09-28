package org.rdfhdt.hdt.graphs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.quads.QuadID;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;

public interface Annotator {
	void load(HashSet<Integer> graphsOfTriple);
	void load(InputStream input, ProgressListener iListener) throws IOException;
	void save(OutputStream output, ProgressListener iListener) throws IOException;
	void trimBitmaps();
	
	String getVocabularyMode();
	ControlInformation.Type getType();
	IteratorTripleID getBitmapGraphIterator(BitmapTriples bitmapTriples, QuadID pattern);
	IteratorTripleID getBitmapGraphIteratorG(BitmapTriples bitmapTriples, QuadID pattern);
	IteratorTripleID getBitmapGraphIteratorYFOQ(BitmapTriples bitmapTriples, QuadID pattern);
	IteratorTripleID getBitmapGraphIteratorYGFOQ(BitmapTriples bitmapTriples, QuadID pattern);
	IteratorTripleID getBitmapGraphIteratorZFOQ(BitmapTriples bitmapTriples, QuadID pattern);
	IteratorTripleID getBitmapGraphIteratorZGFOQ(BitmapTriples bitmapTriples, QuadID pattern);
}
