package com.cuctomviews.den.FlyWheelMenu;

import android.os.Bundle;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.cuctomviews.den.FlyWheelMenu.R.layout.activity_main);
        FlyWheelMenu pie = (FlyWheelMenu) this.findViewById(com.cuctomviews.den.FlyWheelMenu.R.id.pie);

        for (int i = 0; i < 7; i++){
            pie.addItem(2, getResources().getColor(R.color.fillSector),getResources().getColor(R.color.strokeColor));
        }
    }
}