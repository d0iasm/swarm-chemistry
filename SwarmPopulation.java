// SwarmPopulation.java
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

import java.util.*;

public class SwarmPopulation {
    public ArrayList<SwarmIndividual> population;
    public String title;

    public SwarmPopulation(ArrayList<SwarmIndividual> pop, String t) {
	population = pop;
	title = t;
    }

    public SwarmPopulation(int n, int width, int height, String t) {
	title = t;

	SwarmParameters ancestorGenome = new SwarmParameters();

	population = new ArrayList<SwarmIndividual>();
	for (int i = 0; i < n; i ++)
	    population.add(new SwarmIndividual(Math.random() * width,
					       Math.random() * height,
					       Math.random() * 10 - 5,
					       Math.random() * 10 - 5,
					       new SwarmParameters(ancestorGenome)));
    }

    public SwarmPopulation(SwarmPopulation a, int width, int height, String t) {
	title = t;

	SwarmIndividual temp;

	population = new ArrayList<SwarmIndividual>();
	for (int i = 0; i < a.population.size(); i ++) {
	    temp = a.population.get(i);
	    population.add(new SwarmIndividual(Math.random() * width,
					       Math.random() * height,
					       Math.random() * 10 - 5,
					       Math.random() * 10 - 5,
					       new SwarmParameters(temp.genome)));
	}
    }

    public SwarmPopulation(SwarmPopulation a, SwarmPopulation b, double rate, int width, int height, String t) {
	title = t;

	SwarmIndividual temp;
	SwarmPopulation source;

	population = new ArrayList<SwarmIndividual>();
	for (int i = 0; i < (a.population.size() + b.population.size()) / 2; i ++) {
	    if (Math.random() < rate) source = a;
	    else source = b;
	    temp = source.population.get((int) (Math.random() * source.population.size()));
	    population.add(new SwarmIndividual(Math.random() * width,
					       Math.random() * height,
					       Math.random() * 10 - 5,
					       Math.random() * 10 - 5,
					       new SwarmParameters(temp.genome)));
	}
    }

    public void perturb(double pcm, int spaceSize) {
	int pop = population.size();
	pop += (int) ((Math.random() * 2.0 - 1.0) * pcm * (double) pop);
	if (pop < 1) pop = 1;
	if (pop > SwarmParameters.numberOfIndividualsMax) pop = SwarmParameters.numberOfIndividualsMax;

	ArrayList<SwarmIndividual> newPopulation = new ArrayList<SwarmIndividual>();
	SwarmParameters tempParam;
	for (int i = 0; i < pop; i ++) {
	    tempParam
		= new SwarmParameters(population.get((int) (Math.random() * population.size())).genome);
	    newPopulation.add(new SwarmIndividual(Math.random() * spaceSize,
						  Math.random() * spaceSize,
						  Math.random() * 10 - 5,
						  Math.random() * 10 - 5,
						  tempParam));
	}
	population = newPopulation;
    }
}
