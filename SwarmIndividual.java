// SwarmIndividual.java
//
// Part of:
// SwarmChemistry.java
// Public Release Version 1.2.0
//
// 2006-2009 (c) Copyright by Hiroki Sayama. All rights reserved.
//
// Send any correspondences to:
//   Hiroki Sayama, D.Sc.
//   Assistant Professor, Department of Bioengineering
//   Binghamton University, State University of New York
//   P.O. Box 6000, Binghamton, NY 13902-6000, USA
//   Tel: +1-607-777-4439  Fax: +1-607-777-5780
//   Email: sayama@binghamton.edu
//
// For more information about this software, see:
//   http://bingweb.binghamton.edu/~sayama/SwarmChemistry/
//

import java.awt.*;
import java.util.*;

public class SwarmIndividual {
    public double x, y, dx, dy, dx2, dy2;
    public SwarmParameters genome;
    public int rankInXOrder, rankInYOrder;

    public SwarmIndividual() {
	this(0.0, 0.0, 0.0, 0.0, new SwarmParameters());
    }

    public SwarmIndividual(double xx, double yy, double dxx, double dyy,
			   SwarmParameters g) {
	x = xx;
	y = yy;
	dx = dx2 = dxx;
	dy = dy2 = dyy;
	genome = g;
	rankInXOrder = 0;
	rankInYOrder = 0;
    }

    public void accelerate(double ax, double ay, double maxMove) {
	dx2 += ax;
	dy2 += ay;

	double d = dx2 * dx2 + dy2 * dy2;
	if (d > maxMove * maxMove) {
	    double normalizationFactor = maxMove / Math.sqrt(d);
	    dx2 *= normalizationFactor;
	    dy2 *= normalizationFactor;
	}
    }

    public void move() {
	dx = dx2;
	dy = dy2;
	x += dx;
	y += dy;
    }

    public Color displayColor() {
	return genome.displayColor();
    }
}
