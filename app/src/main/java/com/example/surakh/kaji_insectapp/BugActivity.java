package com.example.surakh.kaji_insectapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import java.util.Random;

import static com.example.surakh.kaji_insectapp.R.*;
import static java.lang.Math.PI;
import static java.lang.Math.*;
import static java.lang.Math.cos;

/*
  Created by surakh on 15-April-20.
 */
public class BugActivity implements SurfaceHolder.Callback, Runnable, View.OnTouchListener{
    private SurfaceHolder holder;
    private static final String TAG = "BugActivity";
    private Thread thread;

    public final static int ELECTRODE_NUM = 61;
    TactileDisplay shapes;
    Physicaloid mPhysicaloid;
    UartConfig uartConfig = new UartConfig(921600, 8, 0, 0, false, false);
    boolean enableSerial;
    boolean touchFlag=false;
    byte electrodeFlag[] = new byte[ELECTRODE_NUM+3];
    byte electrode[] = new byte[(ELECTRODE_NUM+3)/8];
    int x,y;
    boolean pinFlag=false;

    float screenWidth, screenHeight;
    float bgWidth, bgHeight;
    float insectW, insectH;
    Bitmap background;

    Bitmap insectHead;
    Bitmap insectBody;
    Bitmap insectTail;
    Matrix mInsect = new Matrix();
    Paint pInsect = new Paint();
    private float angleFactor;
    int insectNum=20;
    private float angleInsect[] = new float[insectNum];
    private float angleReal;
    private float sX[] = new float[insectNum];
    private float sY[] = new float[insectNum];
    private float vX;
    private float vY;
    private float vA;
    private float vG;
    private float vGx;
    private float vGy;
    int counter, counter1;
    private Random r1 = new Random();

