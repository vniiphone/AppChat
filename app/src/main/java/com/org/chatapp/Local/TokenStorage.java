package com.org.chatapp.Local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {

    /*
        saveAccessToken(String accessToken): Lưu trữ mã thông báo truy cập vào SharedPreferences.
        getAccessToken(): Truy xuất mã thông báo truy cập từ SharedPreferences.
        hasAccessToken(): Kiểm tra xem đã tồn tại mã thông báo truy cập hay chưa.
        clearAccessToken(): Xóa mã thông báo truy cập khỏi SharedPreferences.
    */
    private static final String PREF_NAME = "TokenStorage";
    private static final String ACCESS_TOKEN_KEY = "access_token";

    private SharedPreferences sharedPreferences;

    public TokenStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String accessToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public boolean hasAccessToken() {
        return sharedPreferences.contains(ACCESS_TOKEN_KEY);
    }

    public void clearAccessToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.apply();
    }
}

