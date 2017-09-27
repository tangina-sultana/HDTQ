package org.rdfhdt.hdt.quads;

import org.rdfhdt.hdt.triples.TripleString;

public class QuadString extends TripleString{
	private CharSequence graph;
	
	public QuadString() {
		super();
		this.graph = null;
	}
	
	/**
	 * Basic constructor
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
	public QuadString(CharSequence subject, CharSequence predicate, CharSequence object, CharSequence graph) {
		super(subject, predicate, object);
		this.graph = graph;
	}
	
	/**
	 * Copy constructor
	 */
	public QuadString(QuadString other) {
		this(other.getSubject(), other.getPredicate(), other.getObject(), other.graph);
	}
	
	/**
	 * @return the graph
	 */
	public CharSequence getGraph() {
		return graph;
	}

	/**
	 * @param graph
	 *            the graph to set
	 */
	public void setGraph(CharSequence graph) {
		this.graph = graph;
	}
	
	public void setAll(CharSequence subject, CharSequence predicate, CharSequence object, CharSequence graph) {
		setAll(subject, predicate, object);
		this.graph = graph;
	}
	
	/**
	 * Set all components to ""
	 */
	public void clear() {
		super.clear();
		graph = "";
	}
	
	/**
	 * Checks wether all components are empty.
	 * @return
	 */
	public boolean isEmpty() {
		return super.isEmpty() && graph.length() == 0;
	}
	
	/**
	 * Checks wether any component is empty.
	 * @return
	 */
	public boolean hasEmpty() {
		return super.hasEmpty() || graph.length() == 0;
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
}
