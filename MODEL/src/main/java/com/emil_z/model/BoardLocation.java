package com.emil_z.model;

import android.graphics.Point;

public class BoardLocation {
	private Point outer;
	private Point inner;

	public BoardLocation(Point outer, Point inner) {
		this.outer = outer;
		this.inner = inner;
	}

	public BoardLocation(int oRow, int oCol, int iRow, int iCol) {
		{
			this.outer = new Point(oRow, oCol);
			this.inner = new Point(iRow, iCol);
		}

	}

	public Point getOuter() {
		return outer;
	}

	public void setOuter(Point outer) {
		this.outer = outer;
	}

	public Point getInner() {
		return inner;
	}

	public void setInner(Point inner) {
		this.inner = inner;
	}
}