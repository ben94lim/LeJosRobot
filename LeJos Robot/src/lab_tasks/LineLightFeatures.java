package lab_tasks;

public class LineLightFeatures {

	public double	meanLeft,
					meanRight,
					meanTot,
					mean3Left,
					mean3Mid,
					mean3Right;

	public LineLightFeatures() {
		init_all();
	}

	private void init_all() {
		meanLeft = 0.0;
		meanRight = 0.0;
		meanTot = 0.0;
	}

	public void compLeftRight(byte [][] luminanceFrame, int height, int width) {
		init_all();
		int halfWidth = width/2;
		double totPix = (height*width);
		double halfPix = totPix/2;
		for (int y=0; y<height; y++) {
			for (int x=0; x<halfWidth; x++) {
				meanLeft += (double) (luminanceFrame[y][x] & 0xFF); 
				meanTot += (double) (luminanceFrame[y][x] & 0xFF);
			}
			for (int x=halfWidth+1; x<width; x++) {
				meanRight += (double) (luminanceFrame[y][x] & 0xFF); 
				meanTot += (double) (luminanceFrame[y][x] & 0xFF); 
			}
		}
		meanLeft = meanLeft/halfPix;
		meanRight = meanRight/halfPix;
		meanTot = meanTot/totPix;
	}

	public void compThree(byte [][] luminanceFrame, int height, int width) {
		mean3Left = 0;
		mean3Mid = 0;
		mean3Right = 0;
		
		int countL = 0;
		int countM = 0;
		int countR = 0;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<(width/3); x++) {
				mean3Left += (double) (luminanceFrame[y][x] & 0xFF);
				countL++;
			}

			for (int x=(width/3)+1; x<((width/3)*2); x++) {
				mean3Mid += (double) (luminanceFrame[y][x] & 0xFF);
				countM++;
			}
			
			for (int x=((width/3)*2)+1; x<width; x++) {
				mean3Right += (double) (luminanceFrame[y][x] & 0xFF);
				countR++;
			}			
			
			mean3Left = mean3Left/countL;
			mean3Mid = mean3Mid/countM;
			mean3Right = mean3Right/countR;			
		}
	}
}
