package lab_tasks;

import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class Robot {

	/** The EV3 brick we're controlling */
    static EV3 brick = LocalEV3.get();
    
    /** The motor on the left side of the robot */
    static RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));

    /** The motor on the right side of the robot */
    static RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));

}


