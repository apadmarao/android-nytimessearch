package com.ani.nytimessearch.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ani.nytimessearch.nytclient.Article;
import com.ani.nytimessearch.R;
import com.squareup.picasso.Picasso;

import java.util.List;

class ArticleArrayAdapter extends ArrayAdapter<Article> {

    private ImageView ivImage;
    private TextView tvTitle;

    ArticleArrayAdapter(Context context, List<Article> articles) {
        super(context, android.R.layout.simple_list_item_1, articles);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Article article = getItem(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.item_article_result, parent, false);
        }

        ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
        ivImage.setImageResource(0);

        tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(article.getHeadline());

        String thumbnail = article.getThumbnail();
        if (!TextUtils.isEmpty(thumbnail)) {
            Picasso.with(getContext()).load(thumbnail).into(ivImage);
        }



        return convertView;
    }
}
