package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.accounts.CheckingAccount;
import com.rajarata.banking.domain.accounts.SavingsAccount;

public class InterestSimulationEngine {

    /**
     * Dyamically calculates compounded yield based on account type, balance limitations, and duration.
     * @param account The bank account
     * @param durationMonths Months to simulate into the future
     * @return The final simulated balance after compounded interest is applied.
     */
    public static double simulateFutureBalance(BankAccount account, int durationMonths) {
        double simulatedBalance = account.getBalance();
        double monthlyRate = getRate(account);

        for (int i = 0; i < durationMonths; i++) {
            double interest = simulatedBalance * monthlyRate;
            
            if (account instanceof CheckingAccount) {
                if (simulatedBalance < 1000.0) {
                    interest -= 50.0; // Checking penalty per month for low balance
                }
            } else if (account instanceof SavingsAccount) {
                if (simulatedBalance > 50000.0) {
                    interest += simulatedBalance * 0.001; // Bonus loyalty yield for high tier savings
                }
            }
            
            simulatedBalance += interest;
        }

        return simulatedBalance;
    }

    private static double getRate(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return 0.05 / 12.0; // 5% APR
        } else if (account instanceof CheckingAccount) {
            return 0.01 / 12.0; // 1% APR
        }
        return 0.0;
    }
}
