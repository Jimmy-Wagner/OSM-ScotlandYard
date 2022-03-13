package game;

import i18n.I18n;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A Dialog Box for showing a Table.
 */
class TableDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	TableDialog(JFrame parentFrame, String title, TestBoard b, PlayGame playGame, boolean revealAll) {
		super(parentFrame, title, false);
		setResizable(true);
		TableModel model = new PreviousMoves(b, revealAll);
		JTable table = new JTable(model);
		table.getSelectionModel().addListSelectionListener(playGame);
		setSize(500, table.getRowHeight() * (b.getCurrentMoves() + 4));
		setFont(PlayGame.font);
		getContentPane().add(new JScrollPane(table));
		setLocationRelativeTo(null);
	}

	public void actionPerformed(ActionEvent ae) {
		dispose();
	}
}

/**
 * Extends the abstract table class to show the previous positions of the
 * detectives and Mr. X.
 */
class PreviousMoves extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	protected TestBoard board;
	protected int numDetectives;
	boolean revealAll;

	public PreviousMoves(TestBoard b, boolean all) {
		board = b;
		numDetectives = board.getDetectives().length;
		revealAll = all;
	}

	public int getRowCount() {
		return board.getCurrentMoves();
	}

	public int getColumnCount() {
		// + 1 because the first entry will be for Mr. X
		return numDetectives + 1;
	}

	public Object getValueAt(int r, int c) {
		String pos = "";
		if (c > 0) {
			// information of detective
			int ln = board.getDetectives()[c - 1].getPrevPos().size();
			if (r + 1 < ln)
				pos = board.getDetectives()[c - 1].getPrevPos().get(r + 1).toString();
		} else {
			// information of Mr. X
			Fugitive fg = board.getMrX();
			if (!board.isCheckPoint(r + 1) && !revealAll)
				pos += "" + fg.getPrevPos().get(r + 1).toStringTicket();
			else
				pos += fg.getPrevPos().get(r + 1).toString();
		}
		return pos;
	}

	public String getColumnName(int c) {
		if (c > 0)
			return I18n.tr("DetectiveColumnHeader", c);
		else
			return I18n.tr("MrXColumnHeader");
	}
}

/**
 * This class provides the GUI interfacing between the user and the machine It
 * uses the swing functionality to provide a good interfacing
 * 
 * @author Shashi Mittal
 * @version 2.4 (19-APR-2010)
 */
