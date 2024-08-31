package gameServer;

public class Laser extends Projectile {
	public static final int WIDTH = 14, HEIGHT = 47, SPEED = 300, DAMAGE = 2;

	public Laser(int playerID, int projectileID, double xPos, double yPos, int dx, int dy) {
		super(playerID, projectileID, xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED);
	}

	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
