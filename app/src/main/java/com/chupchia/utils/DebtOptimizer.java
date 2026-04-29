package com.chupchia.utils;

import com.chupchia.models.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtOptimizer {

    /**
     * Optimize debt transactions using graph algorithm
     * Reduces the number of transactions to settle all debts
     * 
     * @param transactions List of original transactions (who owes whom)
     * @param userIdToNameMap Map of user IDs to names for display
     * @return List of optimized transactions
     */
    public static List<Transaction> optimize(List<Transaction> transactions, 
                                              Map<String, String> userIdToNameMap) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 1: Calculate net balance for each person
        Map<String, Long> balance = new HashMap<>();
        
        for (Transaction t : transactions) {
            // Debtor (from user) owes money -> negative balance
            balance.put(t.getFromUserId(), 
                balance.getOrDefault(t.getFromUserId(), 0L) - t.getAmount());
            // Creditor (to user) is owed money -> positive balance
            balance.put(t.getToUserId(), 
                balance.getOrDefault(t.getToUserId(), 0L) + t.getAmount());
        }
        
        // Step 2: Separate into creditors (positive) and debtors (negative)
        List<Map.Entry<String, Long>> creditors = new ArrayList<>();
        List<Map.Entry<String, Long>> debtors = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : balance.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.add(entry);
            } else if (entry.getValue() < 0) {
                debtors.add(entry);
            }
        }
        
        // Step 3: Greedy matching to minimize transactions
        List<Transaction> optimized = new ArrayList<>();
        int i = 0, j = 0;
        
        // Use a copy to avoid modifying original map entries if they are shared
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
                
                // Add names if available
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
            
            // Update balances
            debtorBalances.set(i, debtorBalances.get(i) + settleAmount);
            creditorBalances.set(j, creditorBalances.get(j) - settleAmount);
            
            if (debtorBalances.get(i) == 0) i++;
            if (creditorBalances.get(j) == 0) j++;
        }
        
        return optimized;
    }
    
    /**
     * Simplified optimization when we only have net balances
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
