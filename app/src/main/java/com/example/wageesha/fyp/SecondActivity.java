package com.example.wageesha.fyp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SecondActivity extends Activity {

    private Button next_button;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        tv = (TextView) findViewById(R.id.text_HelloUser);
        SpannableString ss = new SpannableString("Great!\n\nSelect your preferred note display method");
        ss.setSpan(new RelativeSizeSpan(3f), 0, 6, 0);
        ss.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 6, 0);
        tv.setText(ss);

        next_button = (Button) findViewById(R.id.buttonGoBack);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPage2();
            }
        });
    }

    private void goToPage2(){
        Intent intent = new Intent(this, ThirdActivity.class);
        startActivity(intent);
    }
}
