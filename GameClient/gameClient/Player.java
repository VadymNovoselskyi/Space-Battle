package gameClient;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import gameServer.Server;

import java.awt.Image;

public class Player {
	protected static final int HITBOX_WIDTH = 42, HITBOX_HEIGHT = 84, SPEED = 70;
	protected static final double MAX_ROTATION_SPEED = Math.PI, INTERPOLATION_FACTOR = 0.25, JITTER_THRESHOLD = 0.1;
	private int playerID;
	protected double xPos, yPos, xOffset, yOffset;
	protected long lastUpdateTime = 0;
	private double angle = 0, supposedAngle;
	private Image img;
	private boolean still = true;

	public Player(int playerID, int xPos, int yPos, Image img) {
		this(playerID, xPos, yPos, 0, img);
	}
	public Player(int playerID, int xPos, int yPos, double angle, Image img) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.angle = angle;
		this.img = img;
	}

	public void move(long deltaTime) {
		if(!still) {			
			xPos += (deltaTime/1e9)*SPEED * Math.sin(angle);
			yPos -= (deltaTime/1e9)*SPEED * Math.cos(angle);
			System.out.println("Before Update:");
			System.out.println("xPos: " + xPos + ", yPos: " + yPos);
			System.out.println("xOffset: " + xOffset + ", yOffset: " + yOffset);

			// Your existing update code here
			xPos = (Math.abs(xOffset) < 10) ? xPos + Math.signum(xOffset) * xOffset : xPos + xOffset * 0.5;
			yPos = (Math.abs(yOffset) < 10) ? yPos + Math.signum(yOffset) * yOffset : yPos + yOffset * 0.5;

			xOffset = (Math.abs(xOffset) < 10) ? 0 : xOffset - xOffset * 0.5;
			yOffset = (Math.abs(yOffset) < 10) ? 0 : yOffset - yOffset * 0.5;

			System.out.println("After Update:");
			System.out.println("xPos: " + xPos + ", yPos: " + yPos);
			System.out.println("xOffset: " + xOffset + ", yOffset: " + yOffset);

		}
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

			//Check if any pice of myArea is inside of playerArea
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
			Area projectileArea = new Area(projectileHitbox);

			//Check if any pice of myArea is inside of projectileArea
			myArea.intersect(projectileArea);
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


	public void draw(Graphics2D g) {
		if(!still) {
			double angleDifference = (supposedAngle - angle) % (2 * Math.PI);
			
			// Ensure angleDifference is within [-π, π] so the ship takes the shortest path
			//the shortestroute will always be pi or less, so if angleDifference is more than that
			//we need to fix it by chnaging its sign and value to 2pi - angle
			if (Math.abs(angleDifference) > Math.PI) {
				angleDifference -= Math.signum(angleDifference) * 2 * Math.PI;
			}
			if(Math.abs(angleDifference) > Math.PI * 19 / 20) angleDifference = Math.PI;

			if(Math.abs(angleDifference * INTERPOLATION_FACTOR) > MAX_ROTATION_SPEED / GameController.FPS_PLAYER) {
				angle += Math.signum(angleDifference) * MAX_ROTATION_SPEED / GameController.FPS_PLAYER;
			}
			else if (Math.abs(angleDifference) > JITTER_THRESHOLD) { // A small threshold to stop jitter
				angle = angle + INTERPOLATION_FACTOR * angleDifference;
			} else {
				angle = supposedAngle;

			}
		}

		AffineTransform old = g.getTransform();
		// Translate to the center of the player image for rotation
		g.translate(xPos + HITBOX_WIDTH / 2, yPos + HITBOX_HEIGHT / 2);
		g.rotate(angle);

		// Draw the image, centered on the translation point
		g.drawImage(img, -HITBOX_WIDTH / 2, -HITBOX_HEIGHT / 2, null);

		// Restore the old transform
		g.setTransform(old);	
	}


	// Method to get the corners of the rotated rectangle
	private Path2D getHitbox() {
		double centerX = xPos + HITBOX_WIDTH / 2;
		double centerY = yPos + HITBOX_HEIGHT / 2;

		// Define the original corners of the image
		double[][] corners = {
				{xPos, yPos},             // Top-left
				{xPos + HITBOX_WIDTH, yPos},     // Top-right
				{xPos + HITBOX_WIDTH, yPos + HITBOX_HEIGHT}, // Bottom-right
				{xPos, yPos + HITBOX_HEIGHT}     // Bottom-left
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

	public void update(int xPos, int yPos, double angle, double supposedAngle) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.angle = angle;
		this.supposedAngle = supposedAngle;
	}


	public int getPlayerID() {
		return playerID;
	}

	public double getAngle() {
		return angle;
	}

	public boolean isStill() {
		return still;
	}


	public void setSupposedAngle(double supposedAngle) {
		this.supposedAngle = supposedAngle;
	}

	public void setStill(boolean still) {
		this.still = still;
	}


	@Override
	public String toString() {
		return playerID + "," + (int)xPos + "," + (int)yPos + "," + supposedAngle +"," + still;
	}
}

