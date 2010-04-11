package com.augb.autometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.augb.autometer.activity.Settings;
import com.augb.autometer.util.Util;

public class Autometer extends Activity implements GpsNotificationListener {
	double distanceValue, fareValue;
	long waitingtimeValue;
	private static final int FINAL_VALUE_DLG = 1;
	
	Handler handler = new Handler();

	private TextView waitingTimeView, DistanceView, fareView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Typeface myTypeface = Typeface.createFromAsset(this.getAssets(),"DS-DIGIB.TTF");
        waitingTimeView = (TextView) findViewById(R.id.WaitingTime);
        waitingTimeView.setTypeface(myTypeface);
        DistanceView = (TextView) findViewById(R.id.Distance);
        DistanceView.setTypeface(myTypeface);
        fareView = (TextView) findViewById(R.id.Fare);
        fareView.setTypeface(myTypeface);
        ToggleButton VacantButton = (ToggleButton) findViewById(R.id.VacantHiredButton);
        VacantButton.setOnClickListener( new View.OnClickListener(){
        	
        	public void onClick(View v)
        	{//do the toggle thing
        		if (v instanceof ToggleButton) {
        			ToggleButton tb = (ToggleButton) v;
        			if (tb.isChecked()) {
        				GpsLocationManager locManager = GpsLocationManager.getGPSLocationManger();
		        		locManager.start( Autometer.this, Autometer.this);
		        		Log.d("test", "start");
		        		init(true);
	        		} else {
	        			//its in hiring mode, change to vacant
	        			handler.post(new Runnable() {
	        	    		@Override
	        	    		public void run() {
	        	    			stopMeter();
	        	    			init(true);
	        	    		}
	        	    	});
	        		}
        		}
        	}
        }
        );
        Button stopButton = (Button) findViewById(R.id.StopButton);
        stopButton.setOnClickListener(new View.OnClickListener (){
        	public void onClick(View v)
        	{
        		handler.post(new Runnable() {
    	    		@Override
    	    		public void run() {
    	    			stopMeter();        		
    	    		}
    	    	});
        	}
        });
        init(false);
    }
    private void stopMeter() {
    	ToggleButton VacantButton = (ToggleButton) findViewById(R.id.VacantHiredButton);
        VacantButton.setChecked(false);
        //TODO Stop capturing data.
        GpsLocationManager locManager = GpsLocationManager.getGPSLocationManger();
		locManager.stop();
		Log.d("test", "stop");
		showDialog(FINAL_VALUE_DLG);
    }
    public void init(boolean toZero)
    {
    	distanceValue =  fareValue = 0.0;
    	waitingtimeValue = 0;
    	if (toZero) {
    		waitingTimeView.setText("00.00");
	    	DistanceView.setText("00.0");
	    	fareView.setText(String.format("%.2f", Util.getBaseFare(this)));
    	} else {
	    	waitingTimeView.setText("--.--");
	    	DistanceView.setText("---.-");
	    	fareView.setText("---.--");
    	}
    }
    @Override
    public void onUpdate(final GpsLocation loc) {
    	Log.d("test", "got update: " + loc.toString());
    	handler.post(new Runnable() {
    		@Override
    		public void run() {
    			// TODO Auto-generated method stub
    	    	distanceValue = loc.getTotalDistance()/1000;
    	    	waitingtimeValue = loc.getTotalWaitingDT();//in millisecs
    	    	waitingTimeView.setText(String.format("%d:%d",waitingtimeValue/(1000*60), waitingtimeValue/(1000)));
    	    	DistanceView.setText(String.format("%.2f", distanceValue));
    	    	fareValue = Util.getFareFromDistance(distanceValue, Autometer.this);
    	    	fareView.setText(String.format("%.2f", fareValue));    	    		
			}
		});

	}

	/**
	 * Display the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inFlater = getMenuInflater();
		inFlater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * When the menu item is clicked
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));

		}
		return false;
	}
	
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder;
		Dialog dialog;
		switch(id)
		{
			case FINAL_VALUE_DLG:
			{

	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.trip_complete_dialog, null);
	            builder = new AlertDialog.Builder(Autometer.this);
                builder.setTitle(R.string.trip_complete)
                .setView(textEntryView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                        /* User clicked OK so do some stuff */
                    }
                });
                dialog = builder.create();
                TextView finalDistance = (TextView) textEntryView.findViewById(R.id.TotalDistanceValue);
                finalDistance.setText(String.format("%.2f Km",distanceValue ));
                
                TextView finalFare = (TextView) textEntryView.findViewById(R.id.TotalFareValue);
                finalFare.setText(String.format("%.2f Km",fareValue ));
                return dialog;
			}
		}
		return null;
		
	}
}