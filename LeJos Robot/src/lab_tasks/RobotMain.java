package lab_tasks;

import java.io.IOException;

import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class RobotMain {
	
	/** The EV3 brick we're controlling */
    //private static EV3 brick = LocalEV3.get();;
    
    /** The motor on the left side of the robot */
    //static RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));

    /** The motor on the right side of the robot */
   //static RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));
    
	
	//Constructor
    public RobotMain() {

    	// permanently store the brick in our instance variable
    	Robot.brick = LocalEV3.get();
                
    	// Establish a fail-safe: pressing Escape quits
    	Robot.brick.getKey("Escape").addKeyListener(new KeyListener() {
    		@Override
    		public void keyPressed(Key k) {
    		}

    		@Override
    		public void keyReleased(Key k) {
    			System.exit(0);
    		}
    	});
    	
    	// Connect the motors
    	//leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));
    	//rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));
    }
	
	public static void main(String[] args) throws IOException {
		
		// Initialise Robot
		Robot robot = new Robot();
		
		// Initialise AvoidObstacles Class
		RoboFace avoid = new AvoidObstacles();
		
		// Initialise FollowMotion Class
		RoboFace followMotion = new FollowMotion(avoid);
		
		//Array to store behaviours
		RoboFace[] behaviours = {avoid, followMotion};
		
		// Initialise Controller
		Controller mastermind = new Controller(behaviours);
		
		mastermind.start();
	}

}
