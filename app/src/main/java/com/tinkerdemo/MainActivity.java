package com.tinkerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void Toast(View v) {
        Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }

    public void TestCrash(View v){
        Toast.makeText(this, ToastString(), Toast.LENGTH_SHORT).show();
    }

    private String ToastString(){
        String str = null;
       // Log.d("Bruce","------1111111111111---------"+str.length());
        return "this is test base 补丁 bugly";
    }
}
