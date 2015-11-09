package lab_tasks;

public class LineLightFeatures {

	public double	meanLeft,
					meanRight,
					meanTot,
					meanMidLeft,
					meanMidRight,
					meanTopLeft,
					meanTopRight,
					meanBtmLeft,
					meanBtmRight,
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

	public void compMidLeftRight(byte [][] luminanceFrame, int height, int width) {
		meanMidLeft = 0;
		meanMidLeft = 0;
		int countL = 0;
		int countR = 0;
		int halfWidth = width/2;

		// Detect Horizontal Middle Values
		for (int y=0; y<height; y++) {
			for (int x=halfWidth-20; x<halfWidth; x++) {
				meanMidLeft += (double) (luminanceFrame[y][x] & 0xFF);
				countL++;
			}

			for (int x=halfWidth+1; x<halfWidth+20; x++) {
				meanMidRight += (double) (luminanceFrame[y][x] & 0xFF);
				countR++;
			}
		}

		meanMidLeft = meanMidLeft/countL;
		meanMidRight = meanMidRight/countR;
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
	
	public void compQuarter(byte [][] luminanceFrame, int height, int width) {
		meanTopLeft= 0;
		meanTopRight=0;
		meanBtmLeft=0;
		meanBtmRight=0;
		int countL = 0;
		int countR = 0;
		int halfWidth = width/2;

		// Detect Top Left and Right Values
		for (int y=0; y<height/2; y++) {
			for (int x=0; x<halfWidth; x++) {
				meanTopLeft += (double) (luminanceFrame[y][x] & 0xFF);
				countL++;
			}

			for (int x=halfWidth+1; x<width; x++) {
				meanTopRight += (double) (luminanceFrame[y][x] & 0xFF);
				countR++;
			}
		}
		meanTopLeft = meanTopLeft/countL;
		meanTopRight = meanTopRight/countR;
		
		countL = 0;
		countR = 0;

		// Detect Bottom Left and Right Values
		for (int y=(height/2)+1; y<height; y++) {
			for (int x=0; x<halfWidth; x++) {
				meanBtmLeft += (double) (luminanceFrame[y][x] & 0xFF);
				countL++;
			}

			for (int x=halfWidth+1; x<width; x++) {
				meanBtmRight += (double) (luminanceFrame[y][x] & 0xFF);
				countR++;
			}
		}
		meanBtmLeft = meanBtmLeft/countL;
		meanBtmRight = meanBtmRight/countR;
	}

}
