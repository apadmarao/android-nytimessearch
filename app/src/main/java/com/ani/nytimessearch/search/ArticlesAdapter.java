package com.ani.nytimessearch.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ani.nytimessearch.R;
import com.ani.nytimessearch.nytclient.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ani.nytimessearch.R.id.ivImage;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private static final int TEXT = 0;
    private static final int TEXT_PLUS_IMAGE = 1;

    private Context context;
    private List<Article> articles;

    public ArticlesAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemViewType(int position) {
        //More to come
        Article article = articles.get(position);
        return article.getThumbnail().isEmpty() ? TEXT : TEXT_PLUS_IMAGE;
    }

    @Override
    public ArticlesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        ViewHolder viewHolder = null;
        switch (viewType) {
            case TEXT: {
                View articleView = inflater.inflate(R.layout.item_article_text_result, parent, false);
                viewHolder = new ViewHolder(articleView);
                break;
            }
            case TEXT_PLUS_IMAGE: {
                View articleView = inflater.inflate(R.layout.item_article_result, parent, false);
                viewHolder = new ViewHolder(articleView);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected view type: " + viewType);
        }

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ArticlesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Article article = articles.get(position);


        switch (viewHolder.getItemViewType()) {
            case TEXT: {
                viewHolder.tvTitle.setText(article.getHeadline());
                break;
            }
            case TEXT_PLUS_IMAGE: {
                // Set item views based on your views and data model
                viewHolder.ivImage.setImageResource(0);
                String thumbnail = article.getThumbnail();
                if (!TextUtils.isEmpty(thumbnail)) {
                    Picasso.with(getContext()).load(thumbnail).into(viewHolder.ivImage);
                }

                viewHolder.tvTitle.setText(article.getHeadline());
                break;
            }
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;

        ViewHolder(View itemView) {
            super(itemView);

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public TextViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }
}
