package gameClient;

import java.awt.Image;

public class ClientPlayer extends Player {
	private static final double SHOT_COOLDOWN = 1.4; //in seconds
	private static final int SHOTS_FOR_MISSILE = 3; //how many shots to fire before shooting missile
	private int dx = 0, dy = 0;
	private int shotCounter = 0;
	private long lastShotTime;

	public ClientPlayer(int playerID, int xPos, int yPos, double angle, String name, Image image) {
		super(playerID, xPos, yPos, angle, name, image);
		lastShotTime = System.nanoTime();
	}

	public String tryToFire() {
		if(System.nanoTime() - lastShotTime > SHOT_COOLDOWN * 1e9) {
			lastShotTime = System.nanoTime();
			shotCounter++;
			if(shotCounter % SHOTS_FOR_MISSILE == 0) return "FIRE_MISSILE";
			else return "FIRE_LASER";
		}
		else return null;
	}
	
	public void move(long deltaTime) {
		if(dx == 0 && dy == 0) {
			super.setSupposedAngle(super.getAngle());
			super.setStill(true);
		}
		else  {
			super.setSupposedAngle(Math.atan2(dy, dx) + Math.PI / 2);
			super.setStill(false);
		}
		super.move(deltaTime);
	}

	
	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}

	public void setDirectionX(int dx) {
		this.dx = dx;
	}

	public void setDirectionY(int dy) {
		this.dy = dy;
	}
}

