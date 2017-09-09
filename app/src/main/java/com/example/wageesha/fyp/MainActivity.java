package com.example.wageesha.fyp;

import android.content.Intent;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Button next_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        next_button = (Button) findViewById(R.id.buttonNext);

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPage1();
            }
        });
    }

    private void goToPage1(){

        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
