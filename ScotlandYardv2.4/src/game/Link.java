package game;

/**
 *Defines the basic properties of a Link.This class can be used to define a
 * graph or a multi-graph with weighted edges.
 * 
 * @author ShashiMittal
 * @version 1.0 (18-09-2002)
 */
public class Link {
	@SuppressWarnings("unused")
	private Node from;
	private Node to;
	private int type;

	/**
	 * This constructor initializes the given link
	 * 
	 * @param n
	 *            the starting node
	 * @param t
	 *            the type(weight of the node
	 */
	Link(Node n, int t) {
		from = n;
		type = t;
	}

	/**
	 * Initializes the to node of this link
	 * 
	 * @param x
	 *            the to node of this link
	 */
	public void setToNode(Node x) {
		to = x;
	}

	/**
	 * Returns the to node of this link
	 * 
	 * @return the to node of this link
	 */
	public Node getToNode() {
		return to;
	}

	/**
	 * This method is used to get the type(weight) of the link
	 * 
	 * @return the type of the link
	 */
	public int getType() {
		return type;
	}

}
