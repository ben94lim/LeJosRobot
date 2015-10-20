package lab_tasks;

import java.io.IOException;
import java.util.Random;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.device.NXTMMX;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.video.Video;
import lejos.robotics.EncoderMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * <p>
 * A program that uses a webcam to follow light.
 * </p>
 *
 * @author Benjamin Lim
 *
 */

public class FollowLight {	
	
	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	
	// Frames and motion maps
	private static byte [][] luminanceFrame = new byte[HEIGHT][WIDTH];
	private static int threshold = 70;
	
	/** The EV3 brick we're controlling */
    private EV3 brick;
	
	// Motors
	private static RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);

	private static RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);

	// Light features
	private static LightFeatures aLightFeat = new LightFeatures();
	
	private static double oldMeanLight, newMeanLight;
	
	private static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);
   
	private static SampleProvider getLight = lightSensor.getAmbientMode();
   
    private static float[] lightSample = new float[getLight.sampleSize()];
    

       

    

	
	/*// Randomness
	//private static Random randGenerator = new Random();
	private static int randDegLeft, randDegRight;
	private static int randMotor; // 0 = left; 1 = right
*/	 
    public FollowLight() {
    	
    	// permanently store the brick in our instance variable
    	brick = (EV3) BrickFinder.getLocal();;
    	
    	/*// Establish a fail-safe: pressing Escape quits
    	brick.getKey("Escape").addKeyListener(new KeyListener() {
    		@Override
    		public void keyPressed(Key k) {
    		}

    		@Override
    		public void keyReleased(Key k) {
    			System.exit(0);
    		}
    	});*/
		// Various initializations
		//randDegLeft = 0;
		//randDegRight = 0;
		// Initialize luminance frame
		for (int x=0; x<WIDTH; x += 1) {
			for (int y=0; y<HEIGHT; y += 1) {
				luminanceFrame[y][x] = 0;
			}
		}
	}


	public static void main(String[] args) throws IOException {
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		Video video = ev3.getVideo();
		video.open(WIDTH, HEIGHT);
		byte[] frame = video.createFrame();
		
		double mot_amplif_larger = 1.0*0.6;
		double mot_amplif_smaller = 1.0*0.3;
		double left_field = 0;
		double right_field = 0;
		
		// Grab frame
		video.grabFrame(frame);
		
		// Extract luminanceFrame
		extractLuminanceValues(frame);
		
	 	// Compute light features
		aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
	 	 
		while(Button.ESCAPE.isUp()) {
	     	
			// --- Get webcam information
			
			// Grab frame
			video.grabFrame(frame);
			
			// Extract luminanceFrame
			extractLuminanceValues(frame);
			
			// Compute light features
			aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
			
			getLight.fetchSample(lightSample, 0);
			
			int i = 0;
			while(lightSample[0]>0.4 && Button.ESCAPE.isUp())
			{				
				leftMotor.stop();
				rightMotor.stop();
				getLight.fetchSample(lightSample, 0);
				
				
				System.out.printf("%d. Bright!%n", i++);
				
				// Grab frame
				video.grabFrame(frame);
				
				// Extract luminanceFrame
				extractLuminanceValues(frame);
				
			 	// Compute light features
				aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
				
				
				right_field = (aLightFeat.meanRight/255)*180;
		     	left_field = (aLightFeat.meanLeft/255)*180;
		     	
				if ((right_field - left_field) > 30)
				{
					System.out.printf("Right");
					rightMotor.setSpeed(50); 
			     	leftMotor.setSpeed(50); 
		     		leftMotor.rotate(90);
		     		rightMotor.rotate(-45);
				}
		     	if ((left_field - right_field) > 30)
		     	{
		     		System.out.printf("Left");
		     		rightMotor.setSpeed(50); 
			     	leftMotor.setSpeed(50); 
		     		rightMotor.rotate(90);
		     		leftMotor.rotate(-45);
		     	}
			}
		     	
			
	     	// Display
	     	//System.out.println("Mean right: " + aLightFeat.meanRight);
	     	//System.out.println("Mean left: " + aLightFeat.meanLeft);
	     	//dispFrame();
	     	
	     	// Regulated motors
	     	//motorB.rotate((int) (mot_amplif*(aLightFeat.meanRight/255)*180), true); // true is for immediate return -> to parallelize motors
	     	//motorC.rotate((int) (mot_amplif*(aLightFeat.meanLeft/255)*180));
	     	// Unregulated motors
	     	
	     	right_field = (aLightFeat.meanRight/255)*180;
	     	left_field = (aLightFeat.meanLeft/255)*180;
	     	
	     	
	     	if ((right_field - left_field) > 20) {
	     		System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
	     		right_field = right_field * mot_amplif_larger;
	     		left_field = left_field * mot_amplif_smaller;
	     		
	     		//right_field = ;
	     	} else if ((left_field - right_field) > 20){
	     		System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
	     		left_field = right_field * mot_amplif_larger;
	     		right_field = left_field * mot_amplif_smaller;
	     	}
	     	
	     	// Go straight
	     	else
	     	{
	     		System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
	     		left_field = right_field * mot_amplif_smaller;
	     		right_field = left_field;
	     	}
	     	
	     	
	     	float maxSpeed = 5;//rightMotor.getMaxSpeed();
	     	rightMotor.forward();
	     	leftMotor.forward();
	     	System.out.printf("L Speed:%d   R Speed:%d%n",(int)right_field, (int)left_field);
	     	rightMotor.setSpeed((int)(left_field)*(int)(maxSpeed)); 
	     	leftMotor.setSpeed((int)(right_field)*(int)(maxSpeed)); 
	     	
	     	
	     	/**
	     	// Compute a random move
	     	randDegLeft = randGenerator.nextInt(181);
	     	randDegRight = randGenerator.nextInt(181);  	
	     	// Make move
	     	motorB.rotate(randDegRight, true); // true is for immediate return -> to parallelize motors
	     	motorC.rotate(randDegLeft);
	     	// Grab frame
	     	video.grabFrame(frame);
	     	// Extract luminanceFrame
	     	extractLuminanceValues(frame);
	     	// Compute light features
	     	aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
	     	newMeanLight = aLightFeat.meanTot;
	     	// Display mean light
	     	System.out.println("Mean light: " + newMeanLight);
	     	// If mean light has decreased, backtrack
	     	if (newMeanLight < oldMeanLight) {
	     		motorB.rotate(-randDegRight, true); // true is for immediate return -> to parallelize motors
	         	motorC.rotate(-randDegLeft);
	     	} else {
	     		oldMeanLight = newMeanLight;
	     	}
	     	**/
	     	        	        
		}
		video.close();
		lightSensor.close();
		leftMotor.close();
		rightMotor.close();		
	} 
	
 
	// DO: Improve this possibly by combining with chrominance values.
	public static void extractLuminanceValues(byte [] frame) {
		int x,y;
		int doubleWidth = 2*WIDTH; // y1: pos 0; u: pos 1; y2: pos 2; v: pos 3.
		int frameLength = frame.length;
		for(int i=0;i<frameLength;i+=2) {
			x = (i / 2) % WIDTH;
			y = i / doubleWidth;
			luminanceFrame[y][x] = frame[i];
		}
	}
 
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


