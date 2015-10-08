package lab_tasks;

public class MotionMap {
	
	private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int NUMPIXELS = WIDTH*HEIGHT;
    
    private int [][] prevFrame = new int[HEIGHT][WIDTH];
    private int [][] curFrame = new int[HEIGHT][WIDTH];
    
    public int [][] motionMap = new int[HEIGHT][WIDTH];
    public double leftMotion = 0;
    public double rightMotion = 0;
    public double totalMotion = 0;
    
    public MotionMap() {
    	// Initialize maps
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<WIDTH; x++) {
    			prevFrame[y][x] = 0;
    			curFrame[y][x] = 0;
    			motionMap[y][x] = 0;
    		}
    	}
    }
    
    public void addFrame(int [][] aFrame) {
    	// Update frames; innefficient but clearer
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<WIDTH; x++) {
    			prevFrame[y][x] = curFrame[y][x];
    			curFrame[y][x] = aFrame[y][x];
    		}
    	}
    }
    
    public void compMotion() {
    	int aDiff;
    	if (prevFrame != null) {
    		for (int y=0; y<HEIGHT; y++) {
        		for (int x=0; x<WIDTH; x++) {
        			aDiff = curFrame[y][x]-prevFrame[y][x];
        			motionMap[y][x] = Math.abs(aDiff);
        		}
        	}
    	}
    }
    
    public boolean isMotion(){
    	// Detect if there is motion
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<(WIDTH); x++) {
    			totalMotion += (double) motionMap[y][x];
    		}
    	}
    	
    	if(totalMotion == 0)
    		return false;
    	else
    		return true;
    }
    
    public double compMaxMotion() {
    	double curMax = 0;
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<WIDTH; x++) {
    			if (curMax < motionMap[y][x]) {
    				curMax = motionMap[y][x];
    			}
    		}
    	}
    	return curMax;
    }
    
    public void compLeftRight() {
    	// Add left motion
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=0; x<(WIDTH/2); x++) {
    			leftMotion += (double) motionMap[y][x];
    		}
    	}
    	leftMotion = leftMotion / (NUMPIXELS/2);
    	
    	// Add right motion
    	for (int y=0; y<HEIGHT; y++) {
    		for (int x=(WIDTH/2); x<WIDTH; x++) {
    			rightMotion += (double) motionMap[y][x];
    		}
    	}
    	rightMotion = rightMotion / (NUMPIXELS/2);
    }
    
	
}
