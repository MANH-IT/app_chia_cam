package com.chupchia.repositories;

import com.chupchia.models.Debt;
import com.chupchia.models.Bill;
import com.chupchia.models.Transaction;
import com.chupchia.utils.DebtOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtRepository {
    
    /**
     * Calculate debts from a list of bills for given member IDs.
     * Uses Bill.getActorId() as the payer and splits equally among members.
     */
    public List<Debt> calculateDebts(List<Bill> bills, List<String> memberIds) {
        Map<String, Map<String, Long>> balanceMap = new HashMap<>();
        
        for (String fromId : memberIds) {
            balanceMap.put(fromId, new HashMap<>());
            for (String toId : memberIds) {
                balanceMap.get(fromId).put(toId, 0L);
            }
        }
        
        for (Bill bill : bills) {
            String buyerId = bill.getActorId();
            long amount = bill.getAmount();
            int splitCount = bill.getSplitCount();
            
            if (splitCount <= 0) splitCount = memberIds.size();
            long perPerson = amount / splitCount;
            
            for (String memberId : memberIds) {
                if (!memberId.equals(buyerId)) {
                    Map<String, Long> memberDebts = balanceMap.get(memberId);
                    if (memberDebts != null) {
                        memberDebts.put(buyerId, memberDebts.getOrDefault(buyerId, 0L) + perPerson);
                    }
                }
            }
        }
        
        List<Debt> debts = new ArrayList<>();
        for (String fromId : memberIds) {
            for (String toId : memberIds) {
                Map<String, Long> fromMap = balanceMap.get(fromId);
                if (fromMap != null) {
                    Long amount = fromMap.get(toId);
                    if (amount != null && amount > 0) {
                        debts.add(new Debt(fromId, "", toId, "", amount, null));
                    }
                }
            }
        }
        
        return debts;
    }
    
    /**
     * Optimize debts by converting to Transactions and using the DebtOptimizer.
     */
    public List<Transaction> optimizeDebts(List<Debt> debts) {
        List<Transaction> transactions = new ArrayList<>();
        for (Debt debt : debts) {
            transactions.add(new Transaction(debt.getFromUserId(), debt.getToUserId(), debt.getAmount()));
        }
        return DebtOptimizer.optimize(transactions, null);
    }
}
