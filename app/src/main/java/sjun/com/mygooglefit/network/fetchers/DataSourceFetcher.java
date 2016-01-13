package sjun.com.mygooglefit.network.fetchers;

import android.content.Intent;
import android.net.Network;
import android.support.annotation.NonNull;

import java.util.List;

import io.reark.reark.network.fetchers.FetcherBase;
import io.reark.reark.pojo.NetworkRequestStatus;
import io.reark.reark.utils.Log;
import retrofit.client.OkClient;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sjun.com.mygooglefit.data.GoogleFitDataSource;
import sjun.com.mygooglefit.network.NetworkApi;
import timber.log.Timber;

/**
 * Created by user on 2016-01-10.
 */
public class DataSourceFetcher extends FetcherBase {
    NetworkApi networkApi;

    public DataSourceFetcher(@NonNull Action1<NetworkRequestStatus> updateNetworkRequestStatus) {
        super(updateNetworkRequestStatus);

        networkApi = new NetworkApi(new OkClient());
    }

    @Override
    public void fetch(Intent intent) {
        Subscription subscription = createNetworkObservable()
                .subscribeOn(Schedulers.computation())
                .doOnError(doOnError("testUri"))
                .doOnCompleted(() -> completeRequest("testUri"))
                .subscribe(googleFitDataSources -> Timber.d("datasource.lenght: " + googleFitDataSources.size()),
                        e -> Timber.e("Error fetching google fit datasources ", e));
        requestMap.put(1, subscription);
        startRequest("testUri");
    }

    @NonNull
    private Observable<List<GoogleFitDataSource>> createNetworkObservable() {
        return networkApi.getDataSources();
    }

    @Override
    public Object getServiceUri() {
        return null;
    }
}
