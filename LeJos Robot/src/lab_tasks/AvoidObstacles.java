package lab_tasks;

import java.io.IOException;


import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
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
    
    /** Pilot for the robot */
    private DifferentialPilot pilot;
    
    /** Ultrasonic Sensor*/
    private EV3UltrasonicSensor sonicSensor;
    
    /** RangeFinder for Sonic Sensor*/
    private RangeFinderAdapter sonar;
    
    //Constructor
    public AvoidObstacles(EV3 pBrick, String lPort, String rPort, String sonicPort) {

    	super();

    	// permanently store the brick in our instance variable
    	brick = pBrick;
                
    	// Establish a fail-safe: pressing Escape quits
    	brick.getKey("Escape").addKeyListener(new KeyListener() {
    		@Override
    		public void keyPressed(Key k) {
    		}

    		@Override
    		public void keyReleased(Key k) {
    			System.exit(0);
    		}
    	});

    	// Connect the motors
    	leftMotor = new EV3LargeRegulatedMotor(brick.getPort(lPort));
    	rightMotor = new EV3LargeRegulatedMotor(brick.getPort(rPort));
    	
    	//Connect Ultrasonic Sensor
    	sonicSensor = new EV3UltrasonicSensor(brick.getPort(sonicPort));
    	
    	// Set Ultrasonicsonic Sensor to Distance Mode
    	sonar = new RangeFinderAdapter(sonicSensor);
    }
    
    void go() throws IOException {    	
    	
    	leftMotor.stop();
    	rightMotor.stop();
    	
    	leftMotor.rotate(180);
    	rightMotor.rotate(-180);
    	
    	leftMotor.forward();
    	rightMotor.forward();
    	
    	Delay.msDelay(1000);
    	
    	leftMotor.rotate(-180);
    	rightMotor.rotate(180);
    }

	@Override
	public void action() throws IOException {
		go();		
	}

	@Override
	public boolean takeControl() {
		if(sonar.getRange() < 0.2)
			return true;
		else 
			return false;
	}

	@Override
	public void suppressed() {
		
	}
}


