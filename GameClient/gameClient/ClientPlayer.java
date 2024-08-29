package gameClient;

import java.awt.Image;

public class ClientPlayer extends Player {
	private int shotCounter;
	private long lastShot;
	private static final double SHOT_COOLDOWN = 1.4; //in seconds
	private static final int SHOTS_FOR_MISSILE = 3; //how many shots to fire before shooting missile
	
	public ClientPlayer(int playerID, int xPos, int yPos, Image img) {
		super(playerID, xPos, yPos, img);
		shotCounter = 0;
		lastShot = System.nanoTime();
	}
	
	public void move(long deltaTime) {
		xPos += super.getDirectionX()*(deltaTime/1000000000.0)*Player.SPEED;
		yPos += super.getDirectionY()*(deltaTime/1000000000.0)*Player.SPEED;
	}
	
	public String tryToFire() {
		if(System.nanoTime() - lastShot > SHOT_COOLDOWN * 1e9) {
//			System.out.println("Fireeeee!");
			lastShot = System.nanoTime();
			shotCounter++;
			if(shotCounter % SHOTS_FOR_MISSILE == 0) return "FIRE_MISSILE";
			else return "FIRE_LASER";
		}
		return null;
	}
}

