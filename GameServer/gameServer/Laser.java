package gameServer;

public class Laser extends Projectile {
	public static final int WIDTH = 14, HEIGHT = 47, SPEED = 330, DAMAGE = 2;

	public Laser(Player player, int projectileID, double xPos, double yPos, double angle) {
		super(player, projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED);
	}

	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
