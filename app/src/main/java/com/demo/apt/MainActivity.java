package com.demo.apt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.demo.annotation.BindView;
import com.demo.annotation.DIActivity;
import com.demo.bind.BindView_MainActivity;

@DIActivity
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textView1)
    public TextView textView1;
    @BindView(R.id.textView2)
    public TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindView_MainActivity.bind(this);
        textView1.setText("我被Bind了");
        textView2.setText("我也被Bind了");
    }
}
