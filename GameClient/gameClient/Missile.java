package gameClient;

import java.awt.Image;

public class Missile extends Projectile {
	public static final int WIDTH = 26, HEIGHT = 42, SPEED = 190;
	
	public Missile(int projectileID, int xPos, int yPos, double angle, Image image) {
		super(projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED, image);
	}
}
