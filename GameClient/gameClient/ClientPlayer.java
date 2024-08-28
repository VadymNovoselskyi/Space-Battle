package gameClient;

import java.awt.Image;

public class ClientPlayer extends Player {
	public ClientPlayer(int playerID, int xPos, int yPos, Image img) {
		super(playerID, xPos, yPos, img);
	}
	
	public void move(long deltaTime) {
		xPos += super.getDirectionX()*(deltaTime/1000000000.0)*super.getSpeed();
		yPos += super.getDirectionY()*(deltaTime/1000000000.0)*super.getSpeed();
	}
}

