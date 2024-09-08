package duoGame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;


public class GameController {
	public static final int GAME_WIDTH = 1080, GAME_HEIGHT = 720;
	private static final int SPAWN_CALCULATION_ATTEMPTS = 250, SPAWN_OFFSET = 100, SPAWN_X_r = GAME_HEIGHT / 20, SPAWN_Y_r = GAME_WIDTH / 20, SPAWN_X_R = GAME_HEIGHT / 2 - SPAWN_OFFSET - SPAWN_X_r, SPAWN_Y_R = GAME_WIDTH / 2 - SPAWN_OFFSET - SPAWN_Y_r;
	public static final int FPS = 60;

	ScheduledExecutorService executor;
	private GameFrame gameFrame;
	private Player playerArrows;
	private Player playerKeys;
	private static Random random = new Random();

	protected static HashMap<Projectile, Player> projectileMap = new HashMap<>();
	private static ArrayList<Explosion> explosionList = new ArrayList<>();

	private boolean gameActive;
	private int playerArrowsWinCounter = 0, playerKeysWinCounter = 0;

	protected static Image playerKeysImage, playerArrowsImage, laserImage, missileImage, explosionImage;
	private Font gameNameFont, gameOverFont, playerScoreFont, playAgainFont;

	public GameController() throws IOException {
		executor = Executors.newScheduledThreadPool(1);
		gameFrame = new GameFrame(GAME_WIDTH, GAME_HEIGHT);
		loadImages();
		loadFonts();
		newGame();

		Runnable clientTask = () -> updateClient();
		executor.scheduleAtFixedRate(clientTask, 0, 1000 / FPS, TimeUnit.MILLISECONDS);

		gameFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				executor.shutdown();
				gameFrame.dispose();
				System.exit(0);
			}
		});
	}

	public void updateClient() {
		if(gameActive) {
			try {			
				checkMovement((long)(1e9 / FPS));
				updatePlayerState((long)(1e9 / FPS));
				movePlayers();
				moveProjectiles();
				checkCollisions();
				checkExplosions();
				checkWin();

				gameFrame.renderBG();
				writeText();
				render();

			} catch (Exception e) {e.printStackTrace();}
		}
		else {
			gameFrame.renderBG();

			writeText();
			gameFrame.write("Game Over", 0.5, 0.4, Color.RED, gameOverFont);
			gameFrame.write("To play again press 'Enter'", 0.5, 0.52, Color.RED, playAgainFont);

			render();

			if(gameFrame.keyDown.get(GameFrame.Key.ESC)) {
				executor.shutdown();
				gameFrame.dispose();
				System.exit(0);
			}
			if(gameFrame.keyDown.get(GameFrame.Key.ENTER)) {
				newGame();
			}
		}
	}

	public void writeText() {
		gameFrame.write("Player with keys: " +playerKeysWinCounter, 0.96, 0.02, Color.GREEN, playerScoreFont);
		gameFrame.write("Player on arrows: " +playerArrowsWinCounter, 0.96, 0.08, Color.GREEN, playerScoreFont);
		gameFrame.write("THE BEST GAME EVER", 0.02, 0.02, Color.YELLOW, gameNameFont);
	}

	public void render() {
		gameFrame.render(playerKeys);
		gameFrame.render(playerArrows);
		gameFrame.renderProjectiles(projectileMap);
		gameFrame.renderExplosions(explosionList);
	}


	public void checkMovement(long deltaTime) {
		//Player on awsd
		int dxBefore = playerKeys.getDirectionX();
		int dyBefore = playerKeys.getDirectionY();

		if(gameFrame.keyDown.get(GameFrame.Key.D)) playerKeys.setDirectionX(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.A)) playerKeys.setDirectionX(-1);
		else playerKeys.setDirectionX(0);

		if(gameFrame.keyDown.get(GameFrame.Key.S)) playerKeys.setDirectionY(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.W)) playerKeys.setDirectionY(-1);
		else playerKeys.setDirectionY(0);

		if(playerKeys.getDirectionX() != dxBefore || playerKeys.getDirectionY() != dyBefore) {
			playerKeys.move(deltaTime);
		}	

		//player on <- ^ -> 
		dxBefore = playerArrows.getDirectionX();
		dyBefore = playerArrows.getDirectionY();

		if(gameFrame.keyDown.get(GameFrame.Key.RIGHT)) playerArrows.setDirectionX(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.LEFT)) playerArrows.setDirectionX(-1);
		else playerArrows.setDirectionX(0);

		if(gameFrame.keyDown.get(GameFrame.Key.DOWN)) playerArrows.setDirectionY(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.UP)) playerArrows.setDirectionY(-1);
		else playerArrows.setDirectionY(0);

		if(playerArrows.getDirectionX() != dxBefore || playerArrows.getDirectionY() != dyBefore) {
			playerArrows.move(deltaTime);
		}		
	}

	public void updatePlayerState(long deltaTime) {
		if(gameFrame.keyDown.get(GameFrame.Key.SPACE)) playerKeys.tryToFire();
		if(gameFrame.keyDown.get(GameFrame.Key.SHIFT)) playerArrows.tryToFire();


		if(playerKeys.borderCollision()) playerKeys.setDead(true);
		if(playerArrows.borderCollision()) playerArrows.setDead(true);

		if(gameFrame.keyDown.get(GameFrame.Key.ESC)) {
			executor.shutdown();
			gameFrame.dispose();
			System.exit(0);
		}
	}

	public void checkCollisions() {
		Iterator<Projectile> iterator = projectileMap.keySet().iterator();
		while (iterator.hasNext()) {
			Projectile projectile = iterator.next();
			if(projectileMap.get(projectile) == playerArrows && playerKeys.collision(projectile)) {				
				iterator.remove();
				projectile.hit(playerKeys);
				double x = projectile.getxPos() + projectile.getWidth() / 2 - Math.sin(projectile.getAngle()) * (-projectile.getHeight() / 2) - Explosion.WIDTH / 2;
				double y = projectile.getyPos() + projectile.getHeight() / 2 + Math.cos(projectile.getAngle()) * (-projectile.getHeight() / 2) - Explosion.HEIGHT / 2;
				Explosion explosion = new Explosion((int)x, (int)y, 1, explosionImage);
				explosion.setStartTime(System.nanoTime());
				explosionList.add(explosion);
			}		
			else if(projectileMap.get(projectile) == playerKeys && playerArrows.collision(projectile)) {				
				iterator.remove();
				projectile.hit(playerArrows);
				double x = projectile.getxPos() + projectile.getWidth() / 2 - Math.sin(projectile.getAngle()) * (-projectile.getHeight() / 2) - Explosion.WIDTH / 2;
				double y = projectile.getyPos() + projectile.getHeight() / 2 + Math.cos(projectile.getAngle()) * (-projectile.getHeight() / 2) - Explosion.HEIGHT / 2;
				Explosion explosion = new Explosion((int)x, (int)y, 1, explosionImage);
				explosion.setStartTime(System.nanoTime());
				explosionList.add(explosion);
			}
		}
	}


	public void movePlayers() {
		playerArrows.move((System.nanoTime() - playerArrows.lastUpdateTime));
		playerArrows.lastUpdateTime = System.nanoTime();

		playerKeys.move((System.nanoTime() - playerKeys.lastUpdateTime));
		playerKeys.lastUpdateTime = System.nanoTime();

	}
	public void moveProjectiles() {
		Iterator<Projectile> iterator = projectileMap.keySet().iterator();
		while (iterator.hasNext()) {
			Projectile projectile = iterator.next();
			projectile.move(System.nanoTime() - projectile.lastUpdateTime);
			projectile.lastUpdateTime = System.nanoTime();
			if(projectile.borderCollision())  iterator.remove();
		}
	}

	public void checkExplosions() {
		long currentTime = System.nanoTime();
		Iterator<Explosion> iterator = explosionList.iterator();
		while (iterator.hasNext()) {
			Explosion explosion = iterator.next();
			if (explosion.getDuration() * 1e9 < currentTime - explosion.getStartTime()) iterator.remove();
		}
	}

	public void checkWin() {
		if(playerKeys.isDead()) {
			playerArrowsWinCounter++;
			gameActive = false;
		}
		if(playerArrows.isDead()) {
			playerKeysWinCounter++;
			gameActive = false;
		}
	}



	public void newGame() {
		int[] coords = getSpawnCoords();
		double angle = Math.atan2(GAME_HEIGHT / 2 - coords[1], GAME_WIDTH / 2 - coords[0]) + Math.PI / 2;

		playerArrows = new Player(coords[0], coords[1], angle, playerKeysImage);
		playerKeys = new Player(GAME_WIDTH - coords[0], GAME_HEIGHT - coords[1], angle + Math.PI, playerArrowsImage);
		gameActive = true;
	}

	public int[] getSpawnCoords() {
		int[] coords = new int[2];

		for (int i = 0; i < SPAWN_CALCULATION_ATTEMPTS; i++) {
			double theta = random.nextDouble() * 2 * Math.PI;
			double phi = random.nextDouble() * 2 * Math.PI;

			coords[0] = (int) ((SPAWN_Y_R + SPAWN_Y_r * Math.cos(theta)) * Math.sin(phi)) + GAME_WIDTH / 2 - Player.HITBOX_WIDTH / 2;
			coords[1] = (int) ((SPAWN_X_R + SPAWN_X_r * Math.cos(theta)) * Math.cos(phi)) + GAME_HEIGHT / 2 - Player.HITBOX_HEIGHT / 2;

	        if (isValidSpawn(coords)) {
	            break;
	        }
	    }		
		return coords;

	}
	
	private boolean isValidSpawn(int[] coords) {
	    return coords[0] > GAME_WIDTH * 3 / 4 && coords[0] < GAME_WIDTH && coords[1] > 0 && coords[1] < GAME_HEIGHT;
	}

	public void loadImages() {
		playerKeysImage = new ImageIcon(getClass().getResource("/ship_yellow.png")).getImage();
		playerArrowsImage = new ImageIcon(getClass().getResource("/ship_green.png")).getImage();
		laserImage = new ImageIcon(getClass().getResource("/laser.png")).getImage();
		missileImage = new ImageIcon(getClass().getResource("/missile.png")).getImage();
		explosionImage = new ImageIcon(getClass().getResource("/explosion.png")).getImage();

		Image background = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
		gameFrame.setBackground(background);
	}

	public void loadFonts() {
		try {
			String path = getClass().getResource("/arcade_game.ttf").getFile();
			path = URLDecoder.decode(path, "utf-8");
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(path));

			gameNameFont = baseFont.deriveFont(20f);
			gameOverFont = baseFont.deriveFont(66f);
			playerScoreFont = baseFont.deriveFont(14f);
			playAgainFont = baseFont.deriveFont(22f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 24);
			gameOverFont = new Font("Serif", Font.PLAIN, 60);
			e.printStackTrace();
		}
	}
}

