package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

import static com.codepath.apps.restclienttemplate.TimelineActivity.sREQUEST_CODE;

public class TweetDetailActivity extends AppCompatActivity {

    public static final String sTAG = "TweetDetailActivity";

    TwitterClient mClient;

    Tweet mTweet;
    TextView mNameTextView;
    TextView mScreenNameTextView;
    TextView mBodyTextView;
    TextView mRetweetsTextView;
    TextView mFavoritesTextView;
    ImageView mProfileImageView;
    ImageView mMediaImageView;
    ImageView mReplySymbolImageView;
    ImageView mRetweetSymbolImageView;
    ImageView mFavoriteSymbolImageView;
    TextView mTimeTextView;
    TextView mDateTextView;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        mContext = getApplicationContext();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mClient = TwitterApp.getRestClient(this);

        mTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        mNameTextView = (TextView) findViewById(R.id.tvName);
        mScreenNameTextView = (TextView) findViewById(R.id.tvScreenName);
        mBodyTextView = (TextView) findViewById(R.id.tvBody);
        mRetweetsTextView = (TextView) findViewById(R.id.tvRetweets);
        mFavoritesTextView = (TextView) findViewById(R.id.tvFavorites);
        mProfileImageView = (ImageView) findViewById(R.id.ivProfileImage);
        mMediaImageView = (ImageView) findViewById(R.id.ivMedia);
        mRetweetSymbolImageView = (ImageView) findViewById(R.id.ivRetweetSymbol);
        mFavoriteSymbolImageView = (ImageView) findViewById(R.id.ivFavoriteSymbol);
        mTimeTextView = (TextView) findViewById(R.id.tvTime);
        mDateTextView = (TextView) findViewById(R.id.tvDate);
        mReplySymbolImageView = (ImageView) findViewById(R.id.ivReply);

        mNameTextView.setText(mTweet.mUser.mName);
        mScreenNameTextView.setText("@"+ mTweet.mUser.mScreenName);
        mBodyTextView.setText(mTweet.mBody);

        mReplySymbolImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent reply = new Intent(mContext, ReplyActivity.class);
                reply.putExtra("user_screenName", mTweet.mUser.mScreenName);
                reply.putExtra("status_id", mTweet.mId);
                startActivity(reply);
            }
        });

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        try {
            Date date = format.parse(mTweet.mCreatedAt);
            mDateTextView.setText((String) DateFormat.format("h:mm aa", date));
            mTimeTextView.setText((String) DateFormat.format("M/d/yy", date));

        } catch (ParseException e) {
            e.printStackTrace();
        }


        mRetweetsTextView.setText(""+ mTweet.mRetweets);
        mFavoritesTextView.setText(""+ mTweet.mFavorites);
        Glide.with(this).load(mTweet.mUser.mProfileImageUrl).transform(new CircleCrop()).into(mProfileImageView);
        if(!mTweet.mMedia.isEmpty()){
            mMediaImageView.setVisibility(View.VISIBLE);
            int radius = 30;
            int margin = 10;
            Glide.with(this).load(mTweet.mMedia).transform(new RoundedCornersTransformation(radius, margin)).into(mMediaImageView);
        }
        else{
            mMediaImageView.setVisibility(View.GONE);
        }

        mRetweetSymbolImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTweet.mRetweeted){
                    mClient.removeRetweet(mTweet.mId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d("Mishka", "unretweet worked");
                            mTweet.mRetweeted = false;
                            mTweet.mRetweets = mTweet.mRetweets -1;
                            mRetweetsTextView.setText(""+ mTweet.mRetweets);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.d(sTAG, "onFailure to removeRetweet");
                        }
                    });
                }
                else{
                    mClient.addRetweet(mTweet.mId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d("Mishka", "retweet worked");
                            mTweet.mRetweeted = true;
                            mTweet.mRetweets = mTweet.mRetweets +1;
                            mRetweetsTextView.setText(""+ mTweet.mRetweets);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.d(sTAG, "onFailure to addRetweet");
                        }
                    });
                }
            }
        });
        mFavoriteSymbolImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTweet.mFavorited){
                    mClient.removeFavorite(mTweet.mId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d("Mishka", "unfavorite worked");
                            mTweet.mFavorited = false;
                            mTweet.mFavorites = mTweet.mFavorites -1;
                            mFavoritesTextView.setText(""+ mTweet.mFavorites);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.d(sTAG, "onFailure to removeFavorite");
                        }
                    });
                }
                else{
                    mClient.addFavorite(mTweet.mId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d("Mishka", "favorite worked");
                            mTweet.mFavorited = true;
                            mTweet.mFavorites = mTweet.mFavorites +1;
                            mFavoritesTextView.setText(""+ mTweet.mFavorites);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.d(sTAG, "onFailure to addFavorite");
                        }
                    });
                }
            }
        });

    }

}