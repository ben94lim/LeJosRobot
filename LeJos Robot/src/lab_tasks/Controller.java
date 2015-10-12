package lab_tasks;

import java.io.IOException;

public class Controller {
	
	/** Array to contain behaviour list */
    private RoboFace[] behaviours;
	
    /** Constructor*/
	public Controller(RoboFace[] behaviourList) {
		// Initialise behaviour list
		behaviours = behaviourList;
	}
	
	/** Variable to store monitor instance*/
	private Monitor monitor;
	
	int topPriority = 0;
	int priorityAction = 0;
	int currentAction = 0;
	
	
	public void start() throws IOException
	{
		monitor.start();
		while (true)
		{
			synchronized (monitor)
			{
				priorityAction = topPriority;
			}// monitor released before action is called
			
			if(priorityAction > currentAction)
			{
				behaviours[currentAction].suppress();
				while(!behaviours[currentAction].isSuppressed())
				{
					 
				}
				 
				currentAction = priorityAction;
			}
			 
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
				//FIND HIGHEST PRIORITY BEHAVIOR THAT WANTS CONTROL
				synchronized (this)
				{
					for (int i = 0; i < behaviours.length; i++)
					{
						if (behaviours[i].takeControl())
						{
							topPriority = i;
						}
					}
				}// End of synchronise block - main thread can run now
				Thread.yield();
			}
	    }
	}
}
