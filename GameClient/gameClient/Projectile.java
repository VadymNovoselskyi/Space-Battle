package gameClient;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class Projectile {
	private double xPos, yPos, angle;
	private int dx, dy, width, height, speed, projectileID;
	public long lastUpdateTime;
	private Image img;
	
	public Projectile(Image img, int xPos, int yPos, int dx, int dy, int width, int height, int speed, int projectileID) {
		this.img = img;
		this.xPos = xPos;
		this.yPos = yPos;
		
		dy = (dx == 0 && dy == 0) ? -1 : dy;
		this.dx = dx;
		this.dy = dy;
		this.width = width;
		this.height = height;
		this.speed = speed; 
		this.projectileID = projectileID;
		
	    if(dx == 0 && dy == 0) angle = 0;
	    else angle = Math.atan2(dy, dx) + Math.PI / 2;
	}
	
	public void move(long deltaTime) {
		xPos += dx*(deltaTime/1e9)*speed;
		yPos += dy*(deltaTime/1e9)*speed;
	}
	
	public void draw(Graphics2D g) {
	    AffineTransform old = g.getTransform();

	    // Translate to the center of the player image for rotation
	    g.translate(xPos + img.getWidth(null) / 2, yPos + img.getHeight(null) / 2);
	    g.rotate(angle);

	    // Draw the image, centered on the translation point
	    g.drawImage(img, -img.getWidth(null) / 2, -img.getHeight(null) / 2, null);

	    // Restore the old transform
	    g.setTransform(old);
	}
	
	
	
	// Method to get the corners of the rotated rectangle
	public Path2D getHitbox() {
		double centerX = xPos + width / 2;
	    double centerY = yPos + height / 2;

	    // Define the original corners of the image
	    double[][] corners = {
	        {xPos, yPos},             // Top-left
	        {xPos + width, yPos},     // Top-right
	        {xPos + width, yPos + height}, // Bottom-right
	        {xPos, yPos + height}     // Bottom-left
	    };

	    // Create a Path2D object to hold the hitbox outline
	    Path2D hitbox = new Path2D.Double();

	    // Calculate the rotated corners and add them to the path
	    for (int i = 0; i < corners.length; i++) {
	        double[] corner = corners[i];
	        double px = corner[0];
	        double py = corner[1];

	        // Rotate the corner around the center
	        double new_px = Math.cos(angle) * (px - centerX) - Math.sin(angle) * (py - centerY) + centerX;
	        double new_py = Math.sin(angle) * (px - centerX) + Math.cos(angle) * (py - centerY) + centerY;

	        if (i == 0) {
	            hitbox.moveTo(new_px, new_py); // Move to the first corner
	        } else {
	            hitbox.lineTo(new_px, new_py); // Draw lines to the other corners
	        }
	    }
	    hitbox.closePath();
		return hitbox;
	}

	
	
	
	@Override
	public String toString() {
		return dx + "," + dy + "," + (int)xPos + "," + (int)yPos;
	}

	public double getxPos() {
		return xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public int getSpeed() {
		return speed;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	public int getDirectionX() {
		return dx;
	}

	public int getDirectionY() {
		return dy;
	}

}
