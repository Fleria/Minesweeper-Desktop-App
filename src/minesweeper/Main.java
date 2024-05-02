package minesweeper;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.geometry.Pos;
import javafx.util.Duration;

public class Main extends Application {
	public static Scene scene;
	public static Stage stage;
	public static GridPane gridPane;
	private static VBox vBox;
	private static VBox parentBox;
	private static GridPane new_gridPane;

	public static int difficulty=0;
	public static int total_mines;
	public static int time;
	public static int super_mine;

	private int a, b, c, d;
	private String ID;

	public static int[][] rounds_list;

	private static String path = "./medialab";
	
	private final Menu menu_app = new Menu("Application");
	private final Menu menu_det = new Menu("Details");

	private MenuItem the_create = new MenuItem("Create");
	private MenuItem the_load = new MenuItem("Load");
	private MenuItem the_start = new MenuItem("Start");
	private MenuItem the_exit = new MenuItem("Exit");
	private MenuItem the_rounds = new MenuItem("Rounds");
	private MenuItem the_solution = new MenuItem("Solution");

	private static final Duration DURATION = Duration.seconds(5);
	public static Duration durationFinal;
	public static Timeline timeline = null;
	public static int timestamp;
	public static Label timerLabel = new Label("");

	static void startTimer() {
		timestamp = 0;
		Duration duration = Duration.seconds(time);

		timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			final Duration durationCopy = durationFinal.subtract(Duration.seconds(1)); // modify copy
			durationFinal = durationCopy;
			// System.out.println("this many seconds left " + durationCopy.toSeconds());
			timerLabel.setText("Total mines:  " + total_mines + "  Marked:  " + Game1.marked + "  Total time:   "
					+ durationFinal.toSeconds());
			timerLabel.setAlignment(Pos.CENTER);
			timerLabel.setTextFill(Color.BLACK);
			if (durationCopy.toSeconds() == 0) {
				timeline.stop();
				timestamp = (int) durationFinal.toSeconds();
				Stage popup = new Stage();
				VBox popupContent = new VBox();
				Label label = new Label("Time's up!:(");
				stopTimer();
				Rounds this_round = new Rounds(1, Main.total_mines, Tile.total_tries, (Main.time-Main.timestamp), "Computer");
				label.setFont(Font.font("Arial", FontWeight.BOLD, 24)); // set font size to 24 and weight to bold
				label.setStyle("-fx-text-fill: #f7b492;");
				label.setAlignment(Pos.CENTER); // center the text horizontally
				popupContent.getChildren().addAll(label);
				VBox layout = new VBox(10);
				layout.getChildren().addAll(popupContent);
				Scene popupScene = new Scene(layout, 150, 150);
				popup.setScene(popupScene);
				popup.setResizable(false);
				popup.show();// lose
				gridPane.getChildren().remove(1);
			}
		}));

		timeline.setCycleCount(time); // set the cycle count to the time limit
		timeline.play();
		durationFinal = duration;

	}

	static void stopTimer() {
		timeline.stop();
		timestamp = (int) durationFinal.toSeconds();
	}

	@Override
	public void start(Stage stage) throws Exception {
		// default parameters used, comment those out if needed
		difficulty = 2;
		total_mines = 25;
		time = 300;
		super_mine = 1;

		gridPane = new GridPane();
		scene = new Scene(gridPane, 300, 300); // 640 and 680 for 16x16

		String fileName = "Scenario-ID";

		MenuBar menuBar = new MenuBar();
		menuBar.setStyle("-fx-font: 14px 'Segoe UI';" + "-fx-background-color: #F5F5DC;" + "-fx-text-fill: #333333;");

		// load option from file
		the_load.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Scenario-ID File");
			File initialDirectory = new File(path);
			fileChooser.setInitialDirectory(initialDirectory);
			File selectedFile = fileChooser.showOpenDialog(stage);
			
		    if (selectedFile == null) {
		        try {
		            throw new InvalidGameException("No file selected");
		        } catch (InvalidGameException ex) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Warning");
					alert.setContentText(ex.getErrorMessage());
					alert.showAndWait();
		        }
		        return;
		    }


			try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
				String line = reader.readLine();
				ID = line;
				line = reader.readLine();
				difficulty = Integer.parseInt(line);
				line = reader.readLine();
				total_mines = Integer.parseInt(line);
				line = reader.readLine();
				time = Integer.parseInt(line);
				line = reader.readLine();
				super_mine = Integer.parseInt(line);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (selectedFile != null) {
				// read file here
				System.out.println(selectedFile.getAbsolutePath());
			}
		});

		// create option, user inputs values
		the_create.setOnAction(e -> {
			Stage popup = new Stage();
			VBox popupContent = new VBox();
			
			TextField textFieldID = new TextField();
			popupContent.getChildren().addAll(new Label("Scenario ID"), textFieldID);

			TextField textFieldD = new TextField();
			popupContent.getChildren().addAll(new Label("Difficulty, 1 (9x9) or 2 (16x16)"), textFieldD);

			TextField textFieldM = new TextField();
			popupContent.getChildren().addAll(new Label("Mines, 9-11 or 35-45"), textFieldM);

			TextField textFieldT = new TextField();
			popupContent.getChildren().addAll(new Label("Time, 120-180 or 240-360"), textFieldT);

			TextField textFieldS = new TextField();
			popupContent.getChildren().addAll(new Label("Superbomb, 0 or 1, only allowed for difficulty 2"),
					textFieldS);

			Button enterButton = new Button("Okay");
			enterButton.setOnAction(ev -> popup.close());

			VBox layout = new VBox(10);
			layout.getChildren().addAll(popupContent, enterButton);

			Scene popupScene = new Scene(layout, 350, 250);
			popup.setScene(popupScene);
			popup.setResizable(false);
			popup.showAndWait();

			try {
				String userInputID = textFieldID.getText();
				if (userInputID.isEmpty()) {
					throw new InvalidDescriptionException("Please enter all values");
				}
				ID = userInputID; // user ID
				
				String userInputD = textFieldD.getText();
				if (userInputD.isEmpty()) {
					throw new InvalidDescriptionException("Please enter all values");
				}
				a = Integer.parseInt(userInputD); // difficulty

				String userInputM = textFieldM.getText();
				if (userInputM.isEmpty()) {
					throw new InvalidDescriptionException("Please enter all values");
				}
				b = Integer.parseInt(userInputM); // number of mines

				String userInputT = textFieldT.getText();
				if (userInputT.isEmpty()) {
					throw new InvalidDescriptionException("Please enter all values");
				}
				c = Integer.parseInt(userInputT); // time

				String userInputS = textFieldS.getText();
				if (userInputS.isEmpty()) {
					throw new InvalidDescriptionException("Please enter all values");
				}
				d = Integer.parseInt(userInputS); // supermine

			} catch (InvalidDescriptionException ev) {

				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Warning");
				alert.setContentText(ev.getErrorMessage());
				alert.showAndWait();
			}

			// exception handling
			try {
				if (a == 1) {
					if (b < 9 || b > 11) {
						throw new InvalidValueException("Number must be between 9 and 11");
					}
					if (c < 120 || c > 180) {
						throw new InvalidValueException("Number must be between 120 and 180");
					}
					if (d == 1) {
						throw new InvalidValueException("Can't have supermine in easy difficulty");
					}
				}
				if (a == 2) {
					if (b < 35 || b > 45) {
						throw new InvalidValueException("Number must be between 35 and 45");
					}
					if (c < 240 || c > 360) {
						throw new InvalidValueException("Number must be between 240 and 360");
					}
				}
			} catch (InvalidValueException ev) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Warning");
				alert.setContentText(ev.getErrorMessage());
				alert.showAndWait();
			}

			try {
				File file = new File(path);
				file.createNewFile();
				FileWriter writer = new FileWriter(path + "\\"+ ID+ ".txt");
				writer.write(ID + System.lineSeparator() + a + System.lineSeparator() + b + System.lineSeparator() + c + System.lineSeparator() + d
						+ System.lineSeparator());
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		// start option
		the_start.setOnAction(e -> {
			try {
				if (difficulty==0) {
					throw new InvalidGameException("You need to load a game first");
				}
				startTimer();
				if (difficulty == 1) {
					double newWidth = 380; // new width in pixels
					double newHeight = 460; // new height in pixels
					stage.setWidth(newWidth);
					stage.setHeight(newHeight);
				} else {
					double newWidth = 655; // new width in pixels
					double newHeight = 725; // new height in pixels
					stage.setWidth(newWidth);
					stage.setHeight(newHeight);
				}
			} catch (InvalidGameException ev) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Warning");
				alert.setContentText(ev.getErrorMessage());
				alert.showAndWait();
				return;
			}
			Tile.total_tries = 0;
			GridPane new_gridPane = gridPane;
			new_gridPane.add(Game1.createContent(difficulty, total_mines, super_mine), 0, 2);
			if (gridPane.getChildren().size() > 2) {
				gridPane.getChildren().remove(1);
				stopTimer();
			}
		});

		// exit option
		the_exit.setOnAction(e -> {
			System.exit(0);
		});

		// solution
		the_solution.setOnAction(e -> {
			try {
				if (timeline == null) {
					throw new InvalidGameException("You need to create a game first");
				}
				Game1.reveal_solution();
				Rounds this_round = new Rounds(1, total_mines, Tile.total_tries, (time - timestamp), "Computer");
				stopTimer();
			} catch (InvalidGameException ev) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Warning");
				alert.setContentText(ev.getErrorMessage());
				alert.showAndWait();
			}
		});

		// rounds
		the_rounds.setOnAction(e -> {

			Stage popup = new Stage();
			popup.setResizable(false);
			VBox popupContent = new VBox(); 
			popupContent.getChildren().addAll(new Label("Rounds"));

			ListView<String> roundsList = new ListView<String>();
			
			if (Rounds.rounds_list.size() >= 6) {
			    Rounds.rounds_list.remove(0); // remove the first element
			}
			
			for (Rounds round : Rounds.rounds_list) {
				String roundString = "Total mines: " + round.getMines(round) + ", Total tries: " + round.getTries(round)
						+ ", Time left: " + (time-round.getTotalTime(round)) + ", Winner: "
						+ round.getWinner(round);
				roundsList.getItems().add(roundString);
			}


			popupContent.getChildren().add(roundsList);

			VBox layout = new VBox(10);
			layout.getChildren().addAll(popupContent);

			Scene popupScene = new Scene(layout, 350, 250);
			popup.setScene(popupScene);
			popup.showAndWait();

		});

		menu_app.getItems().addAll(the_create, the_load, the_start, the_exit);
		menu_det.getItems().addAll(the_rounds, the_solution);

		menuBar.getMenus().addAll(menu_app, menu_det);
		menuBar.setStyle("-fx-background-color: #f7b492;");
		menuBar.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());

		vBox = new VBox(menuBar);
		VBox parentBox = new VBox();

		parentBox.getChildren().addAll(vBox, timerLabel);
		parentBox.setAlignment(Pos.CENTER);

		gridPane.setStyle("-fx-background-color: beige;");
		gridPane.add(parentBox, 0, 1, 3, 1);

		stage.setTitle("MediaLab Minesweeper");
		stage.setScene(scene);
		stage.setResizable(false); // player can't resize window
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
