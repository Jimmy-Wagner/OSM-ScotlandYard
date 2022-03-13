package game;

/**
 * Uses class Player to define the properties of a fugitive.
 * 
 * @author Shashi Mittal
 * @version 2.4 (19-APR-2010)
 */
public class Fugitive extends AbstractPlayer {
	private int blackTickets;
	
	/**
	 * Initializes the position of the fugitive
	 */
	Fugitive(Node n) {
		super(n);
	}

	/** Set the number of black tickets the fugitive has initially.
	 */
	public void setBlackTickets(int n){
		blackTickets = n;
	}
	
	/**
	 * This is called when a black ticket is used to move the fugitive.
	 * It decreases the number of black tickets by 1, if not already zero,
	 * and uses a black ticket for the move last made by the fugitive.
	 * @return true if the fugitive can use a black ticket, false otherwise
	 */
	public boolean useBlackTicket(){
		if (blackTickets > 0){
			blackTickets --;
			//retrieve the last move and change its type to black
			Move oldMove = prevPositions.getLast();
			Move newMove = new Move(oldMove.getNode(), BLACK);
			prevPositions.removeLast();
			prevPositions.add(newMove);
			return true;
		}
		return false;
	}
	
	/**
	 * @return the number of black tickets this fugitive has.
	 */
	public int getBlackTickets(){
		return blackTickets;
	}
	
	/**
	 * This method changes the position of the fugitive
	 * 
	 * @param n
	 *            the node to where fugitive has to be shifted
	 *@return the type of the link connecting n and the previous position of
	 *         fugitive
	 */
	public int changePosition(Node n) {
		int type = 0;
		Link[] lk = position.getLinks();
		position = n;
		for (int i = 0; i < lk.length; i++)
			if (n.equals(lk[i].getToNode()))
				type = lk[i].getType();
		prevPositions.add(new Move(n.getPosition(), type));
		if (type == FERRY){
			if (blackTickets > 0) blackTickets --;
			else throw new IllegalArgumentException();
		}
		return type;
	}
}
