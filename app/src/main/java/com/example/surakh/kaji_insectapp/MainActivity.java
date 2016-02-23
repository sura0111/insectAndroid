package com.example.surakh.kaji_insectapp;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;


public class MainActivity extends Activity{
    private SurfaceView surfaceView;
    public BugActivity bugActivity;
    private static final String TAG = "MainActivity";
    int x,y;
    boolean touchFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        bugActivity = new BugActivity(this, surfaceView);
        //surfaceView.setOnTouchListener((View.OnTouchListener) this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        bugActivity.pause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(Menu.NONE, 0, Menu.NONE, "OpenPort");
        menu.add(Menu.NONE, 1, Menu.NONE, "ClosePort");
        menu.add(Menu.NONE, 2, Menu.NONE, "ShowPin");
        menu.add(Menu.NONE, 3, Menu.NONE, "HidePin");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case 0:
                if(bugActivity.mPhysicaloid.open(bugActivity.uartConfig)){
                    bugActivity.enableSerial=true;}
                break;

            case 1:
                if(bugActivity.mPhysicaloid.close()){
                    bugActivity.enableSerial = false;}
                break;
            case 2:
                bugActivity.pinFlag = true;
                break;
            case 3:
                bugActivity.pinFlag = false;
                break;

        }
        return true;
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x = (int)event.getX();
                y = (int)event.getY();
                touchFlag = true;
                break;
            case MotionEvent.ACTION_MOVE:
                x = (int)event.getX();
                y = (int)event.getY();
                touchFlag = true;
                break;
            case MotionEvent.ACTION_UP:
                x = 0;
                y = 0;
                touchFlag = false;
                break;
            default:
                touchFlag=false;
                return true;
        }
        bugActivity.x=x;
        bugActivity.y=y;
        bugActivity.touchFlag=touchFlag;
        return true;
    }*/

}
