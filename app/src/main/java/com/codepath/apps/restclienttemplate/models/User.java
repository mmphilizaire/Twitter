package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    public String mName;
    public String mScreenName;
    public String mProfileImageUrl;

    //empty constructor needed by the parceler library
    public User(){}

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.mName = jsonObject.getString("name");
        user.mScreenName = jsonObject.getString("screen_name");
        user.mProfileImageUrl = jsonObject.getString("profile_image_url_https");
        return user;
    }
}
