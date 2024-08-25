package gameClient;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
	private int playerID;
	protected double xPos, yPos;
	private int health;
	private Color color = Color.RED;

	public Player(int playerID, int xPos, int yPos, int health) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.health = health;
	}

	public void setColor(Color color){
		this.color = color;
	}

	public void update(int health){
		this.health = health;
	}

	public void update(int xPos, int yPos){
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public void update(int xPos, int yPos, int health){
		this.update(health);
		this.update(xPos, yPos);
	}

	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int)xPos, (int)yPos, 32, 32);
	}

	@Override
	public String toString() {
		return playerID + "," + (int)xPos + "," + (int)yPos + "," + health;
	}
}

