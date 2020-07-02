package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    public static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    //pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    //for each row, inflate the tweet layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    //bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get the data at position
        Tweet tweet = tweets.get(position);

        //bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    //clean all elements of the recycler
    public void clear(){
        tweets.clear();
        notifyDataSetChanged();
    }

    //add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList){
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    //define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TwitterClient client;

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvName;
        TextView tvScreenName;
        TextView tvTime;
        TextView tvRetweets;
        TextView tvFavorites;
        ImageView ivMedia;
        ImageView ivReply;
        ImageView ivRetweetSymbol;
        ImageView ivFavoriteSymbol;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            client = TwitterApp.getRestClient(context);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRetweets = itemView.findViewById(R.id.tvRetweets);
            tvFavorites = itemView.findViewById(R.id.tvFavorites);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweetSymbol = itemView.findViewById(R.id.ivRetweetSymbol);
            ivFavoriteSymbol = itemView.findViewById(R.id.ivFavoriteSymbol);

        }

        public void bind(final Tweet tweet){
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText("@"+tweet.user.screenName);
            tvTime.setText(tweet.relativeTimeAgo);
            tvRetweets.setText(""+tweet.retweets);
            tvFavorites.setText(""+tweet.favorites);
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);
            if(!tweet.media.isEmpty()){
                ivMedia.setVisibility(View.VISIBLE);
                int radius = 30;
                int margin = 10;
                Glide.with(context).load(tweet.media).transform(new RoundedCornersTransformation(radius, margin)).into(ivMedia);
            }
            else{
                ivMedia.setVisibility(View.GONE);
            }
            ivReply.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent reply = new Intent(context, ComposeActivity.class);
                    reply.putExtra("user_screenName", tweet.user.screenName);
                    reply.putExtra("status_id", tweet.id);
                    context.startActivity(reply);
                }
            });
//            if(tweet.retweeted){
//                //Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweetSymbol);
//            }
//            if(tweet.favorited){
//                //Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavoriteSymbol);
//            }
            ivRetweetSymbol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tweet.retweeted){
                        client.removeRetweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "unretweet worked");
                                tweet.retweeted = false;
                                tweet.retweets = tweet.retweets-1;
                                tvRetweets.setText(""+tweet.retweets);
                                //Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweetSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(TAG, "onFailure to removeRetweet");
                            }
                        });
                    }
                    else{
                        client.addRetweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "retweet worked");
                                tweet.retweeted = true;
                                tweet.retweets = tweet.retweets+1;
                                tvRetweets.setText(""+tweet.retweets);
                                //Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweetSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(TAG, "onFailure to addRetweet");
                            }
                        });
                    }
                }
            });
            ivFavoriteSymbol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tweet.favorited){
                        client.removeFavorite(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "unfavorite worked");
                                tweet.favorited = false;
                                tweet.favorites = tweet.favorites-1;
                                tvFavorites.setText(""+tweet.favorites);
                                //Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavoriteSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(TAG, "onFailure to removeFavorite");
                            }
                        });
                    }
                    else{
                        client.addFavorite(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "favorite worked");
                                tweet.favorited = true;
                                tweet.favorites = tweet.favorites+1;
                                tvFavorites.setText(""+tweet.favorites);
                                //Glide.with(context).load(R.drawable.ic_vector_heart).into(ivFavoriteSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(TAG, "onFailure to addFavorite");
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Tweet tweet = tweets.get(position);
                Intent intent = new Intent(context, TweetDetailActivity.class);
                intent.putExtra("tweet", Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }


}
