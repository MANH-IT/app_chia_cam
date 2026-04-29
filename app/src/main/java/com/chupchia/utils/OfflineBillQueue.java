package com.chupchia.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OfflineBillQueue {
    
    private static final String PREF_NAME = "OfflineBillQueue";
    private static final String KEY_BILLS = "pending_bills";
    
    private static OfflineBillQueue instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public static class PendingBill {
        public String imageUri;
        public String productName;
        public String amount;
        public String splitType;
        public List<String> memberIds;
        public long timestamp;
        
        public PendingBill(String imageUri, String productName, String amount, 
                          String splitType, List<String> memberIds) {
            this.imageUri = imageUri;
            this.productName = productName;
            this.amount = amount;
            this.splitType = splitType;
            this.memberIds = memberIds;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private OfflineBillQueue(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized OfflineBillQueue getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineBillQueue(context);
        }
        return instance;
    }
    
    /**
     * Add bill to offline queue
     */
    public void addBill(String imageUri, String productName, String amount, 
                        String splitType, List<String> memberIds) {
        List<PendingBill> bills = getPendingBills();
        bills.add(new PendingBill(imageUri, productName, amount, splitType, memberIds));
        saveBills(bills);
    }
    
    /**
     * Get all pending bills
     */
    public List<PendingBill> getPendingBills() {
        String json = sharedPreferences.getString(KEY_BILLS, "[]");
        Type type = new TypeToken<List<PendingBill>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Remove bill from queue
     */
    public void removeBill(int index) {
        List<PendingBill> bills = getPendingBills();
        if (index >= 0 && index < bills.size()) {
            bills.remove(index);
            saveBills(bills);
        }
    }
    
    /**
     * Clear all pending bills
     */
    public void clearAll() {
        saveBills(new ArrayList<>());
    }
    
    /**
     * Get pending bills count
     */
    public int getPendingCount() {
        return getPendingBills().size();
    }
    
    private void saveBills(List<PendingBill> bills) {
        String json = gson.toJson(bills);
        sharedPreferences.edit().putString(KEY_BILLS, json).apply();
    }
}
