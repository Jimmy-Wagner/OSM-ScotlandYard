package game;

import i18n.I18n;

import java.awt.Point;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

/**
 * Defines the board for the game. The main workhorse of the game.
 * 
 * @author Shashi Mittal
 * @version 2.4 (19-APR-2010)
 */
public class TestBoard implements Transport, Comparator<Object>, Comparable<Object> {
	static private final int NO_OF_MOVES = 23;
	static private int NO_OF_DETECTIVES = 5;
	static private final int CHECK_POINTS = 5;
	static private final int INF = 100;
	static private final int WIN = 200;
	static private final int LOSE = -200;
	static private final int NBEST = 1000000000;
	static private final int BLACK_TICKETS = 5;

	static private int DEPTH = 2;
	static private int[][] shortestDistance;
	static private Node[] nodes;
	static private Point[] nodePositions;
	static private int[] checkPoints;
	static private int noOfNodes;

	private int currentMoves;
	private Detective[] detectives;
	private Fugitive MrX;

	/**
	 * This constructor initializes the board
	 */
	TestBoard() {
		currentMoves = 0;
		checkPoints = new int[CHECK_POINTS];
		for (int i = 0; i < CHECK_POINTS; i++)
			checkPoints[i] = 3 + 5 * i;
		readFile();
		readPosFile();
		detectives = new Detective[NO_OF_DETECTIVES];
		int partition = nodes.length / NO_OF_DETECTIVES - 1;
		int part = 1;

		if (nodes.length < 6){
			System.out.println("The selected gameplan is too small, please restart the program and select another mapcenter");
			System.exit(0);
		}

		for (int i = 0; i < NO_OF_DETECTIVES; i++) {
			int rnd = (int) (partition * Math.random());
			int pos = rnd + part;
			detectives[i] = new Detective(nodes[pos]);
			part += partition;
		}
		int xPos = 0;
		boolean done = true;
		do {
			xPos = 1 + (int) (noOfNodes * Math.random());
			for (int i = 0; i < NO_OF_DETECTIVES; i++)
				if (xPos == detectives[i].getPosition().getPosition())
					done = false;

		} while (!done);
		MrX = new Fugitive(nodes[xPos]);
		MrX.setBlackTickets(BLACK_TICKETS);
		shortestDistance = new int[nodes.length][nodes.length];
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++)
				shortestDistance[i][j] = weight(nodes[i], nodes[j]);
		shortestDistance = getShortestDistanceMatrix(shortestDistance, 1);
	}

	/**
	 * This constructor make a copy of the TestBoard board
	 * 
	 * @param board
	 *            the TestBoard whose copy has to be made
	 */
	private TestBoard(TestBoard board) {
		this.currentMoves = board.currentMoves;
		detectives = new Detective[board.detectives.length];
		for (int i = 0; i < detectives.length; i++) {
			detectives[i] = new Detective(board.detectives[i]);
		}
		Node n = board.MrX.getPosition();
		MrX = new Fugitive(n);
	}

	/**
	 * This method changes the difficulty level for the game
	 * 
	 * @param d
	 *            the required difficulty level
	 */
	public void setDepth(int d) {
		DEPTH = d;
	}

	/**
	 * This method reads the text file which contains the map
	 * Reads number of nodes and the connections of the nodes.
	 */
	private void readFile() {
		String fileName = "./SCOTMAP.TXT";
		try {
			File f = new File(fileName);
			if (!f.exists())
				throw new IOException();
			RandomAccessFile map = new RandomAccessFile(f, "r");
			String buffer = map.readLine();
			StringTokenizer token;
			token = new StringTokenizer(buffer);
			// number of stop positions
			noOfNodes = Integer.parseInt(token.nextToken());
			nodes = new Node[noOfNodes];
			for (int i = 0; i < nodes.length; i++)
				nodes[i] = new Node(i);
			// number of links between nodes
			int lks = Integer.parseInt(token.nextToken());
			for (int i = 0; i < lks; i++) {
				buffer = map.readLine();
				token = new StringTokenizer(buffer);
				int node1 = Integer.parseInt(token.nextToken());
				int node2 = Integer.parseInt(token.nextToken());
				String strType = token.nextToken();
				int type = INF;
				if (strType.equals("T"))
					type = TAXI;
				if (strType.equals("B"))
					type = BUS;
				if (strType.equals("U"))
					type = UG;
				if (strType.equals("F"))
					type = FERRY;
				nodes[node1].addLink(nodes[node2], type);
				nodes[node2].addLink(nodes[node1], type);
			}
		} catch (Exception e) {
			 JOptionPane.showMessageDialog(null, I18n.tr("ErrorFileNotFound", fileName), 
					 I18n.tr("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 * This method reads the text file which contains the positions of the nodes
	 * on the map.
	 * Nodes are numbered from 0 to maxSize-1 and saved with their position as a {@link Point} object
	 */
	private void readPosFile() {
        String fileName = "./SCOTPOS.TXT";
		try {
			File f = new File(fileName);
			if (!f.exists())
				throw new IOException();
			RandomAccessFile map = new RandomAccessFile(f, "r");
			String buffer = map.readLine();
			StringTokenizer token = new StringTokenizer(buffer);
			noOfNodes = Integer.parseInt(token.nextToken());
			nodePositions = new Point[noOfNodes];

			for (int i = 0; i < noOfNodes; i++) {
				buffer = map.readLine();
				token = new StringTokenizer(buffer);
				int node = Integer.parseInt(token.nextToken());
				int x = Integer.parseInt(token.nextToken());
				int y = Integer.parseInt(token.nextToken());

				nodePositions[node] = new Point(x, y);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, I18n.tr("ErrorFileNotFound", fileName), 
					I18n.tr("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 * This method prints the board i.e what are the nodes what are the links.
	 * It is useful for testing if the map which has been entered is correct or
	 * not
	 */
	public void printBoard() {
		for (int i = 0; i < nodes.length; i++) {
			System.out.print("\nNode:" + nodes[i].getPosition() + "\t");
			Link[] links = nodes[i].getLinks();
			for (int j = 0; j < links.length; j++) {
				Node to = links[j].getToNode();
				System.out.print("\t" + to.getPosition());
				System.out.println("\t" + links[j].getType());
			}
		}
	}

	/**
	 * This method returns the distance between the two nodes
	 * 
	 * @param x
	 *            ,y the two nodes given
	 * @return 0 if x and y are the same nodes, 100 if they are not adjacent
	 *         nodes, 50 if they are part of a ferry route, 1 if the two nodes
	 *         are connected by any other transport mode.
	 */
	private int weight(Node x, Node y) {
		if (x.equals(y))
			return 0;
		int weight = INF;
		Link[] lk = x.getLinks();
		for (int i = 0; i < lk.length; i++) {
			Node n = lk[i].getToNode();
			if (n.equals(y))
				weight = lk[i].getType();
		}
		if (weight != INF && weight != FERRY)
			weight = 1;
		return weight;
	}

	/**
	 * This method evaluates the shortest distance between all the possible
	 * nodes. It uses the Floyd-Warshall's Algorithm
	 */
	private int[][] getShortestDistanceMatrix(int[][] mat, int k) {
		if (k == nodes.length - 1)
			return mat;
		int newMat[][] = new int[nodes.length][nodes.length];
		{
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < nodes.length; j++)
					newMat[i][j] = Math.min(mat[i][j], mat[i][k] + mat[k][j]);
		}
		k = k + 1;
		return getShortestDistanceMatrix(newMat, k);
	}

	/**
	 * Prints the shortest distance matrix
	 */
	public void test() {
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++)
				System.out.println("Weight function for " + i + " " + j + " = "
						+ shortestDistance[i][j]);
	}

	public static int getNumberOfDetectives() {
		return TestBoard.NO_OF_DETECTIVES;
	}

	/**
	 * Checks if the move to along the link l is legal or not for the fugitive
	 * 
	 * @param l
	 *            the link to be tested
	 *@return true if the move is legal otherwise it returns false
	 */
	private boolean isLegalMove(Link l) {
		if (l.getType() == FERRY && MrX.getBlackTickets() <= 0)
			return false;
		boolean canMove = true;
		for (int i = 0; i < detectives.length; i++) {
			Node n = detectives[i].getPosition();
			if (n.getPosition() == l.getToNode().getPosition())
				canMove = false;
		}
		return canMove;
	}

	/**
	 * This method returns all the possible moves for a given detective
	 * 
	 * @param i
	 *            the detective index of the detective whose possible moves we
	 *            want
	 *@return the possible moves of this detective in a TreeSet. If this
	 *			detective cannot move, it returns null.
	 */
	public TreeSet<Move> getDetectivePossibleMoves(int i) {
		return detectives[i].getPossibleMoves(this);
	}

	/**
	 * This method is used to change the position of a detective
	 * 
	 * @param i
	 *            the index of the detective whose position we want to change
	 *@param move
	 *            the new move for this detective
	 */
	public void changeDetectivePosition(int i, Move move) {
		detectives[i].changePosition(nodes[move.getNode()], move.getType());
	}

	/**
	 * Checks whether the machine has won
	 * 
	 * @return true if machine has won,otherwise false
	 */
	public boolean isMachineWin() {
		boolean noneCanMove = true;
		for (int i = 0; i < NO_OF_DETECTIVES; i++)
			if (detectives[i].canMove(this))
				noneCanMove = false;
		return ((currentMoves == NO_OF_MOVES) || noneCanMove);
	}

	/**
	 * Checks if the user has won
	 * 
	 * @return true if the user has won, otherwise false
	 */
	public boolean isUserWin() {
		Link[] xLinks = MrX.getPosition().getLinks();
		boolean isBlocked = true;
		for (int i = 0; i < xLinks.length; i++) {
			Node xNode = xLinks[i].getToNode();
			boolean thisIsOccupied = false;
			for (int j = 0; j < NO_OF_DETECTIVES; j++) {
				Node dNode = detectives[j].getPosition();
				if (dNode.equals(xNode))
					thisIsOccupied = true;
			}
			if (!thisIsOccupied)
				isBlocked = false;
		}
		boolean isCaptured = false;
		for (int i = 0; i < NO_OF_DETECTIVES; i++) {
			Node detNode = detectives[i].getPosition();
			if (detNode.equals(MrX.getPosition()))
				isCaptured = true;
		}
		return (isBlocked || isCaptured);
	}

	/**
	 * This method makes a random move for the MrX
	 * 
	 * @return a random legal Node position for the MrX
	 */
	@SuppressWarnings("unused")
	private Node randomMove() {
		Node n = MrX.getPosition();
		Node toNode = n;
		Link[] lk = n.getLinks();
		boolean done = false;
		while (!done) {
			int rnd = (int) (lk.length * Math.random());
			if (isLegalMove(lk[rnd]))
				done = true;
		}
		return toNode;
	}

	/**
	 * This is the most important method of this class. It returns the best
	 * possible move by calling the evaluate() method.
	 * 
	 * @return the best node position of MrX
	 */
	private Node bestMove(AtomicBoolean useBlackTicket) {
		Node n = MrX.getPosition();
		Link[] lk = n.getLinks();
		int score[] = new int[lk.length];
		Node possibleNodes[] = new Node[lk.length];
		int legalMoves = 0;
		int beta = LOSE;
		for (int i = 0; i < lk.length; i++) {
			if (isLegalMove(lk[i])) {
				if (legalMoves > 0 && beta < score[legalMoves - 1])
					beta = score[legalMoves - 1];
				TestBoard board = new TestBoard(this);
				board.MrX.change(lk[i].getToNode());
				possibleNodes[legalMoves] = lk[i].getToNode();
				score[legalMoves] = evaluateMove(board, false, 0, beta);
				legalMoves++;
			}
		}
		Node toNode = possibleNodes[0];
		int max = score[0];
		//System.out.println("Scores at Node " + n.getPosition());
		for (int i = 0; i < legalMoves; i++) {
			//System.out.println("Position " + possibleNodes[i].getPosition() + " Score " + score[i]);
			if (max < score[i]) {
				toNode = possibleNodes[i];
				max = score[i];
			}
		}
		//System.out.println("");

		useBlackTicket.set(isBlackTicketUsable(n, possibleNodes, legalMoves, score));
		return toNode;
	}

	/**
	 * Prints all the possible detective moves for the current board positions
	 * of the detectives, together with the corresponding scores for each new
	 * positions of the detectives.
	 */
	public void printPossibleDetectiveMoves() {
		System.out.println("Generating detective moves:");
		LinkedList<TestBoard> possibleMoves = new LinkedList<TestBoard>();
		generateDetectiveMoves(this, possibleMoves, 0);
		Object[] moves = possibleMoves.toArray();
		Arrays.sort(moves);
		int size = moves.length;
		for (int count = 0; count < size && count <= NBEST; count++) {
			TestBoard b = (TestBoard) moves[count];
			for (int i = 0; i < NO_OF_DETECTIVES; i++)
				System.out.print(b.detectives[i].getPosition().getPosition() + " ");
			System.out.println("Score " + scoreBoard(b) + "");
		}
		possibleMoves = null;
		moves = null;
		System.gc();
	}

	/**
	 * This method evaluates the position of the Node using depth first shallow
	 * search algorithm
	 * 
	 * @param b
	 *            the initial TestBoard passed by the user
	 *@param depth
	 *            the current depth of the recursion tree (in this version,
	 *            depth is set to 0)
	 *@param isMachineMove
	 *            true if the next move is of the machine,else returns false
	 */
	private int evaluateMove(TestBoard b, boolean isMachineMove, int depth, int alphabeta) {
		if (depth == DEPTH)
			return scoreBoard(b);
		else if (isMachineMove) {
			Node dPos = b.MrX.getPosition();
			Link[] lk = dPos.getLinks();
			int score[] = new int[lk.length];
			int legalMoves = 0;
			int beta = LOSE;
			for (int i = 0; i < lk.length; i++) {
				Node newPos = lk[i].getToNode();
				if (!b.isLegalMove(lk[i]))
					continue;

				if (legalMoves > 0 && beta < score[legalMoves - 1])
					beta = score[legalMoves - 1];
				TestBoard board = new TestBoard(b);
				board.MrX.change(newPos);
				if (board.isUserWin())
					score[legalMoves] = LOSE;
				else if (board.isMachineWin())
					score[legalMoves] = WIN;
				else
					score[legalMoves] = evaluateMove(board, false, depth + 1, beta);

				// alpha cutoff here
				if (score[legalMoves] > alphabeta) {
					return score[legalMoves];
				}
				legalMoves++;
			}
			int max = score[0];
			for (int i = 0; i < legalMoves; i++)
				if (score[i] > max)
					max = score[i];
			return max;
		} else {
			LinkedList<TestBoard> possibleMoves = new LinkedList<TestBoard>();
			generateDetectiveMoves(b, possibleMoves, 0);
			Object[] possibleMovesArray = possibleMoves.toArray();
			int score[] = new int[possibleMovesArray.length];
			int alpha = WIN;
			for (int i = 0; i < score.length && i < NBEST; i++) {
				if (i > 0 && alpha > score[i - 1])
					alpha = score[i - 1];
				TestBoard board = (TestBoard) possibleMovesArray[i];
				if (board.isUserWin())
					score[i] = LOSE;
				else if (board.isMachineWin())
					score[i] = WIN;
				else
					score[i] = evaluateMove(board, true, depth + 1, alpha);

				// beta cutoff here
				if (score[i] < alphabeta) {
					return score[i];
				}
			}
			possibleMoves = null;
			possibleMovesArray = null;
			// Call the garbage collector to reclaim unused memory
			System.gc();
			int min = score[0];
			for (int i = 0; i < score.length && i < NBEST; i++)
				if (score[i] < min)
					min = score[i];
			return min;
		}
	}

	/**
	 * This method recursively generates all the possible detective moves, given
	 * a board as the input.
	 * 
	 * @param board
	 *            The board from which to generate all the possible detective
	 *            moves.
	 * @param possibleMoves
	 *            The linked list in which all the detective moves are stored.
	 * @param detIndex
	 *            the detective index - to be set to 0 when first called.
	 */
	private void generateDetectiveMoves(TestBoard board, LinkedList<TestBoard> possibleMoves,
			int detIndex) {
		if (!board.detectives[detIndex].canMove(board)) {
			// keep the position of this detective the same
			// i.e. do nothing
			if (detIndex == NO_OF_DETECTIVES - 1)
				possibleMoves.add(board);
			else
				generateDetectiveMoves(board, possibleMoves, detIndex + 1);
		} else {
			TreeSet<Move> m = board.detectives[detIndex].getPossibleMoves(board);
			int size = m.size();
			for (int i = 0; i < size; i++) {
				Move l = m.pollFirst();
				TestBoard temp = new TestBoard(board);
				temp.detectives[detIndex].change(nodes[l.getNode()], l.getType());
				if (detIndex == NO_OF_DETECTIVES - 1) {
					possibleMoves.add(temp);
				} else
					generateDetectiveMoves(temp, possibleMoves, detIndex + 1);
			}
			m = null;
		}
	}

	/**
	 * This method recursively generates all the possible detective moves, given
	 * initial board b This method is supposed to return a trimmed list of the
	 * possible detective moves: Only those moves are returned, which the human
	 * player will make logically.
	 */
	@SuppressWarnings("unused")
	private void generatePrunedDetectiveMoves(TestBoard board, LinkedList<TestBoard> possibleMoves,
			int detIndex) {
		if (!board.detectives[detIndex].canMove(board)) {
			// keep the position of this detective the same
			// i.e. do nothing
			if (detIndex == NO_OF_DETECTIVES - 1)
				possibleMoves.add(board);
			else
				generateDetectiveMoves(board, possibleMoves, detIndex + 1);
		} else {
			TreeSet<Move> m = board.detectives[detIndex].getPossibleMoves(board);
			int size = m.size();
			for (int i = 0; i < size; i++) {
				Move l = m.pollFirst();
				TestBoard temp = new TestBoard(board);
				temp.detectives[detIndex].change(nodes[l.getNode()], l.getType());
				if (detIndex == NO_OF_DETECTIVES - 1) {
					possibleMoves.add(temp);
				} else
					generatePrunedDetectiveMoves(temp, possibleMoves, detIndex + 1);
			}
			m = null;
		}
	}

	/**
	 * This method checks if Mr. X has to reveal his position at this move or not.
	 * 
	 * @return true if Mr. X has to reveal himself at this move, false
	 *         otherwise.
	 */
	public boolean isCheckPoint() {
		boolean isCheckPoint = false;
		for (int i = 0; i < checkPoints.length; i++)
			if (currentMoves == checkPoints[i])
				isCheckPoint = true;
		return isCheckPoint;
	}

	/**
	 * This method checks if Mr. X has to reveal his position at a given move or not
	 * 
	 * @param move
	 *            the index of the move for which we want to check if it is a
	 *            checkpoint
	 * @return true if move is a checkpoint, false otherwise
	 */
	public boolean isCheckPoint(int move) {
		boolean isCheckPoint = false;
		for (int i = 0; i < checkPoints.length; i++)
			if (move == checkPoints[i])
				isCheckPoint = true;
		return isCheckPoint;
	}

	/**
	 * This method is called to see if it makes sense to use a black ticket to
	 * go from the source node to the target node for Mr. X. The black ticket
	 * will NOT be used in the following circumstances: 1. Mr. X will reveal its
	 * position in this move. 2. The from node has transportation of one type
	 * only 3. There is only one possible move for Mr. X
	 * 
	 * @param from
	 *            the source node
	 * @param to
	 *            the target node
	 * @return true if black ticket should be used for this move, false
	 *         otherwise
	 */
	private boolean isBlackTicketUsable(Node from, Node[] possibleNodes, int legalMoves, int[] score) {
		// First find all the valid moves from the source node
		// This is a code duplication, should be fixed later!
		Link[] links = from.getLinks();

		// If only one legal move from this move then obviously using
		// black ticket makes no sense
		if (legalMoves == 1)
			return false;

		// If no black tickets, then obviously Mr. X cannot use them
		if (MrX.getBlackTickets() <= 0)
			return false;

		// If Mr. X is going to reveal itself in this move or move after this,
		// then no need of using black ticket
		if (isCheckPoint(currentMoves + 1) || isCheckPoint(currentMoves + 2))
			return false;

		// check if there are multiple destinations in possibleNodes. If not,
		// then don't use the black ticket.
		int pos1 = possibleNodes[0].getPosition();
		boolean multi = false;
		for (int i = 1; i < legalMoves; i++)
			if (possibleNodes[i].getPosition() != pos1)
				multi = true;
		if (!multi)
			return false;

		// check if there are multiple transports available from the source.
		// If not, then don't use the black ticket.
		int taxiLink = 0;
		int busLink = 0;
		int ugLink = 0;
		int ferryLink = 0;
		for (int i = 0; i < links.length; i++) {
			boolean canVisit = false;
			for (int j = 0; j < legalMoves; j++)
				if (possibleNodes[j] == links[i].getToNode() && score[j] > 0)
					canVisit = true;
			if (canVisit && links[i].getType() == TAXI)
				taxiLink = 1;
			if (canVisit && links[i].getType() == BUS)
				busLink = 1;
			if (canVisit && links[i].getType() == UG)
				ugLink = 1;
			if (canVisit && links[i].getType() == FERRY)
				ferryLink = 1;
		}
		if (taxiLink + busLink + ugLink + ferryLink <= 1)
			return false;

		// Don't use consecutive black tickets
		if (MrX.prevPositions.getLast().getType() == BLACK)
			return false;

		// OK, the false rules end here. Now the true rules: when to use black
		// tickets

		// if revealed in this move or the previous one, use black ticket
		if (isCheckPoint() || isCheckPoint(currentMoves - 1))
			return true;

		return false;
	}

	/**
	 * This method evaluates the given board
	 * 
	 * @param board
	 *            the given board
	 *@return the score for this board
	 */
	private static int scoreBoard(TestBoard board) {
		Detective[] det = board.detectives;
		Fugitive mrx = board.MrX;
		int minDistance = INF;
		int totalMobility = 0;
		for (int count = 0; count < NO_OF_DETECTIVES; count++) {
			int distance = shortestDistance[det[count].getPosition().getPosition()][mrx
					.getPosition().getPosition()];
			if (distance < minDistance)
				minDistance = distance;
			// totalMobility -= (det[count].mobility() / 3);
		}

		Node n = board.MrX.getPosition();
		Link[] lk = n.getLinks();
		for (int i = 0; i < lk.length; i++)
			if (board.isLegalMove(lk[i]))
				totalMobility++;

		int score = 20 * minDistance + totalMobility;
		return score;
	}

	/**
	 * This method compares two objects of this class depending on the score of
	 * the boards
	 * 
	 * @param b1
	 *            the first board
	 *@param b2
	 *            the second board
	 *@return negative if score of b1 less then score of b2 positive otherwise
	 */
	public int compare(Object b1, Object b2) {
		TestBoard board1 = (TestBoard) b1;
		TestBoard board2 = (TestBoard) b2;
		Detective[] det1 = board1.detectives;
		Detective[] det2 = board2.detectives;

		int[] d1 = new int[NO_OF_DETECTIVES];
		int[] d2 = new int[NO_OF_DETECTIVES];
		for (int i = 0; i < NO_OF_DETECTIVES; i++) {
			d1[i] = shortestDistance[det1[i].getPosition().getPosition()][board1.MrX.getPosition()
					.getPosition()];
			d2[i] = shortestDistance[det2[i].getPosition().getPosition()][board2.MrX.getPosition()
					.getPosition()];
		}
		Arrays.sort(d1);
		Arrays.sort(d2);
		int count = 0;
		while ((count < NO_OF_DETECTIVES) && (d1[count] == d2[count]))
			count++;

		if (count >= NO_OF_DETECTIVES)
			return 0;
		else
			return d1[count] - d2[count];
	}

	/**
	 * This method checks whether two boards are equal
	 * 
	 * @param b1
	 *            the first board
	 *@param b2
	 *            the second board
	 *@return true if the boards are equal,false otherwise
	 */
	public boolean equal(TestBoard b1, TestBoard b2) {
		return (compare(b1, b2) == 0);
	}

	/**
	 * This method compares this board to another board o
	 * 
	 * @param o
	 *            the board with which this is to be compared
	 *@return similar to the compare() method
	 */
	public int compareTo(Object o) {
		TestBoard b = (TestBoard) o;
		return compare(this, b);
	}

	/**
	 * This method computes a move for Mr. X and returns that move.
	 * 
	 * @return The move computed for Mr. X.
	 */
	public Move moveMrX() {
		AtomicBoolean useBlackTicket = new AtomicBoolean();
		Node bestNode = bestMove(useBlackTicket);
		int type = MrX.changePosition(bestNode);
		int pos = MrX.getPosition().getPosition();
		if (useBlackTicket.get()) {
			MrX.useBlackTicket();
			type = BLACK;
		}
		currentMoves++;
		return (new Move(pos, type));
	}

	/**
	 * This method is used to get the detectives of this board.
	 * 
	 * @return the array containing the detectives of the current game.
	 */
	public Detective[] getDetectives() {
		return detectives;
	}

	/**
	 * This method is used to get the MrX of this object.
	 * 
	 * @return the MrX of this object.
	 */
	public Fugitive getMrX() {
		return MrX;
	}

	/**
	 * This method returns the currentMoves of this object.
	 * 
	 * @return the currentMoves of this object
	 */
	public int getCurrentMoves() {
		return currentMoves;
	}

	/**
	 * String representation of this board.
	 * 
	 * @return the score of this board in String form.
	 */
	public String toString() {
		return "" + scoreBoard(this);
	}

	/**
	 * Returns the pixel coordinates of the specified node on the map.
	 */
	public Point getPoint(int nodeIndex) {
		return nodePositions[nodeIndex];
	}
}
