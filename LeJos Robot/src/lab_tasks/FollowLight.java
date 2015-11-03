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

	private static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);

	private static SampleProvider getLight = lightSensor.getAmbientMode();

	private static float[] lightSample = new float[getLight.sampleSize()];



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

		computeNextFrame(video, frame);

		while(Button.ESCAPE.isUp()) {

			// --- Get webcam information

			computeNextFrame(video, frame);

			getLight.fetchSample(lightSample, 0);

			int i = 0;
			while(lightSample[0]>0.3 && Button.ESCAPE.isUp())
			{				
				leftMotor.stop();
				rightMotor.stop();
				getLight.fetchSample(lightSample, 0);

				if(checkWall(video, frame))
					break;

				System.out.printf("%d. Bright!%n", i++);

				computeNextFrame(video, frame);				

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

			// Regulated motors
			//motorB.rotate((int) (mot_amplif*(aLightFeat.meanRight/255)*180), true); // true is for immediate return -> to parallelize motors
			//motorC.rotate((int) (mot_amplif*(aLightFeat.meanLeft/255)*180));
			// Unregulated motors

			right_field = (aLightFeat.meanRight/255)*180;
			left_field = (aLightFeat.meanLeft/255)*180;
			System.out.printf("L: %d R:%d%n", (int)left_field, (int)right_field);

			if ((right_field - left_field) > 10) {
				System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				right_field = right_field * mot_amplif_larger;
				left_field = left_field * mot_amplif_smaller;

				//right_field = ;
			} else if ((left_field - right_field) > 10){
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


			float maxSpeed = 6;//rightMotor.getMaxSpeed();
			rightMotor.forward();
			leftMotor.forward();
			System.out.printf("Spd:%d  Spd:%d%n",(int)(right_field)*(int)(maxSpeed), (int)(left_field)*(int)(maxSpeed));
			rightMotor.setSpeed((int)(left_field)*(int)(maxSpeed)); 
			leftMotor.setSpeed((int)(right_field)*(int)(maxSpeed)); 
			
		}
		video.close();
		lightSensor.close();
		leftMotor.close();
		rightMotor.close();		
	} 

	static boolean checkWall(Video video, byte[] frame) throws IOException
	{
		computeNextFrame(video, frame);

		double right_field = (aLightFeat.meanRight/255)*180;
		double left_field = (aLightFeat.meanLeft/255)*180;
		double frame1 = right_field + left_field;

		Delay.msDelay(1000);

		computeNextFrame(video, frame);

		right_field = (aLightFeat.meanRight/255)*180;
		left_field = (aLightFeat.meanLeft/255)*180;

		double result = right_field + left_field - frame1;
		System.out.printf("L: %d R:%d%n", (int)left_field, (int)right_field);
		System.out.printf("Wall %d%n", (int)Math.abs(result));

		if(Math.abs(result) < 2)
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

	static void computeNextFrame(Video video, byte[] frame) throws IOException
	{
		// Grab frame
		video.grabFrame(frame);

		// Extract luminanceFrame
		extractLuminanceValues(frame);

		// Compute light features
		aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
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


