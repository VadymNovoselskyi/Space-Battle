package gameClient;

public class ClientPlayer extends Player {
	private int dx = 0, dy = 0;

	public ClientPlayer(int playerID, int xPos, int yPos, int health) {
		super(playerID, xPos, yPos, health);
	}
	
	public void move(long deltaTime) {
		xPos += dx*(deltaTime/1000000000.0)*speed;
		yPos += dy*(deltaTime/1000000000.0)*speed;
	}

	public void setDirectionX(int dx){
		this.dx = dx;
	}
	public void setDirectionY(int dy){
		this.dy = dy;
	}

	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}
}

