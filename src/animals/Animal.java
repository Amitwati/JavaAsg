package animals;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;

import com.sun.org.apache.xpath.internal.operations.Bool;
import diet.IDiet;
import food.EFoodType;
import food.IEdible;
import graphics.IAnimalBehavior;
import graphics.IDrawable;
import graphics.ZooPanel;
import mobility.Mobile;
import mobility.Point;

public abstract class Animal extends Mobile implements ColoredAnimalDecorator,IEdible,IDrawable,IAnimalBehavior,Runnable {

	int aviv_homo_ss;

	protected final int EAT_DISTANCE = 5;
	private IDiet diet;
	protected String name;
	private double weight;
	boolean isRun;
	protected int size;
	protected String col;
	protected int horSpeed;
	protected int verSpeed;
	protected boolean coordChanged;
	protected Future<?> task;
	protected int x_dir;
	protected int y_dir;
	protected int eatCount;
	protected ZooPanel pan;
	protected boolean threadSuspended;	 
	protected BufferedImage img1, img2;
	protected int cor_w, cor_h;
	
	
	public Animal(String nm, int sz, int w, int hor, int ver, String c, ZooPanel p) {
		super(new Point(0,0));
        System.out.println("animal");
        name = new String(nm);
		size = sz;
		weight = w;
		isRun = false;
		horSpeed = hor;
		verSpeed = ver;
		col = c;
		pan = p;
		x_dir = 1;
		y_dir = 1;
        cor_w = cor_h = size;
		coordChanged = false;

	}	
	
	public EFoodType getFoodtype() { return EFoodType.MEAT;	}	
	public IDiet getDiet() { return diet; }
	public String getName() { return this.name;	}
	public double getWeight() {	return this.weight;	}
	public void setWeight(double w) { weight = w; }
	protected void setDiet(IDiet diet) { this.diet = diet;}
	public int getSize() { return size; }
	public int getHorSpeed() { return horSpeed; }
	public void setHorSpeed(int hor) { horSpeed  = hor; }
	public int getVerSpeed() { return verSpeed; }
	public void setVerSpeed(int ver) { verSpeed  = ver; }
	public void eatInc() { eatCount++; }
	public int getEatCount() { return eatCount; }
	synchronized public void setSuspend() { threadSuspended = true; }
	synchronized public void setResume() { threadSuspended = false; notify(); }
	synchronized public boolean getChanges(){ return coordChanged; }
	synchronized public void setChanges(boolean state){ coordChanged = state; }	 
	public String getColor() { return col; }
	public boolean isRunning() { return isRun; }

	@Override
	public boolean PaintAnimal(String col) {
	    String nm = "";
		if(!this.col.equals("Natural"))
			return false;
        this.col = col;

        if(this instanceof Lion){
            nm = "lio";
        }
        if(this instanceof Bear){
            nm = "bea";
        }
        if(this instanceof Giraffe){
            nm = "grf";
        }
        if(this instanceof Elephant){
            nm = "elf";
        }
        if(this instanceof Turtle){
            nm = "trt";
        }

        loadImages(nm);
		return true;

	}

	public void interrupt() {
		isRun = false;
		task.cancel(true); // to remove thread of animal from Threadpool
	}

	public void setTask(Future<?> tsk) {
		this.task = tsk;
	}

	public void loadImages(String nm){
		 switch(getColor()){
			 case "Red":
				 try { img1 = ImageIO.read(new File(PICTURE_PATH + nm + "_r_1.png"));
				 	   img2 = ImageIO.read(new File(PICTURE_PATH + nm + "_r_2.png"));} 
				 catch (IOException e) { System.out.println("Cannot load picture"); }
				 break;
			 case "Blue":
				 try { img1 = ImageIO.read(new File(PICTURE_PATH + nm + "_b_1.png"));
				 	   img2 = ImageIO.read(new File(PICTURE_PATH + nm + "_b_2.png"));} 
				 catch (IOException e) { System.out.println("Cannot load picture"); }
				 break;
			 default:
				 try { img1 = ImageIO.read(new File(PICTURE_PATH + nm + "_n_1.png"));
			 	       img2 = ImageIO.read(new File(PICTURE_PATH + nm + "_n_2.png"));} 
				 catch (IOException e) { System.out.println("Cannot load picture"); }			 
		 }
	}

