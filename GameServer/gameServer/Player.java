package gameServer;

import java.awt.Rectangle;

public class Player {
	private int playerID;
	private double xPos, yPos;
	private int dx = 0, dy = 0;
	private static int speed = 70;
	private double angle = 0;
	protected long lastUpdateTime;
//	private int health;
	private boolean isDead = false;

	private Rectangle area;

	public Player(int playerID, int xPos, int yPos) {
		this(playerID, xPos, yPos, new Rectangle(xPos,yPos, 42, 84));
	}

	public Player(int playerID, int xPos, int yPos, Rectangle area) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
//		this.health = health;
		this.area = area;
	}
	
	public void move(long deltaTime) {
//		System.out.println(dx*(deltaTime/1e9)*speed);
//		System.out.println(dy*(deltaTime/1e9)*speed);
		xPos += dx*(deltaTime/1e9)*speed;
		yPos += dy*(deltaTime/1e9)*speed;
	}

	public void update(int health) {
//		this.health = health;
	}

	public void update(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.area.setLocation(xPos, yPos);
	}

	public void update(int dx, int dy, int xPos, int yPos) {
		this.dx = dx;
		this.dy = dy;
		this.xPos = xPos;
		this.yPos = yPos;
		this.area.setLocation(xPos, yPos);
	}

	public void update(int xPos, int yPos, int health) {
		this.update(health);
		this.update(xPos, yPos);
	}

	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public Rectangle getArea(){
		return area;
	}

	public boolean collision(Player otherPlayer) {
		return this.area.intersects(otherPlayer.getArea());
	}

	public int getID(){
		return playerID;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	@Override
	public String toString() {
		return playerID + "," + dx +"," + dy + "," + (int)xPos + "," + (int)yPos + "," + lastUpdateTime;
	}
}
