package duoGame;

import java.awt.Image;

public class Missile extends Projectile {
	public static final int WIDTH = 26, HEIGHT = 42, SPEED = 240, DAMAGE = 4;
	
	public Missile(int projectileID, int xPos, int yPos, double angle, Image image) {
		super(projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED, image);
	}
	
	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
