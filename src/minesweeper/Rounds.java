package minesweeper;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Rounds {
	private int roundNumber;
	private int minesForRound;
	private int tries;
	private int totalTime;
	private String winner;
	static List<Rounds> rounds_list = new ArrayList<>();

	public Rounds(int roundNumber, int minesForRound, int tries, int totalTime, String winner) {
		this.roundNumber = roundNumber;
		this.minesForRound = minesForRound;
		this.tries = tries;
		this.totalTime = totalTime;
		this.winner = winner;
		rounds_list.add(this);
	}
	
    public static void addRound(Rounds round) {
        rounds_list.add(round);
    }

	public static List<Rounds> getRounds() {
		return rounds_list;
	}
	
	public static int getMines (Rounds round) {
		return round.minesForRound;
	}
	
	public static int getTries (Rounds round) {
		return round.tries;
	}
	
	public static int getTotalTime (Rounds round) {
		return round.totalTime;
	}
	
	public static String getWinner (Rounds round) {
		return round.winner;
	}


}
