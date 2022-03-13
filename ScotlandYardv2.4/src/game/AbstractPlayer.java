package game;

import java.util.LinkedList;

/**
 * Abstract class AbstractPlayer - Defines the properties of player
 * 
 * @author: Shashi Mittal Date: 14-10-2002
 */
public abstract class AbstractPlayer implements Transport {
	protected Node position; // The position of the player
	protected LinkedList<Move> prevPositions;
	
	/**
	 * The default constructor. This does not initialize the prevPositions array.
	 * Useful for initializing player in tree search.
	 */
	AbstractPlayer(){
		//Do nothing
	}
	
	/**
	 * This constructor initializes the position of this player
	 * 
	 * @param x
	 *            the position of this player
	 */
	AbstractPlayer(Node x) {
		position = x;
		prevPositions = new LinkedList<Move>();
		prevPositions.add(new Move(position.getPosition(), 0));
	}

	/**
	 * This method gives the position of this player
	 * 
	 * @return the position of the player
	 */
	public Node getPosition() {
		return position;
	}

	/**
	 * This method returns the previous positions of the player
	 * 
	 * @return the array of the previous node positions of the player
	 */
	public LinkedList<Move> getPrevPos() {
		return prevPositions;
	}

	/**
	 * This method changes the position of the Fugitive without adding it to the
	 * prevPositions array.
	 * 
	 * @param n
	 *            the new node position of the detective
	 */
	public void change(Node n) {
		position = n;
	}
}
