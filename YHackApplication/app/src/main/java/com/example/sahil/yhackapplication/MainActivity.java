package com.example.sahil.yhackapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import jama.Matrix;
import jkalman.JKalman;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public Matrix test() throws Exception {
        JKalman k = new JKalman(1, 0);
        return k.Correct(new Matrix(2,2));
    }

    public void startAccelerometer(View v) {
        Intent intent = new Intent(this, AccelerometorSensor.class);
        startActivity(intent);
    }
}
