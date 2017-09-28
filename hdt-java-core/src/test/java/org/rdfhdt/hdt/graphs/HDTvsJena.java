package org.rdfhdt.hdt.graphs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.quads.QuadString;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;

public class HDTvsJena {
	private static HDT hdtAnnotatedTriples;
	private static HDT hdtAnnotatedGraphs;
	private static HDT hdtTriples;
	
	private static Dataset jenaQuadDataset;
	private static Dataset jenaTripleDataset;
	
	private static final String BASE_URI = "http://example.com/3.5";
	private static final String FOLDER = "/home/javi/git/hdtVersions/tests/";
	
	private static final String QUAD_INPUT = FOLDER + "quad20.nq";
	//private static final String QUAD_INPUT = FOLDER + "bear.nq";
	//private static final String QUAD_INPUT = FOLDER + "liddi.nq";
	private static final RDFNotation QUAD_INPUT_TYPE = RDFNotation.NQUADS;
	private static final Lang   QUAD_INPUT_TYPE_JENA = Lang.NQUADS;
	
	private static final String TRIPLE_INPUT = FOLDER + "triple20.nt";
	private static final String TRIPLE_INPUT_TYPE = "ntriples";
	private static final Lang   TRIPLE_INPUT_TYPE_JENA = Lang.NTRIPLES;
	
	private static int queriesPerformed = 0;
	
	@BeforeClass
	public static void setUpQuadHDT() throws IOException, ParserException {
		String hdtOutputAnnotatedTriples = FOLDER + "quad20AnnotedTriples.hdt";
		String hdtOutputAnnotatedGraphs = FOLDER + "quad20AnnotedGraphs.hdt";
		String configFileAnnotatedTriples = FOLDER + "annotatedTriples.cfg";
		String configFileAnnotatedGraphs = FOLDER + "annotatedGraphs.cfg";
		
		HDT hdtAnnotatedTriplesToSave = HDTManager.generateHDT(QUAD_INPUT, BASE_URI, QUAD_INPUT_TYPE, new HDTSpecification(configFileAnnotatedTriples), null);
		hdtAnnotatedTriplesToSave.saveToHDT(hdtOutputAnnotatedTriples, null);
		HDT hdtAnnotatedGraphsToSave = HDTManager.generateHDT(QUAD_INPUT, BASE_URI, QUAD_INPUT_TYPE, new HDTSpecification(configFileAnnotatedGraphs), null);
		hdtAnnotatedGraphsToSave.saveToHDT(hdtOutputAnnotatedGraphs, null);
		
		hdtAnnotatedTriples = HDTManager.loadIndexedHDT(hdtOutputAnnotatedTriples, null);
		hdtAnnotatedGraphs = HDTManager.mapIndexedHDT(hdtOutputAnnotatedGraphs, null);
	}
	
	@BeforeClass
	public static void setUpTripleHDT() throws IOException, ParserException {
		String hdtOutput = FOLDER + "triple20.hdt";
		String configFile = FOLDER + "TripleConfig.cfg";
		
		HDT hdtToSave = HDTManager.generateHDT(TRIPLE_INPUT, BASE_URI, RDFNotation.parse(TRIPLE_INPUT_TYPE), new HDTSpecification(configFile), null);
		hdtToSave.saveToHDT(hdtOutput, null);
		
		hdtTriples = HDTManager.loadIndexedHDT(hdtOutput, null);
	}
	
	@BeforeClass
	public static void setupJena() {
		jenaQuadDataset = RDFDataMgr.loadDataset(QUAD_INPUT, QUAD_INPUT_TYPE_JENA);
		jenaTripleDataset = RDFDataMgr.loadDataset(TRIPLE_INPUT, TRIPLE_INPUT_TYPE_JENA);
	}
	
	@AfterClass
	public static void printNumberOfQueriesPerformed() {
		System.out.println("Number of queries performed: " + queriesPerformed);
	}
	
	/********************************************************/
	/******* Testing triples *******/
	
	@Test
	public void simpleTriplesVVV() throws NotFoundException {
		testTriplePattern("???");
	}
	
	@Test
	public void simpleTriplesSVV() throws NotFoundException {
		testTriplePattern("S??");
	}
	
	@Test
	public void simpleTriplesVPV() throws NotFoundException {
		testTriplePattern("?P?");
	}
	
