package sjun.com.mygooglefit.network;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import io.reark.reark.utils.Preconditions;
import retrofit.RestAdapter;
import retrofit.client.Client;
import rx.Observable;
import sjun.com.mygooglefit.data.GoogleFitDataSource;

/**
 * Created by user on 2016-01-11.
 */
public class NetworkApi {
    private final GoogleFitService googleFitService;

    public NetworkApi(@NonNull Client client) {
        Preconditions.checkNotNull(client, "Client cannot be null.");

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("https://www.googleapis.com/fitness")
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .build();
        googleFitService = restAdapter.create(GoogleFitService.class);
    }

    public Observable<List<GoogleFitDataSource>> getDataSources() {
        return googleFitService.getDataSource()
                .map(DataSourcesResult::getItems);
    }
}
