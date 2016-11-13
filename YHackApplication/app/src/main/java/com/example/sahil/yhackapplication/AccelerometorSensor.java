package com.example.sahil.yhackapplication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import com.example.sahil.yhackapplication.R;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jama.Matrix;
import jkalman.JKalman;

/**
 * Created by Sahil on 11/12/2016.
 */

public class AccelerometorSensor extends Activity implements SensorEventListener{
    private SensorManager sensorManager;
    double ax,ay,az;   // these are the acceleration in x,y and z axis
    float[] last;
    JKalman kalman;
    private float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private float[] gravityValues = null;
    private float[] magneticValues = null;
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private UsbDevice device;
    private UsbDeviceConnection connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        device = null;
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        last = new float[3];
        try {
            kalman = new JKalman(6, 3);
        } catch (Exception e){}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    protected float[] lowPass( float[] input, float[] output ) {
        final float ALPHA = 0.99f;
        if ( output == null ) return null;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = ALPHA * output[i] + (1 - ALPHA) * input[i]; } return output; }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long actualTime = System.currentTimeMillis();
            last = lowPass(event.values.clone(), last);
        } else {
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                for(int i = 0; i < event.values.length; i++) {
                    mMagnetometerReading[i] = event.values[i];
                }
                magneticValues = event.values;
                // Update rotation matrix, which is needed to update orientation angles.
                SensorManager.getRotationMatrix(mRotationMatrix, null,
                        mAccelerometerReading, mMagnetometerReading);
                // "mRotationMatrix" now has up-to-date information.\
                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
                // "mOrientationAngles" now has up-to-date information
            } else {
                if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mAccelerometerReading = event.values;
                    if ((mAccelerometerReading != null) && (gravityValues != null) && (magneticValues != null)) {
                        float[] deviceRelativeAcceleration = new float[4];
                        deviceRelativeAcceleration[0] = event.values[0];
                        deviceRelativeAcceleration[1] = event.values[1];
                        deviceRelativeAcceleration[2] = event.values[2];
                        deviceRelativeAcceleration[3] = 0;

                        // Change the device relative acceleration values to earth relative values
                        // X axis -> East
                        // Y axis -> North Pole
                        // Z axis -> Sky

                        float[] R = new float[16], I = new float[16], earthAcc = new float[16];

                        SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);

                        float[] inv = new float[16];

                        android.opengl.Matrix.invertM(inv, 0, R, 0);
                        android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                        //System.out.println("Acceleration" + "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + (earthAcc[2] - 9.8) + ")");
                    }
                } else {
                    if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                        gravityValues = event.values;
                    }
                }
            }
        }
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry entry : usbDevices.entrySet()) {
                UsbDevice device = (UsbDevice) entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x1F00) {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    UsbDeviceConnection connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    public void startAccelerometer(View v) {
        final float[] rotationMatrix = new float[9];
        sensorManager.getRotationMatrix(rotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        final float[] orientationAngles = new float[3];
        sensorManager.getOrientation(rotationMatrix, orientationAngles);
        
    }
}