package com.cuctomviews.den.FlyWheelMenu;

import android.graphics.Color;
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

//        pie.addItem(1, Color.RED,getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));
//        pie.addItem(1, Color.BLUE,getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));
//        pie.addItem(1, Color.YELLOW,getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));
//        pie.addItem(1, Color.GRAY,getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));
//        pie.addItem(1, Color.MAGENTA,getResources().getColor(com.cuctomviews.den.FlyWheelMenu.R.color.strokeColor));

    }
}