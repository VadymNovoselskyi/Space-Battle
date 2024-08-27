package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;

import javax.swing.ImageIcon;

import gameServer.Command;

public class GameController extends Thread{
	private GameFrame gameFrame;
	private Communicator communicator;
	private ClientPlayer me;

	private HashMap<Integer,Player> playerMap = new HashMap<>();
	private int gameWidth, gameHeight;


	private int fpsPlayer = 30;
	private int fpsServer = 12;
	private boolean gameRunning = true, dead = false;

	private Font gameNameFont, gameOverFont;

	public GameController(String host, int port) throws IOException {
		this(host, port, 800, 600);
	}
	public GameController(String host, int port, int gameWidth, int gameHeight) throws IOException {
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
		gameFrame = new GameFrame(gameWidth, gameHeight);
		communicator = new Communicator(this, host, port);
		loadFonts();
		loadScene();
	}

	public void closeConnection(){
		communicator.closeConnection();
	}

	public void update(long deltaTime) {
		int dx = me.getDirectionX(), dy = me.getDirectionY();
		
		if(gameFrame.keyDown.get("right")) me.setDirectionX(1);
		else if(gameFrame.keyDown.get("left")) me.setDirectionX(-1);
		else me.setDirectionX(0);
		
		if(gameFrame.keyDown.get("down")) me.setDirectionY(1);
		else if(gameFrame.keyDown.get("up")) me.setDirectionY(-1);
		else me.setDirectionY(0);
		
		
		if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
			communicator.notifyServer(Command.DISCONNECT, System.nanoTime());
		}

		if(me.getDirectionX() != dx || me.getDirectionY() != dy) {
			me.move(deltaTime);
			communicator.notifyServer(Command.MOVE, me.toString(), System.nanoTime());
		}
		
		if(me.xPos <= 0 || me.xPos >= gameWidth || me.yPos <= 0 || me.yPos >= gameHeight) {
			communicator.notifyServer(Command.DEAD, System.nanoTime());
			dead = true;
		}
	}

	public void updateAll(String[] dataList) {
		int playerID, dx, dy, xPos, yPos, health; 
		for(int i = 1; i < dataList.length-1; i += 6 ) {
			playerID = Integer.valueOf(dataList[i]);
			dx = Integer.valueOf(dataList[i+1]);
			dy = Integer.valueOf(dataList[i+2]);
			xPos = Integer.valueOf(dataList[i+3]);
			yPos = Integer.valueOf(dataList[i+4]);
			health = Integer.valueOf(dataList[i+5]);
			
			playerMap.get(playerID).update(dx, dy, xPos, yPos);
		}

	}
	public void updatePlayerMap(String data) {
		String[] dataList = data.split(",");

		Command cmd = Command.valueOf(dataList[0]);
		int playerID = Integer.valueOf(dataList[1]);
		int xPos = Integer.valueOf(dataList[4]);
		int yPos = Integer.valueOf(dataList[5]);
		int health = Integer.valueOf(dataList[6]);

		switch(cmd) {
		case CONNECTED:
			me = new ClientPlayer(playerID, xPos, yPos, health);
			me.setColor(Color.GREEN);
			playerMap.put(playerID, me);
			this.start();
			break;
			
		case RECEIVE_ALL:
			for(int i = 1; i < dataList.length-1; i += 6 ) {
				playerID = Integer.valueOf(dataList[i]);
				xPos = Integer.valueOf(dataList[i+3]);
				yPos = Integer.valueOf(dataList[i+4]);
				health = Integer.valueOf(dataList[i+5]);

				playerMap.put(playerID, new Player(playerID,xPos, yPos,health));
			}
			break;

		case NEW_PLAYER:
			playerMap.put(playerID, new Player(playerID,xPos, yPos,health));
			break;

		case REMOVE:
			playerMap.remove(playerID);
			break;

		case DISCONNECT:
			communicator.closeConnection();
			System.exit(0);
		default:
		}
	}

	public void loadScene() {
		Image background = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
		gameFrame.setBackground(background);
	}

	public void loadFonts() {
		try {
			String path = getClass().getResource("/droidlover.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(30f);
			gameOverFont = baseFont.deriveFont(100f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 3);
			gameOverFont = new Font("Serif", Font.PLAIN, 100);
			e.printStackTrace();
		}
	}
	
	public void movePlayers(long deltaTime) {
		for(Player player : playerMap.values()) {
			player.move(deltaTime);
		}
	}

	@Override
	public void run() {
		long lastPlayerUpdateTime = System.nanoTime();
		double delayClient = 1e9 / fpsPlayer;
		long lastServerUpdateTime = System.nanoTime();
		double delayServer = 1e9 / fpsServer;

		while(gameRunning) {
			long deltaTimeClient = System.nanoTime() - lastPlayerUpdateTime;
			long deltaTimeServer = System.nanoTime() - lastServerUpdateTime;

			if(deltaTimeServer > delayServer) {
				if(!dead) {
					update(deltaTimeServer);
				} else if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
					communicator.notifyServer(Command.DISCONNECT, System.nanoTime());
				}
				lastServerUpdateTime = System.nanoTime();
			}
			else if(deltaTimeClient > delayClient) {
				gameFrame.write("The best game ever", 10, 40, Color.YELLOW, gameNameFont);
				if(dead) {
					gameFrame.write("You dead lol", Color.RED, gameOverFont);
				}
				movePlayers(deltaTimeClient);
				gameFrame.render(playerMap);
				lastPlayerUpdateTime = System.nanoTime();
			}

			else {
				try {
					Thread.sleep((long) ((delayClient - deltaTimeClient) / 1e6));
				} catch (InterruptedException e) {}
			}
		}
	}
}

