package duoGame;

import java.awt.Image;

public class Laser extends Projectile {
	public static final int WIDTH = 14, HEIGHT = 47, SPEED = 350, DAMAGE = 2;

	public Laser(int projectileID, int xPos, int yPos, double angle, Image image) {
		super(projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED, image);
	}
	
	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
