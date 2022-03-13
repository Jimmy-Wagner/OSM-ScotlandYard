package game;

import i18n.I18n;
import java.util.TreeSet;

/**
 * Inherits the class AbstractPlayer to define a detective
 * 
 * @author Shashi Mittal
 * @version 2.4 (19-APR-2010)
 */
public class Detective extends AbstractPlayer {

	private int taxiTickets;
	private int busTickets;
	private int ugTickets;

	/**
	 * Initializes the position of the detective
	 * 
	 * @param n
	 *            the node position of the detective
	 */
	Detective(Node n) {
		super(n);
		taxiTickets = 10;
		busTickets = 8;
		ugTickets = 4;
	}

	/**
	 * Initializes the detective with the same position and number of tickets as detective d.
	 * Note that this does not copy the prevPositions linked list, to make this constructor
	 * more efficient. It is supposed to be used mainly in the tree search for best moves.
	 * 
	 * @param d the detective whose copy is to be made
	 */
	Detective(Detective d){
		this.taxiTickets = d.taxiTickets;
		this.busTickets = d.busTickets;
		this.ugTickets = d.ugTickets;
		this.position = d.position;
	}
	
	/**
	 * This method changes the position of the detective provided he has the
	 * requisite tickets and the availability of that particular node
	 */
	public void changePosition(Node n, int ticket) {
		boolean canMove = false;
		Link[] links = getPosition().getLinks();
		for (int i = 0; i < links.length; i++) {
			Node temp = links[i].getToNode();
			int type = links[i].getType();
			if ((temp.equals(n)) && (ticket == type))
				canMove = true;
		}
		if (canMove) {
			switch (ticket) {
			case TAXI:
				if (taxiTickets == 0)
					canMove = false;
				else
					taxiTickets--;
				break;
			case BUS:
				if (busTickets == 0)
					canMove = false;
				else
					busTickets--;
				break;
			case UG:
				if (ugTickets == 0)
					canMove = false;
				else
					ugTickets--;
			}
		}
		if (canMove) {
			prevPositions.add(new Move(n.getPosition(), ticket));
			position = n;
		}
	}

	/**
	 * This method is used to make the necessary changes in case the detective
	 * cannot move i.e. when the detective is stranded either because it does
	 * not have the required ticket, or all the neighbor nodes are occupied by
	 * other detectives.
	 */
	public void setStaticState() {
		Move m = prevPositions.getLast();
		prevPositions.add(new Move(m.getNode(), NONE));
	}

	/**
	 * Checks if the detective can make a move or not
	 * 
	 * @return true if the detective can move,false if the detective is stranded
	 * @param board
	 *            the current board positions
	 */
	public boolean canMove(TestBoard board) {
		Node n = getPosition();
		Link[] lk = n.getLinks();
		boolean canMove = false;
		for (int i = 0; i < lk.length; i++) {
			boolean canGoToThisNode = true;
			Node toNode = lk[i].getToNode();
			Detective[] det = board.getDetectives();
			for (int j = 0; j < det.length; j++)
				if (toNode.equals(det[j].getPosition()))
					canGoToThisNode = false;

			int t = lk[i].getType();
			switch (t) {
			case TAXI:
				if (taxiTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
				break;
			case BUS:
				if (busTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
				break;
			case UG:
				if (ugTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
				break;
			case FERRY:
				canGoToThisNode = false;
			}
			if (canGoToThisNode)
				canMove = true;
		}
		return canMove;
	}

	/**
	 * This method returns the possible moves of the detective in a TreeSet
	 * 
	 * @param board
	 *            the board of which this detective is a part of
	 * @return all the possible moves in TreeSet
	 */
	public TreeSet<Move> getPossibleMoves(TestBoard board) {
		if (!canMove(board))
			return null;
		Node n = getPosition();
		Link[] lk = n.getLinks();
		TreeSet<Move> possibleMoves = new TreeSet<Move>();
		for (int i = 0; i < lk.length; i++) {
			//cannot use ferry, so ignore if the link is of type ferry
			if (lk[i].getType() == FERRY) continue;
			
			boolean canGoToThisNode = true;
			Node toNode = lk[i].getToNode();
			Detective[] det = board.getDetectives();
			for (int j = 0; j < det.length; j++)
				if (toNode.equals(det[j].getPosition()))
					canGoToThisNode = false;

			int t = lk[i].getType();
			switch (t) {
			case TAXI:
				if (taxiTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
				break;
			case BUS:
				if (busTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
				break;
			case UG:
				if (ugTickets <= 0)
					if (canGoToThisNode)
						canGoToThisNode = false;
			}
			if (canGoToThisNode)
				possibleMoves.add(new Move(toNode.getPosition(), t));
		}
		return possibleMoves;
	}

	/** This method displays the current status of the detective */
	public String toString() {
		return I18n.tr("DetectiveStatus", getPosition().getPosition(), taxiTickets, busTickets, ugTickets);
	}

	/**
	 * This method calculates the mobility of the detective the mobility is a
	 * parameter which depends on the number of tickets of the detectives and
	 * on the ways the detective can go from his current position to adjacent
	 * positions.
	 * 
	 * @return the mobility of this detective
	 */
	public int mobility() {
		int mobility = 0; //3 * taxiTickets + 2 * busTickets + ugTickets;
		for (int i = 0; i < position.getLinks().length; i++)
			mobility += position.getLinks()[i].getType();
		return mobility;
	}
	
	/**
	 * This method is a "quick and dirty" way of changing the position of a detective.
	 * It does not verify if the move is valid or not. This is supposed to be used in
	 * the tree search algorithm for finding best moves.
	 * 
	 * @param n the new node position to which the detective will be moved to 
	 * @param ticket the ticket used for moving to the new position
	 */
	public void change(Node n, int ticket) {
		position = n;
		if (ticket == TAXI) taxiTickets --;
		else if (ticket == BUS) busTickets --;
		else if (ticket == UG) ugTickets --;
	}
}
