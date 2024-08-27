package gameClient;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
	protected int playerID;
	protected double xPos, yPos;
	private int dx = 0, dy = 0;
	protected int speed = 70;
	protected long lastUpdateTime = 0;
//	protected int health;
	private Color color = Color.RED;

	public Player(int playerID, int xPos, int yPos) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
//		this.health = health;
	}
	
	public void move(long deltaTime) {
		xPos += dx*(deltaTime/1000000000.0)*speed;
		yPos += dy*(deltaTime/1000000000.0)*speed;
	}

	public void setDirectionX(int dx) {
		this.dx = dx;
	}

	public void setDirectionY(int dy) {
		this.dy = dy;
	}

	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void update(int health) {
//		this.health = health;
	}

	public void update(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public void update(int dx, int dy, int xPos, int yPos) {
		this.dx = dx;
		this.dy = dy;
		this.xPos = xPos;
		this.yPos = yPos;
	}
	
	public void update(int xPos, int yPos, int health) {
		this.update(health);
		this.update(xPos, yPos);
	}

	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int)xPos, (int)yPos, 32, 32);
	}

	@Override
	public String toString() {
		return playerID + "," + dx + "," + dy + "," + (int)xPos + "," + (int)yPos;
	}
}

