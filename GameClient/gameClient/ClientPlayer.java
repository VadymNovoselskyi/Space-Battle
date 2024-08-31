package gameClient;

import java.awt.Image;

public class ClientPlayer extends Player {
	private int shotCounter;
	private long lastShotTime;
	private static final double SHOT_COOLDOWN = 1.4; //in seconds
	private static final int SHOTS_FOR_MISSILE = 3; //how many shots to fire before shooting missile

	public ClientPlayer(int playerID, int xPos, int yPos, Image image) {
		super(playerID, xPos, yPos, image);
		shotCounter = 0;
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
}

