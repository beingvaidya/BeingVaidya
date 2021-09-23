package com.mayurkakade.beingvaidya.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mayurkakade.beingvaidya.R;

public class HowToAddPatientsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_patients);
//
//        String frameVideo = "<html><body><br><iframe width=\"320\" height=\"200\" src=\"https://www.youtube.com/watch?v=KPK23OVr95k\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
//        WebView displayYoutubeVideo = (WebView) findViewById(R.id.videoView);
//        displayYoutubeVideo.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//        });
//        WebSettings webSettings = displayYoutubeVideo.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        displayYoutubeVideo.loadData(frameVideo, "text/html", "utf-8");

        Button btn_open = findViewById(R.id.btn_open);
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=KPK23OVr95k")));

            }
        });
    }


}