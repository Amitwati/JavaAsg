package graphics;
import Memento.Memento;
import Memento.Originator;
import Memento.ZooMemento;
import mobility.Point;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.*;
import animals.Animal;
import food.EFoodType;
import plants.Cabbage;
import plants.Lettuce;
import plants.Plant;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * singleton class that reprenset the main panel of the program
 */
public class ZooPanel extends JPanel implements ActionListener
{
   private static final long serialVersionUID = 1L;
   private static final int MAX_ANIMAL_NUMBER  = 10;
   private final String BACKGROUND_PATH = Animal.PICTURE_PATH+"savanna.jpg";
   private final String MEAT_PATH = Animal.PICTURE_PATH+"meat.gif";
   private final int RESOLUTION = 25;
   private ZooFrame frame;
   private EFoodType Food;
   private JPanel p1,p2,p3;
   private JButton[] b_num,b_num2;
   private String[] names = {"Add Animal","Sleep","Wake up","Clear","Food","Info"},
	names2 = {"Decorator","Duplicate","Save state","Restore state","Exit"};
   private ArrayList<Animal> animals;
   private Plant forFood = null;
   private JScrollPane scrollPane;
   private boolean isTableVisible;
   private int totalCount;
   private BufferedImage img, img_m;
   private boolean bgr;
   private ZooObserver controller;
   private ExecutorService pool;
   private static ZooPanel instance;
   private ZooMemento states;

	/**
	 * private ctor for the singleton
	 * @param f
	 * 			the zooframe instance in case of first call to the get instance method
	 */
    private ZooPanel(ZooFrame f)
   {
   		states = new ZooMemento();
	    frame = f;
	    Food = EFoodType.NOTFOOD;
	    totalCount = 0;
	    isTableVisible = false;
	    
	    animals = new ArrayList<Animal>();

	    controller = new ZooObserver();
	    controller.start();

	   pool = Executors.newFixedThreadPool(5);

	   setBackground(new Color(255,255,255));
	    
	    p1=new JPanel();
		p1.setLayout(new GridLayout(1,7,0,0));
		p1.setBackground(new Color(0,150,255));

	   	p2=new JPanel();
	   	p2.setLayout(new GridLayout(1,5,0,0));
	   	p2.setBackground(new Color(0,150,255));

	   	p3 = new JPanel();
	   	p3.setLayout(new GridLayout(2,1,0,0));

		b_num = new JButton[names.length];
	   	b_num2 = new JButton[names2.length];
		for(int i=0;i<names.length;i++)
		{
		    b_num[i]=new JButton(names[i]);
		    b_num[i].addActionListener(this);
		    b_num[i].setBackground(Color.lightGray);
		    p1.add(b_num[i]);		
		}

		for(int i=0;i<names2.length;i++){
			b_num2[i]=new JButton(names2[i]);
			b_num2[i].addActionListener(this);
			b_num2[i].setBackground(Color.lightGray);
			p2.add(b_num2[i]);
		}

		setLayout(new BorderLayout());
		p3.add(p1);
	   	p3.add(p2);

	   	add("South", p3);
		
		img = img_m = null;
		bgr = false;
		try { img = ImageIO.read(new File(BACKGROUND_PATH)); } 
		catch (IOException e) { System.out.println("Cannot load background"); }
		try { img_m = ImageIO.read(new File(MEAT_PATH)); } 
		catch (IOException e) { System.out.println("Cannot load meat"); }
   }

	/**
	 * get the instance of the zoopanel and initlize the instance if its null
	 * @param f
	 * 			the param for the initilize instance
	 * @return the instance of the zoo panel
	 */
   public static ZooPanel getInstance(ZooFrame f){
       if(instance == null)
           instance = new ZooPanel(f);

       return instance;
   }

   @Override
   public void paintComponent(Graphics g)
   {
	   	super.paintComponent(g);	
	   	
	   	if(bgr && (img!=null))
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

	   	if(Food == EFoodType.MEAT)
	   		g.drawImage(img_m, getWidth()/2, getHeight()/2-45, 40, 40, this);
	    
	   	if((Food == EFoodType.VEGETABLE) && (forFood != null))
	   		forFood.drawObject(g);

	   	synchronized(this) {
		   	for(Animal an : animals)
		   	    if(an.isRunning()) {
                    an.drawObject(g);
                }
	   	}
   }

