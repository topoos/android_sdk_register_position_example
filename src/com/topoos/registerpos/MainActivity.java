package com.topoos.registerpos;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This app is a very simple project that show an example about using Get Position SDK Operation
 * 
 * www.topoos.com
 * Read documentation and examples in http://docs.topoos.com
 */
public class MainActivity extends Activity {

	private static final String APPTOKEN_USER = "XXXXXXXXXXXXXXXXXXXXXXXX";
	
	

	public final int WORKER_ERROR = -1;
	public final int WORKER_OK = 1;

	private Handler handler = new Handler(new WorkerResultMessageCallback());
	
	private LocationManager mLocationManager;
	private CustomLocationListener mCustomLocationListener;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        //build and save topoos access token in preferences
        topoos.AccessTokenOAuth token = new topoos.AccessTokenOAuth(APPTOKEN_USER);
        token.save_Token(this);
        
        mCustomLocationListener = new CustomLocationListener();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, mCustomLocationListener);
        
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (mLocationManager != null && mCustomLocationListener != null) {
    		mLocationManager.removeUpdates(mCustomLocationListener);
    		mLocationManager = null;
    	}
    }

    /**
     * Detects changes on device location 
     */
    private class CustomLocationListener implements LocationListener{
		  public void onLocationChanged(Location argLocation) {
			  
			  RegisterPositionWorker worker = new RegisterPositionWorker(argLocation);
			  Thread thread = new Thread(worker);
			  thread.start();
				
		  }
		  public void onProviderDisabled(String provider) {}
		  public void onProviderEnabled(String provider) {}
		  public void onStatusChanged(String provider, int status, Bundle extras) {}
	 }
    
    /**
     * Register position on topoos
     */
    private class RegisterPositionWorker implements Runnable {
		
    	public Location position;
    	
    	public RegisterPositionWorker(Location loc)
    	{
    		position = loc;
    	}
    	
		public void run(){
			try {
				
				topoos.Objects.Position p = topoos.Positions.Operations.Add(MainActivity.this, position.getLatitude(), position.getLongitude(), null, null, null, null, null, null, null);
				
				Message msg = new Message();
				msg.what = WORKER_OK;
				msg.obj = p;
				
				handler.sendMessage(msg);
				
			} catch (Exception e) {
				e.printStackTrace();
				handler.sendEmptyMessage(WORKER_ERROR);
			}
		}
	}
    
    

	private class WorkerResultMessageCallback implements Callback {

		public boolean handleMessage(Message arg0) {
			switch(arg0.what)
			{
			case WORKER_ERROR:
				Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
				break;
			case WORKER_OK:
				topoos.Objects.Position p = (topoos.Objects.Position)arg0.obj;
				((TextView)MainActivity.this.findViewById(R.id.positionid)).setText(p.getId().toString());
				break;
			}
			return true;
		}
	}

    
}
