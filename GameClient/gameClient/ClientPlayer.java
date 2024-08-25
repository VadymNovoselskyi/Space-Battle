package gameClient;

public class ClientPlayer extends Player {
	private int dx = 0, dy = 0, speed = 175;
	private double xLast = 0, yLast = 0;

	public ClientPlayer(int playerID, int xPos, int yPos, int health) {
		super(playerID, xPos, yPos, health);
		xLast = xPos;
		yLast = yPos;
	}

	public boolean update(long deltaTime){
		xPos += dx*(deltaTime/1000000000.0)*speed;
		yPos += dy*(deltaTime/1000000000.0)*speed;

		if(Math.abs(xLast - xPos) > 1 || Math.abs(yLast - yPos) > 1){
			xLast = xPos;
			yLast = yPos;
			return true;
		} else {
			return false;
		}
	}

	public void setDirectionX(int dx){
		this.dx = dx;
	}
	public void setDirectionY(int dy){
		this.dy = dy;
	}
}

