package org.rdfhdt.hdt.quads;

import org.rdfhdt.hdt.triples.TripleID;

public class QuadID extends TripleID{
	private int graph;
	
	/**
	 * Basic constructor
	 */
	public QuadID() {
		super();
	}
	
	/**
	 * Constructor
	 * 
	 * @param subject
	 *            The subject
	 * @param predicate
	 *            The predicate
	 * @param object
	 *            The object
	 * @param graph
	 * 			  The graph
	 */
	public QuadID(int subject, int predicate, int object, int graph) {
		super(subject, predicate, object);
		this.graph = graph;
	}
	
	/**
	 * Build a QuadID as a copy of another one.
	 * @param other
	 */
	public QuadID(QuadID other) {
		super(other);
		this.graph = other.graph;
	}
	
	/**
	 * @return the graph
	 */
	public int getGraph() {
		return graph;
	}
	
	/**
	 * Replace all components of a QuadID at once. Useful to reuse existing objects.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param graph
	 */
	public void setAll(int subject, int predicate, int object, int graph) {
		super.setAll(subject, predicate, object);
		this.graph = graph;
	}
	
	@Override
	public void assign(TripleID replacement) {
		super.setAll(replacement.getSubject(), replacement.getPredicate(), replacement.getObject());
        graph = ((QuadID) replacement).getGraph();
	}
	
	/**
	 * Set all components to zero.
	 */
	public void clear() {
		super.clear();
		graph = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " " + graph;
	}
	
	/**
	 * Get the pattern of the triple as String, such as "SP?".
	 * @return
	 */
	public String getPatternString() {
		return super.getPatternString() + (graph==0   ? '?' : 'G');
	}
}
