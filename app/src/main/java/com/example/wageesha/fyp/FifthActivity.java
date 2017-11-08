package com.example.wageesha.fyp;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.Environment;
import android.content.Context;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class FifthActivity extends Activity {

    public static final int  INT = 600;
    private TextView cont_view;
    private TextView pitch_view;
    private File file;
    private Context context;
    private ScrollView cont_scrollview;

    private Button start_button;
    private Button stop_button;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;

    private Thread recordingThread = null;
    private Thread processingThread = null;
    private Thread pitchThread = null;
    private Thread amdfThread = null;
    private Thread acfThread = null;
    private boolean isRecording = false;

    private int BufferElements2Rec = 1024;
    private int BytesPerElement = 2;

    private int[] amdf_array = new int[SAMPLE_LAG];
    private long[] acf_array = new long[SAMPLE_LAG];
    private short[] data = new short[BufferElements2Rec];
    private double pitch;
    private int i = 0;
    private int count = 0;
    private long previous_frame_energy = 0;

    private static final int SAMPLE_LAG = 100;
    private static final int FRAME_SIZE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fifth);

        String text;

        start_button = (Button) findViewById(R.id.buttonStart);
        stop_button = (Button) findViewById(R.id.buttonStop);

        cont_view = (TextView) findViewById(R.id.viewContDisplay);
        cont_view.setMovementMethod(new ScrollingMovementMethod());
        pitch_view = (TextView) findViewById(R.id.viewPitchDisplay);

        cont_view.setBackgroundColor(Color.rgb(255,255,255));
        pitch_view.setBackgroundColor(Color.rgb(255,255,255));

        cont_scrollview = (ScrollView) findViewById(R.id.scrollContDisplay);

        text = "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "C-C-|G-G-|A-A-|G---";

        cont_view.setText(text);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
            }
        });

    }


    private void startPlaying(){

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processAudio();
            }
        });
        recordingThread.start();
    }

    private void stopPlaying(){
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private void processAudio(){

        final short[] temp_buffer = new short[FRAME_SIZE*4];
        final boolean flag = false;

        while (isRecording){

            i += 1;

            recorder.read(data, 0, FRAME_SIZE);

            processingThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    if (i == 4){
                        previous_frame_energy = energyODF(Arrays.copyOfRange(temp_buffer,2*FRAME_SIZE,3*FRAME_SIZE));
                        //System.arraycopy(temp_buffer, FRAME_SIZE, temp_buffer, 0, FRAME_SIZE*3);
                    }

                    if (i >= 4){

                        System.arraycopy(data, 0, temp_buffer, 3*FRAME_SIZE, FRAME_SIZE);

                        if ((i - 4) % 4 == 0) {

                            if(detectOnsets(Arrays.copyOfRange(temp_buffer,3*FRAME_SIZE,4*FRAME_SIZE), previous_frame_energy)){
                                cont_view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        cont_scrollview.smoothScrollBy(0,50);
                                        count += 1;
                                        amdf_array = amdf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);
                                        pitch = checkPitch(amdf_array, SAMPLE_LAG);
                                    }
                                });
                            }

                        }
                        System.arraycopy(temp_buffer, FRAME_SIZE, temp_buffer, 0, FRAME_SIZE*3);

                        previous_frame_energy = energyODF(Arrays.copyOfRange(temp_buffer,3*FRAME_SIZE,4*FRAME_SIZE));

                    }

                    else {
                        System.arraycopy(data, 0, temp_buffer, (i-1)*FRAME_SIZE, FRAME_SIZE);
                    }

                }
            });

            processingThread.start();

            amdfThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        amdf_array = amdf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);

                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });

            acfThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        acf_array = acf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);

                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });

            pitchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(0);

                        pitch = detectPitch(amdf_array, acf_array, SAMPLE_LAG);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pitch_view.setText(Double.toString(pitch));
                                //pitch_view.setText(Math.abs(prev_E - energyODF(buffer))+"  "+ Integer.toString(i));
                            }
                        });
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
            });

            //amdfThread.start();
            //acfThread.start();
            //pitchThread.start();


            /*if (flag == true) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitch_view.setText(Integer.toString(i));
                    }
                });
            }

            acfThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        acf_array = acf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);

                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });

            amdfThread.start();
            acfThread.start();
            pitchThread.start();*/

        }
    }

    private int[] amdf(short[] input, int Fs, int sample_lag){

        // conversion of short array of inputs to int array

        int int_input[] = new int[input.length];

        for (int i=0; i<input.length; i++){

            int_input[i] = input[i];

        }


        // array to store output AMDF values

        int amdf_frame[] = new int[sample_lag];


        // length of array that is to be shifted in time domain

        int frame_length = input.length - sample_lag;


        // array that is to be shifted in time domain

        int shift_frame[] = new int[frame_length];


        // array to store absolute-difference values

        int abs_frame[] = new int[frame_length];


        // putting values to shift_frame

        System.arraycopy(int_input, 0, shift_frame, 0, frame_length);


        // variable to store sum of each of the absolute-difference frames

        int sum;

        int[] temp = new int[frame_length];


        // AMDF

        for (int i=0; i<sample_lag; i++){

            sum = 0;

            System.arraycopy(int_input, i, temp, 0, frame_length);

            // creating abs_frame and summing values

            for (int j=0; j<frame_length; j++){

                abs_frame[j] = Math.abs(shift_frame[j] - temp[j]);

                sum += abs_frame[j];

            }


            // taking sum of absolute-difference values

            amdf_frame[i] = sum;
        }

        return amdf_frame;
    }

    private long[] acf(short input[], int Fs, int sample_lag){

        // conversion of short array of inputs to int array

        int int_input[] = new int[input.length];

        for (int i=0; i<input.length; i++){

            int_input[i] = input[i];

        }


        // array to store output ACF values

        long acf_frame[] = new long[sample_lag];


        // length of array that is to be shifted in time domain

        int frame_length = input.length - sample_lag;


        // array that is to be shifted in time domain

        int shift_frame[] = new int[frame_length];


        // array to store absolute-multiplication values

        int abs_frame[] = new int[frame_length];


        // putting values to shift_frame

        System.arraycopy(int_input, 0, shift_frame, 0, frame_length);


        long sum;

        int[] temp = new int[frame_length];

        // ACF

        for (int i=0; i<sample_lag; i++){

            sum = 0;

            System.arraycopy(int_input, i, temp, 0, frame_length);

            // creating abs_frame

            for (int j=0; j<frame_length; j++){

                abs_frame[j] = Math.abs(shift_frame[j]*temp[j]);

                sum += abs_frame[j];

            }


            // taking sum of auto-correlation values

            acf_frame[i] = sum;
        }

        return acf_frame;
    }

    private double detectPitch(int[] amdf_frame, long[] acf_frame, int lag){

        double[] temp_acf = new double[acf_frame.length];

        double[] temp_amdf = new double[amdf_frame.length];

        for (int i=0; i<lag; i++){
            temp_acf[i] = acf_frame[i];
            temp_amdf[i] = amdf_frame[i];
        }

        Arrays.sort(acf_frame);

        Arrays.sort(amdf_frame);

        long max_acf = acf_frame[acf_frame.length - 1];

        int max_amdf = amdf_frame[amdf_frame.length - 1];

        double[] multiplied = new double[lag];

        if (max_amdf == 0) return 5;

        else {
            for (int i = 0; i < lag; i++) {
                temp_acf[i] = temp_acf[i] / max_acf;
            }

            for (int i = 0; i < lag; i++) {

                if (temp_amdf[i] == 0) continue;

                else temp_amdf[i] = 1 / temp_amdf[i];
            }

            double[] dup = new double[temp_amdf.length];

            for (int i=0; i<lag; i++){
                dup[i] = temp_amdf[i];
            }

            Arrays.sort(dup);

            double max_temp_amdf = dup[dup.length - 1];

            for (int i = 0; i < lag; i++){

                temp_amdf[i] = temp_amdf[i] / max_temp_amdf;

                multiplied[i] = temp_amdf[i];
            }

            boolean flag = false;

            double temp = multiplied[1];

            int[] peaks = new int[2];

            peaks[0] = 1;

            for (int i = 2; i < multiplied.length; i++) {
                if (flag == true) {
                    if (multiplied[i] < temp && temp - multiplied[i] > 0.001) {
                        peaks[1] = i;
                        break;
                    }
                }

                if (multiplied[i] > temp) {
                    flag = true;
                } else flag = false;

                temp = multiplied[i];
            }

            double pitch_in_hertz = RECORDER_SAMPLERATE / (peaks[1] - peaks[0]);

            return pitch_in_hertz;
        }

    }

    private boolean detectOnsets(final short[] buffer, final long prev_E){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pitch_view.setText(Boolean.toString(Math.abs(prev_E - energyODF(buffer)) > 1500)+"  "+count+"  "+pitch);
                //pitch_view.setText(Math.abs(prev_E - energyODF(buffer))+"  "+ Integer.toString(i));
            }
        });

        if (Math.abs(prev_E - energyODF(buffer)) > 1500){

            return true;

        }

        else return false;

    }

    private long energyODF(short[] buffer){

        long energy = 0;

        int int_buffer[] = new int[buffer.length];

        for (int i=0; i<buffer.length; i++){
            int_buffer[i] = buffer[i]*buffer[i];
            energy += int_buffer[i];
        }

        return energy/1000000;
    }

    private double checkPitch(int[] amdf_frame, int lag){

        final double[] temp_amdf = new double[amdf_frame.length];

        for (int i=0; i<lag; i++){
            temp_amdf[i] = amdf_frame[i];
        }

        Arrays.sort(amdf_frame);

        int max_amdf = amdf_frame[amdf_frame.length - 1];

        if(max_amdf == 0) return 0;

        else{

            for (int i = 0; i < lag; i++) {

                if (temp_amdf[i] == 0) continue;

                else temp_amdf[i] = 1 / temp_amdf[i];
            }

            double[] dup = new double[temp_amdf.length];

            for (int i=0; i<lag; i++){
                dup[i] = temp_amdf[i];
            }

            double max_temp_amdf = dup[dup.length - 1];

            for (int i = 0; i < lag; i++){

                temp_amdf[i] = temp_amdf[i] / max_temp_amdf;
            }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitch_view.setText(Double.toString(temp_amdf[62]));
                        //pitch_view.setText(Math.abs(prev_E - energyODF(buffer))+"  "+ Integer.toString(i));
                    }
                });

            //if(temp_amdf[62] > 0.5) return true;
            return temp_amdf[62];

        }
    }
}

































