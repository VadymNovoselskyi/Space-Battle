package gameClient;

import java.awt.Image;

public class Laser extends Projectile {
	public static final int SPEED = 300, WIDTH = 14, HEIGHT = 47;

	public Laser(Image img, int xPos, int yPos, int dx, int dy, int projectileID) {
		super(img, xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED, projectileID);
	}
}
