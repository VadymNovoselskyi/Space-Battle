package gameServer;

import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Player {
	protected static final int HITBOX_WIDTH = 42, HITBOX_HEIGHT = 84, SPEED = 70, TIMEOUT = 5;
	protected static final double MAX_ROTATION_SPEED = Math.PI * 1.8, INTERPOLATION_FACTOR = 0.25, JITTER_THRESHOLD = 0.1;
	private int playerID;
	private String playerAddress;
	private double xPos, yPos;
	private int dx = 0, dy = 0;
	protected long lastUpdateTime, lastPingTime;
	private double angle = 0, supposedAngle;
	private int health;
	private boolean isDead = false;

	public Player(int playerID, int xPos, int yPos) {
		this.playerID = playerID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.health = 10;
	}

	public void move(long deltaTime) {
		double angleDifference = (supposedAngle - angle) % (2 * Math.PI);

		// Ensure angleDifference is within [-π, π] so the ship takes the shortest path
		//the shortestroute will always be pi or less, so if angleDifference is more than that
		//we need to fix it by chnaging its sign and value to 2pi - angle
		if (Math.abs(angleDifference) > Math.PI) {
			angleDifference -= Math.signum(angleDifference) * 2 * Math.PI;
		}
		
		if(Math.abs(angleDifference * Server.FPS_RENDER) > MAX_ROTATION_SPEED) {
			angle += Math.signum(angleDifference) * MAX_ROTATION_SPEED / Server.FPS_RENDER;
		}
		else if (Math.abs(supposedAngle - angle) > JITTER_THRESHOLD) { // A small threshold to stop jitter
			angle = angle + INTERPOLATION_FACTOR * angleDifference;
		} else {
			angle = supposedAngle;
		}
		
		xPos += dx*(deltaTime/1e9)*SPEED * Math.abs(Math.sin(angle));
		yPos += dy*(deltaTime/1e9)*SPEED * Math.abs(Math.cos(angle));
	}

	public void updateDirections(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
		supposedAngle = (dx == 0 && dy == 0) ? angle : Math.atan2(dy, dx) + Math.PI / 2;
	}
	
	public void updateAngle(double angle) {
		supposedAngle = angle;
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


	public int getID(){
		return playerID;
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
	
	public String getPlayerAddress() {
		return playerAddress;
	}

	public int getHealth() {
		return health;
	}

	public boolean isDead() {
		return isDead;

	}

	public void setDirectionX(int dx) {
		this.dx = dx;
	}

	public void setDirectionY(int dy) {
		this.dy = dy;
	}

	public void setPlayerAddress(String playerAddress) {
		this.playerAddress = playerAddress;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public void ping() {
		lastPingTime = System.nanoTime();
	}
	

	@Override
	public String toString() {
		return playerID +","+ (int)xPos +","+ (int)yPos +","+ dx +","+ dy +","+ lastUpdateTime;
	}
}
