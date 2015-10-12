package lab_tasks;

import java.io.IOException;

public interface RoboFace {
	
	// Method for what action the behaviour wants to do
	public void action() throws IOException;
	
	// Method to indicate if the behaviour wants to take control
	public boolean takeControl();
	
	// Method to safely suppress currently running action from behaviour
	public void suppressed();
}
