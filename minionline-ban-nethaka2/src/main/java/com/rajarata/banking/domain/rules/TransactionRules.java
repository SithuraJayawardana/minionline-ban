package com.rajarata.banking.domain.rules;

/**
 * Interface defining the rules for transactions.
 * Different account types will implement these rules differently to satisfy Polymorphism.
 */
public interface TransactionRules {
    /**
     * Calculates the interest for the account based on its specific rules.
     * @return the calculated interest amount
     */
    double calculateInterest();

    /**
     * Checks if a withdrawal of the specified amount is allowed.
     * @param amount the amount to withdraw
     * @return true if withdrawal is permitted, false otherwise
     */
    boolean checkWithdrawalLimit(double amount);

    /**
     * Applies any necessary penalties (e.g., overdraft fee, early withdrawal fee).
     * @return the penalty amount applied
     */
    double applyPenalty();
}
