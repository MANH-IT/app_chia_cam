package com.chupchia.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "ChiaCamPrefs";
    private static final String KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_CURRENT_GROUP_ID = "current_group_id";
    private static final String KEY_PENDING_INVITE_CODE = "pending_invite_code";
    
    private static SharedPrefManager instance;
    private static SharedPreferences sharedPrefs;
    private final SharedPreferences.Editor editor;
    
    private SharedPrefManager(Context context) {
        sharedPrefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();
    }
    
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }
    
    /**
     * Static access to SharedPreferences for convenience
     */
    public static SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }
    
    // Onboarding
    public void setHasSeenOnboarding(boolean hasSeen) {
        editor.putBoolean(KEY_HAS_SEEN_ONBOARDING, hasSeen);
        editor.apply();
    }
    
    public boolean hasSeenOnboarding() {
        return sharedPrefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false);
    }
    
    // Auth Token
    public void setAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }
    
    public String getAuthToken() {
        return sharedPrefs.getString(KEY_AUTH_TOKEN, null);
    }
    
    /**
     * Alias for setAuthToken for backward compatibility
     */
    public void saveToken(String token) {
        setAuthToken(token);
        // Set token expiry to 30 days from now
        editor.putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
        editor.apply();
    }
    
    /**
     * Check if token has expired
     */
    public boolean isTokenExpired() {
        long expiry = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0);
        return expiry > 0 && System.currentTimeMillis() > expiry;
    }
    
    /**
     * Set token expiry time
     */
    public void setTokenExpiry(long expiryTime) {
        editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);
        editor.apply();
    }
    
    /**
     * Clear auth data (token, login state)
     */
    public void clearAuthData() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRY);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
    
    // Login state
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    // User ID
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }
    
    public String getUserId() {
        return sharedPrefs.getString(KEY_USER_ID, "user_1");
    }
    
    // User Name
    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
    
    public String getUserName() {
        return sharedPrefs.getString(KEY_USER_NAME, "Mạnh Nguyễn");
    }
    
    // User Phone
    public void setUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }
    
    public String getUserPhone() {
        return sharedPrefs.getString(KEY_USER_PHONE, null);
    }
    
    // User Email
    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    public String getUserEmail() {
        return sharedPrefs.getString(KEY_USER_EMAIL, null);
    }
    
    // User Avatar
    public void setUserAvatar(String avatar) {
        editor.putString(KEY_USER_AVATAR, avatar);
        editor.apply();
    }
    
    public String getUserAvatar() {
        return sharedPrefs.getString(KEY_USER_AVATAR, null);
    }
    
    /**
     * Save user data in one call
     */
    public void saveUser(String userId, String name, String phone, String email, String avatarUrl) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_EMAIL, email);
        if (avatarUrl != null) {
            editor.putString(KEY_USER_AVATAR, avatarUrl);
        }
        editor.apply();
    }
    
    // Current Group
    public void setCurrentGroupId(String groupId) {
        editor.putString(KEY_CURRENT_GROUP_ID, groupId);
        editor.apply();
    }
    
    public String getCurrentGroupId() {
        return sharedPrefs.getString(KEY_CURRENT_GROUP_ID, null);
    }
    
    // Pending Invite
    public void setPendingInviteCode(String inviteCode) {
        editor.putString(KEY_PENDING_INVITE_CODE, inviteCode);
        editor.apply();
    }
    
    public String getPendingInviteCode() {
        return sharedPrefs.getString(KEY_PENDING_INVITE_CODE, null);
    }
    
    public void clearPendingInvite() {
        editor.remove(KEY_PENDING_INVITE_CODE);
        editor.apply();
    }
    
    public boolean hasPendingInvite() {
        return getPendingInviteCode() != null;
    }
    
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
