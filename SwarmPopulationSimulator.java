// SwarmPopulationSimulator.java
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
import java.awt.event.*;
import javax.swing.*;

public class SwarmPopulationSimulator extends JFrame implements ActionListener {
    private SwarmPopulationSimulator myself;
    public int width, height, originalWidth, originalHeight;
    private JPanel cp;
    private Insets ins;
    private int menuHeight;
    private Graphics img, sfg;

    private JCheckBox tracking, mouseEffect;

    private JMenuItem menuMutate, menuMix, menuReplicate, menuEdit, menuKill;
    private JMenuBar mb;

    private double currentMidX, currentMidY, currentScalingFactor;
    private double swarmRadius = 3;
    private double swarmDiameter;
    private int mouseX, mouseY;
    private int weightOfMouseCursor = 20;
    private boolean isMouseIn;

    public int frameNumber;
    public RecipeFrame displayedRecipe;
    public SwarmPopulation population;
    private ArrayList<SwarmPopulation> originalPopulationList;
    private java.util.List<RecipeFrame> recipeFrames;

    public Image im;
    public ArrayList<SwarmIndividual> swarmInBirthOrder, swarmInXOrder, swarmInYOrder;
    public boolean isSelected, notYetNoticed, isToMutate, isToReplicate;

    public SwarmPopulationSimulator(int frameSize, int spaceSize, SwarmPopulation sol, ArrayList<SwarmPopulation> solList, int num, JCheckBox tr, JCheckBox mo, java.util.List<RecipeFrame> rcfs) {
	super("Swarm #" + num + ": " + sol.title);
	myself = this;

	frameNumber = num;
	displayedRecipe = null;
	recipeFrames = rcfs;
	population = sol;
	originalPopulationList = solList;

	width = height = frameSize;
	originalWidth = originalHeight = spaceSize;
	tracking = tr;
	mouseEffect = mo;

 	Font font = new Font("dialog", Font.BOLD, 16);
 	UIManager.put("Menu.font", font);
 	UIManager.put("MenuItem.font", font);

	menuMutate = new JMenuItem("Mutate");
	menuMutate.addActionListener(this);
	menuMix = new JMenuItem("Mix");
	menuMix.addActionListener(this);
	menuReplicate = new JMenuItem("Replicate");
	menuReplicate.addActionListener(this);
	menuEdit = new JMenuItem("Edit");
	menuEdit.addActionListener(this);
	menuKill = new JMenuItem("Kill");
	menuKill.addActionListener(this);

	mb = new JMenuBar();
	mb.add(menuReplicate);
	mb.add(menuMutate);
	mb.add(menuMix);
	mb.add(menuEdit);
	mb.add(menuKill);
	setJMenuBar(mb);

	cp = new JPanel(new BorderLayout());
	cp.setBackground(Color.white);
	setContentPane(cp);

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	setVisible(true);

	ins = getInsets();
	menuHeight = mb.getHeight();
	setSize(width + ins.left + ins.right, height + ins.top + ins.bottom + menuHeight);

	while (sfg == null) sfg = getGraphics();

	synchronized(this) {
	    while (im == null) im = createImage(width, height);
	    while (img == null) img = im.getGraphics();
	}
	clearImage();

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    if (displayedRecipe != null) {
			displayedRecipe.orphanize();
		    }
		    dispose();
		}
	    });

	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    ins = getInsets();
		    menuHeight = mb.getHeight();
		    width = getWidth() - ins.left - ins.right;
		    height = getHeight() - ins.top - ins.bottom - menuHeight;
		    synchronized(myself) {
			im = null;
			img = null;
			while (im == null) im = createImage(width, height);
			while (img == null) img = im.getGraphics();
		    }
		    redraw();
		}
	    });

	addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent me) {
		    if (me.getModifiers() == InputEvent.BUTTON3_MASK) {
			outputRecipe();
		    }
		    else if (isSelected == false) isSelected = true;
		    else notYetNoticed = true;
		}
		public void mouseEntered(MouseEvent me) {
		    isMouseIn = true;
		}
		public void mouseExited(MouseEvent me) {
		    isMouseIn = false;
		}
	    });

	mouseX = mouseY = -100;

	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent me) {
		    mouseX = me.getX() - ins.left;
		    mouseY = me.getY() - ins.top - menuHeight;
		}
		public void mouseMoved(MouseEvent me) {
		    mouseX = me.getX() - ins.left;
		    mouseY = me.getY() - ins.top - menuHeight;
		}
	    });

	isSelected = false;
	notYetNoticed = true;
	isToMutate = false;
	isToReplicate = false;

	currentMidX = 0;
	currentMidY = 0;
	currentScalingFactor = 0;
	swarmDiameter = swarmRadius * 2;
	swarmInBirthOrder = new ArrayList<SwarmIndividual>();
	swarmInXOrder = new ArrayList<SwarmIndividual>();
	swarmInYOrder = new ArrayList<SwarmIndividual>();

	for (int i = 0; i < sol.population.size(); i ++)
	    addSwarm(sol.population.get(i));

	displayStates();
    }

    public void paint(Graphics g) {
	synchronized(this) {
	    while (im == null) im = createImage(width, height);
	    while (img == null) img = im.getGraphics();
	}
	mb.repaint();
	g.drawImage(im, ins.left, ins.top + menuHeight, width, height, this);
    }

    public void redraw() {
	mb.repaint();
	sfg.drawImage(im, ins.left, ins.top + menuHeight, width, height, this);
    }

    public void clearImage() {
	img.setColor(Color.white);
	img.fillRect(0, 0, width, height);
	redraw();
    }

    public synchronized void addSwarm(SwarmIndividual b) {
	int i;

	swarmInBirthOrder.add(b);

	if (swarmInXOrder.isEmpty()) {
	    swarmInXOrder.add(b);
	    swarmInYOrder.add(b);
	}

	else {

	    if ((b.x - swarmInXOrder.get(0).x)
		< (swarmInXOrder.get(swarmInXOrder.size() - 1).x
		   - b.x)) {
		i = 0;
		while (i < swarmInXOrder.size()) {
		    if (swarmInXOrder.get(i).x >= b.x) break;
		    i ++;
		}
		swarmInXOrder.add(i, b);
	    }
	    else {
		i = swarmInXOrder.size();
		while (i > 0) {
		    if (swarmInXOrder.get(i - 1).x <= b.x) break;
		    i --;
		}
		swarmInXOrder.add(i, b);
	    }

	    if ((b.y - swarmInYOrder.get(0).y)
		< (swarmInYOrder.get(swarmInYOrder.size() - 1).y
		   - b.y)) {
		i = 0;
		while (i < swarmInYOrder.size()) {
		    if (swarmInYOrder.get(i).y >= b.y) break;
		    i ++;
		}
		swarmInYOrder.add(i, b);
	    }
	    else {
		i = swarmInYOrder.size();
		while (i > 0) {
		    if (swarmInYOrder.get(i - 1).y <= b.y) break;
		    i --;
		}
		swarmInYOrder.add(i, b);
	    }

	}

    }

    public synchronized void replacePopulationWith(SwarmPopulation newpop) {
	setTitle("Swarm #" + frameNumber + ": " + newpop.title);

	for(int i = 0; i < originalPopulationList.size(); i ++) {
	    if (originalPopulationList.get(i) == population)
		population = newpop;
		originalPopulationList.set(i, population);
	}

	currentMidX = 0;
	currentMidY = 0;
	currentScalingFactor = 0;
	swarmDiameter = swarmRadius * 2;
	swarmInBirthOrder = new ArrayList<SwarmIndividual>();
	swarmInXOrder = new ArrayList<SwarmIndividual>();
	swarmInYOrder = new ArrayList<SwarmIndividual>();

	for (int i = 0; i < newpop.population.size(); i ++)
	    addSwarm(newpop.population.get(i));

	displayStates();
    }

    public synchronized void resetRanksInSwarm() {
	SwarmIndividual tempSwarm;

	for (int i = 0; i < swarmInXOrder.size(); i ++) {
	    tempSwarm = swarmInXOrder.get(i);
	    if (tempSwarm.rankInXOrder != -1) tempSwarm.rankInXOrder = i;
	    else swarmInXOrder.remove(i --);
	}

	for (int i = 0; i < swarmInYOrder.size(); i ++) {
	    tempSwarm = swarmInYOrder.get(i);
	    if (tempSwarm.rankInYOrder != -1) tempSwarm.rankInYOrder = i;
	    else swarmInYOrder.remove(i --);
	}
    }

    public synchronized void simulateSwarmBehavior(){
	SwarmIndividual tempSwarm, tempSwarm2;
	SwarmParameters param;

	double tempX, tempY, tempX2, tempY2, tempDX, tempDY;
	double localCenterX, localCenterY, localDX, localDY, tempAx, tempAy, d;
	int n;

	ArrayList<SwarmIndividual> neighbors = new ArrayList<SwarmIndividual>();

	int numberOfSwarm = swarmInBirthOrder.size();

	SwarmIndividual mouseCursor = new SwarmIndividual();

	if (mouseEffect.isSelected() && isMouseIn) {
	    mouseCursor.x = ((double) (mouseX - width / 2)) / currentScalingFactor + currentMidX;
	    mouseCursor.y = ((double) (mouseY - height / 2)) / currentScalingFactor + currentMidY;
	}

	for (int i = 0; i < numberOfSwarm; i ++) {
	    tempSwarm = swarmInBirthOrder.get(i);
	    param = tempSwarm.genome;
	    tempX = tempSwarm.x;
	    tempY = tempSwarm.y;

	    double neighborhoodRadiusSquared
		= param.neighborhoodRadius * param.neighborhoodRadius;

	    neighbors.clear();

	    // Detecting neighbors using sorted lists

	    double minX = tempX - param.neighborhoodRadius;
	    double maxX = tempX + param.neighborhoodRadius;
	    double minY = tempY - param.neighborhoodRadius;
	    double maxY = tempY + param.neighborhoodRadius;
	    int minRankInXOrder = tempSwarm.rankInXOrder;
	    int maxRankInXOrder = tempSwarm.rankInXOrder;
	    int minRankInYOrder = tempSwarm.rankInYOrder;
	    int maxRankInYOrder = tempSwarm.rankInYOrder;

	    for(int j = tempSwarm.rankInXOrder - 1; j >= 0; j --) {
		if (swarmInXOrder.get(j).x >= minX)
		    minRankInXOrder = j;
		else break;
	    }
	    for(int j = tempSwarm.rankInXOrder + 1; j < numberOfSwarm; j ++) {
		if (swarmInXOrder.get(j).x <= maxX)
		    maxRankInXOrder = j;
		else break;
	    }
	    for(int j = tempSwarm.rankInYOrder - 1; j >= 0; j --) {
		if (swarmInYOrder.get(j).y >= minY)
		    minRankInYOrder = j;
		else break;
	    }
	    for(int j = tempSwarm.rankInYOrder + 1; j < numberOfSwarm; j ++) {
		if (swarmInYOrder.get(j).y <= maxY)
		    maxRankInYOrder = j;
		else break;
	    }

	    if (maxRankInXOrder - minRankInXOrder < maxRankInYOrder - minRankInYOrder) {
		for (int j = minRankInXOrder; j <= maxRankInXOrder; j ++) {
		    tempSwarm2 = swarmInXOrder.get(j);
		    if (tempSwarm != tempSwarm2) 
			if (tempSwarm2.rankInYOrder >= minRankInYOrder &&
			    tempSwarm2.rankInYOrder <= maxRankInYOrder) {
			    if ((tempSwarm2.x - tempSwarm.x) * (tempSwarm2.x - tempSwarm.x) +
				(tempSwarm2.y - tempSwarm.y) * (tempSwarm2.y - tempSwarm.y)
				< neighborhoodRadiusSquared) neighbors.add(tempSwarm2);
			}
		}
	    }
	    else {
		for (int j = minRankInYOrder; j <= maxRankInYOrder; j ++) {
		    tempSwarm2 = swarmInYOrder.get(j);
		    if (tempSwarm != tempSwarm2) 
			if (tempSwarm2.rankInXOrder >= minRankInXOrder &&
			    tempSwarm2.rankInXOrder <= maxRankInXOrder) {
			    if ((tempSwarm2.x - tempSwarm.x) * (tempSwarm2.x - tempSwarm.x) +
				(tempSwarm2.y - tempSwarm.y) * (tempSwarm2.y - tempSwarm.y)
				< neighborhoodRadiusSquared) neighbors.add(tempSwarm2);
			}
		}
	    }

	    if (mouseEffect.isSelected() && isMouseIn) {
		if ((mouseCursor.x - tempSwarm.x) * (mouseCursor.x - tempSwarm.x) +
		    (mouseCursor.y - tempSwarm.y) * (mouseCursor.y - tempSwarm.y)
		    < neighborhoodRadiusSquared)
		    for (int j = 0; j < weightOfMouseCursor; j ++)
			neighbors.add(mouseCursor);
	    }

	    // simulating the behavior of swarm agents

	    n = neighbors.size();

	    if (n == 0) {
		tempAx = Math.random() - 0.5;
		tempAy = Math.random() - 0.5;
	    }

	    else {
		localCenterX = localCenterY = 0;
		localDX = localDY = 0;
		for (int j = 0; j < n; j ++) {
		    tempSwarm2 = neighbors.get(j);
		    localCenterX += tempSwarm2.x;
		    localCenterY += tempSwarm2.y;
		    localDX += tempSwarm2.dx;
		    localDY += tempSwarm2.dy;
		}
		localCenterX /= n;
		localCenterY /= n;
		localDX /= n;
		localDY /= n;

		tempAx = tempAy = 0;

		tempAx += (localCenterX - tempX) * param.c1;
		tempAy += (localCenterY - tempY) * param.c1;

		tempAx += (localDX - tempSwarm.dx) * param.c2;
		tempAy += (localDY - tempSwarm.dy) * param.c2;

		for (int j = 0; j < n; j ++) {
		    tempSwarm2 = neighbors.get(j);
		    tempX2 = tempSwarm2.x;
		    tempY2 = tempSwarm2.y;
		    d = (tempX - tempX2) * (tempX - tempX2) +
			(tempY - tempY2) * (tempY - tempY2);
		    if (d == 0) d = 0.001;
		    tempAx += (tempX - tempX2) / d * param.c3;
		    tempAy += (tempY - tempY2) / d * param.c3;
		}

		if (Math.random() < param.c4) {
		    tempAx += Math.random() * 10 - 5;
		    tempAy += Math.random() * 10 - 5;
		}
	    }

	    tempSwarm.accelerate(tempAx, tempAy, param.maxSpeed);

	    tempDX = tempSwarm.dx2;
	    tempDY = tempSwarm.dy2;
	    d = Math.sqrt(tempDX * tempDX + tempDY * tempDY);
	    if (d == 0) d = 0.001;
	    tempSwarm.accelerate(tempDX * (param.normalSpeed - d) / d * param.c5,
				 tempDY * (param.normalSpeed - d) / d * param.c5,
				 param.maxSpeed);
	}

    }

    public synchronized void updateStates() {
	SwarmIndividual tempSwarm, tempSwarm2;
	int numberOfSwarm = swarmInBirthOrder.size();
	int j;

	for (int i = 0; i < numberOfSwarm; i ++)
	    swarmInBirthOrder.get(i).move();

	// Sorting swarmInXOrder and swarmInYOrder using insertion sorting algorithm

	for (int i = 1; i < numberOfSwarm; i ++) {
	    tempSwarm = swarmInXOrder.get(i);
	    j = i;
	    while (j > 0) {
		tempSwarm2 = swarmInXOrder.get(j - 1);
		if (tempSwarm2.x > tempSwarm.x) {
		    swarmInXOrder.set(j, tempSwarm2);
		    j --;
		}
		else break;
	    }
	    swarmInXOrder.set(j, tempSwarm);
	
	    tempSwarm = swarmInYOrder.get(i);
	    j = i;
	    while (j > 0) {
		tempSwarm2 = swarmInYOrder.get(j - 1);
		if (tempSwarm2.y > tempSwarm.y) {
		    swarmInYOrder.set(j, tempSwarm2);
		    j --;
		}
		else break;
	    }
	    swarmInYOrder.set(j, tempSwarm);
	}
	
	resetRanksInSwarm();

    }

    public synchronized void displayStates() {
	SwarmIndividual ag, ag2;
	int max, x, y;
	double minX, maxX, minY, maxY, tempX, tempY, midX, midY, scalingFactor;
	double averageInterval;
	double intervalCoefficient = 10.0;
	int tempRadius, tempDiameter;
	int margin = 30;
	double gridInterval = 300;

	while (img == null);

	if (isSelected) img.setColor(Color.cyan);
	else img.setColor(Color.white);
	img.fillRect(0, 0, width, height);

	if ((max = swarmInBirthOrder.size()) == 0) {
	    redraw();
	    return;
	}

	minX = swarmInXOrder.get(0).x;
	maxX = swarmInXOrder.get(max - 1).x;
	minY = swarmInYOrder.get(0).y;
	maxY = swarmInYOrder.get(max - 1).y;

	if (tracking.isSelected() && max > 10) {

	    averageInterval = 0;
	    for (int i = 0; i < max - 1; i ++) {
		ag = swarmInXOrder.get(i);
		ag2 = swarmInXOrder.get(i + 1);
		averageInterval += ag2.x - ag.x;
	    }
	    averageInterval /= max - 1;
	    for (int i = 0; i < max - 10; i ++) {
		ag = swarmInXOrder.get(i);
		ag2 = swarmInXOrder.get(i + 10);
		if (ag2.x - ag.x < averageInterval * intervalCoefficient) {
		    minX = ag.x;
		    break;
		}
	    }
	    for (int i = max - 1; i >= 10; i --) {
		ag = swarmInXOrder.get(i - 10);
		ag2 = swarmInXOrder.get(i);
		if (ag2.x - ag.x < averageInterval * intervalCoefficient) {
		    maxX = ag2.x;
		    break;
		}
	    }

	    tempX = (maxX - minX) * 0.1;
	    minX -= tempX;
	    maxX += tempX;

	    averageInterval = 0;
	    for (int i = 0; i < max - 1; i ++) {
		ag = swarmInYOrder.get(i);
		ag2 = swarmInYOrder.get(i + 1);
		averageInterval += ag2.y - ag.y;
	    }
	    averageInterval /= max - 1;
	    for (int i = 0; i < max - 10; i ++) {
		ag = swarmInYOrder.get(i);
		ag2 = swarmInYOrder.get(i + 10);
		if (ag2.y - ag.y < averageInterval * intervalCoefficient) {
		    minY = ag.y;
		    break;
		}
	    }
	    for (int i = max - 1; i >= 10; i --) {
		ag = swarmInYOrder.get(i - 10);
		ag2 = swarmInYOrder.get(i);
		if (ag2.y - ag.y < averageInterval * intervalCoefficient) {
		    maxY = ag2.y;
		    break;
		}
	    }

	    tempY = (maxY - minY) * 0.1;
	    minY -= tempY;
	    maxY += tempY;
	}

	if (maxX - minX < (double) originalWidth)
	    maxX = (minX = (minX + maxX - (double) originalWidth) / 2) + (double) originalWidth;
	if (maxY - minY < (double) originalHeight)
	    maxY = (minY = (minY + maxY - (double) originalHeight) / 2) + (double) originalHeight;

	midX = (minX + maxX) / 2;
	midY = (minY + maxY) / 2;

	if ((maxX - minX) * height > (maxY - minY) * width)
	    scalingFactor = ((double) (width - 2 * margin)) / (maxX - minX);
	else
	    scalingFactor = ((double) (height - 2 * margin)) / (maxY - minY);

	if (currentScalingFactor == 0) {
	    currentMidX += midX;
	    currentMidY += midY;
	    currentScalingFactor = scalingFactor;
	}
	else {
	    currentMidX += (midX - currentMidX) * 0.1;
	    currentMidY += (midY - currentMidY) * 0.1;
	    currentScalingFactor += (scalingFactor - currentScalingFactor) * 0.5;
	}

	// Drawing grids

	img.setColor(Color.lightGray);
	for (tempX = Math.floor((-((double) width) / 2 / currentScalingFactor + currentMidX) / gridInterval) * gridInterval;
	     tempX < ((double) width) / 2 / currentScalingFactor + currentMidX;
	     tempX += gridInterval)
	    img.drawLine((int) ((tempX - currentMidX) * currentScalingFactor) + width/2,
			 0,
			 (int) ((tempX - currentMidX) * currentScalingFactor) + width/2,
			 height);
	for (tempY = Math.floor((-((double) height) / 2 / currentScalingFactor + currentMidY) / gridInterval) * gridInterval;
	     tempY < ((double) height) / 2 / currentScalingFactor + currentMidY;
	     tempY += gridInterval)
	    img.drawLine(0,
			 (int) ((tempY - currentMidY) * currentScalingFactor) + height/2,
			 width,
			 (int) ((tempY - currentMidY) * currentScalingFactor) + height/2);

	// Drawing swarm

	tempRadius = (int) (swarmRadius * currentScalingFactor);
	tempDiameter = (int) (swarmDiameter * currentScalingFactor);
	if (tempDiameter < 3) tempDiameter = 3;

	for (int i = 0; i < max; i ++) {
	    ag = swarmInBirthOrder.get(i);
	    x = (int) ((ag.x - currentMidX) * currentScalingFactor) + width / 2;
	    y = (int) ((ag.y - currentMidY) * currentScalingFactor) + height / 2;
	    img.setColor(ag.displayColor());
	    img.fillOval(x - tempRadius, y - tempRadius, tempDiameter, tempDiameter);
	}

	redraw();

	// Relocating swarm if they went too far

	if (midX < - 3 * gridInterval) {
	    currentMidX += gridInterval;
	    for (int i = 0; i < max; i ++)
		swarmInBirthOrder.get(i).x += gridInterval;
	}
	else if (midX > 3 * gridInterval) {
	    currentMidX -= gridInterval;
	    for (int i = 0; i < max; i ++)
		swarmInBirthOrder.get(i).x -= gridInterval;
	}

	if (midY < - 3 * gridInterval) {
	    currentMidY += gridInterval;
	    for (int i = 0; i < max; i ++)
		swarmInBirthOrder.get(i).y += gridInterval;
	}
	else if (midY > 3 * gridInterval) {
	    currentMidY -= gridInterval;
	    for (int i = 0; i < max; i ++)
		swarmInBirthOrder.get(i).y -= gridInterval;
	}
    }

    public synchronized void outputRecipe() {
	if (displayedRecipe == null) {
	    displayedRecipe = new RecipeFrame(this, swarmInBirthOrder, originalWidth, originalHeight, recipeFrames);
	    synchronized (recipeFrames) {
		recipeFrames.add(displayedRecipe);
	    }
	    displayedRecipe.setVisible(true);
	}
	else {
	    displayedRecipe.putImage(im);
	    displayedRecipe.setState(JFrame.NORMAL);
	    displayedRecipe.toFront();
	}
    }

    public void actionPerformed(ActionEvent e) {
	Object src = e.getSource();

	if (src == menuMutate) {
	    isToMutate = true;
	}

	else if (src == menuMix) {
	    if (isSelected == false) isSelected = true;
	    else isSelected = false;
	}

	else if (src == menuReplicate) {
	    isToReplicate = true;
	}

	else if (src == menuEdit) {
	    outputRecipe();
	}

	else if (src == menuKill) {
	    if (displayedRecipe != null) {
		displayedRecipe.orphanize();
	    }
	    dispose();
	}
    }

    public void rescale(int targetFrameSize) {
	int prevWidth = width;
	int prevHeight = height;

	int w = prevWidth + (targetFrameSize - prevWidth) / 2;
	if (w < 30) w = 30;
	int h = prevHeight + (targetFrameSize - prevHeight) / 2;
	if (h < 30) h = 30;

	setSize(w + ins.left + ins.right, h + ins.top + ins.bottom + menuHeight);
	ins = getInsets();
	menuHeight = mb.getHeight();
	width = getWidth() - ins.left - ins.right;
	height = getHeight() - ins.top - ins.bottom - menuHeight;

	int offsetx = (prevWidth - width) / 2;
	int offsety = (prevHeight - height) / 2;
	setLocation(getLocation().x + offsetx, getLocation().y + offsety);

	synchronized(myself) {
	    im = null;
	    img = null;
	    while (im == null) im = createImage(width, height);
	    while (img == null) img = im.getGraphics();
	}
	redraw();
    }
}
