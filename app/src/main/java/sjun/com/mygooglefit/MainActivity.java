package sjun.com.mygooglefit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @Bind(R.id.webview)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // start google login

        // after logging in
        setWebView();
    }

    private void setWebView() {
        // web setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // load html
        webView.loadUrl("file:///android_asset/index.html");
    }
}