    public BugActivity(Context context, SurfaceView sv) {
        Resources r = context.getResources();
        holder = sv.getHolder();
        holder.addCallback(this);
        shapes = new TactileDisplay();
        mPhysicaloid = new Physicaloid(context);
        background = BitmapFactory.decodeResource(r, mipmap.background);
        insectHead = BitmapFactory.decodeResource(r, mipmap.mushihead);
        insectBody = BitmapFactory.decodeResource(r, mipmap.mushibody);
        insectTail = BitmapFactory.decodeResource(r, mipmap.mushitail);
        insectH=insectHead.getHeight();
        insectW=insectHead.getWidth();
        sv.setOnTouchListener(this);
        angleFactor=0;
        vA=4;vX=0;vY=0;vGx=0;vGy=0;
        for(int i=0; i<10; i++){
            sX[i]=0; sY[i]=0;
        }

        vG=0.003f;
        counter=0;
        counter1=0;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLUE);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mPhysicaloid.open(uartConfig)){
            enableSerial = true;
        }
        Log.d(TAG, "surfaceChanged");
        screenWidth = width;
        screenHeight = height;
        bgWidth = background.getWidth();
        bgHeight = background.getHeight();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }
    public void pause() {
        Log.d(TAG, "surfaceDestroyed");
        thread = null;
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        if(mPhysicaloid.open(uartConfig)){
            enableSerial = true;
        }
        shapes.statPos(screenWidth / 2, screenHeight / 2);
        while(thread!=null){
            //Log.d(TAG, String.valueOf(x));
            counter++;
            if(counter>30){
                angleFactor=r1.nextInt(11)-5;
                counter=0;
            }
            angleReal+=angleFactor;
            vX= vA*(float)cos(angleReal*PI/180);
            vY= vA*(float)sin(angleReal* PI/180);
            vGx=vG*sX[0];
            vGy=vG*sY[0];
            float vRx = vX - vGx;
            float vRy = vY - vGy;
            if(vRy >0){
                angleInsect[0]=abs((float)(acos(vRx /sqrt(vRx * vRx + vRy * vRy))*180/PI));
            }
            else
                angleInsect[0]=-abs((float)(acos(vRx /sqrt(vRx * vRx + vRy * vRy))*180/PI));
            sX[0] += vRx;
            sY[0] += vRy;
            //Log.d(TAG, String.valueOf(angleInsect)+", "+String.valueOf(angleReal[0]));
            // 拡大・縮小率の設定
            //mInsect.postScale(30, 30);
            // 回転角の設定
            //mInsect.postRotate(aInsect);
            // 表示する座標の設定
            //mInsect.postTranslate(0, -5);

            //if(touchFlag) {
                electrodeFlag = shapes.getPin();
                for(int pinNo = 0; pinNo < (ELECTRODE_NUM+3)/8; pinNo++){
                    int pinNo1 = pinNo*8;
                    electrode[pinNo] = (byte) (
                            (electrodeFlag[pinNo1] << 7) |
                                    (electrodeFlag[pinNo1 + 1] << 6) |
                                    (electrodeFlag[pinNo1 + 2] << 5) |
                                    (electrodeFlag[pinNo1 + 3] << 4) |
                                    (electrodeFlag[pinNo1 + 4] << 3) |
                                    (electrodeFlag[pinNo1 + 5] << 2) |
                                    (electrodeFlag[pinNo1 + 6] << 1) |
                                    (electrodeFlag[pinNo1 + 7]));
                }
            /*}else{
                for(int pinNo=0; pinNo<8; pinNo++){
                    electrode[pinNo]=0;
                }
            }*/
            if(enableSerial){
                mPhysicaloid.write(electrode, electrode.length);
            }
            if(touchFlag){
                shapes.pinPos(x,y);
            }

            doDraw(holder);
            counter1++;
            if(counter1>2){
                for(int i=1; i<insectNum;i++){

                    sX[insectNum-i]=sX[insectNum-1-i];
                    sY[insectNum-i]=sY[insectNum-1-i];
                    angleInsect[insectNum-i]=angleInsect[insectNum-1-i];
                }
                counter1=0;
            }
        }
    }
    private void doDraw(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas();
        //canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(background, new Rect((int) (bgWidth / 2 - screenWidth * bgWidth / (2 * screenHeight)), 0, (int) (bgWidth / 2 + screenWidth * bgWidth / (2 * screenHeight)), (int) bgHeight), new Rect(0, 0, (int) screenWidth, (int) screenHeight), null);
        /*for(int i=9; i>=0; i--){
            mInsect.preTranslate(insectW / 2, insectH / 2);
            mInsect.setRotate(angleInsect[i] + 90);
            mInsect.preTranslate(-insectW / 2, -insectH / 2);
            mInsect.postTranslate((screenWidth / 2 - insectW / 2 + sX[i]), (screenHeight / 2 - insectH / 2 + sY[i]));
            canvas.drawBitmap(insect, mInsect, pInsect);
            mInsect.postTranslate(-(screenWidth / 2 - insectW / 2 + sX[i]), -(screenHeight / 2 - insectH / 2 + sY[i]));
        }*/

        shapes.InitShapes(canvas);
        shapes.initPin();

        onDraw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }
    protected void onDraw(Canvas canvas){
        for(int i=insectNum-1; i>=0; i--){
            mInsect.preTranslate(insectW / 2, insectH / 2);
            mInsect.setRotate(angleInsect[i] + 90);
            mInsect.preTranslate(-insectW / 2, -insectH / 2);
            mInsect.postScale(0.4f, 0.4f);
            mInsect.postTranslate((screenWidth / 2 - insectW / 2 + sX[i]), (screenHeight / 2 - insectH / 2 + sY[i]));
            //mInsect.postTranslate((screenWidth / 2 + insectW / 2 + sX[i]), (screenHeight / 2 - insectH / 2 + sY[i]));
            if(i==0)canvas.drawBitmap(insectHead, mInsect, pInsect);
            else if(i==insectNum-1) canvas.drawBitmap(insectTail, mInsect, pInsect);
            else canvas.drawBitmap(insectBody, mInsect, pInsect);
            mInsect.postTranslate(-(screenWidth / 2 - insectW / 2 + sX[i]), -(screenHeight / 2 - insectH / 2 + sY[i]));
            //mInsect.postTranslate(-(screenWidth / 2 + insectW / 2 + sX[i]), -(screenHeight / 2 - insectH / 2 + sY[i]));
            if(touchFlag){
                shapes.electricInsect((int) (screenWidth / 2 - insectW / 2 + sX[i]), (int) (screenHeight / 2 - insectH / 2 + sY[i]));
                //if(pinFlag){
//                    shapes.drawPin(electrodeFlag, true);
//                }
            }
        }
        if(touchFlag) {
            if (pinFlag) {
                shapes.drawPin(electrodeFlag, true);
            }
        }
        else if(!touchFlag) {
            shapes.displayInsect((int) (screenWidth / 2 - insectW / 2 + sX[0]), (int) (screenHeight / 2 - insectH / 2 + sY[0]), (int) screenWidth / 13);
            if(pinFlag){
                shapes.statDrawPin(electrodeFlag);
            }
        }
    }

    @Override
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
        return true;
    }
}
