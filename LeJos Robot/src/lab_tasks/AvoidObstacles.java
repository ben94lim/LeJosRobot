package lab_tasks;

import java.io.IOException;

import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;

public class AvoidObstacles {
	
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
    
    /** Sample Provider for Sonic Sensor*/
    SampleProvider sonar = sonicSensor.getDistanceMode();
    
    /** Float to store sample from Sonic Sensor*/
    float[] sample = new float[sonicSensor.sampleSize()];
    
	public static void main(String[] args) throws IOException {
		AvoidObstacles robot = new AvoidObstacles(
    			LocalEV3.get(), // brick
    			"B",            // left motor port
    			"C",            // right motor port
    			"S4"			//Ultrasonic Sensor Port
    			);
        
    	robot.go(); // start it running
    }

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
    	
    	// Set pilot
    	pilot = new DifferentialPilot(56, 118, leftMotor, rightMotor);
    	
    	//Connect Ultrasonic Sensor
    	sonicSensor = new EV3UltrasonicSensor(brick.getPort(sonicPort));

    }
    
    private void go() throws IOException {
    	    	
    	sonar.fetchSample(sample, 0);
    	
    	pilot.forward();
    	
    	while (sample[0] > 0.2) {
    		Thread.yield();    		
    	}
    	
    	pilot.stop();   	
    }
}


