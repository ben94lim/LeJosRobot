package lab_tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.video.Video;
import lejos.robotics.GyroscopeAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.CompassPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.utility.Delay;
import lejos.utility.GyroDirectionFinder;

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
	
	//Differential Pilot
	private static DifferentialPilot pilot = new DifferentialPilot(56,120,leftMotor,rightMotor);
	
	//Gyro Sensor
	private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S4);
	
	private static SampleProvider gyroSampleProv = gyroSensor.getAngleAndRateMode();
	
	//Gyro Adapter and Direction Finder
	
	private static GyroscopeAdapter gyroAdapter = new GyroscopeAdapter(gyroSampleProv, 2);
	
	private static GyroDirectionFinder gyroDirectFinder = new GyroDirectionFinder(gyroAdapter);
	
	//Compass Pose Provider
	private static CompassPoseProvider compassPoseProv = new CompassPoseProvider(pilot, gyroDirectFinder);
	
	//Navigator
	private static Navigator navigator = new Navigator(pilot, compassPoseProv);

	// Colour Sensor
	private static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);

	private static SampleProvider lightSampleProv = lightSensor.getAmbientMode();
	
	// Light features
	private static LineLightFeatures aLightFeat = new LineLightFeatures();

	private static float[] lightSample = new float[lightSampleProv.sampleSize()];


	public LineFollower() {

		// permanently store the brick in our instance variable
		brick = (EV3) BrickFinder.getLocal();

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
		
		double[][] qLearn = new double[5][6];
		
		File checkFile = new File("learning.dat");		
		FileInputStream fileIn = new FileInputStream("learning.dat");
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream("learning.dat"));
                
        try {
        	if(checkFile.exists())
        		qLearn = (double[][]) objIn.readObject();        		
		}
        catch (Exception e) {
			e.printStackTrace();
		}
        finally
		{
			objIn.close();
	        fileIn.close();	
		}       
		
		
		while(Button.ESCAPE.isUp()) {

			// Check for target
			
			// Check for Junction
			checkJunction(video, frame);

			

			// Display
			//System.out.println("Mean right: " + aLightFeat.meanRight);
			//System.out.println("Mean left: " + aLightFeat.meanLeft);
			//dispFrame();
			computeLeftRight(video, frame);
			right_field = ((aLightFeat.meanRight)/255)*180;
			left_field = ((aLightFeat.meanLeft)/255)*180;
			
			System.out.printf("L: %d R:%d%n", (int)left_field, (int)right_field);
			
			pilot.setTravelSpeed(100);
			
			// Tend left
			//if ((left_field) < 60) {
			if((left_field-right_field)>10)
			{
				System.out.printf("Left%n");
				pilot.steer(50,10);				
				//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				/*rightMotor.setSpeed(200); 
				leftMotor.setSpeed(150);*/
				//right_field = right_field * mot_amplif_larger;
				//left_field = left_field * mot_amplif_smaller;
			}
			
			// Tend right
			//else if ((left_field-right_field) > 20 || right_field > 70 )
			else if((right_field-left_field)>10)
			{
				System.out.printf("Right%n");
				pilot.steer(-50,-10);
				
				/*//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				rightMotor.setSpeed(150); 
				leftMotor.setSpeed(200);
				//left_field = right_field * mot_amplif_larger;
				//right_field = left_field * mot_amplif_smaller;
*/			}

			// Go straight
			else
			{
				//System.out.printf("L:%d R:%d%n",(int)left_field, (int)right_field);
				System.out.printf("Straight%n");
				pilot.steer(0);				
				//left_field = right_field * mot_amplif_smaller;
				//right_field = left_field;
			}


			float maxSpeed = 6;//rightMotor.getMaxSpeed();
			//rightMotor.forward();
			//leftMotor.forward();
			//System.out.printf("Spd:%d  Spd:%d%n",(int)(right_field)*(int)(maxSpeed), (int)(left_field)*(int)(maxSpeed));
			//rightMotor.setSpeed((int)(left_field)*(int)(maxSpeed)); 
			//leftMotor.setSpeed((int)(right_field)*(int)(maxSpeed)); 
			
				
			
	        try {			
				objOut.writeObject(qLearn);
			}
			catch (Exception e) {
				e.printStackTrace();
			}		  
		}
		video.close();
		objOut.close();
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
	static boolean checkTarget()
	{		
		lightSampleProv.fetchSample(lightSample, 0);
		
		if(lightSample[0] >= 0.25 && lightSample[0] <= 0.31)
			return true;
		else
			return false;
	}
	
	static void checkJunction(Video video, byte[] frame) throws IOException
	{
		computeQuarter(video, frame);

		double left = (aLightFeat.mean3Left/255)*180;
		double mid = (aLightFeat.mean3Mid/255)*180;
		double right = (aLightFeat.mean3Right/255)*180;

		System.out.printf("3L: %d 3M: %d 3R:%d%n", (int)left, (int)mid, (int)right);
		
		/*if(top_field<70)
			pilot.steer(100);
			//rightMotor.rotate(360);
*/		
		if(left>80)
		{
			System.out.printf("Right Junction%n");
			pilot.steer(-100,-90);			
			/*leftMotor.stop();
			rightMotor.stop();
			leftMotor.rotate(360);*/
		}
		
		else if(right>80)
		{
			System.out.printf("Left Junction%n");
			pilot.steer(100,90);			
		}
	}

	static void computeLeftRight(Video video, byte[] frame) throws IOException
	{
		// Grab frame
		video.grabFrame(frame);

		// Extract luminanceFrame
		extractLuminanceValues(frame);

		// Compute light features
		aLightFeat.compLeftRight(luminanceFrame, HEIGHT, WIDTH);
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


