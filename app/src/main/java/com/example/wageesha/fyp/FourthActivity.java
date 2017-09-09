package com.example.wageesha.fyp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FourthActivity extends Activity {

    private Boolean id;
    private Button beat_button;
    private Button free_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);

        id = getIntent().getBooleanExtra("SESSION_ID", true);

        beat_button = (Button) findViewById(R.id.buttonBeatPlay);
        free_button = (Button) findViewById(R.id.buttonFreePlay);

        beat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextPage1();
            }
        });

        free_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextPage2();
            }
        });

    }

    private void goToNextPage1(){
        if (id){
            Intent intent = new Intent(this, FifthActivity.class);
            intent.putExtra("PLAY_ID", "beatplay");
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, SixthActivity.class);
            intent.putExtra("PLAY_ID", "beatplay");
            startActivity(intent);
        }
    }

    private void goToNextPage2(){
        if (id){
            Intent intent = new Intent(this, FifthActivity.class);
            intent.putExtra("PLAY_ID", "freeplay");
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, SixthActivity.class);
            intent.putExtra("PLAY_ID", "freeplay");
            startActivity(intent);
        }
    }
}
