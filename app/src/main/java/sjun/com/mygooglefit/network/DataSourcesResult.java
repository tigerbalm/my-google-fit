package sjun.com.mygooglefit.network;

import android.support.annotation.NonNull;

import java.util.List;

import sjun.com.mygooglefit.data.GoogleFitDataSource;

/**
 * Created by user on 2016-01-10.
 */
public class DataSourcesResult {
    @NonNull
    final private List<GoogleFitDataSource> items;

    public DataSourcesResult(@NonNull final List<GoogleFitDataSource> items) {
        this.items = items;
    }

    @NonNull
    public List<GoogleFitDataSource> getItems() {
        return items;
    }
}
