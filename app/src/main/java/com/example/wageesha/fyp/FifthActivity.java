package com.example.wageesha.fyp;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    private TextView cont_view;
    private TextView pitch_view;
    private File file;
    private Context context;

    private Button start_button;
    private Button stop_button;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;

    private Thread recordingThread = null;
    private Thread pitchThread = null;
    private Thread amdfThread = null;
    private Thread acfThread = null;
    private boolean isRecording = false;

    private int BufferElements2Rec = 2000;
    private int BytesPerElement = 2;

    private int[] amdf_array = new int[SAMPLE_LAG];
    private long[] acf_array = new long[SAMPLE_LAG];
    private short[] data = new short[BufferElements2Rec];
    private double pitch;
    private double i = 0;

    private static final int SAMPLE_LAG = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fifth);

        String text;

        start_button = (Button) findViewById(R.id.buttonStart);
        stop_button = (Button) findViewById(R.id.buttonStop);

        cont_view = (TextView) findViewById(R.id.viewContDisplay);
        pitch_view = (TextView) findViewById(R.id.viewPitchDisplay);

        cont_view.setBackgroundColor(Color.rgb(255,255,255));
        pitch_view.setBackgroundColor(Color.rgb(255,255,255));

        text = "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "G-G-|F-F-|E-E-|D---\n" +
                "C-C-|G-G-|A-A-|G---\n" +
                "F-F-|E-E-|D-D-|C---";

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

        while (isRecording){

            i += 1;

            recorder.read(data, 0, BufferElements2Rec);

            if (i%4 == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitch_view.setText(Double.toString(pitch));
                    }
                });
            }

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

                    pitch = detectPitch(amdf_array, acf_array, SAMPLE_LAG);

                }
            });

            amdfThread.start();
            acfThread.start();
            pitchThread.start();

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

        if (max_acf == 0 || max_amdf == 0) return 5;

        else {
            for (int i = 0; i < lag; i++) {
                temp_acf[i] = temp_acf[i] / max_acf;
            }

            for (int i = 0; i < lag; i++) {

                if (temp_amdf[i] == 0) continue;

                else temp_amdf[i] = 1 / temp_amdf[i];
            }

            double[] dup = temp_amdf;

            Arrays.sort(dup);

            double max_temp_amdf = dup[dup.length - 1];

            for (int i = 0; i < lag; i++){

                temp_amdf[i] = temp_amdf[i] / max_temp_amdf;

                multiplied[i] = temp_amdf[i] * temp_acf[i];
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

            double pitch_in_hertz = RECORDER_SAMPLERATE / (peaks[1] - peaks[0] - 1);

            return pitch_in_hertz;
        }

    }
}
