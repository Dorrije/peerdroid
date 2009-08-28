package peerdroid.sample.graph;

import peerdroid.sample.info.NetworkInformation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Android View used to draw the Graph of NetworkInformation.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class GraphView extends View {
	
	//Paint variable used to draw elements in to the View
    private Paint mPaint = new Paint();
    
	//Variable that contains all networkInformation
    private NetworkInformation networkInfo;

    /**
     * Class Constructor
     * @param context
     */
    public GraphView(Context context) {
        super(context);
    }
    
    /**
     * Method called when the View will draw the elements
     */
    @Override
    protected void onDraw(Canvas canvas) {
        
    	
    	Paint paint = mPaint;
        
        canvas.translate(10, 10);

        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.WHITE);
        canvas.drawLine(0, 350, 270,350, paint);
        canvas.drawLine(270, 0, 270,350, paint);
        canvas.drawLine(0, 0, 0, 350, paint);
        
        for( int i = 0 ; i < (350/70) ; i++ )
        {	
        	canvas.drawText(new Integer((350/70)-i).toString() + "Mbyte/s",5, (i*70), paint);
        	canvas.drawLine(0, (i*70),270,(i*70), paint);
        }
        paint.setColor(Color.GREEN);
        
        float stopX = 0,stopY;
        int count = 0;
        double sum = 0.0;
        
        for (int i = 0; i < networkInfo.getDownloadInfoSize(); i++) {
        	
        	sum = sum + networkInfo.getDownloadInfoAt(i).floatValue()/1000.0;
        	
        	if(count == 5)
        	{
        		stopY = (float)((sum / 5));
            	canvas.drawCircle(stopX*5,350 - stopY*70, 1, paint) ;
            	stopX ++;
            	sum = 0;
            	count = 0;
        	}
        	count ++;
            
         	    
       }
        
    }

	public NetworkInformation getNetworkInfo() {
		return networkInfo;
	}

	public void setNetworkInfo(NetworkInformation networkInfo) {
		this.networkInfo = networkInfo;
	}
}

