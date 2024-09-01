package gameServer;

import java.util.Iterator;

public class ServerRender implements Runnable {

	public ServerRender() {}


	public void run() {
		try {
			movePlayers();
			moveProjectiles();
			checkCollisions();
			checkTimeout();
		} catch (Exception e) {e.printStackTrace();}
	}


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
					}
					else if(projectile.borderCollision()) iterator.remove();
				}
			}
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
					System.out.println("No responce on pings");
					ServerReceiver.disconnectPlayer(Server.playerAddresses.get(player));
					iterator.remove();
				}
			}
		}

	}
}

