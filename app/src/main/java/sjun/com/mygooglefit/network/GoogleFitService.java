package sjun.com.mygooglefit.network;

import android.net.Uri;

import retrofit.http.GET;
import rx.Observable;

/**
 * Created by user on 2016-01-10.
 */
public interface GoogleFitService {
    Uri DATASOURCE_SEARCH = Uri.parse("googlefit/datasorce/get");

    @GET("/v1/users/me/dataSources")
    Observable<DataSourcesResult> getDataSource();
}
