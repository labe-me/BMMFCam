package com.mob.bmmfcam;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CompassSensorForwarder implements SensorEventListener {
	SensorManager manager;
	Sensor magneticSensor;
	Sensor accelerometerSensor;
	boolean magneticReady = false;
	boolean accelerometerReady = false;
	float[] accValues = new float[3];
    float[] magValues = new float[3];
    float[] rotationMatrix = new float[16];
    float[] inclinationMatrix = new float[16];
    float[] orientation = new float[3];

	public CompassSensorForwarder(Activity anActivity){
		manager = (SensorManager)anActivity.getSystemService(Activity.SENSOR_SERVICE);
		magneticSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	public void onDestroy(){
		manager.unregisterListener(this);
	}
	
	public void onResume(){
		magneticReady = false;
		accelerometerReady = false;
		manager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
		manager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void onPause(){
		manager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Don't care
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				accelerometerReady = true;
                System.arraycopy(event.values, 0, accValues, 0, 3);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				magneticReady = true;
                System.arraycopy(event.values, 0, magValues, 0, 3);
				break;
		}
		if (accelerometerReady && magneticReady){
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accValues, magValues);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                double rad = orientation[0];
                double deg = rad / Math.PI * 180;
                // -90 N
                // 0 E
                // 90 S
                // 180/-180 W
                Log.d("CompassSensorForwarder:", String.format("%f", deg));
            }
			accelerometerReady = false;
			magneticReady = false;
		}
	}
}
