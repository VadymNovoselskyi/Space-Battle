package gameClient;

public class ClientPlayer extends Player {
	public ClientPlayer(int playerID, int xPos, int yPos) {
		super(playerID, xPos, yPos);
	}
	
	public void move(long deltaTime) {
		xPos += super.getDirectionX()*(deltaTime/1000000000.0)*speed;
		yPos += super.getDirectionY()*(deltaTime/1000000000.0)*speed;
	}
}

