package game;

import i18n.I18n;
import java.util.StringTokenizer;
import java.util.Comparator;

/**
 * This class encapsulates a move for the palyers.It has two data members,one
 * for representing the node position and the other for representing the ticket
 * Type for the move
 * 
 * @author Shashi Mittal
 * @version 2.4 (19-APR-2010)
 */
public class Move implements Comparator<Object>, Transport, Comparable<Object> {
	int nodeIndex;
	int ticketType;

	/**
	 * This constructor initializes the data members of the Move
	 * 
	 * @param n
	 *            the node index
	 *@param t
	 *            the ticket Type
	 */
	public Move(int n, int t) {
		nodeIndex = n;
		ticketType = t;
	}

	/**
	 * This constructor initializes the fields using the string representation
	 * of the object of this class e.g. the method toString() returns the string
	 * representation of objects of this class as, for example, 46(Taxi) here 46
	 * is the node position and Taxi is the ticket type. This method takes this
	 * string as the input and then tokenizes it to get the fields.
	 * 
	 * @param str
	 *            the string representation of a Move object
	 */
	public Move(String str) {
		StringTokenizer getFields = new StringTokenizer(str, " (");
		nodeIndex = Integer.parseInt(getFields.nextToken());
		String type = getFields.nextToken();
		type = type.substring(0, type.length() - 1);
		if (type.equals("None"))
			ticketType = NONE;
		if (type.equals(I18n.tr("TaxiTicket")))
			ticketType = TAXI;
		if (type.equals(I18n.tr("BusTicket")))
			ticketType = BUS;
		else if (type.equals(I18n.tr("UndergroundTicket")))
			ticketType = UG;
		else if (type.equals("FERRY") || type.equals("BLACK"))
			ticketType = BLACK;
	}

	/**
	 * This method returns the score for this object which is used in the
	 * equals(),compare() and compareTo() methods
	 * 
	 * @return the score for this Move
	 */
	public int getScore() {
		return 10 * nodeIndex + ticketType;
	}

	/**
	 * This method is used to get the nodeIndex of this class
	 * 
	 * @return the node of the object
	 */
	public int getNode() {
		return nodeIndex;
	}

	/**
	 * This method returns the ticket type
	 * 
	 * @return the ticket type in the object
	 */
	public int getType() {
		return ticketType;
	}

	/**
	 * This method gives a simple string representation of the objects of this
	 * class
	 * 
	 * @return the string representation of object of this class
	 */
	public String toString() {
		return "" + nodeIndex + " (" + toStringTicket() + ")";
	}

	/**
	 * This method returns the string representation of this move (which is subsequently
	 * displayed in the combo box).
	 * @return the string representation of the current move using the current locale
	 */
    public String toDisplayString() {
        return I18n.tr("Move", nodeIndex, toStringTicket());
    }
    
	/**
	 * This method returns the string representation of the ticket which
	 * contained in this object.
	 * 
	 * @return the String representation of the ticket type of this class
	 */
	public String toStringTicket() {
		String type = "None";
		if (ticketType == TAXI)
			type = I18n.tr("TaxiTicket");
		if (ticketType == BUS)
			type = I18n.tr("BusTicket");
		else if (ticketType == UG)
			type = I18n.tr("UndergroundTicket");
		else if (ticketType == BLACK || ticketType == FERRY)
			type = "Black";
		return type;
	}

	/**
	 * Compares two objects of this class and returns the score depending on the
	 * values given by the getScore() method
	 * 
	 * @param o1
	 *            the first Move object
	 *@param o2
	 *            two second Move object
	 *@return positive if score of o1 is greater than o2 0 if the scores are
	 *         equal a negative value otherwise
	 */
	public int compare(Object o1, Object o2) {
		Move m1 = (Move) o1;
		Move m2 = (Move) o2;
		if (m1.getScore() < m2.getScore())
			return -1;
		else if (m1.getScore() == m2.getScore())
			return 0;
		else
			return 1;
	}

	/**
	 * Checks if two objects of this class have the same score
	 * 
	 * @param m1
	 *            the first Move object
	 *@param m2
	 *            the second Move object
	 *@return true if the scores are equal,false otherwise
	 */
	public boolean equal(Move m1, Move m2) {
		return (m1.getScore() == m2.getScore());
	}

	/**
	 * This method compares this object to another objects
	 * 
	 * @param o
	 *            the object which is to be compared to this class
	 *@return same as that given by int compare(Object o1,Object o2)
	 */
	public int compareTo(Object o) {
		Move m = (Move) o;
		return this.getScore() - m.getScore();
	}
}
