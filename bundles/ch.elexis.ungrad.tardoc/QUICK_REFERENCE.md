# Quick Reference - Followup Billing

## Configuration (rsc/config.json)
```json
{
  "3010": {
    "autobilling_enabled": true,
    "initial_billing": "CA.00.0010",
    "followup_billing": {
      "code": "CA.00.0030",
      "after": "5",
      "every": "1", 
      "max": "15"
    }
  }
}
```

## Usage
```java
// Start timer and autobilling
billingsManager.autostart();

// Stop timer
billingsManager.stopTimer();

// Check status
int count = billingsManager.getFollowupBillingCount();
```

## Behavior
1. **Initial**: CA.00.0010 applied at t=0
2. **Wait**: Timer waits 5 minutes
3. **Followup**: CA.00.0030 applied every 1 minute
4. **Stop**: After 15 followup billings or manual billing

## Auto-Stop Triggers
- Manual billing added by user
- Max followup count reached
- `stopTimer()` called

## Logging
```
Applied initial billing CA.00.0010 for dignity 3010
Starting followup timer - after 5 min, every 1 min, max 15 times
Applied followup billing CA.00.0030 (#1)
Applied followup billing CA.00.0030 (#2)
...
Manual billing detected, stopping followup timer
```

## Files Modified
- `BillingsManager.java` - Added timer and followup logic
- `TardocConfig.java` - Added max field to FollowupBilling

## Files Created
- `FOLLOWUP_BILLING_TIMER.md` - Detailed documentation
- `IMPLEMENTATION_COMPLETE.md` - Complete summary
- `QUICK_REFERENCE.md` - This file
