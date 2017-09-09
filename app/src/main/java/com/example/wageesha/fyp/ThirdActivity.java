package com.example.wageesha.fyp;

import android.content.Intent;
import android.graphics.Color;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ThirdActivity extends Activity {

    private ImageView iv1;
    private ImageView iv2;

    private Button continuous_button;
    private Button linear_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        iv1 = (ImageView) findViewById(R.id.viewImage1);
        iv1.setBackgroundColor(Color.rgb(0,0,255));

        iv2 = (ImageView) findViewById(R.id.viewImage2);
        iv2.setBackgroundColor(Color.rgb(0,0,255));

        continuous_button = (Button) findViewById(R.id.buttonContinuous);
        linear_button = (Button) findViewById(R.id.buttonLinear);

        continuous_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextPage1();
            }
        });

        linear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextpage2();
            }
        });

    }

    private void goToNextPage1(){
        Intent intent = new Intent(this, FourthActivity.class);
        intent.putExtra("SESSION_ID", true);
        startActivity(intent);
    }

    private void goToNextpage2(){
        Intent intent = new Intent(this, FourthActivity.class);
        intent.putExtra("SESSION_ID", false);
        startActivity(intent);
    }
}
