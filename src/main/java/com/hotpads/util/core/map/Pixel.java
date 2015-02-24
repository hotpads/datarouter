package com.hotpads.util.core.map;

public class Pixel {

	public Pixel(String quadCode){
		Pixel p = GridTool.getPixelForQuadCode(quadCode);
		this.x = p.getX();
		this.y = p.getY(); 
		this.level = p.getLevel();
	}
	
	public Pixel(long x, long y, int level){
		this.x = x;
		this.y = y;
		this.level = level;
	}
	
	long x;
	long y;
	int level;  //circumference of the earth (screen width and height)
	
	public Pixel getPixelAtLevel(int level){
		return GridTool.getPixelAtLevel(this, level);
	}
	public long getX() {
		return x;
	}
	public void setX(long x) {
		this.x = x;
	}
	public long getY() {
		return y;
	}
	public void setY(long y) {
		this.y = y;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String toString(){
		return "Pixel[x=" + getX() + ",y=" + getY() + ",level=" + level + "]";
	}
}
