package gameClient;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

public class Player {
	protected int playerID;
	protected double xPos, yPos;
	private int dx = 0, dy = 0;
	protected static int speed = 70;
//	private static double rotationSpeed = Math.PI / 2;
	protected long lastUpdateTime = 0;
//	protected int health;
	private Image img;
	private double angle = 0; // Angle of rotation in radians --> is 0; <-- is pi

	public Player(int playerID, int xPos, int yPos, Image img) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.img = img;
//		this.health = health;
	}
	
	public void move(long deltaTime) {
//		System.out.println(dx*(deltaTime/1e9)*speed);
//		System.out.println(dy*(deltaTime/1e9)*speed);
		xPos += dx*(deltaTime/1e9)*speed;
		yPos += dy*(deltaTime/1e9)*speed;
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
	    AffineTransform old = g.getTransform();
	    if(dx == 0 && dy == 0) angle = 0;
	    else angle = Math.atan2(dy, dx) + Math.PI / 2;

	    // Translate to the center of the player image for rotation
	    g.translate(xPos + img.getWidth(null) / 2, yPos + img.getHeight(null) / 2);
	    g.rotate(angle);

	    // Draw the image, centered on the translation point
	    g.drawImage(img, -img.getWidth(null) / 2, -img.getHeight(null) / 2, null);

	    // Restore the old transform
	    g.setTransform(old);
	}

	@Override
	public String toString() {
		return playerID + "," + dx + "," + dy + "," + (int)xPos + "," + (int)yPos;
	}

	
	public void setAngle(double angle) {
	    this.angle = angle;
	}
}

