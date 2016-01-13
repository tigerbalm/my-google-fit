package sjun.com.mygooglefit.network;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import java.util.Arrays;

import io.reark.reark.network.fetchers.Fetcher;
import io.reark.reark.network.fetchers.FetcherManagerBase;
import io.reark.reark.network.fetchers.UriFetcherManager;
import sjun.com.mygooglefit.network.fetchers.DataSourceFetcher;
import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetworkService extends IntentService {
    private FetcherManagerBase<Uri> fetcherManager;

    // TODO: Rename actions, choose action names that describe tasks that this
    public NetworkService() {
        super("NetworkService");

        Timber.d("NetworkService is created!");

        fetcherManager = createFetcherManager();
    }

    private FetcherManagerBase<Uri> createFetcherManager() {
        return new UriFetcherManager.Builder()
                .fetchers(Arrays.asList(
                        new DataSourceFetcher(networkRequestStatus
                                -> Timber.d(networkRequestStatus.getStatus()))
                        )
                )
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            Timber.d("NetworkService.onHandleIntent is called : %s", action);

            final String serviceUriString = intent.getStringExtra("serviceUriString");
            if (serviceUriString != null) {
                final Uri serviceUri = Uri.parse(serviceUriString);
                Fetcher matchingFetcher = fetcherManager.findFetcher(serviceUri);
                if (matchingFetcher != null) {
                    Timber.v("Fetcher found for %s", serviceUri);
                    matchingFetcher.fetch(intent);
                } else {
                    Timber.e("Unknown Uri %s", serviceUri);
                }
            } else {
                Timber.e("No Uri defined");
            }
        }
    }
}
