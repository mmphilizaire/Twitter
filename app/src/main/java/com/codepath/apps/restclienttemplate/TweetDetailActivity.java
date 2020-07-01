package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    public static final String TAG = "TweetDetailActivity";

    TwitterClient client;

    Tweet tweet;
    TextView tvName;
    TextView tvScreenName;
    TextView tvBody;
    TextView tvRetweets;
    TextView tvFavorites;
    ImageView ivProfileImage;
    ImageView ivMedia;
    ImageView ivRetweetSymbol;
    ImageView ivFavoriteSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        client = TwitterApp.getRestClient(this);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        tvName = (TextView) findViewById(R.id.tvName);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvRetweets = (TextView) findViewById(R.id.tvRetweets);
        tvFavorites = (TextView) findViewById(R.id.tvFavorites);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        ivMedia = (ImageView) findViewById(R.id.ivMedia);
        ivRetweetSymbol = (ImageView) findViewById(R.id.ivRetweetSymbol);
        ivFavoriteSymbol = (ImageView) findViewById(R.id.ivFavoriteSymbol);

        tvName.setText(tweet.user.name);
        tvScreenName.setText("@"+tweet.user.screenName);
        tvBody.setText(tweet.body);
        tvRetweets.setText(""+tweet.retweets);
        tvFavorites.setText(""+tweet.favorites);
        Glide.with(this).load(tweet.user.profileImageUrl).into(ivProfileImage);
        if(!tweet.media.isEmpty()){
            ivMedia.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.media).into(ivMedia);
        }
        else{
            ivMedia.setVisibility(View.GONE);
        }

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

}