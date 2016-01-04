package sjun.com.mygooglefit;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by user on 2016-01-05.
 */
public class WebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getWorkoutDataOn(long timemillis) {
        return "[{\n" +
                "            \"value\": 300,\n" +
                "            \"color\":\"#F7464A\",\n" +
                "            \"highlight\": \"#FF5A5E\",\n" +
                "            \"label\": \"Walking\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 50,\n" +
                "            \"color\": \"#46BFBD\",\n" +
                "            \"highlight\": \"#5AD3D1\",\n" +
                "            \"label\": \"Running\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 100,\n" +
                "            \"color\": \"#FDB45C\",\n" +
                "            \"highlight\": \"#FFC870\",\n" +
                "            \"label\": \"Cycling\"\n" +
                "        }]";
    }
}