public class PlayGame extends JApplet implements ActionListener, Transport, ItemListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private static final int NO_OF_DETECTIVES = 5;

	public static Font font = new Font("SansSerif", Font.PLAIN, 15);
	TestBoard board;
	JFrame parentFrame;
	Container container;
	MapLabel the_map;
	String mrX = "Mr. X";
	JButton start, det, mx;
	JTextField msg;
	int currentDetectiveIndex = 0;
	Move recentMove;
	boolean gameStarted = false;
	boolean canMove = true;
	JTextField detectiveStatus;
	JButton done;
	JComboBox getMove;
	JMenuItem newGame, exitGame, about, help, ackn;
	GridBagConstraints mapC, detC, mxC, startC, doneC, getMoveC, msgC, detectiveStatusC;

	/**
	 * This method builds up the basic user interface between the user and the
	 * machine
	 * 
	 * @param f
	 *            the parent Frame in which the JApplet is encapsulated
	 */
	void buildUI(JFrame f) {
		parentFrame = f;
		container = parentFrame.getContentPane();
		container.setLayout(new GridBagLayout());

		mapC = new GridBagConstraints();
		mapC.fill = GridBagConstraints.BOTH;

		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		JScrollPane map = new JScrollPane(the_map = new MapLabel(new ImageIcon("./map.jpg"),
				TestBoard.getNumberOfDetectives()), v, h);
		mapC.gridx = 0;
		mapC.gridy = 0;
		mapC.gridwidth = 7;
		mapC.ipady = 1000; // Display long map
		mapC.ipady = 600; // Display wide map
		mapC.weightx = 1.0; // should occupy all available horizontal space on
		// resizing
		mapC.weighty = 1.0; // should occupy all available vertical space on
		// resizing
		mapC.insets = new Insets(10, 10, 10, 10);
		container.add(map, mapC);

		det = new JButton(I18n.tr("DetectivesButton"));
		det.addActionListener(this);
		det.setActionCommand("detectives");
		det.setVisible(true);
		if (!gameStarted)
			det.setEnabled(false);
		detC = new GridBagConstraints();
		detC.gridx = 0;
		detC.gridy = 1;
		detC.insets = new Insets(0, 10, 5, 5);
		container.add(det, detC);

		mx = new JButton(I18n.tr("MovesButton"));
		mx.addActionListener(this);
		mx.setActionCommand(mrX);
		mx.setVisible(true);
		if (!gameStarted)
			mx.setEnabled(false);
		mxC = new GridBagConstraints();
		mxC.gridx = 1;
		mxC.gridy = 1;
		mxC.insets = new Insets(0, 5, 5, 5);
		container.add(mx, mxC);

		start = new JButton(I18n.tr("StartGameButton"));
		start.addActionListener(this);
		start.setActionCommand("start");
		if (gameStarted)
			start.setEnabled(false);
		startC = new GridBagConstraints();
		startC.gridx = 2;
		startC.gridy = 1;
		startC.insets = new Insets(0, 5, 5, 5);
		container.add(start, startC);

		done = new JButton(I18n.tr("DoneButton"));
		done.addActionListener(this);
		done.setActionCommand("done");
		if (!gameStarted)
			done.setEnabled(false);
		doneC = new GridBagConstraints();
		doneC.gridx = 6;
		doneC.gridy = 1;
		doneC.insets = new Insets(0, 5, 5, 5);
		doneC.fill = GridBagConstraints.HORIZONTAL; // fill the remaining space
		// at the end of row
		doneC.anchor = GridBagConstraints.EAST;
		container.add(done, doneC);

		msg = new JTextField(50);
		msgC = new GridBagConstraints();
		msgC.gridx = 3;
		msgC.gridy = 1;
		msgC.ipadx = 150; // make this one a little longer
		msgC.insets = new Insets(0, 5, 5, 5);
		container.add(msg, msgC);
		msg.setEditable(false);
		msg.setText(I18n.tr("MsgClickStartGame"));

		detectiveStatus = new JTextField(120);
		detectiveStatus.setEditable(false);
		detectiveStatusC = new GridBagConstraints();
		detectiveStatusC.gridx = 4;
		detectiveStatusC.gridy = 1;
		detectiveStatusC.ipadx = 320;
		detectiveStatusC.insets = new Insets(0, 5, 5, 5);

		getMoveC = new GridBagConstraints();
		getMoveC.gridx = 5;
		getMoveC.gridy = 1;
		getMoveC.insets = new Insets(0, 5, 5, 5);

		parentFrame.setVisible(true);
		addMenu();
	}

	/**
	 * This method is used to set the menu for the game.
	 */
	private void addMenu() {
		JMenuBar mbar = new JMenuBar();
		parentFrame.setJMenuBar(mbar);
		JMenu fileMenu = new JMenu(I18n.tr("FileMenu"));
		mbar.add(fileMenu);

		newGame = new JMenuItem(I18n.tr("File_NewMenuItem"));
		fileMenu.add(newGame);
		newGame.addActionListener(this);

		exitGame = new JMenuItem(I18n.tr("File_ExitMenuItem"));
		fileMenu.add(exitGame);
		exitGame.addActionListener(this);

		JMenu helpMenu = new JMenu(I18n.tr("HelpMenu"));
		mbar.add(helpMenu);

		about = new JMenuItem(I18n.tr("Help_AboutMenuItem"));
		helpMenu.add(about);
		about.addActionListener(this);

		help = new JMenuItem(I18n.tr("Help_HelpMenuItem"));
		helpMenu.add(help);
		help.addActionListener(this);
	}

	/**
	 * This method handles the menu events.
	 * 
	 * @param source
	 *            the menu item which is selected by the user.
	 */
	private void handleMenuEvent(Object source) {
		if (source == newGame)
			reset();
		else if (source == exitGame)
			System.exit(0);
		else if (source == about)
			aboutThisGame();
		else if (source == help)
			help();
	}

	/**
	 * This method tells the user about this game.
	 */
	private void aboutThisGame() {
		JOptionPane.showMessageDialog(parentFrame, I18n.tr("AboutText"), I18n.tr("AboutTitle"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * This method provides help tips to the user.
	 */
	private void help() {
		JOptionPane.showMessageDialog(parentFrame, I18n.tr("HelpText"), I18n.tr("HelpTitle"),
				JOptionPane.INFORMATION_MESSAGE);

	}

	/**
	 * This method is used to handle the events caused by the clicking of the
	 * buttons.
	 * 
	 * @param ae
	 *            the ActionEvent object which has the details of the event.
	 */
	public void actionPerformed(ActionEvent ae) {
		String order = ae.getActionCommand();
		Object source = ae.getSource();
		handleMenuEvent(source);
		if (order.equals("start")) {
			gameStarted = true;
			det.setEnabled(true);
			mx.setEnabled(true);
			start.setEnabled(false);
			parentFrame.setVisible(true);
			msg.setText(I18n.tr("MsgLoading"));
			board = new TestBoard();

			for (int i = 1; i <= TestBoard.getNumberOfDetectives(); i++) {
				the_map.setPlayerPos(i, board.getPoint(board.getDetectives()[i - 1].position
						.getPosition()));
			}

			the_map.setCurrentPlayer(1);

			parentFrame.setVisible(true);
			msg.setText(I18n.tr("MsgLoadingDone"));

			container.add(detectiveStatus, detectiveStatusC);
			moveMrX();
			done.setVisible(true);
			detectiveStatus.setVisible(true);
		} else if (order.equals("detectives")) {
			String dets = "";
			for (int i = 0; i < NO_OF_DETECTIVES; i++)
				dets += I18n.tr("DetectiveInfoText", (i + 1), board.getDetectives()[i].toString())
						+ "\n";
			JOptionPane.showMessageDialog(parentFrame, dets, I18n.tr("DetectiveInfoTitle"),
					JOptionPane.INFORMATION_MESSAGE);

		} else if (order.equals(mrX)) {
			TableDialog tb = new TableDialog(parentFrame, I18n.tr("PreviousMovesTitle"), board,
					this, false);
			tb.setVisible(true);
		} else if (order.equals("done")) {
			if (canMove) {
				board.changeDetectivePosition(currentDetectiveIndex, recentMove);
				container.remove(getMove);
			}

			int nodePos = board.getDetectives()[currentDetectiveIndex].position.getPosition();
			Point currentDetectivePos = board.getPoint(nodePos);
			the_map.setPlayerPos(currentDetectiveIndex + 1, currentDetectivePos.x,
					currentDetectivePos.y);

			parentFrame.setVisible(true);
			currentDetectiveIndex = (currentDetectiveIndex + 1) % NO_OF_DETECTIVES;
			the_map.setCurrentPlayer(currentDetectiveIndex + 1);
			if (board.isUserWin())
				humanWin();
			else {
				if (currentDetectiveIndex == 0 && !board.isUserWin())
					moveMrX();
				else
					getDetectiveMove();
			}
		}
	}

	/**
	 * Used to move the fugitive in the game.
	 */
	private void moveMrX() {
		if (board.isUserWin())
			humanWin();
		Move move = board.moveMrX();
		String str = "Mr. X moved by ";
		switch (move.getType()) {
		case TAXI:
			str += "Taxi";
			break;
		case BUS:
			str += "Bus";
			break;
		case UG:
			str += "Underground";
			break;
		case FERRY:
			str += "unknown transport (Black Ticket)";
			break;
		case BLACK:
			str += "unknown transport (Black Ticket)";
		}
		if (board.isCheckPoint()) {
			str += " and is at the position " + move.getNode();
			the_map.mrXVisible = true;
		} else {
			the_map.mrXVisible = false;
		}

		the_map.setPlayerPos(0, board.getPoint(move.getNode()).getLocation());
		JOptionPane.showMessageDialog(parentFrame, str, "Mr. X move",
				JOptionPane.INFORMATION_MESSAGE);

		if (board.isMachineWin())
			machineWin();
		else
			getDetectiveMove();
	}

	/**
	 * Called when the human player has won the game.
	 */
	private void humanWin() {
		the_map.mrXVisible = true;
		reset();
		msg.setText(I18n.tr("MsgHumanWin"));
		JOptionPane.showMessageDialog(parentFrame, I18n.tr("HumanWinText"), I18n
				.tr("HumanWinTitle"), JOptionPane.INFORMATION_MESSAGE);
		repaint();
		displayPrevPos();
	}

	/**
	 * Called when the machine has won the game.
	 */
	private void machineWin() {
		reset();
		msg.setText(I18n.tr("MsgMachineWin"));
		JOptionPane.showMessageDialog(parentFrame, I18n.tr("MachineWinText"), I18n
				.tr("MachineWinTitle"), JOptionPane.INFORMATION_MESSAGE);
		detectiveStatus.setText("");
		repaint();
		displayPrevPos();
	}

	/**
	 * Displays the previous positions of the players in a dialog box.
	 */
	private void displayPrevPos() {
		TableDialog td = new TableDialog(parentFrame, I18n.tr("PreviousMovesTitle"), board, this,
				true);
		td.setVisible(true);
	}

	/**
	 * This method is used to reset the screen when either player wins.
	 */
	private void reset() {
		try {
			det.setEnabled(false);
			mx.setEnabled(false);
			start.setEnabled(true);
			container.remove(getMove);
			done.setEnabled(false);
			done.setVisible(false);
			detectiveStatus.setVisible(false);
			the_map.setCurrentPlayer(-1);
			getMove.repaint();
		} catch (NullPointerException e) {
		}
		currentDetectiveIndex = 0;
		parentFrame.setVisible(true);
		done.repaint();
		msg.setText(I18n.tr("MsgClickStartGame"));
		repaint();
		gameStarted = false;
	}

	/**
	 * Called when the move of the detectives has to be taken from the user.
	 */
	private void getDetectiveMove() {
		getMove = new JComboBox();
		getMove.setSize(new Dimension(120, 30));
		done.setEnabled(true);
		parentFrame.setVisible(true);
		Detective[] detectives = board.getDetectives();
		msg.setText(I18n.tr("MsgMoveDetective", currentDetectiveIndex + 1));
		detectiveStatus.setText(I18n.tr("DetectiveStatusText", currentDetectiveIndex + 1,
				detectives[currentDetectiveIndex].toString()));
		if (detectives[currentDetectiveIndex].canMove(board)) {
			TreeSet<Move> moves = board.getDetectivePossibleMoves(currentDetectiveIndex);
			int i = 0;
			while (!moves.isEmpty()) {
				Move m = moves.first();
				getMove.addItem(m.toDisplayString());
				if (i == 0)
					recentMove = m;
				moves.remove(m);
				i++;
			}
			getMove.setSelectedItem(recentMove.toString());
			getMove.addItemListener(this);
			getMove.setVisible(true);
			container.add(getMove, getMoveC);
			getMove.setEnabled(true);
			canMove = true;
		} else {
			detectives[currentDetectiveIndex].setStaticState();
			msg.setText(I18n.tr("MsgDetectiveStranded", currentDetectiveIndex + 1));
			getMove.setVisible(false);
			canMove = false;
		}
		getMove.repaint();
		repaint();
		parentFrame.setVisible(true);
	}

	/**
	 * Called when the item in the JComboBox is changed
	 * 
	 * @param ie
	 *            the ItemEvent object which has the details of this event
	 */
	public void itemStateChanged(ItemEvent ie) {
		String move = (String) ie.getItem();
		recentMove = new Move(move);
	}

	/**
	 * This is the main method. Called when run as an application.
	 */
	public static void main(String args[]) {
		JFrame f = new JFrame(I18n.tr("WindowTitle"));
		PlayGame game = new PlayGame();
		game.buildUI(f);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		f.setSize(new Dimension(1024, 700));
		f.setVisible(true);
		f.setResizable(true);
	}

	/**
	 * This method displays the previous positions of the detectives and Mr. X on the
	 * map after the game is over.
	 */
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (!gameStarted) {
			DefaultListSelectionModel table = (DefaultListSelectionModel) evt.getSource();
			int index = table.getMaxSelectionIndex();
			for (int i = 0; i <= NO_OF_DETECTIVES; i++) {
				LinkedList<Move> prevPos;
				if (i == 0) {
					prevPos = board.getMrX().getPrevPos();
				} else {
					prevPos = board.getDetectives()[i - 1].getPrevPos();
				}

				int pos = (prevPos.size() <= index + 1 ? prevPos.getLast().getNode() : prevPos.get(
						index + 1).getNode());
				the_map.setPlayerPos(i, board.getPoint(pos));
				the_map.setCurrentPlayer(-1);
			}
		}
	}
}
