package gameClient;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

public class Player {
	protected int playerID;
	protected double xPos, yPos;
	private int dx = 0, dy = 0;
	private static int speed = 70;
	protected long lastUpdateTime = 0;
	//	protected int health;
	private Image img;
	private double angle = 0, supposedAngle;

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
		double interpolationFactor = 0.25;
		double angleDifference = (supposedAngle - angle) % (2 * Math.PI);
		
		// Ensure angleDifference is within [-π, π] so the ship takes the shortest path
		//the shortestroute will always be pi or less, so if angleDifference is more than that
		//we need to fix it by chnaging its sign and value to 2pi - angle
		if (Math.abs(angleDifference) >  Math.PI) {
//			System.out.println(angleDifference);
		    angleDifference -= Math.signum(angleDifference) * 2 * Math.PI;
		}

		if (Math.abs(supposedAngle - angle) > 0.4) { // A small threshold to stop jitter
			angle = angle + interpolationFactor * angleDifference;
		} else {
			angle = supposedAngle;
		}

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

	public void setSupposedAngle(double supposedAngle) {
		this.supposedAngle = supposedAngle;
	}

	public static int getSpeed() {
		return speed;
	}
}

