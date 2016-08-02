package lab_tasks;

import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;
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
public class FollowMotion implements RoboFace{
	

    /** The EV3 brick we're controlling */
    private static EV3 brick;

    /** The motor on the left side of the robot */
    private RegulatedMotor leftMotor;

    /** The motor on the right side of the robot */
    private RegulatedMotor rightMotor;
    
    
    
    /** Width and height of frame*/
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    
    private static int [][] luminanceFrame = new int[HEIGHT][WIDTH];
    private static int threshold = 70;
    private static MotionMap aMotMap = new MotionMap();
    
    /** AvoidObstacles Interface */
    RoboFace avoid;
    
    /** Flag for suppress action */
    private boolean suppress = false;
    
    /** State of this behaviour's action */
    private boolean active = false;

    //Constructor
    public FollowMotion(RoboFace avoidBehaviour) {

    	super();
                
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
    	
    	avoid = avoidBehaviour;
    }

    void go() throws IOException {
    	
    	// While not suppressed, chase movement

    	chase();
    	
    }
    
    private void chase() throws IOException {
        	
    	
    	byte[] frame = Robot.webcam.createFrame();
             
    	//while(!suppress) {
    		Robot.webcam.grabFrame(frame);
    		
    		// Create a frame of luminance values
    		extractLuminanceValues(frame);
    		
    		// Motion processing
    		aMotMap.addFrame(luminanceFrame);
    		aMotMap.compMotion();
    		aMotMap.compLeftRight();
    		
    		if(suppress)
    			return;
    		
    		int baseSpeed = 150;
    		// If motion is detected move forward
    		if(aMotMap.isMotion()){
    			Robot.leftMotor.forward();    			
    			Robot.leftMotor.setSpeed(baseSpeed);
    			Robot.rightMotor.forward();
    			Robot.rightMotor.setSpeed(baseSpeed);
    		} 
    		
    		// Else Stop
    		else {
    			Robot.leftMotor.stop();
    			Robot.rightMotor.stop();
    		}
    		
    		if(suppress)
    			return;
    		
    		// If left motion is higher than right motion then turn left
    		if(aMotMap.leftMotion > aMotMap.rightMotion && (aMotMap.leftMotion - aMotMap.rightMotion) > 4)
    		{
    			Robot.rightMotor.setSpeed(baseSpeed+50);
    			Robot.leftMotor.setSpeed(baseSpeed);
    			//Delay.msDelay(2000);
    		}
    		
    		// If right motion is higher than left motion then turn right
    		else if(aMotMap.rightMotion > aMotMap.leftMotion && (aMotMap.rightMotion - aMotMap.leftMotion) > 4)
    		{
    			Robot.leftMotor.setSpeed(baseSpeed+50);
    			Robot.rightMotor.setSpeed(baseSpeed);
    		}
    		
    		// Display left and right motion values and motor speed
    		System.out.printf("L:%d R:%d LM:%d RM:%d %n",(int)aMotMap.leftMotion, (int)aMotMap.rightMotion, (int)Robot.leftMotor.getSpeed(), (int)Robot.rightMotor.getSpeed());
                
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
    	//}
    	
    	
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

	@Override
	public void action() throws IOException {
		// Set suppressed state to false before performing action
		active = true;
		
		go();
		
		// Return suppressed state to true
		active = false;		
	}

	@Override
	public boolean takeControl() {
		if(avoid.takeControl())
			return false;
		
		else	
			return true;
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