package lab_tasks;

import java.io.IOException;


import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

/**
 * <p>
 * A program that uses an ultrasonic sensor to avoid obstacles
 * </p>
 *
 * @author Benjamin Lim
 *
 */

public class AvoidObstacles implements RoboFace {
	
	/** The EV3 brick we're controlling */
    private EV3 brick;
    
    /** The motor on the left side of the robot */
    private RegulatedMotor leftMotor;
    
    /** The motor on the right side of the robot */
    private RegulatedMotor rightMotor;
    
    /** Ultrasonic Sensor*/
    private EV3UltrasonicSensor sonicSensor;
    
    /** RangeFinder for Sonic Sensor*/
    private RangeFinderAdapter sonar;
    
    /** Flag for suppress action */
    private boolean suppress = false;
    
    /** State of this behaviour's action */
    private boolean active = false;
    
    //Constructor
    public AvoidObstacles(String sonicPort) throws IOException {

    	super();

    	// permanently store the brick in our instance variable
    	//brick = pBrick;
                
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
    	//leftMotor = lPort;
    	//rightMotor = rPort;
    	
    	//Connect Ultrasonic Sensor
    	sonicSensor = new EV3UltrasonicSensor(Robot.brick.getPort(sonicPort));
    	
    	// Set Ultrasonic Sensor to Distance Mode
    	sonar = new RangeFinderAdapter(sonicSensor);
    }
    
    void move()
    {
    	Robot.leftMotor.forward();
    	Robot.rightMotor.forward();
    }
    
    void avoid() throws IOException
    {    	
    	
    	Robot.leftMotor.stop();
    	Robot.rightMotor.stop();
    	
    	Robot.leftMotor.rotate(180);
    	Robot.rightMotor.rotate(-180);
    	
    	Robot.leftMotor.forward();
    	Robot.rightMotor.forward();
    	
    	Delay.msDelay(1000);
    	
    	Robot.leftMotor.rotate(-180);
    	Robot.rightMotor.rotate(180);
    }

	@Override
	public void action() throws IOException {
		active = true;
		
		if(sonar.getRange() > 0.2)
			move();
		
		avoid();
		
		active = false;
	}

	@Override
	public boolean takeControl() {
		if(sonar.getRange() < 0.2)
			return true;
		else 
			return false;
	}
	
	@Override
	public void suppress() {
		suppress = true;		
	}

	@Override
	public boolean isActive() {
		return active;		
	}	
}


