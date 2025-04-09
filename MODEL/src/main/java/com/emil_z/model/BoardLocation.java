package com.emil_z.model;

import android.graphics.Point;

public class BoardLocation {
	private Point x;
	private Point y;
	public BoardLocation(Point x, Point y) {
		this.x = x;
		this.y = y;
	}
	public Point getX() {
		return x;
	}
	public void setX(Point x) {
		this.x = x;
	}
	public Point getY() {
		return y;
	}
	public void setY(Point y) {
		this.y = y;
	}
}