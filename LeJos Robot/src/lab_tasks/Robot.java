package lab_tasks;

import java.io.IOException;

import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.video.Video;
import lejos.robotics.RegulatedMotor;

public class Robot {

	/** The EV3 brick we're controlling */
    static EV3 brick = LocalEV3.get();
    
    /** The motor on the left side of the robot */
    static RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));

    /** The motor on the right side of the robot */
    static RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));
    
    /** Video input from the webcam */
    static Video webcam = brick.getVideo();
    
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    
    public static EV3UltrasonicSensor sonicSensor = new EV3UltrasonicSensor(SensorPort.S4);
    
    public Robot() throws IOException{
    	Robot.webcam.open(WIDTH, HEIGHT);
    }
    

}


