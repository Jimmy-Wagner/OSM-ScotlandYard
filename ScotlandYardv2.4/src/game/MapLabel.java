package game;

import i18n.I18n;

import java.io.IOException;
import java.io.File;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * This class extends the JLabel class to provide the functionality of
 * overlaying images (positions of detectives) on top of the game board.
 * 
 * @author Johannes Jowereit
 * @version 2.4 (19-APR-2010)
 */
public class MapLabel extends JLabel implements ActionListener {

	private static final long serialVersionUID = 2747458994628364853L;
	private static final int NO_OF_DETECTIVES = 5;

	private BufferedImage flagImage[];

	/**
	 * Contains the positions of the players (Mr. X and the detectives) on the
	 * board. For Mr. X (playerPositions[0]) and each of the detectives
	 * (playerPositions[1..n] where n is the number of detectives) the pixel
	 * coordinates of their position relative to the top left corner of the
	 * image is stored.
	 */
	private Point[] playerPositions = null;

	private int currentPlayer = -1;
	boolean blinkOn = false;
	boolean mrXVisible = false; /* Is Mr. X' Position revealed? */

	Timer blinkTimer = new Timer(1000, this);

	public MapLabel(ImageIcon imageIcon, int numDetectives) {
		super(imageIcon);
		playerPositions = new Point[numDetectives + 1];
		flagImage = new BufferedImage[NO_OF_DETECTIVES + 1];
		for (int i = 0; i <= NO_OF_DETECTIVES; i++) {
			String fileName = "./flag" + Integer.toString(i) + ".gif";
			try {
				flagImage[i] = ImageIO.read(new File(fileName));
			} catch (IOException e) {
				 JOptionPane.showMessageDialog(null, I18n.tr("ErrorFileNotFound", fileName), I18n.tr("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
				 System.exit(1);
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);

		if (this.playerPositions != null) {
			Graphics2D g2 = (Graphics2D) g;

			for (int i = 0; i < playerPositions.length; i++) {
				/**
				 * The player's position is marked with a numbered flag, except
				 * when: - The position does not exist OR - The player is the
				 * current player and the blinking is in the "off" phase OR -
				 * The player is Mr. X and his position is currently not
				 * revealed. 
				 */
				if (playerPositions[i] != null && !(blinkOn && i == currentPlayer)
						&& !(i == 0 && !mrXVisible)) {

					Point playerPos = this.getPlayerPos(i);

					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.drawImage(flagImage[i], playerPos.x, playerPos.y - flagImage[i].getHeight(),
							null);
				}
			}
		}
	}

	public void setPlayerPos(int player, int x, int y) {
		this.playerPositions[player] = new Point(x, y);
		this.repaintPlayerPos(player);
	}

	public void setPlayerPos(int player, Point pos) {
		this.setPlayerPos(player, pos.x, pos.y);
	}

	/**
	 * Gets the position of the given player's current position on screen. The
	 * label may be bigger than the image contained in it, in which case the
	 * image will be centered. In this case, an offset has to be added to the
	 * position retrieved in the playerPositions[] array.
	 * 
	 * FIXME: Store xOffset and yOffset in private fields and only change them
	 * when the label's size changes.
	 * 
	 * @param player
	 *            The player (0 = Mr. X, 1..n = detectives) whose position shall
	 *            be determined.
	 * @return The pixel coordinates of the player's position.
	 */
	public Point getPlayerPos(int player) {
		if (player < 0 || player >= playerPositions.length) {
			return null;
		}

		int xOffset = (this.getWidth() - this.getIcon().getIconWidth() < 0) ? 0
				: (this.getWidth() - this.getIcon().getIconWidth()) / 2;
		int yOffset = (this.getHeight() - this.getIcon().getIconHeight() < 0) ? 0 : (this
				.getHeight() - this.getIcon().getIconHeight()) / 2;

		return new Point(xOffset + playerPositions[player].x, yOffset + playerPositions[player].y);
	}

	/**
	 * Sets the currently active player and takes care of the blinking of the
	 * current player's position.
	 * 
	 * @param player
	 *            The number of the new currently active player.
	 */
	public void setCurrentPlayer(int player) {
		if (player != currentPlayer) {
			/*
			 * Stop the blink timer and repaint the current player's position if
			 * it was currently not visible due to blinking.
			 */
			if (blinkTimer.isRunning()) {
				blinkTimer.stop();
			}

			blinkOn = false;
			if (currentPlayer != -1) {
				repaintPlayerPos(currentPlayer);
			}

			/* Change the current player and start the blinking timer */
			currentPlayer = player;
			blinkTimer.start();
			repaintPlayerPos(currentPlayer);
		}
	}

	/**
	 * Repaints the area in which the position of the given player lies.
	 * 
	 * @param player
	 *            The number of the detective, or 0 for Mr. X
	 */
	public void repaintPlayerPos(int player) {
		Point playerPos = getPlayerPos(player);

		if (player != -1 && playerPos != null) {
			int width = flagImage[player].getWidth();
			int height = flagImage[player].getHeight();
			this.repaint(playerPos.x, playerPos.y - height, width, height);
		}
	}

	@Override
	/**
	 * Gets called when the blinking timer fires. Flips the blinking on/off switch
	 * and repaints the active player's position. 
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == blinkTimer) {
			this.blinkOn = !this.blinkOn;

			if (this.playerPositions != null) {
				this.repaintPlayerPos(currentPlayer);
			}
		}
	}

}
