package gameServer;

import java.awt.Rectangle;

public class Player {
  private int playerID;
  private double xPos, yPos;
  private int health;
  private boolean isDead = false;

  private Rectangle area;

  public Player(int playerID, int xPos, int yPos,int health) {
     this(playerID, xPos, yPos, health, new Rectangle(xPos,yPos, 32, 32));
  }

  public Player(int playerID, int xPos, int yPos, int health, Rectangle area) {
     this.playerID = playerID;
     this.xPos = xPos;
     this.yPos = yPos;
     this.health = health;
     this.area = area;
  }

  public void update(int health) {
     this.health = health;
  }

  public void update(int xPos, int yPos) {
     this.xPos = xPos;
     this.yPos = yPos;
     this.area.setLocation(xPos, yPos);
  }

  public void update(int xPos, int yPos, int health) {
     this.update(health);
     this.update(xPos, yPos);
  }

  public Rectangle getArea(){
     return area;
  }

  public boolean collision(Player otherPlayer) {
     return this.area.intersects(otherPlayer.getArea());
  }

  public int getID(){
     return playerID;
  }

  public boolean isDead() {
	return isDead;
}

public void setDead(boolean isDead) {
	this.isDead = isDead;
}

@Override
  public String toString() {
     return playerID + "," + (int)xPos + "," + (int)yPos + "," + health;
  }
}
