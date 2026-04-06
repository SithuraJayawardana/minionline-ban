package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.accounts.CheckingAccount;
import com.rajarata.banking.domain.accounts.SavingsAccount;

public class InterestSimulationEngine {

    /**
     * Calculates future balance with compounded interest for a given account type and duration.
     * @param account The bank account
     * @param durationMonths Number of months to simulate
     * @return Simulated balance after interest
     */
    public static double simulateFutureBalance(BankAccount account, int durationMonths) {
        double simulatedBalance = account.getBalance();

        // get monthly interest rate based on account type
        double monthlyRate = getRate(account);

       // apply interest month by month
        for (int i = 0; i < durationMonths; i++) {
            double interest = simulatedBalance * monthlyRate;
            
            // Checking account: apply low-balance penalty
            if (account instanceof CheckingAccount) {
                if (simulatedBalance < 1000.0) {
                    interest -= 50.0;// monthly penalty
                }
            } 
            // Savings account: apply bonus for high balance
            else if (account instanceof SavingsAccount) {
                if (simulatedBalance > 50000.0) {
                    interest += simulatedBalance * 0.001; // loyalty bonus
                }
            }
            // add interest/bonus/penalty to balance
            simulatedBalance += interest;
        }

        return simulatedBalance;
    }

    // returns monthly interest rate based on account type
    private static double getRate(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return 0.05 / 12.0; // 5% annual interest
        } else if (account instanceof CheckingAccount) {
            return 0.01 / 12.0; // 1% annual interest
        }
        return 0.0;
    }
}