    public void run() 
    {
    	isRun = true;
       while (isRun)
       {
           try 
           {
               Thread.sleep(50);
               synchronized(this) {
                   while (threadSuspended)
   						wait();
   				}  
          } 
           catch (InterruptedException e) 
           {
           	System.out.println(getName()+ " dead...");
           	return;
           }
                      
           if(this.getDiet().canEat(pan.checkFood()))
           {
           		double oldSpead = Math.sqrt(horSpeed*horSpeed+verSpeed*verSpeed);
           		double newHorSpeed = oldSpead*(location.getX() - pan.getWidth()/2)/
           				   (Math.sqrt(Math.pow(location.getX() - pan.getWidth()/2,2)+
           						      Math.pow(location.getY() - pan.getHeight()/2 + 45,2)));
           		double newVerSpeed = oldSpead*(location.getY() - pan.getHeight()/2 + 45)/
           				   (Math.sqrt(Math.pow(location.getX() - pan.getWidth()/2,2)+
           						      Math.pow(location.getY() - pan.getHeight()/2 + 45,2)));
              	int v = 1;
                if(newVerSpeed<0) { v=-1; newVerSpeed = -newVerSpeed; }
              	if(newVerSpeed > 10)
              		newVerSpeed = 10;
              	else if(newVerSpeed < 1) {
              	   if(location.getY() != pan.getHeight()/2 + 45)
              		newVerSpeed = 1;   
              	   else
              		newVerSpeed = 0;  
              	}
              	int h = 1;
                if(newHorSpeed<0) { h=-1; newHorSpeed = -newHorSpeed; }
              	if(newHorSpeed > 10)
              		newHorSpeed = 10;
              	else if(newHorSpeed < 1) {
              	   if(location.getX() != pan.getWidth()/2)
              		newHorSpeed = 1;   
              	   else
              		newHorSpeed = 0;  
              	}
               	location.setX((int)(location.getX() - newHorSpeed*h));
               	location.setY((int)(location.getY() - newVerSpeed*v));
              	if(location.getX()<pan.getWidth()/2)
              		x_dir = 1;
              	else
              		x_dir = -1;
              	if((Math.abs(location.getX()-pan.getWidth()/2)<EAT_DISTANCE) && 
              	   (Math.abs(location.getY()-pan.getHeight()/2 + 45)<EAT_DISTANCE))
              	{
              		pan.eatFood(this);
              	}
           }
           else
           {
			    location.setX(location.getX() + horSpeed*x_dir);
			    location.setY(location.getY() + verSpeed*y_dir);
           }

		    if(location.getX() + cor_w + 10  > pan.getWidth()) {
                x_dir = -1;
            }
		    else if(location.getX() < 0) {
		    	x_dir = 1;
		    }

		    if(location.getY() + cor_h + 50 > (pan.getHeight())) {
		    	y_dir = -1;
		    }
		    else if(location.getY() < 0) {
                y_dir = 1;
		    }
 		    setChanges(true);
      }
   }
 
    public void drawObject(Graphics g)
    {
 		if(x_dir==1) // an animal goes to right side
 		{
 			g.drawImage(img1, location.getX(), location.getY(), cor_w, cor_h, pan);
 		}
 		else // an animal goes to left side
 		{
 			g.drawImage(img2, location.getX(), location.getY(), cor_w, cor_h, pan);
 		}
 		
    }

    public String toString(){
    	return "["+getName() + ": weight=" + weight + ", color="+col+"]";
    }
}
