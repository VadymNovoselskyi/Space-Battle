package gameClient;


import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class GameFrame extends JFrame implements KeyListener{
	public enum Key {
		LEFT, RIGHT, UP, DOWN, SPACE, ESC, Q, ENTER 
	}
	public EnumMap<Key, Boolean> keyDown = new EnumMap<>(Key.class);

	private Canvas gameCanvas;
	private BufferStrategy backBuffer;
	private Image background = null;

	private Dimension canvasDimension;
	private int width, height, xTranslate, yTranslate;

	public GameFrame(int width, int height) {;
	this.width = width;
	this.height = height;
	canvasDimension= new Dimension(width, height);
	createWindow();

	addKeyListener(this);
	for (Key key : Key.values()) {
		keyDown.put(key, false);
	}
	}

	public void createWindow() {
		gameCanvas = new Canvas();
		gameCanvas.setSize(canvasDimension);
		gameCanvas.setFocusable(false);

		this.setTitle("Space battle");
		this.add(gameCanvas);
		this.pack();
		this.setMinimumSize(canvasDimension);
//		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Important to control the closing behavior

		gameCanvas.createBufferStrategy(2);
		backBuffer = gameCanvas.getBufferStrategy();
	}
	
	public void renderBG(int xPos, int yPos) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		int startTileX = xPos / 250;
        int startTileY = yPos / 250;
        int endTileX = (xPos + width) / 250 + 1;
        int endTileY = (yPos + height) / 250 + 1;

        // Loop through and draw each tile
        for (int i = startTileX; i <= endTileX; i++) {
            for (int j = startTileY; j <= endTileY; j++) {
                int tileX = i * 250 - xPos;
                int tileY = j * 250 - yPos;
                g.drawImage(background, tileX, tileY, null);
            }
        }
        this.xTranslate = width / 2 - xPos;
        this.yTranslate = height / 2 - yPos;
		g.translate(xTranslate, yTranslate);
		g.setColor(Color.RED);
		g.drawRect(0, 0, GameController.GAME_WIDTH, GameController.GAME_HEIGHT);
        g.translate(0, 0);
		g.dispose();
	}

	public void render(ConcurrentHashMap<Integer, Player> playerMap, Player me) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		g.translate(width / 2 - me.getXPos(), height / 2 - me.getYPos());
		for(Player player : playerMap.values()) {
			player.draw(g);
		}
		g.translate(0, 0);
		backBuffer.show();
		g.dispose();
	}
	public void renderProjectiles(ConcurrentHashMap<Integer, Projectile> projectileMap) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		g.translate(xTranslate, yTranslate);
		projectileMap.values().forEach(projectile -> projectile.draw(g));
		g.translate(0, 0);
		g.dispose();
	}
	public void renderExplosions(List<Explosion> explosionList) {
		Graphics2D g = (Graphics2D)backBuffer.getDrawGraphics();
		g.translate(xTranslate, yTranslate);
		explosionList.forEach(explosion -> explosion.draw(g));
		g.translate(0, 0);
		g.dispose();
	}

	public void write(String text, double xOffset, double yOffset, Color color, Font font) { //draw with specified x-y offset (0-1)
		Graphics2D g = (Graphics2D) backBuffer.getDrawGraphics();
		FontMetrics fontMetrics = g.getFontMetrics(font);
		int x = (int) ((width - fontMetrics.stringWidth(text)) * xOffset);
		int y = (int) ((height - fontMetrics.getHeight()) * yOffset) + fontMetrics.getAscent();
		drawText(g, text, x, y, color, font);
		g.dispose();
	}

	private void drawText(Graphics2D g, String text, int x, int y, Color color, Font font) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setKeyState(e, true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setKeyState(e, false);
	}

	private void setKeyState(KeyEvent e, boolean state) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT -> keyDown.put(Key.LEFT, state);
		case KeyEvent.VK_RIGHT -> keyDown.put(Key.RIGHT, state);
		case KeyEvent.VK_UP -> keyDown.put(Key.UP, state);
		case KeyEvent.VK_DOWN -> keyDown.put(Key.DOWN, state);
		case KeyEvent.VK_SPACE -> keyDown.put(Key.SPACE, state);
		
		case KeyEvent.VK_Q -> keyDown.put(Key.Q, state);
		case KeyEvent.VK_ESCAPE -> keyDown.put(Key.ESC, state);
		case KeyEvent.VK_ENTER -> keyDown.put(Key.ENTER, state);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	public void setBackground(Image background) {
		this.background = background;
	}
}
