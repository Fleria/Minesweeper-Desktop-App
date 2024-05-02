package minesweeper;

import minesweeper.Game1.*;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class Tile extends StackPane{
	static int TILE_SIZE = 40;
	public int x, y;
	public boolean hasBomb;
	public boolean isOpen = false; 
	private Main game;
	public boolean isMarked = false;
	
	public Rectangle border = new Rectangle(TILE_SIZE-2, TILE_SIZE-2); //square
	public Text text = new Text();
	
	static int total_tries;
	
	public Tile(int x, int y, boolean hasBomb) {
		total_tries=1;
		this.x = x;
		this.y = y;
		this.hasBomb = hasBomb;
		

		Color borderFill = Color.web("#EFE0C9"); //light beige
		border.setFill(borderFill);
		border.setStroke(Color.BLACK);
		
		text.setFont(Font.font(18));
		text.setVisible(false);
		text.setText(hasBomb? "X" : ""); //if it has a bomb, display X
		
		if (this.hasBomb) {
			text.setFill(Color.DARKRED);
		}
		
		getChildren().addAll(border, text);
		
		setTranslateX(x*TILE_SIZE); //position of tile
		setTranslateY(y*TILE_SIZE);
		
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Game1.open(this, game);
    			//System.out.println("total tries "+ total_tries); //for counting clicks
    			total_tries = total_tries + 1;
            } else if (e.getButton() == MouseButton.SECONDARY) {
                Game1.open_bomb_safely(this);
            }
        });
	}
	
	public void Reveal() { //for revealing tiles, for solution and supermine
        if (this.isOpen) return;
        this.isOpen = true;

        if(this.hasBomb) {
        	Game1.mines_left--;
        	Game1.marked++;
        	this.border.setFill(Color.BLACK);
        }
        if(!this.hasBomb) {
        	Game1.opened++;
            this.border.setFill(Color.LIGHTSALMON); 
        }
        
        if(this.isMarked && this.hasBomb) {
        	Game1.marked--;
        }
        this.text.setVisible(true);
    }

}