	@Test
	public void simpleTriplesVVO() throws NotFoundException {
		testTriplePattern("??O");
	}
	
	@Test
	public void simpleTriplesSPV() throws NotFoundException {
		testTriplePattern("SP?");
	}
	
	@Test
	public void simpleTriplesSVO() throws NotFoundException {
		testTriplePattern("S?O");
	}
	
	@Test
	public void simpleTriplesVPO() throws NotFoundException {
		testTriplePattern("?PO");
	}
	
	@Test
	public void simpleTriplesSPO() throws NotFoundException {
		testTriplePattern("SPO");
	}
	
	/********************************************************/
	/******* Testing quads *******/
	
	@Test
	public void annotatedTriplesVVVV() throws NotFoundException {
		testQuadPattern("????", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVVVV() throws NotFoundException {
		testQuadPattern("????", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVVVG() throws NotFoundException {
		testQuadPattern("???G", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVVVG() throws NotFoundException {
		testQuadPattern("???G", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSVVV() throws NotFoundException {
		testQuadPattern("S???", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSVVV() throws NotFoundException {
		testQuadPattern("S???", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSVVG() throws NotFoundException {
		testQuadPattern("S??G", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSVVG() throws NotFoundException {
		testQuadPattern("S??G", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVPVV() throws NotFoundException {
		testQuadPattern("?P??", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVPVV() throws NotFoundException {
		testQuadPattern("?P??", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVPVG() throws NotFoundException {
		testQuadPattern("?P?G", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVPVG() throws NotFoundException {
		testQuadPattern("?P?G", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSPVV() throws NotFoundException {
		testQuadPattern("SP??", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSPVV() throws NotFoundException {
		testQuadPattern("SP??", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSPVG() throws NotFoundException {
		testQuadPattern("SP?G", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSPVG() throws NotFoundException {
		testQuadPattern("SP?G", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSPOV() throws NotFoundException {
		testQuadPattern("SPO?", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSPOV() throws NotFoundException {
		testQuadPattern("SPO?", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSPOG() throws NotFoundException {
		testQuadPattern("SPOG", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSPOG() throws NotFoundException {
		testQuadPattern("SPOG", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVPOG() throws NotFoundException {
		testQuadPattern("?POG", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVPOG() throws NotFoundException {
		testQuadPattern("?POG", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVPOV() throws NotFoundException {
		testQuadPattern("?PO?", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVPOV() throws NotFoundException {
		testQuadPattern("?PO?", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVVOV() throws NotFoundException {
		testQuadPattern("??O?", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVVOV() throws NotFoundException {
		testQuadPattern("??O?", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesVVOG() throws NotFoundException {
		testQuadPattern("??OG", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsVVOG() throws NotFoundException {
		testQuadPattern("??OG", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSVOV() throws NotFoundException {
		testQuadPattern("S?O?", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSVOV() throws NotFoundException {
		testQuadPattern("S?O?", hdtAnnotatedGraphs);
	}
	
	@Test
	public void annotatedTriplesSVOG() throws NotFoundException {
		testQuadPattern("S?OG", hdtAnnotatedTriples);
	}
	
	@Test
	public void annotatedGraphsSVOG() throws NotFoundException {
		testQuadPattern("S?OG", hdtAnnotatedGraphs);
	}
	
	/**
	 * Testing all possible combinations for given pattern
	 * @param pattern e.g. S???
	 * @throws NotFoundException
	 */
	public void testQuadPattern(String pattern, HDT hdt) throws NotFoundException {
		ArrayList<String> variable = new ArrayList<String>();
		variable.add("");
		
		ArrayList<String> subjects = 	pattern.charAt(0) == 'S' ? getSubjects() : variable;
		ArrayList<String> predicates = 	pattern.charAt(1) == 'P' ? getPredicates() : variable;
		ArrayList<String> objects = 	pattern.charAt(2) == 'O' ? getObjects() : variable;
		ArrayList<String> graphs = 		pattern.charAt(3) == 'G' ? getGraphs() : variable;
		
		for (String subject : subjects) {
			for (String predicate : predicates) {
				for (String object : objects) {
					for (String graph : graphs) {
						queriesPerformed++;
						System.out.println("Testing <" + (subject == "" ? "?" : subject) + "> <" + (predicate == "" ? "?" : predicate) + "> <" + (object == "" ? "?" : object) + "> <" + (graph == "" ? "?" : graph) + ">");
						ArrayList<String> expected = getResultAsList(jenaQuadDataset, subject, predicate, object, graph);
						ArrayList<String> actual = getResultAsList(hdt, subject, predicate, object, graph);
						
						assertTrue(expected.containsAll(actual));
						assertTrue(actual.containsAll(expected));
					}
				}
			}
		}
	}
	
	/**
	 * Testing all possible combinations for given pattern
	 * @param pattern e.g. S??
	 * @throws NotFoundException
	 */
	public void testTriplePattern(String pattern) throws NotFoundException {
		ArrayList<String> variable = new ArrayList<String>();
		variable.add("");
		
		ArrayList<String> subjects = 	pattern.charAt(0) == 'S' ? getSubjects() : variable;
		ArrayList<String> predicates = 	pattern.charAt(1) == 'P' ? getPredicates() : variable;
		ArrayList<String> objects = 	pattern.charAt(2) == 'O' ? getObjects() : variable;
		
		for (String subject : subjects) {
			for (String predicate : predicates) {
				for (String object : objects) {
					queriesPerformed++;
					//System.out.println("Testing <" + (subject == "" ? "?" : subject) + "> <" + (predicate == "" ? "?" : predicate) + "> <" + (object == "" ? "?" : object) + ">");
					ArrayList<String> expected = getResultAsList(jenaTripleDataset, subject, predicate, object);
					ArrayList<String> actual = getResultAsList(hdtTriples, subject, predicate, object);
					assertTrue(expected.containsAll(actual));
					assertTrue(actual.containsAll(expected));
				}
			}
		}
	}
	
	public static ArrayList<String> getSubjects() {
		ArrayList<String> subjects = new ArrayList<String>();
		Iterator<? extends CharSequence> it = hdtAnnotatedGraphs.getDictionary().getSubjects().getSortedEntries();
		while(it.hasNext()) {
			subjects.add(it.next().toString());
		}
		return subjects;
	}
	
	public static ArrayList<String> getObjects() {
		ArrayList<String> objects = new ArrayList<String>();
		Iterator<? extends CharSequence> it = hdtAnnotatedGraphs.getDictionary().getObjects().getSortedEntries();
		while(it.hasNext()) {
			objects.add(it.next().toString());
		}
		return objects;
	}
	
	public static ArrayList<String> getPredicates() {
		ArrayList<String> predicates = new ArrayList<String>();
		Iterator<? extends CharSequence> it = hdtAnnotatedGraphs.getDictionary().getPredicates().getSortedEntries();
		while(it.hasNext()) {
			predicates.add(it.next().toString());
		}
		return predicates;
	}
	
	public static ArrayList<String> getGraphs() {
		ArrayList<String> graphs = new ArrayList<String>();
		Iterator<? extends CharSequence> it = hdtAnnotatedGraphs.getDictionary().getGraphs().getSortedEntries();
		while(it.hasNext()) {
			graphs.add(it.next().toString());
		}
		return graphs;
	}
	
	public static ArrayList<String> getResultAsList(HDT hdt, CharSequence subject, CharSequence predicate, CharSequence object, CharSequence graph) throws NotFoundException {
		ArrayList<String> result = new ArrayList<String>();
		IteratorTripleString it = hdt.search(subject, predicate, object, graph);
		while(it.hasNext()) {
			QuadString ts = (QuadString) it.next();
			if(ts != null) {
				if(!"".equals(subject)) {
					assertEquals(ts.getSubject(), subject);
				}
				if(!"".equals(predicate)) {
					assertEquals(ts.getPredicate(), predicate);
				}
				if(!"".equals(object)) {
					assertEquals(ts.getObject(), object);
				}
				if(!"".equals(graph)) {
					assertEquals(ts.getGraph(), graph);
				}
				result.add(ts.toString());
			}
		}
		int estimatedNumberOfResults = (int) it.estimatedNumResults();
		switch(it.numResultEstimation()) {
		case EXACT:
			assertEquals(result.size(), estimatedNumberOfResults);
			break;
		case MORE_THAN:
			assertTrue(result.size() > estimatedNumberOfResults);
			break;
		case EQUAL_OR_MORE_THAN:
			assertTrue(result.size() >= estimatedNumberOfResults);
			break;
		case UP_TO:
			assertTrue(result.size() <= estimatedNumberOfResults);
			break;
		default:
			break;
		}
		return result;
	}
	
	public static ArrayList<String> getResultAsList(HDT hdt, CharSequence subject, CharSequence predicate, CharSequence object) throws NotFoundException {
		ArrayList<String> result = new ArrayList<String>();
		IteratorTripleString it = hdt.search(subject, predicate, object);
		while(it.hasNext()) {
			TripleString ts = it.next();
			if(ts != null) {
				if(!"".equals(subject)) {
					assertEquals(ts.getSubject(), subject);
				}
				if(!"".equals(predicate)) {
					assertEquals(ts.getPredicate(), predicate);
				}
				if(!"".equals(object)) {
					assertEquals(ts.getObject(), object);
				}
				result.add(ts.toString());
			}
		}
		int estimatedNumberOfResults = (int) it.estimatedNumResults();
		switch(it.numResultEstimation()) {
		case EXACT:
			assertEquals(result.size(), estimatedNumberOfResults);
			break;
		case MORE_THAN:
			assertTrue(result.size() > estimatedNumberOfResults);
			break;
		case EQUAL_OR_MORE_THAN:
			assertTrue(result.size() >= estimatedNumberOfResults);
			break;
		case UP_TO:
			assertTrue(result.size() <= estimatedNumberOfResults);
			break;
		default:
			break;
		}
		return result;
	}
	
	public static ArrayList<String> getResultAsList(Dataset dataset, String subject, String predicate, String object, String graph) throws NotFoundException {
		ArrayList<String> result = new ArrayList<String>();
		String sparqlQuery = "SELECT ?s ?p ?o ?g WHERE { "
				+ (subject.equals("") ? "" : "BIND (<"+subject+"> AS ?s)")
				+ (predicate.equals("") ? "" : "BIND (<"+predicate+"> AS ?p)")
				+ (object.equals("") ? "" : "BIND (<"+object+"> AS ?o)")
				+ (graph.equals("") ? "" : "BIND (<"+graph+"> AS ?g)")
				+ "GRAPH ?g { ?s ?p ?o} "
				+ "}";
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		
		try {
			ResultSet results = qe.execSelect();
			while(results.hasNext()) {
				QuerySolution solution = results.next();
				result.add(solution.get("s") + " " + solution.get("p") + " " + solution.get("o") + " " + solution.get("g"));
			}
		} finally {
			qe.close();				
		}
		return result;
	}
	
	public static ArrayList<String> getResultAsList(Dataset dataset, String subject, String predicate, String object) throws NotFoundException {
		ArrayList<String> result = new ArrayList<String>();
		String sparqlQuery = "SELECT ?s ?p ?o WHERE { "
				+ (subject.equals("") ? "" : "BIND (<"+subject+"> AS ?s)")
				+ (predicate.equals("") ? "" : "BIND (<"+predicate+"> AS ?p)")
				+ (object.equals("") ? "" : "BIND (<"+object+"> AS ?o)")
				+ "?s ?p ?o "
				+ "}";
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		
		try {
			ResultSet results = qe.execSelect();
			while(results.hasNext()) {
				QuerySolution solution = results.next();
				result.add(solution.get("s") + " " + solution.get("p") + " " + solution.get("o"));
			}
		} finally {
			qe.close();				
		}
		return result;
	}
	
	/*
	public static String correctQuotes(String string) {
		if(string.contains("^^")) {
			String[] parts = string.split("\\^\\^");
			return "\"" + parts[0] + "\"^^" + parts[1]; 
		}
		if(string.startsWith("localhost")) {
			return string;
		}
		if(string.contains("@")) {
			String[] parts = string.split("@");
			return "\"" + parts[0] + "\"@" + parts[1]; 
		}
		if(!string.startsWith("http")) {
			return "\"" + string + "\"";
		}
		return string;
	}
	
	public static String correctNumerics(String string) {
		if(string.contains("http://www.w3.org/2001/XMLSchema#integer") ||
		   string.contains("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/USD")) {
			String[] parts = string.split("\"");
			string = parts[1];
		} else if(string.startsWith("http")) {
			string = "<" + string + ">";
		}
		
		return string;
	}
	*/
}
