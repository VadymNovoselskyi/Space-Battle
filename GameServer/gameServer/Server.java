package gameServer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	private static ServerReceiver receiver;
	private static ServerSender sender;
	protected static ConcurrentHashMap<String, Integer> playerAddresses = new ConcurrentHashMap<>();
	
	protected static List<Projectile> projectilesList = Collections.synchronizedList(new ArrayList<>());

	protected static ConcurrentHashMap<String, Player> deadPlayersMap = new ConcurrentHashMap<>();
	protected static ConcurrentHashMap<String, Player> alivePlayersMap = new ConcurrentHashMap<>();
	protected static ConcurrentHashMap<String, Player> updatedPlayers = new ConcurrentHashMap<>(); 
	
	protected static int playerID = 1, projectileID = 1;
	protected static final int FPS_SERVER = 12, FPS_RENDER = 24, GAME_WIDTH = 800, GAME_HEIGHT = 600;

	public static void main(String[] args) {
		System.out.println("Server is up and running");
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(9864);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Create and start the Receiver thread
		receiver  = new ServerReceiver(socket);
		Thread receiverThread = new Thread(receiver);
		receiverThread.start();

		// Create and start the Sender thread
		sender = new ServerSender(socket);
		
		ServerRender render = new ServerRender();
		render.start();


		long lastUpdateTime = System.nanoTime();
		double delay = 1e9 / FPS_SERVER;
		while(true) {
			long deltaTime = System.nanoTime() - lastUpdateTime;
			
			if(deltaTime > delay) {
				if(updatedPlayers.size() != 0) {
					for(String address : playerAddresses.keySet()) sender.updatePlayers(updatedPlayers, address);
					updatedPlayers.clear();
				}
				lastUpdateTime = System.nanoTime();
			}
			
			else {
				try {
					Thread.sleep((long) ((delay - deltaTime) / 1e6));
				} catch (InterruptedException e) {}
			}
		}
	}
	
	protected static void notifyClient(Command cmd, String data, String playerAddress) {
		sender.notifyClient(cmd, data, playerAddress);
	}
	
	protected static void notifyAllClients(Command cmd, String data) {
		for(String playerAddress : playerAddresses.keySet()) {
			sender.notifyClient(cmd, data, playerAddress);
		}
	}
	
	protected static void notifyAllButThis(Command cmd, String data, String ignoreAddres) {
		for(String playerAddress : playerAddresses.keySet()) {
			if(!playerAddress.equals(ignoreAddres)) {
				sender.notifyClient(cmd, data, playerAddress);
			}
		}
	}

}

