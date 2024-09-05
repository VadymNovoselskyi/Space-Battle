package duoGame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;

public class GameController extends Thread{
	public static final int GAME_WIDTH = 800, GAME_HEIGHT = 600;
	public static final int PLAYER_HITBOX_WIDTH = 42, PLAYER_HITBOX_HEIGHT = 84;
	public static final int FPS = 60;

	ScheduledExecutorService executor;
	private GameFrame gameFrame;
	private Player playerArrows;
	private Player playerKeys;

	private List<Explosion> explosionList = new ArrayList<>();
	private ConcurrentHashMap<Integer,Player> playerMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Projectile> projectileList = new ConcurrentHashMap<>();

	private Image playerKeysImage, playerArrowsImage, laserImage, missileImage, explosionImage;
	private Font gameNameFont, gameOverFont;
	private boolean dead = false;

	public GameController() throws IOException {
		executor = Executors.newScheduledThreadPool(1);
		gameFrame = new GameFrame(GAME_WIDTH, GAME_HEIGHT);
		loadImages();
		loadFonts();

		gameFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				executor.shutdown();
				gameFrame.dispose();
				System.exit(0);
			}
		});
	}


	@Override
	public void run() {
		Runnable clientTask = () -> updateClient();
		executor.scheduleAtFixedRate(clientTask, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
	}


	public void updateClient() {
		try {			
			checkMovement((long)(1e9 / FPS));
			checkMovement((long)(1e9 / FPS));
			updatePlayerState((long)(1e9 / FPS));
			movePlayers();
			moveProjectiles();
			checkExplosions();
			gameFrame.write("THE BEST GAME EVER", 10, 40, Color.YELLOW, gameNameFont);
			gameFrame.renderProjectiles(projectileList);
			gameFrame.renderExplosions(explosionList);
			gameFrame.render(playerMap);
		} catch (Exception e) {e.printStackTrace();}
	}


	public void checkMovement(long deltaTime) {
		//Player on awsd
		int dxBefore = playerKeys.getDirectionX();
		int dyBefore = playerKeys.getDirectionY();

		if(gameFrame.keyDown.get(GameFrame.Key.D)) playerKeys.setDirectionX(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.A)) playerKeys.setDirectionX(-1);
		else playerKeys.setDirectionX(0);

		if(gameFrame.keyDown.get(GameFrame.Key.S)) playerKeys.setDirectionY(1);
		else if(gameFrame.keyDown.get(GameFrame.Key.S)) playerKeys.setDirectionY(-1);
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
	
	public void fireLaser() {
//		Laser newLaser = new Laser(projectileID, xPos, yPos, angle, laserImage);
//		projectileList.put(projectileID, newLaser);
	}
	public void fireMissile() {
//		Missile newMissile = new Missile(projectileID, xPos, yPos, angle, missileImage);
//		projectileList.put(projectileID, newMissile);
	}
	public void missileCollision() {
//		double x = xPos+ projectile.getWidth() / 2 - Math.sin(angle) * (-projectile.getHeight() / 2) - Explosion.WIDTH / 2;
//		double y = yPos + projectile.getHeight() / 2 + Math.cos(angle) * (-projectile.getHeight() / 2) - Explosion.HEIGHT / 2;
//		Explosion explosion = new Explosion((int)x, (int)y, 1, explosionImage);
//		explosion.setStartTime(System.nanoTime());
//		explosionList.add(explosion);
	}

	public void movePlayers() {
		for(Player player : playerMap.values()) {
			player.move((System.nanoTime() - player.lastUpdateTime));
			player.lastUpdateTime = System.nanoTime();
		}
	}
	public void moveProjectiles() {
		for(Projectile projectile : projectileList.values()) {
			projectile.move(System.nanoTime() - projectile.lastUpdateTime);
			projectile.lastUpdateTime = System.nanoTime();
			if(projectile.borderCollision())  projectileList.remove(projectile.getProjectileID());
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

			gameNameFont = baseFont.deriveFont(24f);
			gameOverFont = baseFont.deriveFont(60f);

		} catch (Exception e) {
			gameNameFont = new Font("Serif", Font.PLAIN, 24);
			gameOverFont = new Font("Serif", Font.PLAIN, 60);
			e.printStackTrace();
		}
	}
}

