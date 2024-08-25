package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;

import gameServer.Command;

public class GameController extends Thread{
	private GameFrame gameFrame;
	private Communicator communicator;
	private ClientPlayer me;

	private HashMap<Integer,Player> playerMap = new HashMap<>();
	private int gameWidth, gameHeight;


	private int fps = 30;
	private boolean gameRunning = true, dead = false;

	private Font gameNameFont, gameOverFont;

	public GameController(String host, int port) throws IOException {
		this(host, port, 800, 600);
	}
	public GameController(String host, int port, int gameWidth, int gameHeight) throws IOException {
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
		loadFonts();
		gameFrame = new GameFrame(gameWidth, gameHeight);
		communicator = new Communicator(this, host, port);
	}

	public void closeConnection(){
		communicator.closeConnection();
	}

	public void update(long deltaTime){
		if(gameFrame.keyDown.get("right")) {
			me.setDirectionX(1);
		}
		if(gameFrame.keyDown.get("left")) {
			me.setDirectionX(-1);
		}
		if(gameFrame.keyDown.get("down")) {
			me.setDirectionY(1);
		}
		if(gameFrame.keyDown.get("up")) {
			me.setDirectionY(-1);
		}
		if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
			communicator.notifyServer(Command.DISCONNECT);
		}

		if(me.update(deltaTime)) {
			communicator.notifyServer(Command.MOVE, me.toString());
		}
		if(me.xPos <= 0 || me.xPos >= gameWidth || me.yPos <= 0 || me.yPos >= gameHeight) {
			communicator.notifyServer(Command.DEAD);
			dead = true;
		}

		me.setDirectionX(0);
		me.setDirectionY(0);
	}


	public synchronized void updatePlayerMap(String data){
		String[] dataList = data.split(",");

		Command cmd = Command.valueOf(dataList[0]);

		int playerID = Integer.valueOf(dataList[1]);
		int xPos = Integer.valueOf(dataList[2]);
		int yPos = Integer.valueOf(dataList[3]);
		int health = Integer.valueOf(dataList[4]);

		switch(cmd) {
		case CONNECTED:
			me = new ClientPlayer(playerID,xPos, yPos, health);
			me.setColor(Color.BLUE);
			playerMap.put(playerID, me);
			this.start();

			communicator.notifyServer(Command.GET_ALL);
			break;

		case UPDATE_ALL:
			for(int i = 1; i < dataList.length-1; i += 4 ) {
				playerID = Integer.valueOf(dataList[i]);
				xPos = Integer.valueOf(dataList[i+1]);
				yPos = Integer.valueOf(dataList[i+2]);
				health = Integer.valueOf(dataList[i+3]);

				playerMap.put(playerID, new Player(playerID,xPos, yPos,health));
			}
			break;

		case MOVE:
			playerMap.get(playerID).update(xPos, yPos);
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

	public void loadFonts() {
		try {
			String path = getClass().getResource("/droidlover.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(30f);
			gameOverFont = baseFont.deriveFont(100f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 32);
			gameOverFont = new Font("Serif", Font.PLAIN, 150);
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long lastUpdateTime = System.nanoTime();
		double delay = 1e9 / fps;

		while(gameRunning) {
			long deltaTime = System.nanoTime() - lastUpdateTime;

			if(deltaTime > delay) {
				if(!dead) {
					update(deltaTime);
				} else if(gameFrame.keyDown.get("esc") || gameFrame.keyDown.get("q")) {
					communicator.notifyServer(Command.DISCONNECT);
				}
				gameFrame.write("The best game ever", 10, 40, Color.BLUE, gameNameFont);
				if(dead) {
					gameFrame.write("You dead lol", Color.RED, gameOverFont);
				}
				gameFrame.render(playerMap);
				lastUpdateTime = System.nanoTime();
			} else {
				try {
					Thread.sleep((long) ((delay - deltaTime) / 1e6));
				} catch (InterruptedException e) {}
			}
		}
	}
}

