package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String sTAG = "TimelineActivity";
    public static final int sREQUEST_CODE = 20;

    TwitterClient mClient;
    RecyclerView mTweetsRecyclerView;
    List<Tweet> mTweets;
    TweetsAdapter mAdapter;
    SwipeRefreshLayout mSwipeContainer;

    EndlessRecyclerViewScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mClient = TwitterApp.getRestClient(this);

        mSwipeContainer = findViewById(R.id.swipeContainer);
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(sTAG, "fetching new data!");
                populateHomeTimeline();
            }
        });

        //find the recycler view
        mTweetsRecyclerView = findViewById(R.id.rvTweets);

        //initialize the list of tweets and adapter
        mTweets = new ArrayList<Tweet>();
        mAdapter = new TweetsAdapter(this, mTweets);

        //recycler view setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mTweetsRecyclerView.setLayoutManager(layoutManager);
        mTweetsRecyclerView.setAdapter(mAdapter);

        mTweetsRecyclerView.addItemDecoration(new DividerItemDecoration(mTweetsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i("Mishka", "load nore");
                loadMoreData();
            }
        };
        mTweetsRecyclerView.addOnScrollListener(mScrollListener);

        populateHomeTimeline();
    }

    private void loadMoreData() {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        mClient.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                //  --> Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try{
                    //  --> Append the new data objects to the existing set of items inside the array of items
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
                    mAdapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        }, mTweets.get(mTweets.size()-1).mId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose){
            //compose icon has been selected
            //navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, sREQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == sREQUEST_CODE && resultCode == RESULT_OK){
            //get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //update the recycler view with the tweet
            //modify data source of tweets
            mTweets.add(0,tweet);
            //update the adapter
            mAdapter.notifyItemInserted(0);
            mTweetsRecyclerView.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        mClient.getHomeTimeline(new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(sTAG, "onSuccess: " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.i(sTAG, "json: " + jsonArray.toString());
                    mAdapter.clear();
                    mAdapter.addAll(Tweet.fromJsonArray(jsonArray));
                    mSwipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(sTAG, "json exception", e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(sTAG, "onFailure: " + response, throwable);
            }
        });
    }
}