package gameClient;

import java.awt.Image;

public class Missile extends Projectile {
	public static final int WIDTH = 26, HEIGHT = 42, SPEED = 160;
	
	public Missile(int projectileID, int xPos, int yPos, int dx, int dy, Image image) {
		super(projectileID, xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED, image);
	}
}
