package as.tra.brayden.blesim;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public abstract class AppBarActivity extends AppCompatActivity {



    private static final String TAG = AppBarActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }


    protected boolean startActivityIfNotThis(Class<? extends Activity> activity) {
        if(this.getClass().equals(activity)) return false;

        Intent intent = new Intent(this, activity);
        startActivity(intent);
        return true;
    }



    public boolean openSupport(MenuItem item) {
        return startActivityIfNotThis(SupportActivity.class);
    }


}
