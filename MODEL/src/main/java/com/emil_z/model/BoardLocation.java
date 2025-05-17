package com.emil_z.model;

import android.graphics.Point;

/**
 * Represents a location on a board, defined by two points: outer and inner.
 * Typically used to specify a position with two levels of granularity (e.g., an outer and inner grid).
 */
public class BoardLocation {
	private Point outer;
	private Point inner;

	/**
	 * Default constructor for BoardLocation.
	 */
	public BoardLocation() {
	}

	/**
	 * Constructs a BoardLocation with specified outer and inner points.
	 *
	 * @param outer the outer point
	 * @param inner the inner point
	 */
	public BoardLocation(Point outer, Point inner) {
		this.outer = outer;
		this.inner = inner;
	}


	/**
	 * Constructs a BoardLocation with specified row and column values for both outer and inner points.
	 *
	 * @param oRow the row of the outer point
	 * @param oCol the column of the outer point
	 * @param iRow the row of the inner point
	 * @param iCol the column of the inner point
	 */
	public BoardLocation(int oRow, int oCol, int iRow, int iCol) {
		{
			this.outer = new Point(oRow, oCol);
			this.inner = new Point(iRow, iCol);
		}

	}

	/**
	 * Gets the outer point.
	 *
	 * @return the outer point
	 */
	public Point getOuter() {
		return outer;
	}

	/**
	 * Sets the outer point.
	 *
	 * @param outer the outer point to set
	 */
	public void setOuter(Point outer) {
		this.outer = outer;
	}

	/**
	 * Gets the inner point.
	 *
	 * @return the inner point
	 */
	public Point getInner() {
		return inner;
	}

	/**
	 * Sets the inner point.
	 *
	 * @param inner the inner point to set
	 */
	public void setInner(Point inner) {
		this.inner = inner;
	}

	/**
	 * Checks if this BoardLocation is equal to another object.
	 * Two BoardLocations are equal if both their outer and inner points are equal.
	 *
	 * @param o the object to compare with
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BoardLocation)) return false;

		BoardLocation that = (BoardLocation) o;

		if (!outer.equals(that.outer)) return false;
		return inner.equals(that.inner);
	}
}