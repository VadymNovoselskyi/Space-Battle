package duoGame;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.Image;

public class Projectile {
	private int projectileID, dx, dy, width, height, speed;
	private double xPos, yPos, angle;
	protected long lastUpdateTime;
	private Image image;

	public Projectile(int projectileID, int xPos, int yPos, double angle, int width, int height, int speed, Image image) {
		this.projectileID = projectileID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.angle = angle;
		this.width = width;
		this.height = height;
		this.speed = speed; 
		this.image = image;	 
		
		this.lastUpdateTime = System.nanoTime();
	}

	public void move(long deltaTime) {
		xPos += (deltaTime/1e9)*speed * Math.sin(angle);
		yPos -= (deltaTime/1e9)*speed * Math.cos(angle);
	}
	
	public void hit(Player player) {}

	public void draw(Graphics2D g) {
		AffineTransform old = g.getTransform();

		// Translate to the center of the player image for rotation
		g.translate(xPos + image.getWidth(null) / 2, yPos + image.getHeight(null) / 2);
		g.rotate(angle);

		// Draw the image, centered on the translation point
		g.drawImage(image, -image.getWidth(null) / 2, -image.getHeight(null) / 2, null);

		// Restore the old transform
		g.setTransform(old);
	}



	// Method to get the corners of the rotated rectangle
	public Path2D getHitbox() {
		double centerX = xPos + width / 2;
		double centerY = yPos + height / 2;

		// Define the original corners of the image
		double[][] corners = {
				{xPos, yPos},             // Top-left
				{xPos + width, yPos},     // Top-right
				{xPos + width, yPos + height}, // Bottom-right
				{xPos, yPos + height}     // Bottom-left
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


	@Override
	public String toString() {
		return (int)xPos + "," + (int)yPos + "," + dx + "," + dy;
	}

	public int getProjectileID() {
		return projectileID;
	}

	public double getAngle() {
		return angle;
	}

	public double getxPos() {
		return xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}
}
