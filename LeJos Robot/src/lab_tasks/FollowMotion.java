package lab_tasks;

import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.video.Video;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;


/**
 * <p>
 * A program that uses a webcam to follow motion.
 * </p>
 *
 * @author Benjamin Lim
 *
 */
public class FollowMotion {
	

    /** The EV3 brick we're controlling */
    private EV3 brick;

    /** The motor on the left side of the robot */
    private RegulatedMotor leftMotor;

    /** The motor on the right side of the robot */
    private RegulatedMotor rightMotor;
    /** Pilot for the robot */
    private DifferentialPilot pilot;

    /** The raw EV3 Colour Sensor object */
    private EV3ColorSensor colorSensor;
    
    /** Video input from the webcam */
    private static Video webcam;
    
    /** Width and height of frame*/
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    
    private static int [][] luminanceFrame = new int[HEIGHT][WIDTH];
    private static int threshold = 70;
    private static MotionMap aMotMap = new MotionMap();

    //Constructor
    public FollowMotion(EV3 pBrick, String lPort, String rPort) {

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
    	
    	// Connect to webcam
    	webcam = brick.getVideo();
    	
    	// Set pilot
    	pilot = new DifferentialPilot(56, 118, leftMotor, rightMotor);

    }

    void go() throws IOException {
    	
    	// Displays introduction
    	intro();
    	
    	while (true) {
    		chase();
    	}
    }

    private void intro() {
    	// Clear the screen
    	brick.getTextLCD().clear();

    	brick.getTextLCD().drawString("Calibrate:", 0, 0);
    	brick.getTextLCD().drawString("Place the robot", 0, 1);
    	brick.getTextLCD().drawString("on or near the", 0, 2);
    	brick.getTextLCD().drawString("line start.", 0, 3);
    	brick.getTextLCD().drawString("Then press the", 0, 5);
    	brick.getTextLCD().drawString("Enter key", 0, 6);

    	brick.getKey("Enter").waitForPressAndRelease();
    }
    
    private void chase() throws IOException {
        	
    	webcam.open(WIDTH, HEIGHT);
    	byte[] frame = webcam.createFrame();
             
    	while(Button.ESCAPE.isUp()) {
    		webcam.grabFrame(frame);
    		// y1: pos 0; u: pos 1; y2: pos 2; v: pos 3.
    		
    		// Create a frame of luminance values
    		extractLuminanceValues(frame);
    		
    		// Motion processing
    		aMotMap.addFrame(luminanceFrame);
    		aMotMap.compMotion();
    		aMotMap.compLeftRight();
    		
    		int baseSpeed = 200;
    		
    		// If motion is detected move forward
    		if(aMotMap.isMotion()){
    			leftMotor.forward();    			
				leftMotor.setSpeed(baseSpeed);
				rightMotor.forward();
				rightMotor.setSpeed(baseSpeed);
    		} 
    		
    		else {
    			leftMotor.stop();
    			rightMotor.stop();
    		}
    		
    		// If left motion is higher than right motion then turn left
    		if(aMotMap.leftMotion > aMotMap.rightMotion && aMotMap.leftMotion > 20)
    		{
    			rightMotor.setSpeed(baseSpeed+50);
    			leftMotor.setSpeed(baseSpeed);
    		}
    		
    		// If right motion is higher than left motion then turn right
    		else if(aMotMap.rightMotion > aMotMap.leftMotion && aMotMap.rightMotion > 20)
    		{
    			leftMotor.setSpeed(baseSpeed+50);
    			rightMotor.setSpeed(baseSpeed);
    		}
    		
    		// System.out.println("Max motion: " + aMotMap.compMaxMotion());
    		
    		// Display left and right motion values and motor speed
    		System.out.printf("L:%d R:%d LM:%d RM:%d %n",(int)aMotMap.leftMotion, (int)aMotMap.rightMotion, (int)leftMotor.getSpeed(), (int)rightMotor.getSpeed());
                
    		// Display the frame or motion
    		//dispFrame();
    		//dispMotion();
                
    		// Adjust threshold?
    		if (Button.UP.isDown()) {
    			threshold +=5;
    			if (threshold > 255)
    				threshold = 255;
    		}
    		else if (Button.DOWN.isDown()) { 
    			threshold -=5;
    			if (threshold < 0)
    				threshold = 0;
    		}
    	}
    	
    	// Close motor ports and webcam port
    	webcam.close();
    	leftMotor.close();
    	rightMotor.close(); 	
    }        
        
    public static void extractLuminanceValues(byte [] frame) {
    	int x,y;
    	int doubleWidth = 2*WIDTH; // y1: pos 0; u: pos 1; y2: pos 2; v: pos 3.
    	int frameLength = frame.length;
    	for(int i=0;i<frameLength;i+=2) {
    		x = (i / 2) % WIDTH;
    		y = i / doubleWidth;
    		luminanceFrame[y][x] = frame[i] & 0xFF;   		
    	}
    }
    
    // Display on the LCD the input from the webcam
    public static void dispFrame() {
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<WIDTH; x++) {
    			if (luminanceFrame[y][x] <= threshold) {
    				LCD.setPixel(x, y, 1);
    			}
    			else {
    				LCD.setPixel(x, y, 0);
    			}	
    		}
    	}
        	
    }
}