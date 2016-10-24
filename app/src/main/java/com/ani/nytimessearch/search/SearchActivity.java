package com.ani.nytimessearch.search;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements FilterFragment.Listener {

    private final NYTClient nytClient = new NYTClient();
    private Filter filter;
    private RecyclerView rvArticles;
    private ArticlesAdapter articlesAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private List<Article> articles = new ArrayList<>();

    private int page = 0;
    @Nullable
    private String query = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rvArticles = (RecyclerView) findViewById(R.id.rvArticles);

        articlesAdapter = new ArticlesAdapter(this, articles);
        rvArticles.setAdapter(articlesAdapter);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvArticles.setLayoutManager(layoutManager);

        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        Article article = articles.get(position);
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
                }
        );
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                SearchActivity.this.page = SearchActivity.this.page + 1;
                onArticleSearch(query, SearchActivity.this.page);
            }
        };
        rvArticles.addOnScrollListener(scrollListener);

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
                SearchActivity.this.page = 0;
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
            showErrorToast("Enter a non-empty search query");
            return;
        }

        if (!isNetworkAvailable() || !isOnline()) {
            showErrorToast("Check network availability");
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
                        articles.clear();
                        scrollListener.resetState();
                        articlesAdapter.notifyDataSetChanged();
                    }
                    articles.addAll(Article.fromJSONArray(docs));
                    articlesAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(SearchActivity.class.getCanonicalName(), "error while reading json", e);
                    showErrorToast(null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                    JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());

                String message = null;
                try {
                    message = errorResponse.getString("message");
                } catch (JSONException e) {
                    Log.e(SearchActivity.class.getCanonicalName(), "error while reading json", e);
                }

                showErrorToast(message);
            }
        });
    }

    @Override
    public void onFinishFilterDialog(Filter filter) {
        this.filter = filter;
        this.page = 0;
        onArticleSearch(query, page);
    }

    private void showErrorToast(@Nullable String message) {
        String defaultMessage = "Error loading results from network";
        Toast.makeText(this, message == null ? defaultMessage : message, Toast.LENGTH_LONG).show();
    }


    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
