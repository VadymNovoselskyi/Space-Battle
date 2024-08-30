package gameClient;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class Player {
	protected int playerID;
	private Image img;
	protected double xPos, yPos;
	private int dx = 0, dy = 0;
	protected long lastUpdateTime = 0;
	private double angle = 0, supposedAngle;
	//	protected int health;
	public static final int SPEED = 70, PLAYER_HITBOX_WIDTH = 42, PLAYER_HITBOX_HEIGHT = 84;

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
		xPos += dx*(deltaTime/1e9)*SPEED;
		yPos += dy*(deltaTime/1e9)*SPEED;
	}

	public boolean collision(Player player) {
		Path2D myHitbox = this.getHitbox();
		Path2D playerHitbox = player.getHitbox();

		// Check if the bounding boxes (x-y alligned, so bigger than hitboxes) intersect first for a quick elimination
		if (!myHitbox.getBounds2D().intersects(playerHitbox.getBounds2D())) return false;
		// Check if the actual hitboxes intersect
		else {
			Area myArea = new Area(myHitbox);
			Area playerArea = new Area(playerHitbox);

			myArea.intersect(playerArea);

			return !myArea.isEmpty();	
		}
	}
	public boolean collision(Projectile projectile) {
		Path2D myHitbox = this.getHitbox();
		Path2D projectileHitbox = projectile.getHitbox();

		// Check if the bounding boxes (x-y alligned, so bigger than hitboxes) intersect first for a quick elimination
		if (!myHitbox.getBounds2D().intersects(projectileHitbox.getBounds2D())) return false;
		// Check if the actual hitboxes intersect
		else {
			Area myArea = new Area(myHitbox);
			Area projectuleArea = new Area(projectileHitbox);

			myArea.intersect(projectuleArea);

			return !myArea.isEmpty();	
		}
	}
	public boolean borderCollision() {
		Path2D hitbox = this.getHitbox();
		Rectangle2D gameScreen = new Rectangle2D.Double(0, 0, GameController.GAME_WIDTH, GameController.GAME_HEIGHT);

		// Check if any part of the hitbox is outside the game screen
		if (gameScreen.contains(hitbox.getBounds2D())) return false;

		// Create Area objects for more precise intersection checks
		Area hitboxArea = new Area(hitbox);
		Area gameScreenArea = new Area(gameScreen);

		hitboxArea.subtract(gameScreenArea);
		// If the hitboxArea is not empty after subtraction, then some part of it was outside the gameScreen
		return !hitboxArea.isEmpty();
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
		double interpolationFactor = 0.25;
		double angleDifference = (supposedAngle - angle) % (2 * Math.PI);

		// Ensure angleDifference is within [-π, π] so the ship takes the shortest path
		//the shortestroute will always be pi or less, so if angleDifference is more than that
		//we need to fix it by chnaging its sign and value to 2pi - angle
		if (Math.abs(angleDifference) > Math.PI) {
			angleDifference -= Math.signum(angleDifference) * 2 * Math.PI;
		}

		if (Math.abs(supposedAngle - angle) > 0.4) { // A small threshold to stop jitter
			angle = angle + interpolationFactor * angleDifference;
		} else {
			angle = supposedAngle;
		}

		AffineTransform old = g.getTransform();
		// Translate to the center of the player image for rotation
		g.translate(xPos + PLAYER_HITBOX_WIDTH / 2, yPos + PLAYER_HITBOX_HEIGHT / 2);
		g.rotate(angle);

		// Draw the image, centered on the translation point
		g.drawImage(img, -PLAYER_HITBOX_WIDTH / 2, -PLAYER_HITBOX_HEIGHT / 2, null);

		// Restore the old transform
		g.setTransform(old);	
	}


	// Method to get the corners of the rotated rectangle
	private Path2D getHitbox() {
		double centerX = xPos + PLAYER_HITBOX_WIDTH / 2;
		double centerY = yPos + PLAYER_HITBOX_HEIGHT / 2;

		// Define the original corners of the image
		double[][] corners = {
				{xPos, yPos},             // Top-left
				{xPos + PLAYER_HITBOX_WIDTH, yPos},     // Top-right
				{xPos + PLAYER_HITBOX_WIDTH, yPos + PLAYER_HITBOX_HEIGHT}, // Bottom-right
				{xPos, yPos + PLAYER_HITBOX_HEIGHT}     // Bottom-left
		};

		// Create a Path2D object to hold the hitbox outline
		Path2D hitbox = new Path2D.Double();

		// Calculate the rotated corners and add them to the path
		for (int i = 0; i < corners.length; i++) {
			double[] corner = corners[i];
			double px = corner[0];
			double py = corner[1];

			// Rotate the corner around the center
			double new_px = Math.cos(angle) * (px - centerX) - Math.sin(angle) * (py - centerY) + centerX;
			double new_py = Math.sin(angle) * (px - centerX) + Math.cos(angle) * (py - centerY) + centerY;

			if (i == 0) {
				hitbox.moveTo(new_px, new_py); // Move to the first corner
			} else {
				hitbox.lineTo(new_px, new_py); // Draw lines to the other corners
			}
		}
		hitbox.closePath();
		return hitbox;
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

	public int getPlayerID() {
		return playerID;
	}

	@Override
	public String toString() {
		return playerID + "," + dx + "," + dy + "," + (int)xPos + "," + (int)yPos;
	}

	public void setSupposedAngle(double supposedAngle) {
		this.supposedAngle = supposedAngle;
	}
}

