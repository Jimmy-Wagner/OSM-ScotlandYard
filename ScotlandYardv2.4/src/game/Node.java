package game;

/**
 * Describes the basic properties of a node of the map and the various methods
 * associated with this class.
 * 
 * @author Shashi Mittal
 * @version 1.0 (05-09-2002)
 */
public class Node {
	private int position;
	private Link[] links;

	/**
	 * Constructor for the Node Class Initializes the position and links of the
	 * node
	 */
	Node(int pos) {
		position = pos;
	}

	/** Initializes the Links of the Node object */
	public void addLink(Node n, int t) {
		if (links == null) {
			links = new Link[1];
			links[0] = new Link(this, t);
			links[0].setToNode(n);
		} else {
			Link[] temp = new Link[links.length + 1];
			for (int i = 0; i < links.length; i++)
				temp[i] = links[i];
			Link now = new Link(this, t);
			now.setToNode(n);
			temp[links.length] = now;
			links = temp;
		}
	}

	/** Returns the position of this node */
	public int getPosition() {
		return position;
	}

	/** Returns the links of the node */
	public Link[] getLinks() {
		return links;
	}
}
