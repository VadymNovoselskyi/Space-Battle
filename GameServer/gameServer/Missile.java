package gameServer;

public class Missile extends Projectile {
	public static final int DAMAGE = 5, SPEED = 160, WIDTH = 26, HEIGHT = 42;
	
	public Missile(double xPos, double yPos, int dx, int dy, int playerID, int projectileID) {
		super(xPos, yPos, dx, dy, WIDTH, HEIGHT, SPEED, playerID, projectileID);
	}
	
	public void hit(Player player) {
		player.setHealth(player.getHealth() - DAMAGE);
		if(player.getHealth() <= 0) player.setDead(true);
	}
}
