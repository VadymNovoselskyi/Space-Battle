package gameClient;

import java.awt.Image;

public class Missile extends Projectile {
	public static final int SPEED = 160, WIDTH = 26, HEIGHT = 42;
	
	public Missile(Image img, int xPos, int yPos, int dx, int dy, int projectileID) {
		super(img, xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED, projectileID);
	}
}
