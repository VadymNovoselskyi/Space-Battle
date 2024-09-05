package gameServer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
	protected static final int FPS_SERVER = 12, FPS_RENDER = 60, GAME_WIDTH = 800, GAME_HEIGHT = 600;
    private static final int THREAD_POOL_SIZE = 5, NUM_RECEIVERS = 3;
	protected static int playerID = 1, projectileID = 1;

	private static DatagramSocket socket;
	private static ScheduledExecutorService executor;
	private static ServerReceiver receiver;
	private static ServerSender sender;
	private static ServerRender render;

	protected volatile static ConcurrentHashMap<Player, String> playerAddresses = new ConcurrentHashMap<>();	
	protected volatile static ConcurrentHashMap<String, Player> deadPlayersMap = new ConcurrentHashMap<>();
	protected volatile static ConcurrentHashMap<String, Player> alivePlayersMap = new ConcurrentHashMap<>();
	protected static List<Projectile> projectilesList = Collections.synchronizedList(new ArrayList<>());

	protected volatile static ConcurrentHashMap<String, Player> updatedPlayers = new ConcurrentHashMap<>(); 


	public static void main(String[] args) throws SocketException {
		executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

		socket = new DatagramSocket(9001);
		receiver = new ServerReceiver(socket);
		sender = new ServerSender(socket);
		render = new ServerRender();
		Runnable updateTask = () -> updatePlayers();
		System.out.println("Server is up and running");

		executor.scheduleAtFixedRate(updateTask, 0, 1000 / FPS_SERVER, TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(render, 0, 1000 / FPS_RENDER, TimeUnit.MILLISECONDS);
		
		for(int i = 0; i < NUM_RECEIVERS; i++) {			
			executor.scheduleWithFixedDelay(receiver, 0, 1, TimeUnit.MILLISECONDS);
		}

		//shutdown gracefuly
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				System.out.println("Shutting down server...");
				socket.close();
				executor.shutdown();
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}));

	}

	public static void updatePlayers() {
		if(updatedPlayers.size() != 0) {
			for(String address : playerAddresses.values()) sender.updatePlayers(updatedPlayers, address);
			updatedPlayers.clear();
		}
	}

	protected static void notifyClient(Command cmd, String data, String playerAddress) {
		sender.notifyClient(cmd, data, playerAddress);
	}

	protected static void notifyAllClients(Command cmd, String data) {
		for(String playerAddress : playerAddresses.values()) {
			sender.notifyClient(cmd, data, playerAddress);
		}
	}

	protected static void notifyAllButThis(Command cmd, String data, String ignoreAddres) {
		for(String playerAddress : playerAddresses.values()) {
			if(!playerAddress.equals(ignoreAddres)) {
				sender.notifyClient(cmd, data, playerAddress);
			}
		}
	}

}