	/**
	 * set the background for the panel
	 * @param num
	 * 			choose the background from 3 option
	 */
	public void setBackgr(int num) {
	   switch(num) {
	   case 0:
		   setBackground(new Color(255,255,255));
		   bgr = false; 
		   break;
	   case 1:
		   setBackground(new Color(0,155,0));
		   bgr = false; 
		   break;
	   default:
			bgr = true;   
	   }
	   repaint();
   }

	/**
	 * check if there is food on the panel
	 * @return
	 */
	synchronized public EFoodType checkFood()
   {
	   return Food;
   }

   /**
    * CallBack function 
    * @param an
    */
   synchronized public void eatFood(Animal an)
   {
	   if(Food != EFoodType.NOTFOOD)
	   {
		    if(Food == EFoodType.VEGETABLE)
		    	forFood = null;
		   	Food = EFoodType.NOTFOOD;
	   		an.eatInc();
	   		totalCount++;
	   		System.out.println("The "+an.getName()+" with "+an.getColor()+" color and size "+an.getSize()+" ate food.");
	   }
	   else
	   {
		   System.out.println("The "+an.getName()+" with "+an.getColor()+" color and size "+an.getSize()+" missed food.");
	   }
   }

	/**
	 * call and manage the addAnimal dialog
	 */
	public void addDialog()
   {
       if(animals.size()==MAX_ANIMAL_NUMBER) {
		   JOptionPane.showMessageDialog(this, "You cannot add more than "+MAX_ANIMAL_NUMBER+" animals");
	   }
	   else {
           String[] opt = {"Carnivore","Herbivore","Omnivore"};

           int res = JOptionPane.showOptionDialog(null,"choose animal type :","animal select",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,
                   null,opt,"ss");

           String parse;
           switch (res) {
               case 0:
                   parse = "Carnivore";
                   break;
               case 1:
                   parse = "Herbivore";
                   break;
               case 2:
                   parse = "Omnivore";
                   break;
               default:
                   return;
           }
		   AddAnimalDialog dial = new AddAnimalDialog(parse);
		   dial.setVisible(true);
	   }
   }

	/**
	 * add animal to the zoo
	 * @param an
	 * 			animal instance
	 */
	public void addAnimal(Animal an)
   {
	   an.addObserver(controller);
	   animals.add(an);
	   an.setTask(pool.submit(an));
   }

	/**
	 * start the animal's observe
	 */
	public void start() {
	    for(Animal an : animals)
	    	an.setResume();
   }

	/**
	 * stop the animals
	 */
	public void stop() {
	    for(Animal an : animals)
	    	an.setSuspend();
   }

	/**
	 * clear the running animals and show the blocked animals
	 */
	synchronized public void clear()
   {
       ArrayList<Animal> temp = new ArrayList<>();
       for (Animal an:animals) {
           if(an.isRunning()){
               an.interrupt();
           }
           else{
               temp.add(an);
           }
       }
       animals.clear();
       animals = temp;
       Food = EFoodType.NOTFOOD;
	   forFood = null;
	   totalCount = 0;
	   repaint();
   }

	/**
	 * make animal eat another animal
	 * @param predator
	 * 			animal that eat
	 * @param prey
	 * 			animal that be eaten
	 */
   synchronized public void preyEating(Animal predator, Animal prey)
   {
	   predator.eatInc();
	   totalCount -= (prey.getEatCount()-1);
   }

	/**
	 * show add food dialog
	 */
	synchronized public void addFood()
   {
	   if(Food == EFoodType.NOTFOOD){
           Object[] options = {"Meat", "Cabbage", "Lettuce"};
           int n = JOptionPane.showOptionDialog(frame,
                   "Please choose food", "Food for animals",
                   JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                   null, options, options[2]);
           switch(n) {
               case 0: // Meat
                   Food = EFoodType.MEAT;
                   break;
               case 1: // Cabbage
                   Food = EFoodType.VEGETABLE;
                   forFood = Cabbage.getInstance();
                   break;
               case 2: // Lettuce
                   Food = EFoodType.VEGETABLE;
                   forFood = Lettuce.getInstance();
                   break;
               default:
                   break;
		   }
	   }
	   else {
		   Food = EFoodType.NOTFOOD;
		   forFood = null;
	   }
	   repaint();
  }

