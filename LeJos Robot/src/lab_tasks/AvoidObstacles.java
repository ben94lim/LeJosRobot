package lab_tasks;

import java.io.IOException;


import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
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
    private SampleProvider distance = Robot.sonicSensor.getDistanceMode();
    
    /** RangeFinder for Sonic Sensor*/
    private RangeFinderAdapter sonar;
    
    /** Flag for suppress action */
    private boolean suppress = false;
    
    /** State of this behaviour's action */
    private boolean active = false;
    
    //Constructor
    public AvoidObstacles() throws IOException {

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
    	
    	
    	// Set Ultrasonic Sensor to Distance Mode
    	//sonar = new RangeFinderAdapter(sonicSensor);   	


    	 	
    }
    
    // get an instance of this sensor in measurement mode
	//SampleProvider distance = sonicSensor.getMode("Distance");   
    
	// initialize an array of floats for fetching samples. 
	// Ask the SampleProvider how long the array should be
	float[] sample = new float[distance.sampleSize()];
    
    void move()
    {
    	Robot.leftMotor.forward();
    	Robot.rightMotor.forward();
    }
    
    void avoid() throws IOException
    {    	
    	System.out.printf("Object detected");
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
		
		/*distance.fetchSample(sample, 0);
		if(sample[0] > 0.2)
			move();
		else
			avoid();*/
		
		active = false;
	}

	@Override
	public boolean takeControl() {
		distance.fetchSample(sample, 0);
		//System.out.printf("Distance: %d\n", (int)sample[0]);
		if(sample[0] < 0.2)
			return false;
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


