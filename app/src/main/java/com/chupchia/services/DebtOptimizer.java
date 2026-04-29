package com.chupchia.services;

import com.chupchia.models.Debt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtOptimizer {
    
    public static List<Debt> optimize(List<Debt> debts) {
        Map<String, Long> balance = new HashMap<>();
        
        for (Debt debt : debts) {
            balance.put(debt.getFromUserId(), 
                balance.getOrDefault(debt.getFromUserId(), 0L) - debt.getAmount());
            balance.put(debt.getToUserId(), 
                balance.getOrDefault(debt.getToUserId(), 0L) + debt.getAmount());
        }
        
        List<Map.Entry<String, Long>> debtors = new ArrayList<>();
        List<Map.Entry<String, Long>> creditors = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : balance.entrySet()) {
            if (entry.getValue() < 0) {
                debtors.add(entry);
            } else if (entry.getValue() > 0) {
                creditors.add(entry);
            }
        }
        
        List<Debt> optimized = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<String, Long> debtor = debtors.get(i);
            Map.Entry<String, Long> creditor = creditors.get(j);
            
            long amount = Math.min(-debtor.getValue(), creditor.getValue());
            
            if (amount > 0) {
                optimized.add(new Debt(debtor.getKey(), "", creditor.getKey(), "", amount, null));
            }
            
            debtor.setValue(debtor.getValue() + amount);
            creditor.setValue(creditor.getValue() - amount);
            
            if (debtor.getValue() == 0) i++;
            if (creditor.getValue() == 0) j++;
        }
        
        return optimized;
    }
}