	/**
	 * show the info table
	 */
   public void info()
   {  	 
	   if(isTableVisible == false)
	   {
		  int i=0;
		  int sz = animals.size();

		  String[] columnNames = {"Animal","state","Color","Weight","Hor. speed","Ver. speed","Eat counter"};
	      String [][] data = new String[sz+1][columnNames.length];
		  for(Animal an : animals)
	      {
	    	  data[i][0] = an.getName();
	    	  if(an.isRunning())
	    	  	data[i][1] = "Running";
	    	  else
				  data[i][1] = "Blocked";
	    	  data[i][2] = an.getColor();
	    	  data[i][3] = new Integer((int)(an.getWeight())).toString();
		      data[i][4] = new Integer(an.getHorSpeed()).toString();
		      data[i][5] = new Integer(an.getVerSpeed()).toString();
	    	  data[i][6] = new Integer(an.getEatCount()).toString();
	    	  i++;
	      }
	      data[i][0] = "Total";
	      data[i][6] = new Integer(totalCount).toString();
	      
	      JTable table = new JTable(data, columnNames);
	      scrollPane = new JScrollPane(table);
	      scrollPane.setSize(450,table.getRowHeight()*(sz+1)+24);
	      add( scrollPane, BorderLayout.CENTER );
	      isTableVisible = true;
	   }
	   else
	   {
		   isTableVisible = false;
	   }
	   scrollPane.setVisible(isTableVisible);
       repaint();
   }

	/**
	 * open decorate form with the animals with natural color
	 */
	public void Decorate(){
       ArrayList<String> items = new ArrayList<String>();
       ArrayList<Integer> good_index = new ArrayList<>();
       String temp;
        Animal an;
		int j = 1;
        for (int i=0;i<animals.size();i++) {
             an = animals.get(i);
             if(!an.getColor().equals("Natural")) continue;
            temp = (j++)+".["+ an.getName() + " : running = " + an.isRunning() + ", weight = "
                    + an.getWeight() + ", color = "+an.getColor();
            items.add(temp);
            good_index.add(i);

        }
        if(items.size() == 0){
			JOptionPane.showMessageDialog(this,
					"All animal are colored!");
			return;
		}
       JComboBox<String> cmbx = new JComboBox<String>(items.toArray(new String[]{}));

       JRadioButton blue,red;

        blue = new JRadioButton("Blue");
        red = new JRadioButton("Red");
        ButtonGroup btngrp = new ButtonGroup();
		blue.setSelected(true);
        btngrp.add(blue);
        btngrp.add(red);


       JComponent[] inputs = new JComponent[]{
               cmbx,
               blue,
               red
       };

       int res = JOptionPane.showConfirmDialog(null, inputs,
			   "Decorate an animal", JOptionPane.DEFAULT_OPTION,
			   JOptionPane.PLAIN_MESSAGE, null);

       int selected_indx = cmbx.getSelectedIndex();

		if((res == 0) && (selected_indx >= 0)){
			if(blue.isSelected()){
				temp = "Blue";

			}
			else{
				temp = "Red";
			}

			Animal animal = animals.get(good_index.get(selected_indx));
			if(animal.PaintAnimal(temp)){
				animals.set(good_index.get(selected_indx),animal);
			}
			else{
				System.out.println("the selected animals is already colored");
			}
		}
		else{
			System.out.println("User quit the decorator form or did not enter any animal...");
		}


	}

	/**
	 * Duplicate method  using clone
	 */
	public void Duplicate(){
        if (animals.size() == 10){
            JOptionPane.showMessageDialog(this,
                    "Can't duplicate because there too many animals!");
            return;
        }
        ArrayList<String> items = new ArrayList<String>();
        String temp;
        Animal an;
        items.add("No animal");
        for (int i=0;i<animals.size();i++) {
            an = animals.get(i);
            temp = (i+1)+".["+ an.getName() + " : running = " + an.isRunning() + ", weight = "
                    + an.getWeight() + ", color = "+an.getColor();
            items.add(temp);
        }
        if(items.size() == 1){
            JOptionPane.showMessageDialog(this,
                    "There are no animals to duplicate!");
            return;
        }
        JSlider sl_hor = new JSlider(1,10);
        sl_hor.setMajorTickSpacing(2);
        sl_hor.setMinorTickSpacing(1);
        sl_hor.setPaintTicks(true);
        sl_hor.setPaintLabels(true);
        p1.add(sl_hor);

        JSlider sl_ver = new JSlider(1,10);
        sl_ver.setMajorTickSpacing(2);
        sl_ver.setMinorTickSpacing(1);
        sl_ver.setPaintTicks(true);
        sl_ver.setPaintLabels(true);
        p1.add(sl_ver);

        JLabel horLbl = new JLabel("Horizontal speed:");
        JLabel verLbl = new JLabel("Vertical speed:");

        JComboBox<String> cmbx = new JComboBox<String>(items.toArray(new String[]{}));

        JComponent[] inputs = new JComponent[]{cmbx,horLbl,sl_hor,verLbl,sl_ver};

        int res = JOptionPane.showConfirmDialog(null, inputs,
                "Duplicate an animal", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null);

        int selected_indx = cmbx.getSelectedIndex();

        if((res == 0) && (selected_indx > 0)){
            an = animals.get(selected_indx - 1);
            try{
                an = (Animal)an.clone();
                an.setLocation(new Point(0,0));
                an.setHorSpeed(sl_hor.getValue());
                an.setVerSpeed(sl_ver.getValue());
                addAnimal(an);
            }
            catch (CloneNotSupportedException e) { System.out.println("Cannot duplicate animal"); }
        }
    }

