package com.cuctomviews.den.FlyWheelMenu;

import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.Button;

import com.cuctomviews.den.FlyWheelMenu.charting.PieChart;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();

        setContentView(com.cuctomviews.den.FlyWheelMenu.R.layout.activity_main);
        final PieChart pie = (PieChart) this.findViewById(com.cuctomviews.den.FlyWheelMenu.R.id.Pie);
        for (int i = 0; i < 5; i++){
            pie.addItem(1, getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.fillSector),getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));
        }

        ((Button) findViewById(com.cuctomviews.den.FlyWheelMenu.R.id.Reset)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pie.setCurrentItem(0);
            }
        });
    }
}