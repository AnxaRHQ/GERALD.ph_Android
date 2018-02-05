package anxa.com.geraldph;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;



public class MainActivity extends AppCompatActivity {

    public WebView mainContentWebView;
    private LinearLayout mlLayoutRequestError = null;

    String URLPath = "";
    public String contentString = "";
    Boolean isConnected = false;

    final String appShortName = "GeraldPH";


    ProgressBar myProgressBar;
    ImageButton forwardBrowserButton, backBrowserButton, refreshBrowserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fabric.with(this, new Crashlytics());

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
                mlLayoutRequestError.setVisibility(View.VISIBLE);
                if (checkConnection())
                    mainContentWebView.goForward();
            }

        });
        backBrowserButton = (ImageButton) findViewById(R.id.back);
        backBrowserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mlLayoutRequestError.setVisibility(View.VISIBLE);
                if (checkConnection())
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

        //custom user agent
        String defaultagent = mainContentWebView.getSettings().getUserAgentString();
        if (defaultagent == null)
            defaultagent = getDefaultUserAgent();
        mainContentWebView.getSettings().setUserAgentString(appShortName + "/" + BuildConfig.VERSION_NAME + " " + getDeviceName() +  " Mobile " + defaultagent);
        System.out.println(appShortName + "/" + BuildConfig.VERSION_NAME + " " + getDeviceName() +  " Mobile " + defaultagent);


    }


    private boolean checkConnection () {

        myProgressBar.setVisibility(View.INVISIBLE);


        if (Utility.isNetworkAvailable(this)){
            System.out.println("OK!");
            isConnected = true;

            mlLayoutRequestError.setVisibility(View.INVISIBLE);
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

    /* Custom userAget */
    private static String getDefaultUserAgent(){
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version")); // such as 1.1.0
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE; // "1.0" or "3.4b5"
        result.append(version.length() > 0 ? version : "1.0");

        // add the model for the release build
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }
        String id = Build.ID; // "MASTER" or "M4-rc20"
        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }
        result.append(")");
        return result.toString();
    }

    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + "/" + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }




}
