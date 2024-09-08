package gameServer;

import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Projectile {
	private Player player;
	private int projectileID, dx, dy, width, height, speed;
	private double xPos, yPos, angle;
	protected long lastUpdateTime;
	
	public Projectile(Player player, int projectileID, double xPos, double yPos, double angle, int width, int height, int speed) {
		this.player = player; 
		this.projectileID = projectileID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.angle = angle;
		this.width = width;
		this.height = height;
		this.speed = speed;

	    lastUpdateTime = System.nanoTime();
	}
	
	public void move(long deltaTime) {
		xPos += (deltaTime/1e9)*speed * Math.sin(angle);
		yPos -= (deltaTime/1e9)*speed * Math.cos(angle);
	}
	
	public void hit(Player player) {
		player.setHealth(player.getHealth() - 1);
		if(player.getHealth() <= 0) player.setDead(true);
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
		Rectangle2D gameScreen = new Rectangle2D.Double(0, 0, Server.GAME_WIDTH, Server.GAME_HEIGHT);

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
		return projectileID +","+ " " +","+ (int)xPos +","+ (int)yPos +","+ (angle % (Math.PI * 2)) +","+ 0.0 +","+ 0 +","+ true +","+ lastUpdateTime; 
	}

	public double getxPos() {
		return xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public int getSpeed() {
		return speed;
	}

	public Player getPlayer() {
		return player;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}

}
