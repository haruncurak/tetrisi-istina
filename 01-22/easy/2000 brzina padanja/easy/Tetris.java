// Tetris.java
//
// Written in 2013 by Johannes Holzfuß <johannes@holzfuss.name>
//
// To the extent possible under law, the author(s) have dedicated all copyright
// and related and neighboring rights to this software to the public domain
// worldwide. This software is distributed without any warranty.
//
// You should have received a copy of the CC0 Public Domain Dedication along
// with this software. If not, see
// <http://creativecommons.org/publicdomain/zero/1.0/>.

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;


public class Tetris extends JPanel {

	private static final long serialVersionUID = -8715353373678321308L;

 

	private final Point[][][] Tetraminos = {
			// I-Piece
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
			},
			
			// L-Piece
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }
			},
			
			// J-Piece (changed rotation order so it would start facing up rather than down)
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) }, //3
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }, //4
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) }, //1
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) }  //2
			},
			
			// O-Piece
			{
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
			},
			
			// S-Piece
			{
				{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
				{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
			},
			
			// T-Piece
			{
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
				{ new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
			},
			
			// Z-Piece
			{
				{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
				{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }
			}
	};
	
	//Note: Switched colors of J and L to reflect tetris standard
	private final Color[] tetraminoColors = {
		Color.cyan, Color.orange, Color.blue, Color.yellow, Color.green, Color.pink, Color.red
	};
	
	private Point pieceOrigin;
	private int currentPiece;
	private boolean swapped = false;
	private boolean gameOver = false;
	private boolean keepPlaying = true;
	private boolean added = false;
	private static int storedPiece = -1;
	private static int tempPiece = -1;
	private int rotation;
	private ArrayList<Integer> nextPieces = new ArrayList<Integer>();

	private int score;
	private int phase = 1;
	private int timeDec = 200;
	private Color[][] well;
	private static JFrame f;
	
	private static JFrame endGame;
	private static JLabel endGameText;
	private static JButton newGame;
	
	private static BufferedReader inputFile;
	
	private static int sleepTime = 2000;
	
	private static String endGameString;
	
	// Creates a border around the well and initializes the dropping piece
	private void init() {
		well = new Color[12][25];
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 24; j++) {
				if (i == 0 || i == 11 || j == 23 || j == 0) {
					well[i][j] = Color.GRAY;
				} else {
					well[i][j] = Color.BLACK;
				}
			}
		}
		newPiece();
	}
	
	
	//resets the game world
	private void restart() 
	{
		//reset score and level variables
		added = false;
		endGame.setVisible(false);
		gameOver = false;
		phase = 1;
		timeDec = 200;
		sleepTime = 2250;
		storedPiece = -1;
		score = 0;
		
		//clear the game board
		for (int j = 22; j > 0; j--) 
		{
			for(int i = 1; i < 11; i++) 
			{
				well[i][j] = Color.BLACK;
			}
		}
		
		
		newPiece();
		
		repaint();
		
	}
	
	
	// Put a new, random piece into the dropping position
	public void newPiece() {
		pieceOrigin = new Point(5, 1);
		
		rotation = 0;
		
		//Game Over Code
		if(collidesAt(5, 1, 0)) 
		{
			gameOver = true;
			
		} else 
		{
			if (nextPieces.isEmpty()) {
				Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
				Collections.shuffle(nextPieces);
			}
			currentPiece = nextPieces.get(0);
			nextPieces.remove(0);
		}
		
	
	}
	
	//Put a predetermined piece into the starting position
	public void newPiece(int piece) {
		pieceOrigin = new Point(5, 1);
		
		rotation = 0;
		
		//Game Over Code
		if(collidesAt(5, 1, 0)) 
		{
			gameOver = true;
			
		}
		

		currentPiece = piece;
	}
	
	// Collision test for the dropping piece
	private boolean collidesAt(int x, int y, int rotation) {
		for (Point p : Tetraminos[currentPiece][rotation]) {
			if (well[p.x + x][p.y + y] != Color.BLACK) {
				return true;
			}
		}
		return false;
	}
	
	// Rotate the piece clockwise or counterclockwise
	public void rotate(int i) {
		int newRotation = (rotation + i) % 4;
		if (newRotation < 0) {
			newRotation = 3;
		}
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
			rotation = newRotation;
		}
		repaint();
	}
	
	// Move the piece left or right
	public void move(int i) {
		if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
			pieceOrigin.x += i;	
		}
		repaint();
	}
	
	// Drops the piece one line or fixes it to the well if it can't drop
	public void dropDown() {
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			pieceOrigin.y += 1;
		} else {
			fixToWell();
		}	
		
		repaint();
	}
	
	/*
	 * Drops the piece down until it collides. Only used if space is pressed.
	 * Player is awarded two bonus points for every row the piece drops
	 */
	public void dropToBottom() 
	{
		//while it can drop down, drops down.
		while(!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) 
		{
			score += 2;
			pieceOrigin.y += 1;
		}
		
		fixToWell();
		
		repaint();
	}
	
	// Make the dropping piece part of the well, so it is available for
	// collision detection.
	public void fixToWell() {
		for (Point p : Tetraminos[currentPiece][rotation]) {
			well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetraminoColors[currentPiece];
		}
		swapped = false;
		clearRows();
		newPiece();
	}
	
	public void deleteRow(int row) {
		for (int j = row-1; j > 0; j--) {
			for (int i = 1; i < 11; i++) {
				well[i][j+1] = well[i][j];
			}
		}
	}
	
	// Clear completed rows from the field and award score according to
	// the number of simultaneously cleared rows.
	public void clearRows() 
	{
		boolean gap;
		int numClears = 0;
		
		for (int j = 22; j > 0; j--) {
			gap = false;
			for (int i = 1; i < 11; i++) {
				if (well[i][j] == Color.BLACK) {
					gap = true;
					break;
				}
			}
			if (!gap) {
				deleteRow(j);
				j += 1;
				numClears += 1;
			}
		}
				
		switch (numClears) 
		{
		case 1:
			score += 100;
			break;
		case 2:
			score += 300;
			break;
		case 3:
			score += 500;
			break;
		case 4:
			score += 800;
			break;
		}
		
		
	}
	
	// Draw the falling piece
	private void drawPiece(Graphics g) {		
		g.setColor(tetraminoColors[currentPiece]);
		
		for (Point p : Tetraminos[currentPiece][rotation]) 
		{
			g.fillRect((p.x + pieceOrigin.x) * 26, (p.y + pieceOrigin.y) * 26 + 60, 25, 25);
		}
	}
	
	// Returns the standard letter representation for a tetramino
	public String getPieceName(int num) 
	{
		String returnString = null;
		
		if(num == -1) 
		{
			returnString = "None";
		} else if (storedPiece == 0) 
		{
			returnString = "I";
		} else if (storedPiece == 1) 
		{
			returnString = "L";
		} else if (storedPiece == 2) 
		{
			returnString = "J";
		} else if (storedPiece == 3) 
		{
			returnString = "O";
		} else if (storedPiece == 4) 
		{
			returnString = "S";
		} else if (storedPiece == 5) 
		{
			returnString = "T";
		}  else if (storedPiece == 6) 
		{
			returnString = "Z";
		}
		
		return returnString;
		
	}
	
	@Override 
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g); // Call superclass method for proper rendering
		String storedPieceName = null;
		
		// Set background to black
		this.setBackground(Color.BLACK);
		
		// Paint the well
		//Note: Edited to leave blank space at top for scoring data
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 24; j++) {
				g.setColor(well[i][j]);
				g.fillRect(26*i, 26*j + 60, 25, 25);
			}
		}
		
		// Display the score
		g.setColor(Color.WHITE);
		g.drawString("Score: " + score, 20, 35);
		
		//Display the currently stored piece
		g.setColor(Color.WHITE);
		storedPieceName = getPieceName(storedPiece);
		// g.drawString("Stored: " + storedPieceName, 220, 35);
		
		//Draw the currently falling piece
		drawPiece(g);
	}

	
	public static void main(String[] args) throws IOException 
	{

		//JFrame Initialization
		f = new JFrame("Tetris");
		
		endGame = new JFrame("Game Over");
		
		endGame.setLayout(null);
		
		endGame.setSize(12*26+15, 300);

		endGame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		newGame = new JButton();
		newGame.setText("Play Again");
		
		endGameText = new JLabel();
		endGameText.setSize(500, 100);	//200 25
		endGameText.setLocation(90, 160); //90 160
	
		newGame.setSize(100, 100);
		newGame.setLocation(50, 50);
		newGame.setBackground(Color.GREEN);
		
		endGame.add(newGame);
		endGame.add(endGameText);
		endGame.getContentPane().setBackground(Color.BLACK);
		
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Set the main frame to be fullscreen with black background
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setUndecorated(true); // Remove window decorations
		f.getContentPane().setBackground(Color.BLACK);
		f.setLayout(null); // Use null layout to position components manually
		
		// Calculate the game panel size
		final int gameWidth = 12*26;
		final int gameHeight = 26*24 + 60;
		
		// Create the game instance
		final Tetris game = new Tetris();
		game.init();
		
		// Set the size of the game panel and make it opaque with black background
		game.setSize(gameWidth, gameHeight);
		game.setOpaque(true);
		game.setBackground(Color.BLACK);
		
		// Calculate position to center the game in the screen
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int gameX = (screenSize.width - gameWidth) / 2;
		int gameY = (screenSize.height - gameHeight) / 2;
		
		// Position the game panel
		game.setLocation(gameX, gameY);
		
		// Add game to the frame and make it visible
		f.add(game);
		f.setVisible(true);


		newGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Save the current score with timestamp instead of username
				try (FileWriter scoresFileWriter = new FileWriter("scores.txt", true)) {
					// Create timestamp in format: yyyy-MM-dd HH:mm:ss
					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String timestamp = now.format(formatter);
					
					scoresFileWriter.write(timestamp + ": " + game.score + "\n");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
				
				// Now restart the game.
				game.restart();
				endGame.setVisible(false); // Hide the endGame frame after restarting
			}
		});
		
		
		// Keyboard controls
		f.addKeyListener(new KeyListener() 
		{
			public void keyTyped(KeyEvent e) 
			{
				
			}
			
			public void keyPressed(KeyEvent e) 
			{
				switch (e.getKeyCode()) 
				{
					case KeyEvent.VK_UP:
						game.rotate(+1);
						break;
					
					//Note: Drop down has been switched to the down arrow key.
					// case KeyEvent.VK_DOWN:
					// 	game.dropDown();
						// game.score += 1;
					// 	break;
					case KeyEvent.VK_LEFT:
						game.move(-1);
						break;
					case KeyEvent.VK_RIGHT:
						game.move(+1);
						break;
					
					/*
					* Space bar is used for a Hard Drop - Piece instantly moves to the bottom of
					* the screen. This awards 2 points for every row dropped.
					*/
					// case KeyEvent.VK_SPACE:
					// 	game.dropToBottom();
					// 	break;
					
					/*
					* Stored Piece Code 
					* On pressing c, the currently dropping piece should be
					* stored and be replaced by the piece previously in storage. The game only
					* permits "swapping" once per drop, unless the player is storing a piece for
					* the first time.
					*/
					case KeyEvent.VK_C:
						/*
						 * If no piece has been stored yet, store the current piece and drop a new
						 * randomly selected piece
						 */
						if(storedPiece == -1) 
						{
							storedPiece = game.currentPiece;
							game.repaint();
							game.newPiece();
						} else 
						{
						/*
						 * If player has not already swapped this drop, swaps current 
						 * and stored pieces and sets game.swapped to true
						 */
							if(game.swapped == false) 
							{
								tempPiece = game.currentPiece;
								game.newPiece(storedPiece);
								storedPiece = tempPiece;
								game.swapped = true;
								
								game.repaint();
							}
						
							//If player has already swapped this drop, do nothing
						}
					
					break;
				} 
			}
			
			public void keyReleased(KeyEvent e) 
			{
				
			}
		}
		);
		
		
		//Modified dropdown iterator code, removed threads
		while(game.keepPlaying) 
		{
			while(game.gameOver == false) 
			{
				try {
					TimeUnit.MILLISECONDS.sleep(sleepTime);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				game.dropDown();
			}
			

			endGameString = "<html><pre>";
			
			endGameString = endGameString + "</pre></html>";
			
			endGameText.setText(endGameString);
			
			endGame.setVisible(true);

			while (game.gameOver) {
				try {
					TimeUnit.MILLISECONDS.sleep(100); // Sleep for a short duration to prevent busy waiting.
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
		}
		
		// Clean up resources when game ends
		endGame.dispose();
		f.dispose();
		inputFile.close();
		
	}
	
	
	
	
}









