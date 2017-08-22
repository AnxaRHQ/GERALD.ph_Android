package anxa.com.geraldph;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    public WebView mainContentWebView;
    private static final int ERROR_ACTIVITY = 111;
    private LinearLayout mlLayoutRequestError = null;

    String URLPath = "";
    public String contentString = "";
    Boolean isConnected = false;


    ProgressBar myProgressBar;
    ImageButton forwardBrowserButton, backBrowserButton, refreshBrowserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        URLPath = "http://shop.gerald.ph/";

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mlLayoutRequestError = (LinearLayout) findViewById(R.id.lLayoutRequestError);

        // Save the web view
        mainContentWebView = (WebView) findViewById(R.id.maincontentWebView);

        forwardBrowserButton = (ImageButton) findViewById(R.id.forward);
        forwardBrowserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mainContentWebView.goForward();
            }

        });
        backBrowserButton = (ImageButton) findViewById(R.id.back);
        backBrowserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mainContentWebView.goBack();
            }
        });
        refreshBrowserButton = (ImageButton) findViewById(R.id.refresh);
        refreshBrowserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (checkConnection()){
                    //mainContentWebView.reload();
                    loadWebPage();
                }
            }
        });

        // Initialized progress bar
        myProgressBar = (ProgressBar) findViewById(R.id.progressbar);


        //prevent horizontal scrolling
        mainContentWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mainContentWebView.getSettings().setJavaScriptEnabled(true);

        if (checkConnection()){
            loadWebPage();
            isConnected = true;
        }

    }


    private boolean checkConnection () {

        myProgressBar.setVisibility(View.INVISIBLE);

        if (Utility.isNetworkAvailable(this)){
            System.out.println("OK!");
            isConnected = true;
        }
        else {
            isConnected = false;
            String message = getResources().getString(R.string.ALERTMESSAGE_OFFLINE);
            final String messageDialog = message;
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    showDialog(messageDialog, getResources().getString(R.string.app_name));
                }
            });
        }

        return isConnected;
    }

    public static class Utility {
        public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private void showDialog(String message, String title) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }



    private void loadWebPage() {

        mlLayoutRequestError.setVisibility(View.INVISIBLE);

        WebSettings webSettings = mainContentWebView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setGeolocationEnabled(true);
        webSettings.setLoadWithOverviewMode(true);

        mainContentWebView.setWebViewClient(new MyAppWebViewClient());
        mainContentWebView.setWebViewClient(new MyAppWebViewClient());

        System.out.println("URL=" + contentString);
        System.out.println("URL_Path=" + URLPath);

        // added this code to ensure there is a default page that will be loaded (bug fix)
        if(contentString != null){
            if (contentString.contains("http://")) {
                mainContentWebView.loadUrl(contentString);
            }
            else {
                mainContentWebView.loadUrl(URLPath + contentString);
            }
        }else {
            mainContentWebView.loadUrl(URLPath);
        }
        isConnected = true;
    }

    private class MyAppWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (checkConnection() && isConnected == false)
                loadWebPage();
            myProgressBar.setVisibility(View.VISIBLE);
            contentString = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            if (myProgressBar != null)
                myProgressBar.setVisibility(View.INVISIBLE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mlLayoutRequestError.setVisibility(View.VISIBLE);
            System.out.println ("ERROR: " + description +  ";" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }


    private class MyWebChromeClient extends WebChromeClient {
        // page loading progress, gone when fully loaded
        public void onProgressChanged(WebView view, int progress) {
            if (progress < 100 && myProgressBar.getVisibility() == ProgressBar.GONE) {
                myProgressBar.setVisibility(ProgressBar.VISIBLE);
            }
            myProgressBar.setProgress(progress);
            if (progress == 100) {
                myProgressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }





}
