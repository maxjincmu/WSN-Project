package com.example.evalineju.myfirstapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.*;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.os.Environment;

import java.util.List;

import android.media.MediaScannerConnection;
import android.net.Uri;
import org.opencv.video.*;


import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;

import java.util.concurrent.locks.ReentrantLock;

import android.telephony.SmsManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.os.Message;
import android.os.Looper;
import android.os.Handler;

import android.os.*;

import android.media.ToneGenerator;
import android.media.AudioManager;

public class snd_processing extends Activity implements CvCameraViewListener2  {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private int counter = 0;


    //private BackgroundSubtractorMOG sub = new BackgroundSubtractorMOG(3, 4, 0.8);
    private BackgroundSubtractorMOG2 sub;
    private Mat mGray;
    private Mat mRgb;
    private Mat mFGMask;

    //Classification gloabl variables
    Boolean negativetrigger = false;
    int negativecount= 0;
    Boolean fallen = false;

    int prevVal= 0;
    ReentrantLock l = new ReentrantLock();

    private Handler m_Handler;

    private String phoneNumber = "+12408888859";
    private String textmessage = "Someone fell!";
    private String okmessage = "The person is ok";
    private String lastmessgae = "The person is not responding";

    //CameraBridgeViewBase mOpenCvCameraView;
    List<Mat> ring = new ArrayList<Mat>(); // recording buffer
    int delay = 100;                       // delay == length of buffer
    boolean delayed = false;               // state

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.disableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public snd_processing() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.snd_processing_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE); //VISIBLE

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        m_Handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message message) {
                launchAlert();
            }
        };

        sub = Video.createBackgroundSubtractorMOG2(500, 30, false);  //MOG2, KNN, GMG
        mGray = new Mat();
        mRgb = new Mat();
        mFGMask = new Mat();
    }

    public void onCameraViewStopped() {
    }

    // here's the bread & butter stuff:
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        //If the fallen variable is set, make a tone, send a text, and launch the pop up
        if (fallen) {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);;
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600);
            Log.d("Sent SMS!",Boolean.toString(fallen));
            Message message = m_Handler.obtainMessage();
            message.sendToTarget();
            sendSMS(phoneNumber, textmessage);
            fallen = false;
        }

        long start = System.currentTimeMillis();
        mGray = inputFrame.gray();
        Imgproc.cvtColor(mGray, mRgb, Imgproc.COLOR_GRAY2RGB);
        sub.apply(mRgb, mFGMask, -1);
        long end = System.currentTimeMillis();

        //time elapsed
        double time_passed = ((double) end - start)/1000.0;
        Log.d("Back Subtract", "Time(s): " + Double.toString(time_passed));
        final Mat sub_img = mFGMask.clone();

        counter = counter + 1;

        //Create a new thread for each frame
        new Thread(new Runnable() {
            public void run() {
                putBoundingBox(sub_img, counter);
            }
        }).start();

        //time elapsed
        end = System.currentTimeMillis();
        time_passed = ((double) end - start)/1000.0;
        Log.d("Image Process", "Total Time(s): " + Double.toString(time_passed));

        return mFGMask;
    }

    private void putBoundingBox(Mat oldimg, int img_number) {
        //Resizing the image
        long start = System.currentTimeMillis();
        Mat img = new Mat();
        Size sz = new Size(720,420);
        Imgproc.resize(oldimg, img, sz );
        int rows = img.rows();
        int cols = img.cols();
        Log.d("Rows", Integer.toString(rows));
        Log.d("Cols", Integer.toString(cols));
        byte [] data = new byte[1];
        data[0] = 1;

        long end = System.currentTimeMillis();

        //time elapsed
        double time_passed = ((double) end - start)/1000.0;
        Log.d("Resize", "Time(s): " + Double.toString(time_passed));


        Mat result;

        double next_sum;

        int n = 150;
        int stride = 10;

        int[] storeval = new int[1];
        Mat scores = new Mat(rows, cols, CvType.CV_32S, new Scalar(0));
        scores.get(7,7, storeval);
        Log.d("Bounding Box", "test: " + Integer.toString(storeval[0]));

        //Threshold for the decrease in y-coordinate
        int threshold = 60;

        //Calulate the sum over blocks
        start = System.currentTimeMillis();
        for (int i = 0; i < rows - n; i += stride){
            for (int j = 0; j < cols - n; j += stride){
                result = img.submat(i, i + n, j, j + n);
                next_sum = (Core.sumElems(result)).val[0];
                storeval[0] = (int)next_sum;
                scores.put(i, j, storeval);
            }
        }

        end = System.currentTimeMillis();

        //time elapsed
        time_passed = ((double) end - start)/1000.0;
        Log.d("Bounding Box", "Time(s): " + Double.toString(time_passed));

        // Get first max
        Core.MinMaxLocResult sizes = Core.minMaxLoc(scores);

        Point coords = sizes.maxLoc;

        int xcoord = (int) coords.x;
        int ycoord = (int) coords.y;


        Scalar color;

        // mutex start here
        l.lock();
        try {

            //Discount first 15 frames for it to stabilize
            if(counter >= 15)
            {
                //Watch for a large drop greater than or equal to threshold
                int diff = ycoord - prevVal;
                if (diff >= threshold)
                    negativetrigger = true;
                //Watch to see if the person stays down
                if (diff >= -30 && negativetrigger)
                    negativecount++;
                else {
                    negativecount = 0;
                    negativetrigger = false;
                }
                //Printing for debugging
                String c = String.valueOf(negativecount) + " count " + String.valueOf(ycoord) + " x "+ String.valueOf(diff);
                Imgproc.putText(img,c, new Point(300, 300), Core.FONT_HERSHEY_SIMPLEX, .5, new Scalar(255, 0, 0));
            }

            prevVal = ycoord;

            //If the person is down for 12 consecutive frames, set fallen
            if (negativecount == 12) {
                fallen = true;
                color = new Scalar(255, 255, 255);
            }

            // Draw rectangles for the coords ((x,y), (x+n,y+n))
            color = new Scalar(255, 0, 0);

            if (fallen)
            {
                color = new Scalar(0, 0, 0);
            }

        } finally {
            l.unlock();
        }

        //mutex end here

        Imgproc.rectangle(img,
                new Point(xcoord, ycoord),
                new Point(xcoord + n, ycoord + n),
                color, // color red
                5);                    // thickness 5

        //Save files to the tablet for debugging
        String s =  Environment.getExternalStorageDirectory().toString() + "/fallingTest551/file-" + img_number + ".png";
        Imgcodecs.imwrite(s,img);


        MediaScannerConnection.scanFile(this, new String[] { s }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });


    }
    //Send an sms to phoneNumber
    public void sendSMS(String phoneNumber,String message) {
        SmsManager sms = SmsManager.getDefault();
        PendingIntent sentPI;
        String SENT = "SMS_SENT";

        sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);

        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);

    }
    //Launch the alert to ask if ok
    private void launchAlert(){
        final AlertDialog alertViewer = new AlertDialog.Builder(snd_processing.this).create();
        alertViewer.setTitle("Fall Detected");


        alertViewer.setMessage("You appear to have fallen!" +
                " Your emergency contact has been notified");

        // make a 10 second countdown timer
        final CountDownTimer checkin = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                alertViewer.dismiss();
                //sendSMS(phoneNumber, lastmessage);
            }
        };
        checkin.start();


        // set dialog message
        alertViewer.setButton(AlertDialog.BUTTON_NEUTRAL, "I'm OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //send another text from here saying JK I'm fine
                        sendSMS(phoneNumber, okmessage);
                        dialog.dismiss();
                        checkin.cancel();
                    }
                });


        alertViewer.show();
    }

}
