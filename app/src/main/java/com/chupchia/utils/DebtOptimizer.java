package com.chupchia.utils;

import com.chupchia.models.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtOptimizer {

    /**
     * Tối ưu hóa giao dịch công nợ sử dụng thuật toán đồ thị
     * Giảm số lượng giao dịch để thanh toán tất cả công nợ
     * 
     * @param transactions Danh sách giao dịch gốc (ai nợ ai)
     * @param userIdToNameMap Bảng ánh xạ mã người dùng sang tên để hiển thị
     * @return Danh sách giao dịch đã tối ưu
     */
    public static List<Transaction> optimize(List<Transaction> transactions, 
                                              Map<String, String> userIdToNameMap) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        // Bước 1: Tính số dư ròng cho từng người
        Map<String, Long> balance = new HashMap<>();
        
        for (Transaction t : transactions) {
            // Người nợ (từ người dùng) nợ tiền -> số dư âm
            balance.put(t.getFromUserId(), 
                balance.getOrDefault(t.getFromUserId(), 0L) - t.getAmount());
            // Chủ nợ (đến người dùng) được nợ tiền -> số dư dương
            balance.put(t.getToUserId(), 
                balance.getOrDefault(t.getToUserId(), 0L) + t.getAmount());
        }
        
        // Bước 2: Phân loại thành chủ nợ (dương) và người nợ (âm)
        List<Map.Entry<String, Long>> creditors = new ArrayList<>();
        List<Map.Entry<String, Long>> debtors = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : balance.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.add(entry);
            } else if (entry.getValue() < 0) {
                debtors.add(entry);
            }
        }
        
        // Bước 3: Ghép tham lam để giảm thiểu giao dịch
        List<Transaction> optimized = new ArrayList<>();
        int i = 0, j = 0;
        
        // Dùng bản sao để tránh sửa đổi các entry gốc nếu chúng được chia sẻ
        List<Long> debtorBalances = new ArrayList<>();
        for (Map.Entry<String, Long> e : debtors) debtorBalances.add(e.getValue());
        
        List<Long> creditorBalances = new ArrayList<>();
        for (Map.Entry<String, Long> e : creditors) creditorBalances.add(e.getValue());

        while (i < debtors.size() && j < creditors.size()) {
            long debtAmount = -debtorBalances.get(i);
            long creditAmount = creditorBalances.get(j);
            long settleAmount = Math.min(debtAmount, creditAmount);
            
            if (settleAmount > 0) {
                Transaction optimizedTx = new Transaction(
                    debtors.get(i).getKey(),
                    creditors.get(j).getKey(),
                    settleAmount
                );
                
                // Thêm tên nếu có
                if (userIdToNameMap != null) {
                    if (userIdToNameMap.containsKey(debtors.get(i).getKey())) {
                        optimizedTx.setFromUserName(userIdToNameMap.get(debtors.get(i).getKey()));
                    }
                    if (userIdToNameMap.containsKey(creditors.get(j).getKey())) {
                        optimizedTx.setToUserName(userIdToNameMap.get(creditors.get(j).getKey()));
                    }
                }
                
                optimized.add(optimizedTx);
            }
            
            // Cập nhật số dư
            debtorBalances.set(i, debtorBalances.get(i) + settleAmount);
            creditorBalances.set(j, creditorBalances.get(j) - settleAmount);
            
            if (debtorBalances.get(i) == 0) i++;
            if (creditorBalances.get(j) == 0) j++;
        }
        
        return optimized;
    }
    
    /**
     * Tối ưu hóa đơn giản khi chỉ có số dư ròng
     */
    public static List<Transaction> optimizeFromBalances(Map<String, Long> balances,
                                                          Map<String, String> userIdToNameMap) {
        List<Map.Entry<String, Long>> creditors = new ArrayList<>();
        List<Map.Entry<String, Long>> debtors = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : balances.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.add(entry);
            } else if (entry.getValue() < 0) {
                debtors.add(entry);
            }
        }
        
        List<Transaction> optimized = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<String, Long> debtor = debtors.get(i);
            Map.Entry<String, Long> creditor = creditors.get(j);
            
            long debtAmount = -debtor.getValue();
            long creditAmount = creditor.getValue();
            long settleAmount = Math.min(debtAmount, creditAmount);
            
            if (settleAmount > 0) {
                Transaction optimizedTx = new Transaction(
                    debtor.getKey(),
                    creditor.getKey(),
                    settleAmount
                );
                
                if (userIdToNameMap != null) {
                    if (userIdToNameMap.containsKey(debtor.getKey())) {
                        optimizedTx.setFromUserName(userIdToNameMap.get(debtor.getKey()));
                    }
                    if (userIdToNameMap.containsKey(creditor.getKey())) {
                        optimizedTx.setToUserName(userIdToNameMap.get(creditor.getKey()));
                    }
                }
                
                optimized.add(optimizedTx);
            }
            
            debtor.setValue(debtor.getValue() + settleAmount);
            creditor.setValue(creditor.getValue() - settleAmount);
            
            if (debtor.getValue() == 0) i++;
            if (creditor.getValue() == 0) j++;
        }
        
        return optimized;
    }
}
