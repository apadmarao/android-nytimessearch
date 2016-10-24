package com.ani.nytimessearch.search;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.ani.nytimessearch.R;
import com.ani.nytimessearch.filter.FilterFragment;
import com.ani.nytimessearch.nytclient.Article;
import com.ani.nytimessearch.nytclient.Filter;
import com.ani.nytimessearch.nytclient.NYTClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements FilterFragment.Listener {

    private final NYTClient nytClient = new NYTClient();
    private Filter filter;
    private GridView gvResults;

    private ArticleArrayAdapter articleArrayAdapter;

    private List<Article> articles = new ArrayList<>();

    private int page = 0;
    private int lastQuerySize = 10;
    @Nullable
    private String query = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gvResults = (GridView) findViewById(R.id.gvResults);

        articleArrayAdapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(articleArrayAdapter);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = articleArrayAdapter.getItem(position);
                // open up webview
                // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                // set toolbar color and/or setting custom actions before invoking build()
                builder.setToolbarColor(ContextCompat.getColor(SearchActivity.this, R.color.colorAccent));
                // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
                CustomTabsIntent customTabsIntent = builder.build();
                // and launch the desired Url with CustomTabsIntent.launchUrl()
                customTabsIntent.launchUrl(SearchActivity.this, Uri.parse(article.getWebUrl()));
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if (lastQuerySize > 0) {
                    page = page + 1;
                    onArticleSearch(query, page);
                    return true;
                }

                return false; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        filter = new Filter();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform search query
                page = 0;
                lastQuerySize = 10;
                SearchActivity.this.query = query;
                onArticleSearch(query, page);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showFilterDialog();
                return true;
            }
        });

        return true;
    }

    private void showFilterDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FilterFragment filterFragment = FilterFragment.newInstance(filter);
        filterFragment.show(fm, "fragment_filter");
    }

    private void onArticleSearch(String query, final int page) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(this, "Please enter a non-empty search query", Toast.LENGTH_LONG).show();
            return;
        }

        nytClient.articleSearch(query, page, filter, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray docs;

                try {
                    docs = response.getJSONObject("response").getJSONArray("docs");
                    if (page == 0) {
                        articleArrayAdapter.clear();
                    }
                    List<Article> articles = Article.fromJSONArray(docs);
                    articleArrayAdapter.addAll(articles);
                    lastQuerySize = articles.size();
                } catch (JSONException e) {
                    Log.e(SearchActivity.class.getCanonicalName(), "error while reading json", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                    JSONObject errorResponse) {
                Toast.makeText(SearchActivity.this, "Recieved error", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onFinishFilterDialog(Filter filter) {
        this.filter = filter;
        this.page = 0;
        this.lastQuerySize = 10;
        onArticleSearch(query, page);
    }
}
