package com.ani.nytimessearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private String webUrl;
    private String headline;
    private String thumbnail;

    public Article(JSONObject jsonObject) {
        try {
            webUrl = jsonObject.getString("web_url");
            headline = jsonObject.getJSONObject("headline").getString("main");

            JSONArray multimedias = jsonObject.getJSONArray("multimedia");
            if (multimedias.length() > 0) {
                JSONObject multimedia = multimedias.getJSONObject(0);
                thumbnail = "http://www.nytimes.com/" + multimedia.getString("url");
            } else {
                this.thumbnail = "";
            }
        } catch (JSONException e) {

        }
    }

    public static List<Article> fromJSONArray(JSONArray array) {
        List<Article> articles = new ArrayList<>();
        for (int index = 0; index < array.length(); ++index) {
            try {
                JSONObject object = array.getJSONObject(index);
                articles.add(new Article(object));
            } catch (JSONException e) {

            }
        }
        return articles;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
