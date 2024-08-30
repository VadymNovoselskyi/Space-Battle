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
						//						System.out.println("Projectile hit " + player1.toString() + " --- " + projectile.toString());
					}
					else if(projectile.borderCollision()) iterator.remove();
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

	public void checkTimeout() {
		long time = System.nanoTime();
		Iterator<Player> iterator = Server.playerAddresses.keySet().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if(time - player.lastPingTime > Player.TIMEOUT * 1e9 / 2) {
				Server.notifyClient(Command.PING, "", Server.playerAddresses.get(player));
				if(time - player.lastPingTime > Player.TIMEOUT * 1e9) {
					PacketProcessor.disconnect(Server.playerAddresses.get(player));
					iterator.remove();
				}
			}
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
				checkTimeout();
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

