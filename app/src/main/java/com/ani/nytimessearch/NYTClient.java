package com.ani.nytimessearch;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

public class NYTClient {

    private static final String SEARCH_URL =
            "https://api.nytimes.com/svc/search/v2/articlesearch.json";

    private static final String API_KEY = "api-key";
    private static final String QUERY = "q";
    private static final String PAGE = "page";
    private static final String SORT = "sort";
    private static final String FILTER_QUERY = "fq";
    private static final String BEGIN_DATE = "begin_date";

    private final AsyncHttpClient client = new AsyncHttpClient();

    public void articleSearch(String query, int page, Filter filter,
            JsonHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put(API_KEY, "8c025c37c53d4142b27e4484efa7e7dd");
        params.put(QUERY, query);
        params.put(PAGE, page);

        if (filter.getSort() != Filter.Sort.RELEVANCE) {
            params.put(SORT, filter.getSort().getValue());
        }

        if (!filter.getNewsDesks().isEmpty()) {
            params.put(FILTER_QUERY, newsDeskValue(filter.getNewsDesks()));
        }

        if (filter.getBeginDate() != null) {
            params.put(BEGIN_DATE, dateValue(filter.getBeginDate()));
        }

        Log.i("QUERY", "URL: " + AsyncHttpClient.getUrlWithQueryString(true, SEARCH_URL, params));
        client.get(SEARCH_URL, params, responseHandler);
    }

    private String newsDeskValue(Set<String> newsDesks) {
        StringBuilder sb = new StringBuilder();
        for (String newsDesk : newsDesks) {
            sb.append("\"");
            sb.append(newsDesk);
            sb.append("\"");
        }
        return String.format("news_desk:(%s)", sb.toString());
    }

    private String dateValue(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(cal.getTime());
    }
}
