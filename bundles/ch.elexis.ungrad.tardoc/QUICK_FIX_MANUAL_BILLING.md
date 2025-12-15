# QUICK FIX - Manual Billing Not Stopping Timer

## THE PROBLEM
Timer only checks every 60 seconds → Delay before detecting manual billing

## THE SOLUTION
Call `checkBillingChanges()` immediately after adding billing:

```java
// After adding billing in your UI
billingsManager.checkBillingChanges();
```

## WHERE TO ADD IT

```java
private void onBillingAddedToEncounter(ITardocLeistung billing) {
    // Your existing code to add billing
    BillingServiceHolder.get().bill(billing, encounter, 1);
    
    // ADD THIS LINE ↓
    billingsManager.checkBillingChanges();
    
    // Your existing code to refresh UI
    refresh();
}
```

## EXPECTED CONSOLE OUTPUT

```
BillingsManager: Billing count mismatch - Expected: 1, Actual: 2
BillingsManager: Manual billing detected! Expected 1 but found 2 billings
BillingsManager: Manual billing detected via checkBillingChanges()
BillingsManager: Followup timer stopped and cancelled
```

## THAT'S IT!

Just add that ONE line and the timer will stop immediately when you manually add billing.

---

## Alternative: If You Want Explicit Notification

```java
billingsManager.onBillingManuallyAdded();
```

Both work - choose whichever you prefer!
