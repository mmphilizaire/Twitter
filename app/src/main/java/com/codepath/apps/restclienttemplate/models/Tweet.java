package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    public long mId;
    public String mBody;
    public String mCreatedAt;
    public User mUser;
    public String mRelativeTimeAgo;
    public String mMedia;
    public int mRetweets;
    public int mFavorites;
    public boolean mRetweeted;
    public boolean mFavorited;

    //empty constructor needed by the parceler library
    public Tweet(){}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.mId = jsonObject.getLong("id");
        tweet.mBody = jsonObject.getString("text");
        tweet.mCreatedAt = jsonObject.getString("created_at");
        tweet.mUser = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.mRelativeTimeAgo = getRelativeTimeAgo(tweet.mCreatedAt);
        if(jsonObject.getJSONObject("entities").has("media")){
            tweet.mMedia = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
        }
        else{
            tweet.mMedia = "";
        }
        tweet.mRetweets = jsonObject.getInt("retweet_count");
        tweet.mFavorites = jsonObject.getInt("favorite_count");
        tweet.mRetweeted = jsonObject.getBoolean("retweeted");
        tweet.mFavorited = jsonObject.getBoolean("favorited");
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException{
        List<Tweet> tweets = new ArrayList<Tweet>();
        for(int i = 0; i < jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public static String getRelativeTimeAgo(String rawJsonDate){
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat format = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        format.setLenient(true);

        String relativeDate = "";
        try{
            long dateMillis = format.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] split = relativeDate.split(" ");
        return split[0]+split[1].charAt(0);
    }

}
