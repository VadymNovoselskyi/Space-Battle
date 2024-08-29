package gameServer;

import java.util.Iterator;

public class ServerRender extends Thread {

	public ServerRender() {}


	public void movePlayers() {
		for(Player player : Server.alivePlayersMap.values()) {
			player.move((System.nanoTime() - player.lastUpdateTime));
			player.lastUpdateTime = System.nanoTime();
		}
	}

	public void moveProjectiles() {
		for(Projectile projectile : Server.projectilesList) {
			projectile.move(System.nanoTime() - projectile.lastUpdateTime);
			projectile.lastUpdateTime = System.nanoTime();
			//check if its out of borders
		}
	}

	public void checkCollisions() {
		for (Player player1 : Server.alivePlayersMap.values()) {
			synchronized (Server.projectilesList) {
				Iterator<Projectile> iterator = Server.projectilesList.iterator();
				while (iterator.hasNext()) {
					Projectile projectile = iterator.next();
					if (player1.getID() != projectile.getPlayerID() && player1.collision(projectile)) {
						iterator.remove(); // Safe removal using the iterator's remove method
						projectile.hit(player1);
						Server.notifyAllClients(Command.HIT, projectile.toString());
						if (player1.isDead()) {
							Server.notifyAllClients(Command.REMOVE, player1.toString());
							Server.alivePlayersMap.remove(player1.getPlayerAddress());
							Server.deadPlayersMap.put(player1.getPlayerAddress(), player1);
						}
						System.out.println("Projectile hit " + player1.toString() + " --- " + projectile.toString());
					}
				}
			}


//			for(Player player2 : Server.alivePlayersMap.values()) {
//				if(player1 != player2) {
//					if(player1.collision(player2)) {
//						System.out.println("Collision!!!");
//					}
//				}
//			}
		}
	}

	public void run() {
		long lastRenderTime = System.nanoTime();
		double delay = 1e9 / Server.FPS_RENDER;

		while(true) {
			long deltaTime = System.nanoTime() - lastRenderTime;

			if(deltaTime > delay) {
				movePlayers();
				moveProjectiles();
				checkCollisions();
				lastRenderTime = System.nanoTime();
			}

			else {
				try {
					Thread.sleep((long) ((delay - deltaTime) / 1e6));
				} catch (InterruptedException e) {}
			}
		}
	}
}

