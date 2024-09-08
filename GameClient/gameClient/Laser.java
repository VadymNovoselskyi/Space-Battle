package gameClient;

import java.awt.Image;

public class Laser extends Projectile {
	public static final int WIDTH = 14, HEIGHT = 47, SPEED = 330;

	public Laser(int projectileID, int xPos, int yPos, double angle, Image image) {
		super(projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED, image);
	}
}
