package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int sMAX_TWEET_LENGTH = 280;
    public static final String sTAG = "ComposeActivity";

    Button mTweetButton;
    EditText mComposeEditText;

    TwitterClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mClient = TwitterApp.getRestClient(this);

        mTweetButton = findViewById(R.id.btnTweet);
        mComposeEditText = findViewById(R.id.etCompose);

        //set a click listener on the button
        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = mComposeEditText.getText().toString();
                if(tweetContent.isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if(tweetContent.length() > sMAX_TWEET_LENGTH){
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                //make an API call to twitter to publish the tweet
                mClient.publishTweet(tweetContent, -1, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            //set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            //closes the activity, pass data to parent
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(sTAG, "onFailure to publish tweet");
                    }
                });
            }
        });
    }
}