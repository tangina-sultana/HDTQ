package org.rdfhdt.hdt.graphs;

import org.rdfhdt.hdt.compact.bitmap.Bitmap.BitmapType;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.ControlInfo.Type;
import org.rdfhdt.hdt.options.HDTOptions;

public class AnnotatorFactory {
	public static Annotator createAnnotator(GraphInformationImpl graphinformation, HDTOptions spec) {
		String graphType = spec.get("graph.type");
		String graphBitmap = spec.get("graphBitmap.type");
		BitmapType typeBitmap=BitmapType.TYPE_BITMAP_PLAIN;
		if (graphBitmap!=null && graphBitmap.equalsIgnoreCase(HDTVocabulary.BITMAP_TYPE_ROARING)){
			typeBitmap = BitmapType.TYPE_BITMAP_ROAR;
		}
		if(graphType != null) {
			switch(graphType) {
				case "<http://purl.org/HDT/hdt#AG>":
					return createAnnotator(graphinformation, Type.ANNOTATED_GRAPHS,typeBitmap);	
				case "<http://purl.org/HDT/hdt#AT>":
					return createAnnotator(graphinformation, Type.ANNOTATED_TRIPLES,typeBitmap);
				default:
					throw new RuntimeException("Unknown graph.type specified: " + graphType);
			}
		} else {
			return createAnnotator(graphinformation, Type.ANNOTATED_TRIPLES,typeBitmap);
		}
	}

	public static Annotator createAnnotator(GraphInformationImpl graphinformation, Type type, BitmapType typeBitmap) {
		switch(type) {
		case ANNOTATED_GRAPHS:
			return new GraphAnnotator(graphinformation,typeBitmap);
		case ANNOTATED_TRIPLES:
		default:
			return new TripleAnnotator(graphinformation,typeBitmap);
		}
	}
	public static Annotator createAnnotator(GraphInformationImpl graphinformation, Type type) {
		switch(type) {
		case ANNOTATED_GRAPHS:
			return new GraphAnnotator(graphinformation);
		case ANNOTATED_TRIPLES:
		default:
			return new TripleAnnotator(graphinformation);
		}
	}
}
