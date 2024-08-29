package gameServer;

import java.net.DatagramPacket;
import java.net.InetAddress;


public class PacketProcessor implements Runnable {
	private DatagramPacket packet;

	public PacketProcessor(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void run() {
		int length = packet.getLength();
		String message = new String(packet.getData(), 0, length);

		InetAddress playerIP = packet.getAddress();
		int playerPort = packet.getPort();
		String playerAddress = playerIP.getHostAddress() + ":" + playerPort;

//		System.out.println("Received from player: " + message +" Address: " +playerAddress);
		
		if(!Server.deadPlayersMap.containsKey(playerAddress)) {
			exeCommand(message, playerAddress);
		} else {
			String[] dataList = message.split(",");
			Command cmd = Command.valueOf(dataList[0]);
			if(cmd.equals(Command.DISCONNECT)) {
				disconnect(playerAddress);
			}
		}
	}

	public void exeCommand(String data, String playerAddress) {
		String[] dataList = data.split(",");
		Command cmd = Command.valueOf(dataList[0]);

		switch(cmd) {
		case NEW_PLAYER:
			Player newPlayer = new Player(Server.playerID, (int)(Math.random()*500 + 150), (int)(Math.random()*400 + 100));
			System.out.println("PlayerID: " + Server.playerID + " Connected to server");
			Server.playerID++;
			newPlayer.lastUpdateTime = Long.valueOf(dataList[1]);
			newPlayer.setPlayerAddress(playerAddress);

			Server.notifyClient(Command.CONNECTED, newPlayer.toString(), playerAddress);
			if(Server.alivePlayersMap.size() > 0) {
				String playerList = "";
				for (Player player : Server.alivePlayersMap.values()) playerList += "," +player;
				Server.notifyClient(Command.RECEIVE_ALL, playerList.substring(1), playerAddress);
			}
			
			Server.alivePlayersMap.put(playerAddress, newPlayer);
			Server.playerAddresses.put(playerAddress, Server.playerID);
			Server.notifyAllButThis(cmd, newPlayer.toString(), playerAddress);
			break;

		case MOVE: {			
			int dx = Integer.valueOf(dataList[2]);
			int dy = Integer.valueOf(dataList[3]);
			long updateTime = Long.valueOf(dataList[6]);
			
			Player movedPlayer = Server.alivePlayersMap.get(playerAddress);
			movedPlayer.move(updateTime - movedPlayer.lastUpdateTime);
			movedPlayer.setDx(dx);
			movedPlayer.setDy(dy);
			movedPlayer.move(System.nanoTime() - updateTime);
			movedPlayer.lastUpdateTime = System.nanoTime();
			Server.updatedPlayers.put(playerAddress, movedPlayer);
			break;
		}
			
		case FIRE_LASER: {
			int playerID = Integer.valueOf(dataList[1]);
			int dx = Integer.valueOf(dataList[2]);
			int dy = Integer.valueOf(dataList[3]);
			long updateTime = Long.valueOf(dataList[6]);
			
			Player firedPlayer = Server.alivePlayersMap.get(playerAddress);;
			firedPlayer.move(updateTime - firedPlayer.lastUpdateTime);
			firedPlayer.setDx(dx);
			firedPlayer.setDy(dy);
			firedPlayer.move(System.nanoTime() - updateTime);
			firedPlayer.lastUpdateTime = System.nanoTime();
			
			double angle; 
			if (dx == 0 && dy == 0) {
				angle = 0;
			} else {
				angle = Math.atan2(dy, dx) + Math.PI / 2;
			}
			
			double x = firedPlayer.getxPos()+ Player.HITBOX_WIDTH / 2 - Math.sin(angle) * (-Player.HITBOX_HEIGHT / 2);
			double y = firedPlayer.getyPos() + Player.HITBOX_HEIGHT / 2 + Math.cos(angle) * (-Player.HITBOX_HEIGHT / 2);
			Laser newLaser = new Laser(x - Laser.WIDTH / 2, y - Laser.HEIGHT / 2, dx, dy, playerID, Server.projectileID++);
			Server.projectilesList.add(newLaser);
			
			Server.notifyAllClients(Command.LASER_FIRED, newLaser.toString());
			break;			
		}
			
		case FIRE_MISSILE: {			
			int playerID = Integer.valueOf(dataList[1]);
			int dx = Integer.valueOf(dataList[2]);
			int dy = Integer.valueOf(dataList[3]);
			long updateTime = Long.valueOf(dataList[6]);
			
			Player firedPlayer = Server.alivePlayersMap.get(playerAddress);;
			firedPlayer.move(updateTime - firedPlayer.lastUpdateTime);
			firedPlayer.setDx(dx);
			firedPlayer.setDy(dy);
			firedPlayer.move(System.nanoTime() - updateTime);
			firedPlayer.lastUpdateTime = System.nanoTime();
			
			double angle; 
			if (dx == 0 && dy == 0) {
				angle = 0;
			} else {
				angle = Math.atan2(dy, dx) + Math.PI / 2;
			}
			
			double x = firedPlayer.getxPos()+ Player.HITBOX_WIDTH / 2 - Math.sin(angle) * (-Player.HITBOX_HEIGHT / 2);
			double y = firedPlayer.getyPos() + Player.HITBOX_HEIGHT / 2 + Math.cos(angle) * (-Player.HITBOX_HEIGHT / 2);
			Missile newMissile = new Missile(x - Laser.WIDTH / 2, y - Laser.HEIGHT / 2, dx, dy, playerID, Server.projectileID++);
			Server.projectilesList.add(newMissile);
			
			Server.notifyAllClients(Command.MISSILE_FIRED, newMissile.toString());
			break;
		}
		
		case GET_SERVER_TIME:
			Server.notifyClient(Command.GET_SERVER_TIME, String.valueOf(System.nanoTime()), playerAddress);
			break;

		case DEAD:
			Player deadPlayer = Server.alivePlayersMap.get(playerAddress);
			Server.alivePlayersMap.remove(playerAddress);
			Server.deadPlayersMap.put(playerAddress, deadPlayer);

			Server.notifyAllClients(Command.REMOVE, deadPlayer.toString());
			System.out.println("PlayerID: " + deadPlayer.getID() + " DIED");
			deadPlayer.setDead(true);
			break;

		case DISCONNECT:
			disconnect(playerAddress);
			break;

		default:
			break;
		}
	}

	public void disconnect(String playerAddress) {
		Player player = Server.alivePlayersMap.get(playerAddress);
		if(player == null) {
			player = Server.deadPlayersMap.get(playerAddress);
		}

		Server.notifyClient(Command.DISCONNECT, player.toString(), playerAddress);
		if(!player.isDead()) {
			Server.alivePlayersMap.remove(playerAddress);
			Server.notifyAllClients(Command.REMOVE, player.toString());
		} else {
			Server.deadPlayersMap.remove(playerAddress);
		}
		Server.playerAddresses.remove(playerAddress);
		System.out.println("Disconnecting " +player.getID());
	}
}
