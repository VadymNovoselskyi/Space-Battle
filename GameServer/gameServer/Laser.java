package gameServer;

public class Laser extends Projectile {
	public static final int DAMAGE = 2, SPEED = 300, WIDTH = 14, HEIGHT = 47;

	public Laser(double xPos, double yPos, int dx, int dy, int playerID, int projectileID) {
		super(xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED, playerID, projectileID);
	}

	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
