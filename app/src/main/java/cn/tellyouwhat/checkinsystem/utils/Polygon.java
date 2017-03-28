package cn.tellyouwhat.checkinsystem.utils;

import java.util.Arrays;

/**
 * Created by Harbor-Laptop on 2017/3/26.
 */

public class Polygon {
	private static final int MIN_LENGTH = 4;
	public int npoints;
	public int xpoints[];
	public int ypoints[];

	/**
	 * Creates an empty polygon.
	 *
	 * @since 1.0
	 */
	public Polygon() {
		xpoints = new int[MIN_LENGTH];
		ypoints = new int[MIN_LENGTH];
	}

	/**
	 * Constructs and initializes a <code>Polygon</code> from the specified
	 * parameters.
	 *
	 * @param xpoints an array of X coordinates
	 * @param ypoints an array of Y coordinates
	 * @param npoints the total number of points in the
	 *                <code>Polygon</code>
	 * @throws NegativeArraySizeException if the value of
	 *                                    <code>npoints</code> is negative.
	 * @throws IndexOutOfBoundsException  if <code>npoints</code> is
	 *                                    greater than the length of <code>xpoints</code>
	 *                                    or the length of <code>ypoints</code>.
	 * @throws NullPointerException       if <code>xpoints</code> or
	 *                                    <code>ypoints</code> is <code>null</code>.
	 * @since 1.0
	 */
	public Polygon(int xpoints[], int ypoints[], int npoints) {
		// Fix 4489009: should throw IndexOutofBoundsException instead
		// of OutofMemoryException if npoints is huge and > {x,y}points.length
		if (npoints > xpoints.length || npoints > ypoints.length) {
			throw new IndexOutOfBoundsException("npoints > xpoints.length || " +
					"npoints > ypoints.length");
		}
		// Fix 6191114: should throw NegativeArraySizeException with
		// negative npoints
		if (npoints < 0) {
			throw new NegativeArraySizeException("npoints < 0");
		}
		// Fix 6343431: Applet compatibility problems if arrays are not
		// exactly npoints in length
		this.npoints = npoints;
		this.xpoints = Arrays.copyOf(xpoints, npoints);
		this.ypoints = Arrays.copyOf(ypoints, npoints);
	}

	/**
	 * Determines whether the specified {@link Point} is inside this
	 * <code>Polygon</code>.
	 *
	 * @param p the specified <code>Point</code> to be tested
	 * @return <code>true</code> if the <code>Polygon</code> contains the
	 * <code>Point</code>; <code>false</code> otherwise.
	 * @see #contains(double, double)
	 * @since 1.0
	 */
	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	/**
	 * Determines whether the specified coordinates are inside this
	 * <code>Polygon</code>.
	 * <p>
	 *
	 * @param x the specified X coordinate to be tested
	 * @param y the specified Y coordinate to be tested
	 * @return {@code true} if this {@code Polygon} contains
	 * the specified coordinates {@code (x,y)};
	 * {@code false} otherwise.
	 * @see #contains(double, double)
	 * @since 1.1
	 */
	public boolean contains(int x, int y) {
		return contains((double) x, (double) y);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 1.2
	 */
	public boolean contains(double x, double y) {
		int hits = 0;

		int lastx = xpoints[npoints - 1];
		int lasty = ypoints[npoints - 1];
		int curx, cury;

		// Walk the edges of the polygon
		for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
			curx = xpoints[i];
			cury = ypoints[i];

			if (cury == lasty) {
				continue;
			}

			int leftx;
			if (curx < lastx) {
				if (x >= lastx) {
					continue;
				}
				leftx = curx;
			} else {
				if (x >= curx) {
					continue;
				}
				leftx = lastx;
			}

			double test1, test2;
			if (cury < lasty) {
				if (y < cury || y >= lasty) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - curx;
				test2 = y - cury;
			} else {
				if (y < lasty || y >= cury) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - lastx;
				test2 = y - lasty;
			}

			if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
				hits++;
			}
		}

		return ((hits & 1) != 0);
	}
}
