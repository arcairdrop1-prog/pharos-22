# Pharos Recurring Payment Agent

An intelligent agent developed in **Java** for the Pharos Agent Arena designed to manage repeating on-chain payments (such as rent, subscriptions, or payroll-style transfers) on a schedule[cite: 14].

## 📋 Requirements Met

1. **Flexible Scheduling (Req. 01):** Supports a schedule (weekly or monthly) with amount, token, and recipient[cite: 14].
2. **Status Tracking (Req. 02):** Prepares each payment on time and tracks paid, pending, and failed statuses[cite: 14].
3. **Failure & Balance Handling (Req. 03):** Handles insufficient balance or failed transactions without double-paying[cite: 14].
4. **History & Next Payment (Req. 04):** Shows payment history and the next scheduled payment[cite: 14].

## 🚀 How to Run

1. Make sure you have **Java JDK** installed.
2. Compile the file:
   ```bash
   javac RecurringPaymentAgent.java
