package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
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
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    public static final String sTAG = "TweetsAdapter";

    Context mContext;
    List<Tweet> mTweets;

    //pass in the context and list of tweets
    public TweetsAdapter(Context mContext, List<Tweet> mTweets) {
        this.mContext = mContext;
        this.mTweets = mTweets;
    }

    //for each row, inflate the tweet layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    //bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get the data at position
        Tweet tweet = mTweets.get(position);

        //bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    //clean all elements of the recycler
    public void clear(){
        mTweets.clear();
        notifyDataSetChanged();
    }

    //add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList){
        mTweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    //define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TwitterClient mClient;

        ImageView mProfileImageView;
        TextView mBodyTextView;
        TextView mNameTextView;
        TextView mScreenNameTextView;
        TextView mTimeTextView;
        TextView mRetweetsTextView;
        TextView mFavoritesTextView;
        ImageView mMediaImageView;
        ImageView mReplyImageView;
        ImageView mRetweetSymbolImageView;
        ImageView mFavoriteSymbolImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mClient = TwitterApp.getRestClient(mContext);

            mProfileImageView = itemView.findViewById(R.id.ivProfileImage);
            mBodyTextView = itemView.findViewById(R.id.tvBody);
            mNameTextView = itemView.findViewById(R.id.tvName);
            mScreenNameTextView = itemView.findViewById(R.id.tvScreenName);
            mTimeTextView = itemView.findViewById(R.id.tvTime);
            mRetweetsTextView = itemView.findViewById(R.id.tvRetweets);
            mFavoritesTextView = itemView.findViewById(R.id.tvFavorites);
            mMediaImageView = itemView.findViewById(R.id.ivMedia);
            mReplyImageView = itemView.findViewById(R.id.ivReply);
            mRetweetSymbolImageView = itemView.findViewById(R.id.ivRetweetSymbol);
            mFavoriteSymbolImageView = itemView.findViewById(R.id.ivFavoriteSymbol);

        }

        public void bind(final Tweet tweet){
            mBodyTextView.setText(tweet.mBody);
            mNameTextView.setText(tweet.mUser.mName);
            mScreenNameTextView.setText("@"+tweet.mUser.mScreenName);
            mTimeTextView.setText(tweet.mRelativeTimeAgo);
            mRetweetsTextView.setText(""+tweet.mRetweets);
            mFavoritesTextView.setText(""+tweet.mFavorites);
            Glide.with(mContext).load(tweet.mUser.mProfileImageUrl).transform(new CircleCrop()).into(mProfileImageView);
            if(!tweet.mMedia.isEmpty()){
                mMediaImageView.setVisibility(View.VISIBLE);
                int radius = 30;
                int margin = 10;
                Glide.with(mContext).load(tweet.mMedia).transform(new RoundedCornersTransformation(radius, margin)).into(mMediaImageView);
            }
            else{
                mMediaImageView.setVisibility(View.GONE);
            }
            mReplyImageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent reply = new Intent(mContext, ReplyActivity.class);
                    reply.putExtra("user_screenName", tweet.mUser.mScreenName);
                    reply.putExtra("status_id", tweet.mId);
                    mContext.startActivity(reply);
                }
            });
//            if(tweet.retweeted){
//                //Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweetSymbol);
//            }
//            if(tweet.favorited){
//                //Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavoriteSymbol);
//            }
            mRetweetSymbolImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tweet.mRetweeted){
                        mClient.removeRetweet(tweet.mId, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "unretweet worked");
                                tweet.mRetweeted = false;
                                tweet.mRetweets = tweet.mRetweets -1;
                                mRetweetsTextView.setText(""+tweet.mRetweets);
                                //Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweetSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(sTAG, "onFailure to removeRetweet");
                            }
                        });
                    }
                    else{
                        mClient.addRetweet(tweet.mId, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "retweet worked");
                                tweet.mRetweeted = true;
                                tweet.mRetweets = tweet.mRetweets +1;
                                mRetweetsTextView.setText(""+tweet.mRetweets);
                                //Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweetSymbol);
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
                    if(tweet.mFavorited){
                        mClient.removeFavorite(tweet.mId, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "unfavorite worked");
                                tweet.mFavorited = false;
                                tweet.mFavorites = tweet.mFavorites -1;
                                mFavoritesTextView.setText(""+tweet.mFavorites);
                                //Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavoriteSymbol);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(sTAG, "onFailure to removeFavorite");
                            }
                        });
                    }
                    else{
                        mClient.addFavorite(tweet.mId, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.d("Mishka", "favorite worked");
                                tweet.mFavorited = true;
                                tweet.mFavorites = tweet.mFavorites +1;
                                mFavoritesTextView.setText(""+tweet.mFavorites);
                                //Glide.with(context).load(R.drawable.ic_vector_heart).into(ivFavoriteSymbol);
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

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Tweet tweet = mTweets.get(position);
                Intent intent = new Intent(mContext, TweetDetailActivity.class);
                intent.putExtra("tweet", Parcels.wrap(tweet));
                mContext.startActivity(intent);
            }
        }
    }


}