	/**
	 * Save state method using memento
	 */
	public void SaveState() {
		if (states.getAmountOfStates() < 3) {
			Originator originator = new Originator();
			originator.setState(animals, Food, forFood);
			states.addMemento(originator.createMemento());
			JOptionPane.showMessageDialog(frame, "State saved successfully", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(frame, "Can't save more then 3 states", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Delete all animals and food from the panel
	 */
	private void clearAll(){
		for (Animal an:animals) {
			if(an.isRunning()) {
				an.interrupt();
			}
		}
		animals.clear();
		Food = EFoodType.NOTFOOD;
		forFood = null;
		totalCount = 0;
	}

	/**
	 * Restore states from saved states
	 */
	public void RestoreState(){
		if (!states.haveStates()){JOptionPane.showOptionDialog(null, "No states to restore "
				,"Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, new String[] {"Ok"},
				null);
			return;
		}
		String[] labels = new String[states.getAmountOfStates() + 1];
		int i ;
		for (i = 0;i<states.getAmountOfStates();i++){
			labels[i] = "State" + (i+1);
		}
		labels[i] = "Cancel";
		int result = JOptionPane.showOptionDialog(null,"Please choose state for restore",
				"Saved states", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, labels,
				"Metric");
		if (result >= 0 && result < states.getAmountOfStates()){
			Memento tmp = states.getMemento(result);
			if (tmp != null){
				clearAll();
				for (Animal an:tmp.getAnimalState())
					addAnimal(an);
				Food = tmp.getFoodTypeState();
				forFood = tmp.getPlantState();
				repaint();
			}
		}

	}

	/**
	 * Remove the animals that running on the panel.
	 */
   public void destroy()
   { 
	  for(Animal an : animals)
		  an.interrupt();
	  controller.interrupt();
      System.exit(0);
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
	if(e.getSource() == b_num[0]) // "Add Animal"
		addDialog();
	else if(e.getSource() == b_num[1]) // "Sleep"
		stop();
	else if(e.getSource() == b_num[2]) // "Wake up"
		start();
	else if(e.getSource() == b_num[3]) // "Clear"
		clear();
	else if(e.getSource() == b_num[4]) // "Food"
		addFood();
	else if(e.getSource() == b_num[5]) // "Info"
		info();
	//---------------------------------------------------------
	else if(e.getSource() == b_num2[0])  //"Decorate"
		Decorate();
	else if(e.getSource() == b_num2[1]) //"Duplicate"
		Duplicate();
	else if(e.getSource() == b_num2[2]) //"Save State"
		SaveState();
	else if(e.getSource() == b_num2[3]) //"Restore state"
		RestoreState();
	else if(e.getSource() == b_num2[4]) // "Exit"
		destroy();
   }

	/**
	 * check if there are animals that can eat other animals on the panels
	 */
	public synchronized void chckEat(){
	   boolean prey_eaten = false;
	   for(Animal predator : animals) {
	   		if(!predator.isRunning()) continue;
		   for(Animal prey : animals) {
			   if(!prey.isRunning()) continue;
			   if(predator != prey && predator.getDiet().canEat(prey) && predator.getWeight()/prey.getWeight() >= 2 &&
					   (Math.abs(predator.getLocation().getX() - prey.getLocation().getX()) < prey.getSize()) &&
					   (Math.abs(predator.getLocation().getY() - prey.getLocation().getY()) < prey.getSize())) {
				   preyEating(predator,prey);
				   System.out.print("The "+predator+" cought up the "+prey+" ==> ");
				   prey.interrupt();
				   prey.interrupt();
				   animals.remove(prey);
				   repaint();
				   prey_eaten = true;
				   break;
			   }
		   }
		   if(prey_eaten)
			   break;
	   }
   };


}