import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

public class Tetris extends JPanel {
	private static final long serialVersionUID = -8715353373678321308L;

	// Configuration variables (made static for easy access)
	private static int configTimeDec = 50;
	private static int configSleepTime = 1000;
	private static int configLinesThreshold = 5;
	private static int configBlocksThreshold = 30;
	private static boolean persistCounters = false;

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
		// J-Piece
		{
			{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
			{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) },
			{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
			{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) }
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
	private int timeDec = 50;
	private Color[][] well;
	private static JFrame f;
	private static JFrame endGame;
	private static JLabel endGameText;
	private static JButton newGame;
	private static JButton exit;
	private static String endGameString;
	private static String playerName;
	private static int sleepTime = 1000;
	int consecutiveObjects = 0;
	int linesClearedWithin30 = 0;

	// Configuration dialog
	private static void showConfigDialog() {
		JPanel panel = new JPanel(new GridLayout(0, 2));
		JTextField timeDecField = new JTextField(String.valueOf(configTimeDec));
		JTextField sleepTimeField = new JTextField(String.valueOf(configSleepTime));
		JTextField linesField = new JTextField(String.valueOf(configLinesThreshold));
		JTextField blocksField = new JTextField(String.valueOf(configBlocksThreshold));
		JCheckBox persistCheck = new JCheckBox("", persistCounters);

		panel.add(new JLabel("Time Decrement (ms):"));
		panel.add(timeDecField);
		panel.add(new JLabel("Initial Speed (ms):"));
		panel.add(sleepTimeField);
		panel.add(new JLabel("Lines Required:"));
		panel.add(linesField);
		panel.add(new JLabel("Blocks Threshold:"));
		panel.add(blocksField);
		panel.add(new JLabel("Persist Counters:"));
		panel.add(persistCheck);

		int result = JOptionPane.showConfirmDialog(null, panel, "Game Configuration",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			try {
				configTimeDec = Integer.parseInt(timeDecField.getText());
				configSleepTime = Integer.parseInt(sleepTimeField.getText());
				configLinesThreshold = Integer.parseInt(linesField.getText());
				configBlocksThreshold = Integer.parseInt(blocksField.getText());
				persistCounters = persistCheck.isSelected();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Invalid input! Using previous values.");
			}
		}
	}

	@Override 
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Paint the well
		g.fillRect(0, 0, 26*12, 26*24 + 60);
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 24; j++) {
				g.setColor(well[i][j]);
				g.fillRect(26*i, 26*j + 60, 25, 25);
			}
		}

		// Draw info panel
		g.setColor(Color.WHITE);
		g.drawString("Score: " + score, 20, 35);
		g.drawString("Speed: " + sleepTime + "ms", 20, 55);
		g.drawString("Decr: " + timeDec + "ms", 20, 75);
		g.drawString("Lines Req: " + configLinesThreshold, 20, 95);
		g.drawString("Blocks Thresh: " + configBlocksThreshold, 20, 115);
		g.drawString("Persist: " + persistCounters, 20, 135);

		// Draw the currently falling piece
		drawPiece(g);
	} 

	public static void main(String[] args) throws IOException {
		showConfigDialog();  // Show config dialog first
		
		//JFrame Initialization
		f = new JFrame("Tetris Admin Version");
		
		endGame = new JFrame("Game Over");
		endGame.setLayout(null);
		endGame.setSize(12*26+15, 300);
		endGame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		newGame = new JButton();
		newGame.setText("Play Again");
		
		exit = new JButton();
		exit.setText("<html>Exit and Save Scores<html>");
		
		endGameText = new JLabel();
		endGameText.setSize(500, 100);
		endGameText.setLocation(90, 160);
	
		newGame.setSize(100, 100);
		newGame.setLocation(50, 50);
		newGame.setBackground(Color.GREEN);
		
		exit.setSize(100, 100);
		exit.setLocation((100 + 62), 50);
		exit.setBackground(Color.RED);
		
		endGame.add(newGame);
		endGame.add(exit);
		endGame.add(endGameText);
		endGame.getContentPane().setBackground(Color.BLACK);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(12*26+15, 26*24+38 + 60);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		final Tetris game = new Tetris();
		game.init();
		f.add(game);

		// Modified new game button
		newGame.addActionListener(e -> {
			showConfigDialog();  // Show config dialog before restarting
			try (FileWriter writer = new FileWriter("scores.txt", true)) {
				writer.write(playerName + ": " + game.score + "\n");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			game.restart();
			endGame.setVisible(false);
		});

		exit.addActionListener(e -> {
			game.keepPlaying = false;
			try (FileWriter writer = new FileWriter("scores.txt", true)) {
				writer.write(playerName + ": " + game.score + "\n");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		});

		// Keyboard controls
		f.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						game.rotate(+1);
						break;
					case KeyEvent.VK_DOWN:
						game.dropDown();
						game.score += 1;
						break;
					case KeyEvent.VK_LEFT:
						game.move(-1);
						break;
					case KeyEvent.VK_RIGHT:
						game.move(+1);
						break;
					case KeyEvent.VK_SPACE:
						game.dropToBottom();
						break;
					case KeyEvent.VK_C:
						if(storedPiece == -1) {
							storedPiece = game.currentPiece;
							game.repaint();
							game.newPiece();
						} else {
							if(game.swapped == false) {
								tempPiece = game.currentPiece;
								game.newPiece(storedPiece);
								storedPiece = tempPiece;
								game.swapped = true;
								game.repaint();
							}
						}
						break;
				}
			}
			
			public void keyReleased(KeyEvent e) {
			}
		});
		
		// Game loop
		while(game.keepPlaying) {
			while(!game.gameOver) {
				try {
					TimeUnit.MILLISECONDS.sleep(sleepTime);
				} catch (InterruptedException e1) {
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
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		endGame.dispose();
		f.dispose();
	} 

	// Creates a border around the well and initializes the dropping piece
	private void init() {
		playerName = JOptionPane.showInputDialog("Molimo unesite Vašu šifru:");
		if (playerName == null || playerName.trim().isEmpty()) {
			playerName = "Unknown";
		}
		
		// Initialize with config values
		timeDec = configTimeDec;
		sleepTime = configSleepTime;
		
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
	
	private void restart() {
		endGame.setVisible(false);
		gameOver = false;
		storedPiece = -1;
		score = 0;
		
		// Apply current configuration
		timeDec = configTimeDec;
		sleepTime = configSleepTime;
		
		if (!persistCounters) {
			consecutiveObjects = 0;
			linesClearedWithin30 = 0;
		}
		
		for (int j = 22; j > 0; j--) {
			for (int i = 1; i < 11; i++) {
				well[i][j] = Color.BLACK;
			}
		}
		newPiece();
		repaint();
	}
	
	public void newPiece() {
		consecutiveObjects++;
		pieceOrigin = new Point(5, 1);
		rotation = 0;
		
		if (collidesAt(5, 1, 0)) {
			gameOver = true;
		} else {
			if (nextPieces.isEmpty()) {
				Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
				Collections.shuffle(nextPieces);
			}
			currentPiece = nextPieces.get(0);
			nextPieces.remove(0);
		}
	}
	
	public void newPiece(int piece) {
		consecutiveObjects++;
		pieceOrigin = new Point(5, 1);
		rotation = 0;
		
		if (collidesAt(5, 1, 0)) {
			gameOver = true;
		}
		currentPiece = piece;
	}
	
	private boolean collidesAt(int x, int y, int rotation) {
		for (Point p : Tetraminos[currentPiece][rotation]) {
			if (well[p.x + x][p.y + y] != Color.BLACK) {
				return true;
			}
		}
		return false;
	}
	
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
	
	public void move(int i) {
		if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
			pieceOrigin.x += i;
		}
		repaint();
	}
	
	public void dropDown() {
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			pieceOrigin.y += 1;
		} else {
			fixToWell();
		}
		repaint();
	}
	
	public void dropToBottom() {
		while (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			score += 2;
			pieceOrigin.y += 1;
		}
		fixToWell();
		repaint();
	}
	
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
	
	public void clearRows() {
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
		
		linesClearedWithin30 += numClears;
		
		switch (numClears) {
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
		
		if (consecutiveObjects >= configBlocksThreshold || linesClearedWithin30 >= configLinesThreshold) {
			adjustDifficultyForFlow();
			consecutiveObjects = 0;
			linesClearedWithin30 = 0;
		}
	}
	
	private void adjustDifficultyForFlow() {
		if (linesClearedWithin30 >= configLinesThreshold) {
			int decrement = (sleepTime <= 50) ? 10 : timeDec;
			sleepTime = Math.max(1, sleepTime - decrement);
		} else if (linesClearedWithin30 <= (configLinesThreshold - 2)) {
			sleepTime = Math.min(1000, sleepTime + timeDec);
		}
	}
	
	private void drawPiece(Graphics g) {
		g.setColor(tetraminoColors[currentPiece]);
		for (Point p : Tetraminos[currentPiece][rotation]) {
			g.fillRect((p.x + pieceOrigin.x) * 26, (p.y + pieceOrigin.y) * 26 + 60, 25, 25);
		}
	}
	
	public String getPieceName(int num) {
		if (num == -1) return "None";
		String[] names = {"I", "L", "J", "O", "S", "T", "Z"};
		return names[storedPiece];
	}
} 