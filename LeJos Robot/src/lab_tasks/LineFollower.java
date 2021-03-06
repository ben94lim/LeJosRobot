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
import lejos.hardware.Sound;
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
		
		byte actions = 6;
		byte states = 48;
		QFunctions qutil = new QFunctions(actions, states);
		QLearn qTable = new QLearn(actions, states, qutil);
		byte[] e = new byte[4]; // The environment values
		int pilotCommand = 0; // Commands to send to pilot
		pilot.setTravelSpeed(56);

		while(Button.ESCAPE.isUp()) {
			// Check for target
			if(checkTarget())
			{
				pilot.stop();
				Sound.twoBeeps();
				Sound.twoBeeps();
			}
			getEnvironmentVals(e, video, frame);

			byte action = qTable.getAction(e);

			pilotCommand = qutil.getCommands(action);

			performAction(pilotCommand); 
		}
	}
	
	public static void performAction(int pilotCommand) {
		// Move Forward
		if(pilotCommand == 0)
			pilot.steer(0);
		// Steer Left
		else if(pilotCommand == 1)
			pilot.steer(50,10);
		// Steer Right
		else if(pilotCommand == 2)
			pilot.steer(-50,-10);
		// Turn Left
		else if(pilotCommand == 3)
			pilot.steer(100, 45);	
		// Turn Right
		else if(pilotCommand == 4)
			pilot.steer(-100,-45);	
		// Stop
		else if(pilotCommand == 5)
			pilot.stop();

		try {
			Thread.sleep(450);
		} catch(InterruptedException ie) {}
		pilot.stop();
	}

	public static void getEnvironmentVals(byte [] e, Video video, byte[] frame) throws IOException {
		// Following Light
		computeLeftRight(video, frame);
		double right_field = ((aLightFeat.meanRight)/255)*180;
		double left_field = ((aLightFeat.meanLeft)/255)*180;
		
		if(left_field-right_field<5)
			e[0] = 1;
		
		else if((left_field-right_field)>10)
			e[0] = 2;
		
		else if((right_field-left_field)>10)
			e[0] = 3;
		else
			e[0] = 0;
			
		// Junction
		compute3Part(video, frame);
		double threeLeft = ((aLightFeat.mean3Right)/255)*180;
		double threeMid = ((aLightFeat.mean3Mid)/255)*180;
		double threeRight = ((aLightFeat.mean3Left)/255)*180;
		
		if(threeLeft>70)
			e[1] = 1;
		
		else if(threeRight>70)
			e[1] = 2;
		else
			e[1] = 0;
		
		// In Target		
		lightSampleProv.fetchSample(lightSample, 0);
		
		if(lightSample[0] >= 0.25 && lightSample[0] <= 0.31)
			e[2] = 1;
		else
			e[2] =  0;
		
		// In Dark
		if(threeMid < 70)
			e[3] = 1;
		else
			e[3] = 0;
	}
	
	static boolean checkTarget()
	{		
		lightSampleProv.fetchSample(lightSample, 0);
		
		if(lightSample[0] >= 0.25 && lightSample[0] <= 0.31)
			return true;
		else
			return false;
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
	
	static void compute3Part(Video video, byte[] frame) throws IOException
	{
		// Grab frame
		video.grabFrame(frame);

		// Extract luminanceFrame
		extractLuminanceValues(frame);

		// Compute light features
		aLightFeat.compThree(luminanceFrame, HEIGHT, WIDTH);
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


