package gameServer;

public class Missile extends Projectile {
	public static final int WIDTH = 26, HEIGHT = 42, SPEED = 190, DAMAGE = 5;
	
	public Missile(int playerID, int projectileID, double xPos, double yPos, double angle) {
		super(playerID, projectileID, xPos, yPos, angle, WIDTH, HEIGHT, SPEED);
	}
	
	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
