package com.ani.nytimessearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class SearchActivity extends AppCompatActivity {

    private static final String SEARCH_URL =
            "https://api.nytimes.com/svc/search/v2/articlesearch.json";

    private EditText etQuery;
    private Button btnSearch;
    private GridView gvResults;

    private ArticleArrayAdapter articleArrayAdapter;

    private List<Article> articles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupViews();

        articleArrayAdapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(articleArrayAdapter);
    }

    private void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        gvResults = (GridView) findViewById(R.id.gvResults);
    }

    public void onArticleSearch(View view) {
        String query = etQuery.getText().toString();
//        Toast.makeText(this, query, Toast.LENGTH_LONG).show();

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("api-key", "8c025c37c53d4142b27e4484efa7e7dd");
        params.put("q", query);
        params.put("page", 0);

        client.get(SEARCH_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray docs;

                try {
                    docs = response.getJSONObject("response").getJSONArray("docs");
                    articleArrayAdapter.clear();
                    articleArrayAdapter.addAll(Article.fromJSONArray(docs));
                    Log.d("DEBUG", articles.toString());
                } catch (JSONException e) {
                    Log.e(SearchActivity.class.getCanonicalName(), "error while reading json", e);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse); // FIXME
            }
        });
    }
}
