package lab_tasks;

import java.io.IOException;

/**
 *
 * @author Benjamin Lim
 *
 */

public class Controller {
	
	/** Array to contain behaviour list */
    private RoboFace[] behaviours;
	
    /** Constructor*/
	public Controller(RoboFace[] behaviourList) {
		
		// Initialise behaviour list
		behaviours = behaviourList;
		
		// Create instance of Monitor
		monitor = new Monitor();
		
		// Makes the monitor thread a daemon thread
		monitor.setDaemon(true);
	}
	
	/** Variable to store monitor instance*/
	private Monitor monitor;
	
	// Value of highest level behaviour that wants to take control. Determined by monitor
	int topPriority = 0;
	
	// Variable to store output from monitor
	int priorityAction = 0;
	
	// Variable to store which behaviour is currently running its action
	int currentAction = 0;
	
	
	public void start() throws IOException
	{
		// Runs the monitor on a separate thread
		monitor.start();
		while (true)
		{
			synchronized (monitor)
			{
				priorityAction = topPriority;
			}// monitor released before action is called
			
			// If the behaviour returned from monitor is higher than currently running behaviour, 
			// suppress currently running behaviour and sets currentAction accordingly
			if(priorityAction > currentAction)
			{
				behaviours[currentAction].suppress();
				
				// Waits for action to exit
				while(behaviours[currentAction].isActive())
				{
					 
				}
				
				// Sets value of current action to be the highest level behaviour
				currentAction = priorityAction;
			}
			
			// Waits for action to exit
			while(behaviours[currentAction].isActive())
			{
				 
			}
			
			// Calls action of highest level behaviour wanting to take control
			behaviours[currentAction].action();
			
			Thread.yield();
		}
	}
	
	private class Monitor extends Thread
	{
		public void run()
	    {
			while (true)
			{
				//Find the highest level behaviour that wants to take control
				synchronized (this)
				{
					for (int i = 0; i < behaviours.length; i++)
					{
						if (behaviours[i].takeControl())
							topPriority = i;
					}
				}// End of synchronise block - main thread can run now
				Thread.yield();
			}
	    }
	}
}
