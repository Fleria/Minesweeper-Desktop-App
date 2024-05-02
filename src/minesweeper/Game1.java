package minesweeper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class Game1 {

	public static final int TILE_SIZE = 40;

	private static int X_TILES; // tiles in horizontal
	private static int Y_TILES;

	public static Tile[][] grid; // set grid as 2D array
	public static Scene scene;

	private static VBox vBox;
	private static GridPane gridPane;
	private static Stage stage;

	private static List<int[]> mines;
	public static int mines_left = 0;
	public static Label timerLabel;

	public static Rounds this_round;
	public static boolean winner;

	private static int supermine_x;
	private static int supermine_y;
	private static int[] supermine_coords;

	public static int marked;
	public static int opened;

	/**
	 * Method to create the game grid.
	 * Initialises grid, sets random coordinates for mines and supermine if specified.
	 * Records mine positions in the grid in the "mine.txt" file.
	 * 
	 * @param difficulty  the difficulty level chosen by the player
	 * @param total_mines the total number of mines in the game
	 * @param super_mine  the index of the supermine in the mines list
	 * @return a Parent object representing the game grid
	 */

	public static Parent createContent(int difficulty, int total_mines, int super_mine) { // root of application
		Pane root = new Pane();
		root.setPrefSize(655, 725);

		List<int[]> mines = new ArrayList<>();
		mines_left = 0;
		marked = 0;
		opened = 0;
		Random random = new Random();

		// Set height, width according to difficulty
		if (difficulty == 1) {
			X_TILES = Y_TILES = 9;
		}

		if (difficulty == 2) {
			X_TILES = Y_TILES = 16;
		}

		// Initialize grid and map
		grid = new Tile[X_TILES][Y_TILES];
		boolean[][] map = new boolean[X_TILES][Y_TILES];

		// Generate list of mine coordinates
		int count = 0;
		for (int i = 0; i < Main.total_mines; i++) {
			count++;
			int row, col;
			do {
				row = random.nextInt(X_TILES); // random row index
				col = random.nextInt(Y_TILES); // random column index
			} while (map[row][col] == true);

			map[row][col] = true;
			mines.add(new int[] { col + 1, row + 1 }); // indexing starts at 1
			mines_left++;
			if (count >= Main.total_mines) {
				break;
			}
		}

		// Set supermine index as one of the mines
		int super_mine_index = random.nextInt(mines.size());

		for (int i = 0; i < Main.total_mines; i++) {
			int[] bomb_list_elem = mines.get(i);
			String is_super_mine;
			supermine_coords = mines.get(super_mine_index);
			if (i != super_mine_index) {
				int[] this_row = mines.get(i);
				this_row = Arrays.copyOf(this_row, this_row.length + 1);
				this_row[this_row.length - 1] = 0;
				mines.set(i, this_row);
			}
			if (i == super_mine_index) {
				int[] this_row = mines.get(i);
				supermine_x = this_row[0];
				supermine_y = this_row[1];
				this_row = Arrays.copyOf(this_row, this_row.length + 1);
				this_row[this_row.length - 1] = 1;
				mines.set(i, this_row);
			}
		}

		// record of every tile that has a mine, 1 means supermine
		try {
			File file = new File("mines.txt"); 
			file.createNewFile();
			FileWriter writer = new FileWriter("mines.txt");
			for (int[] mine : mines) {
				writer.write(Arrays.toString(mine) + System.lineSeparator());
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int y = 0; y < Y_TILES; y++) {
			for (int x = 0; x < X_TILES; x++) {
				Tile tile = new Tile(x, y, map[x][y]); // value between 0 and 1 for mine

				grid[x][y] = tile; // assign new tile to grid
				root.getChildren().add(tile);
			}
		}

		for (int y = 0; y < Y_TILES; y++) { // populate grid
			for (int x = 0; x < X_TILES; x++) {
				Tile tile = grid[x][y];

				if (tile.hasBomb)
					continue;

				// obtain list of neighbors and filter to see which has a bomb, keep the ones
				// with mines
				long bombs = getNeighbors(tile).stream().filter(t -> t.hasBomb).count();

				if (bombs > 0)
					tile.text.setText(String.valueOf(bombs)); // set value to bombs around tile
			}
		}
		return root;
	}

	private static List<Tile> getNeighbors(Tile tile) { // get neighbors of tile
		List<Tile> neighbors = new ArrayList<>();

		int[] points = new int[] { // distances in x, y axis
				-1, -1, // top left neighbor
				-1, 0, // left neighbor
				-1, 1, // bottom left
				0, -1, // bottom
				1, -1, // top right
				1, 0, // right
				1, 1, // bottom right
				0, 1 // top
		};

		for (int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];

			int newX = tile.x + dx;// neighbor's X coordinate
			int newY = tile.y + dy;//neighbor's Y coordinate

			if (newX >= 0 && newX < X_TILES && newY >= 0 && newY < Y_TILES) { // check for valid
				neighbors.add(grid[newX][newY]);
			}
		}

		return neighbors;
	}

	/**
	 * Reveals the solution by opening all the tiles. Stops the timer and registers
	 * game as lost.
	 */
	public static void reveal_solution() {
		Main.stopTimer();
		//Rounds this_round = new Rounds(1, Main.total_mines, Tile.total_tries, (Main.time - Main.timestamp), "Computer");
		for (int i = 0; i < X_TILES; i++)
			for (int j = 0; j < Y_TILES; j++) {
				try {
					grid[i][j].Reveal();
				} catch (NullPointerException e) {
					return; // Game has not started yet
				}
			}
	}

	/**
	 * Method used to mark and un-mark a tile as a mine. 
	 * Un-marking a tile returns it to its previous state. 
	 * If number of marked tiles is less than the number of the mines in total, more tiles can be marked. 
	 * If number of marked tiles is greater than the number of
	 * the mines in total, more tiles can't be marked before unmarking one. 
	 * If the tile is the supermine tile and there have been less than 4 tries, supermine
	 * opens.
	 * 
	 * @param tile the Tile tile to be marked
	 */
	public static void open_bomb_safely(Tile tile) { 
		if (marked <= Main.total_mines) {
			if (!tile.isOpen) { // define colours
				if (tile.isMarked) {
					tile.isMarked = false;
					tile.text.setVisible(false);
					marked--;
					Color borderFill = Color.web("#EFE0C9"); // light beige
					tile.border.setFill(borderFill); // bring back to first state, without opening
					if (tile.hasBomb) {
						mines_left++;
					}
					return;
				} else {
					tile.isMarked = true;
					marked++;
					tile.border.setFill(Color.DARKRED);

					if (tile.hasBomb) {
						tile.isMarked = true;
						mines_left = mines_left - 1;

						// reveal tiles for super mine
						if (Main.super_mine == 1 && Main.difficulty == 2) {
							if (tile.x == supermine_y - 1 && tile.y == supermine_x - 1 && Tile.total_tries <= 4) {
								mines_left++;

								grid[tile.x][tile.y].border.setFill(Color.BLACK);
								grid[tile.x][tile.y].text.setText("S"); // mark supermine
								grid[tile.x][tile.y].text.setFill(Color.DARKRED);

								for (int dx = 0; dx < X_TILES; dx++) {
									grid[dx][tile.y].Reveal();
									Color borderFill = Color.web("#E2D6F6");
									grid[dx][tile.y].border.setFill(borderFill);
									if (grid[dx][tile.y].hasBomb == true) {
										grid[dx][tile.y].text.setFill(Color.DARKRED);
										grid[dx][tile.y].border.setFill(Color.BLACK);
									}
									if ((grid[dx][tile.y].isMarked == true) && (grid[dx][tile.y].hasBomb == false)) {
										marked--;
									}
								}
								for (int dy = 0; dy < Y_TILES; dy++) {
									grid[tile.x][dy].Reveal();
									Color borderFill = Color.web("#E2D6F6");
									grid[tile.x][dy].border.setFill(borderFill);
									if (grid[tile.x][dy].hasBomb == true) {
										grid[tile.x][dy].border.setFill(Color.BLACK);
										grid[tile.x][dy].text.setFill(Color.DARKRED);
									}
									if ((grid[tile.x][dy].isMarked) == true && (grid[tile.x][dy].hasBomb == false)) {
										marked--;
									}
								}
							}
						}
						
					}
				}
			}
		}

		if (marked > Main.total_mines) {
			if (!tile.isOpen) {
				if (tile.isMarked) {
					tile.isMarked = false;
					marked--;
					Color borderFill = Color.web("#EFE0C9"); // light beige
					tile.border.setFill(borderFill); // bring back to first state, without opening
					return;
				}
			}
		}
	}

	/**
	 * Method used to open tiles. 
	 * If the tile is already opened, the method returns. If it is not, 
	 * it is marked as opened. 
	 * If the tile doesn't have any tiles with mines around it,
	 * the tiles around it open recursively. 
	 * If the tile has a mine, the game is
	 * lost, the game statistics are saved in a
	 * Rounds object. If all tiles that don't have a mine are opened, the game is
	 * Rounds object.
	 * 
	 * @param tile the Tile tile to be opened
	 * @param main the main body of the game that the game returns to if lost or won
	 */
	public static void open(Tile tile, Main main) {
		if(tile.isMarked) {
			marked--;
		}
		
		// if already opened, don't do anything
		if (tile.isOpen) {
			return;
		}

		// if tile has a mine
		if (tile.hasBomb) {
			winner = false;
			tile.text.setVisible(true);
			tile.border.setFill(Color.BLACK);
			Main.stopTimer();
			//System.out.println("Loser");
			Rounds this_round = new Rounds(1, Main.total_mines, Tile.total_tries, (Main.time - Main.timestamp),
					"Computer");
			reveal_solution();
			Stage popup = new Stage();
			VBox popupContent = new VBox();
			Label label = new Label("You lost!");
			label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
			label.setStyle("-fx-text-fill: #f7b492;");
			label.setAlignment(Pos.CENTER);
			popupContent.getChildren().addAll(label);
			VBox layout = new VBox(10);
			layout.getChildren().addAll(popupContent);
			layout.setAlignment(Pos.CENTER);
			Scene popupScene = new Scene(layout, 150, 150);
			popup.setScene(popupScene);
			popup.setResizable(false);
			popup.showAndWait();// lose
			try {
				main.gridPane.getChildren().remove(1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		// if tile doesn't have a mine
		else {
			opened++;
			tile.isOpen = true;
			tile.text.setVisible(true);
			Color borderFill = Color.web("#E2D6F6");
			tile.border.setFill(borderFill);
			// if all tiles without a mine are opened
			if ((Main.difficulty == 1 && opened == 81 - Main.total_mines)
					|| (Main.difficulty == 2 && opened == 256 - Main.total_mines)) {
				winner = true;
				//System.out.println("Congrats!");
				Main.stopTimer();
				Rounds this_round = new Rounds(1, Main.total_mines, Tile.total_tries, (Main.time - Main.timestamp),
						"Player");
				reveal_solution();
				try {
					main.gridPane.getChildren().remove(1);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Stage popup = new Stage();
				popup.setResizable(false);
				VBox popupContent = new VBox();
				Label label = new Label("You won!");
				label.setFont(Font.font("Arial", FontWeight.BOLD, 24)); // set font size to 24 and weight to bold

				label.setStyle("-fx-text-fill: #f7b492;");
				label.setAlignment(Pos.CENTER); // center the text horizontally
				popupContent.getChildren().addAll(label);
				VBox layout = new VBox(10);
				layout.getChildren().addAll(popupContent);
				Scene popupScene = new Scene(layout, 150, 150);
				popup.setScene(popupScene);
				popup.showAndWait();
				return;
			}
		}

		if (tile.text.getText().isEmpty()) { // if it has no neighbors that have a bomb
			getNeighbors(tile).forEach(t -> {
				open(t, main);
			});
		}
	}
}
