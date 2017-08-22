package anxa.com.geraldph;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ivanaanxa on 8/18/2017.
 */

public class SplashActivity extends Activity{

    protected boolean _active = true;
    protected int _splashTime = 3000; // time to display the splash screen in ms
    boolean canTouch;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                                /* Step 2: Create an Intent that will start the HapilabsMainActivity. */
                Intent mainIntent;

                mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, _splashTime);
    }



}
