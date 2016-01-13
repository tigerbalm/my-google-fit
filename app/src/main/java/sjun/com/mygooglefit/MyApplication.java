package sjun.com.mygooglefit;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by user on 2016-01-10.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
