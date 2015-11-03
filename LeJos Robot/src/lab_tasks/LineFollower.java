package lab_tasks;

import java.io.IOException;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.video.Video;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

/**
 * <p>
 * A program that uses a webcam to follow light.
 * </p>
 *
 * @author Benjamin Lim
 *
 */

public class LineFollower {	

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
	private static LineLightFeatures aLightFeat = new LineLightFeatures();

	private static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);

	private static SampleProvider getLight = lightSensor.getAmbientMode();

	private static float[] lightSample = new float[getLight.sampleSize()];


	public LineFollower() {

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

		double mot_amplif_larger = 0.3;
		double mot_amplif_smaller = 0.3;
		double left_field = 0;
		double right_field = 0;
		

		while(Button.ESCAPE.isUp()) {

			// --- Get webcam information
			checkJunction(video, frame);

			getLight.fetchSample(lightSample, 0);

			int i = 0;
			while(lightSample[0]>100 && Button.ESCAPE.isUp())
			{				
				leftMotor.stop();
				rightMotor.stop();
				getLight.fetchSample(lightSample, 0);

				if(checkTarget(video, frame))
					break;

				System.out.printf("%d. Bright!%n", i++);

				computeMidFrame(video, frame);				

				right_field = (aLightFeat.meanRight/255)*180;
				left_field = (aLightFeat.meanLeft/255)*180;

				if ((right_field - left_field) > 5)
				{
					System.out.printf("Right");
					rightMotor.setSpeed(400); 
					leftMotor.setSpeed(400); 
					leftMotor.rotate(-100);
					rightMotor.rotate(-250);
				}
				else if ((left_field - right_field) > 5)
				{
					System.out.printf("Left");
					rightMotor.setSpeed(400); 
					leftMotor.setSpeed(400);
					rightMotor.rotate(-100);
					leftMotor.rotate(-250);
					
				}
				
				else
				{
					System.out.printf("Back");
					rightMotor.setSpeed(300); 
					leftMotor.setSpeed(300);
					rightMotor.rotate(-100);
					leftMotor.rotate(-100);
				}
			}


			// Display
			//System.out.println("Mean right: " + aLightFeat.meanRight);
			//System.out.println("Mean left: " + aLightFeat.meanLeft);
			//dispFrame();
			computeMidFrame(video, frame);
			right_field = ((aLightFeat.meanMidRight)/255)*180;
			left_field = ((aLightFeat.meanMidLeft)/255)*180;
			//System.out.printf("L: %d R:%d%n", (int)left_field, (int)right_field);
			
			// Tend left
			//if ((left_field) < 60) {
			if((left_field-right_field)>20)
			{
				//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				rightMotor.setSpeed(200); 
				leftMotor.setSpeed(150);
				//right_field = right_field * mot_amplif_larger;
				//left_field = left_field * mot_amplif_smaller;
			}
			
			// Tend right
			//else if ((left_field-right_field) > 20 || right_field > 70 )
			else if((right_field-left_field)>20)
			{
				//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				rightMotor.setSpeed(150); 
				leftMotor.setSpeed(200);
				//left_field = right_field * mot_amplif_larger;
				//right_field = left_field * mot_amplif_smaller;
			}

			// Go straight
			else
			{
				//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				rightMotor.setSpeed(150); 
				leftMotor.setSpeed(150);
				//left_field = right_field * mot_amplif_smaller;
				//right_field = left_field;
			}


			float maxSpeed = 6;//rightMotor.getMaxSpeed();
			rightMotor.forward();
			leftMotor.forward();
			//System.out.printf("Spd:%d  Spd:%d%n",(int)(right_field)*(int)(maxSpeed), (int)(left_field)*(int)(maxSpeed));
			//rightMotor.setSpeed((int)(left_field)*(int)(maxSpeed)); 
			//leftMotor.setSpeed((int)(right_field)*(int)(maxSpeed)); 

		}
		video.close();
		lightSensor.close();
		leftMotor.close();
		rightMotor.close();		
	} 
	static boolean detectEdge()
	{
		if((aLightFeat.meanTopLeft + aLightFeat.meanTopRight) > 100)
		{
			rightMotor.stop(); 
			leftMotor.stop();
			rightMotor.rotate(360);
			return true;
		}
		return false;
		
	}
	static boolean checkTarget(Video video, byte[] frame) throws IOException
	{
		computeMidFrame(video, frame);
		
		double right_field = (aLightFeat.meanRight/255)*180;
		double left_field = (aLightFeat.meanLeft/255)*180;
		double frame1 = right_field + left_field;

		Delay.msDelay(1000);

		computeMidFrame(video, frame);

		right_field = (aLightFeat.meanRight/255)*180;
		left_field = (aLightFeat.meanLeft/255)*180;

		double result = right_field + left_field - frame1;
		System.out.printf("L: %d R:%d%n", (int)left_field, (int)right_field);
		System.out.printf("Wall %d%n", (int)Math.abs(result));

		if(Math.abs(result) < 0.0001)
		{
			System.out.printf("Wall %d%n", (int)result);
			rightMotor.setSpeed(300); 
			leftMotor.setSpeed(300);
			rightMotor.rotate(720);
			rightMotor.forward();
			leftMotor.forward();
			Delay.msDelay(1000);
			return true;
		}
		
		return false;
	}
	
	static void checkJunction(Video video, byte[] frame) throws IOException
	{
		computeQuarter(video, frame);

		double top_right = (aLightFeat.meanTopRight/255)*180;
		double top_left = (aLightFeat.meanTopLeft/255)*180;
		double btm_right = (aLightFeat.meanBtmRight/255)*180;
		double btm_left = (aLightFeat.meanBtmLeft/255)*180;
		double top_field = top_left + top_right;

		System.out.printf("TL: %d TR:%d%n", (int)top_left, (int)top_right);
		System.out.printf("BL: %d BR:%d%n%n", (int)btm_left, (int)btm_right);
		
		if(top_field<70)
			rightMotor.rotate(360);
		
		if((top_right-btm_right)>90)
		{
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.rotate(360);
		}

	}

	static void computeMidFrame(Video video, byte[] frame) throws IOException
	{
		// Grab frame
		video.grabFrame(frame);

		// Extract luminanceFrame
		extractLuminanceValues(frame);

		// Compute light features
		aLightFeat.compMidLeftRight(luminanceFrame, HEIGHT, WIDTH);
	}

	static void computeQuarter(Video video, byte[] frame) throws IOException
	{
		// Grab frame
		video.grabFrame(frame);

		// Extract luminanceFrame
		extractLuminanceValues(frame);

		// Compute light features
		aLightFeat.compQuarter(luminanceFrame, HEIGHT, WIDTH);
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


