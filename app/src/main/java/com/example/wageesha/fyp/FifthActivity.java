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
    private boolean isRecording = false;

    private int[] amdf_array;
    private int[] acf_array;
    private short[] data;
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

    private int BufferElements2Rec = 2000;
    private int BytesPerElement = 2;

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
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pitch_view.setText(Double.toString(i));
                }
            });*/
        }
    }

    private void processAudio(){
        data = new short[BufferElements2Rec];

        amdf_array = new int[SAMPLE_LAG];
        acf_array = new int[SAMPLE_LAG];


        while (isRecording){

            i += 1;

            recorder.read(data, 0, BufferElements2Rec);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pitch_view.setText(Double.toString(pitch));
                }
            });

            pitchThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    amdf_array = amdf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);

                    acf_array = acf(data, RECORDER_SAMPLERATE, SAMPLE_LAG);

                    pitch = detectPitch(amdf_array, acf_array, SAMPLE_LAG);

                }
            });
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


        // AMDF

        for (int i=0; i<sample_lag; i++){

            sum = 0;

            // creating abs_frame and summing values

            for (int j=0; j<frame_length; j++){

                abs_frame[j] = Math.abs(shift_frame[j] - Arrays.copyOfRange(int_input, i, i+frame_length)[j]);

                sum += abs_frame[j];

            }


            // taking sum of absolute-difference values

            amdf_frame[i] = sum;
        }

        return amdf_frame;
    }

    private int[] acf(short input[], int Fs, int sample_lag){

        // conversion of short array of inputs to int array

        int int_input[] = new int[input.length];

        for (int i=0; i<input.length; i++){

            int_input[i] = input[i];

        }


        // array to store output AMDF values

        int acf_frame[] = new int[sample_lag];


        // length of array that is to be shifted in time domain

        int frame_length = input.length - sample_lag;


        // array that is to be shifted in time domain

        int shift_frame[] = new int[frame_length];


        // array to store absolute-difference values

        int abs_frame[] = new int[frame_length];


        // putting values to shift_frame

        System.arraycopy(int_input, 0, shift_frame, 0, frame_length);


        int sum;

        // ACF

        for (int i=0; i<sample_lag; i++){

            sum = 0;

            // creating abs_frame

            for (int j=0; j<frame_length; j++){

                abs_frame[j] = Math.abs(shift_frame[j]*Arrays.copyOfRange(int_input, i, i+frame_length)[j]);

                sum += abs_frame[j];

            }


            // taking sum of auto-correlation values

            acf_frame[i] = sum;
        }

        return acf_frame;
    }

    private double detectPitch(int[] amdf_frame, int[] acf_frame, int lag){

        Arrays.sort(acf_frame);

        Arrays.sort(amdf_frame);

        int max_acf = acf_frame[acf_frame.length - 1];

        int max_amdf = amdf_frame[amdf_frame.length - 2];

        int[] multiplied = new int[lag];

        for (int i=0; i<lag; i++){
            acf_frame[i] = acf_frame[i]/max_acf;
        }

        for (int i=0; i<lag; i++){
            amdf_frame[i] = 1/amdf_frame[i];
            amdf_frame[i] = amdf_frame[i]/max_amdf;
            multiplied[i] = amdf_frame[i]*acf_frame[i];
        }

        boolean flag = false;
        int temp = multiplied[0];
        int[] peaks = new int[2];
        peaks[0] = 0;

        for (int i=1; i<multiplied.length; i++){
            if (flag == true){
                if (multiplied[i] < temp && temp - multiplied[i] > 0.05){
                    peaks[2] = i;
                    break;
                }
            }

            if (multiplied[i] > temp){
                flag = true;
            }

            else flag = false;

            temp = multiplied[i];
        }

        double pitch_in_hertz = 16000/(peaks[1] - peaks[0] - 1);

        return pitch_in_hertz;

    }
}
