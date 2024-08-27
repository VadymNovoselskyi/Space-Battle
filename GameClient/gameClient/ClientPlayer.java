package gameClient;

public class ClientPlayer extends Player {
	public ClientPlayer(int playerID, int xPos, int yPos, int health) {
		super(playerID, xPos, yPos, health);
	}
	
	public void move(long deltaTime) {
		xPos += super.getDirectionX()*(deltaTime/1000000000.0)*speed;
		yPos += super.getDirectionY()*(deltaTime/1000000000.0)*speed;
	}
}

