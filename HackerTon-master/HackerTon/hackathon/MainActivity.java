package com.example.hackathon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabHost;

import com.example.hackathon.R;

/**
 * Created by loki3 on 2017-12-19.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabhost =  (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();

        TabHost.TabSpec ts1 = tabhost.newTabSpec("tab spec1");
        ts1.setContent(R.id.tab1) ;
        ts1.setIndicator("TAB 1") ;
        tabhost.addTab(ts1) ;

        TabHost.TabSpec ts2 = tabhost.newTabSpec("tab spec2");
        ts2.setContent(R.id.tab2) ;
        ts2.setIndicator("TAB 2") ;
        tabhost.addTab(ts2) ;

        
    }
}
