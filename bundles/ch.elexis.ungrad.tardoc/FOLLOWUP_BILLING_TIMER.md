# Followup Billing Timer - Implementation Details

## Overview
The BillingsManager now includes automatic followup billing functionality that monitors the timer and applies followup billing codes based on the configuration in `rsc/config.json`.

## How It Works

### 1. Initial Billing and Timer Start
When `autostart()` is called and the initial billing is successfully applied:
- The `initialBillingCount` is incremented
- `startFollowupTimer()` is called to begin monitoring

### 2. Timer Configuration
From `config.json`:
```json
"followup_billing": {
  "code": "CA.00.0030",   // Code to bill for followup
  "after": "5",           // Start after 5 minutes
  "every": "1",           // Repeat every 1 minute
  "max": "15"             // Maximum 15 times
}
```

### 3. Timer Behavior
The timer:
- Checks every minute (60000ms interval)
- Calculates elapsed time since timer start
- After reaching the "after" threshold (5 minutes):
  - Bills the followup code every "every" minutes (1 minute)
  - Increments `followupBillingCount`
  - Continues until `max` count is reached (15 times)

### 4. Automatic Stop Conditions
The timer stops automatically when:
- **Manual billing detected**: User adds any billing manually
- **Max reached**: Followup billing count reaches the `max` value
- **stopTimer() called**: Explicit stop (e.g., encounter closed)

### 5. Manual Billing Detection
The system detects manual billing by:
- Tracking billings applied automatically (`initialBillingCount + followupBillingCount`)
- Comparing with total billings in the encounter
- If `actual > expected`, manual billing is detected → timer stops

## Example Timeline

With configuration: `after=5, every=1, max=15`

```
Time (min)  | Action
------------|----------------------------------------------------
0           | Initial billing CA.00.0010 applied
            | Timer starts
1-4         | Timer checking, no action
5           | Followup billing CA.00.0030 applied (#1)
6           | Followup billing CA.00.0030 applied (#2)
7           | Followup billing CA.00.0030 applied (#3)
...         | ...
19          | Followup billing CA.00.0030 applied (#15)
            | Max reached, timer stops automatically
```

## Code Flow

```
autostart()
    ↓
Apply initial billing
    ↓
startFollowupTimer()
    ↓
Create Timer (checks every 60s)
    ↓
    ├─→ elapsedMinutes < after → wait
    ├─→ elapsedMinutes >= after
    │   ↓
    │   Check if (minutesSinceThreshold % every == 0)
    │   ↓
    │   checkForManualBilling()
    │   ↓
    │   ├─→ Manual billing detected → stopFollowupTimer()
    │   └─→ No manual billing
    │       ↓
    │       applyFollowupBilling()
    │       ↓
    │       Increment followupBillingCount
    │       ↓
    │       Check if followupBillingCount >= max
    │       ├─→ Yes → stopFollowupTimer()
    │       └─→ No → continue
    └─→ timerStopped → cancel
```

## API Methods

### Public Methods

```java
// Start autobilling and timer
void autostart()

// Stop the timer manually
void stopTimer()

// Get number of followup billings applied
int getFollowupBillingCount()
```

### Private Methods

```java
// Start the followup billing timer
void startFollowupTimer()

// Stop the followup billing timer
void stopFollowupTimer()

// Check if user added manual billing
boolean checkForManualBilling()

// Apply the followup billing code
void applyFollowupBilling()
```

## Configuration Options

### followup_billing.after
- **Type**: String (parsed as int)
- **Unit**: Minutes
- **Purpose**: Wait this many minutes before starting followup billing
- **Example**: "5" = start followup billing 5 minutes after initial billing

### followup_billing.every
- **Type**: String (parsed as int)
- **Unit**: Minutes
- **Purpose**: Interval between followup billings
- **Example**: "1" = bill every 1 minute after the threshold

### followup_billing.every
- **Type**: String (parsed as int)
- **Unit**: Count
- **Purpose**: Maximum number of followup billings to apply
- **Example**: "15" = stop after 15 followup billings

### followup_billing.code
- **Type**: String
- **Purpose**: Tardoc code to bill for followup
- **Example**: "CA.00.0030"

## Logging

The implementation provides comprehensive logging:

```
BillingsManager: Starting followup timer - after 5 min, every 1 min, max 15 times
BillingsManager: Applied followup billing CA.00.0030 (#1)
BillingsManager: Applied followup billing CA.00.0030 (#2)
...
BillingsManager: Manual billing detected, stopping followup timer
// or
BillingsManager: Reached max followup billings (15), stopping timer
```

## Edge Cases Handled

### 1. No Followup Configuration
If `followup_billing` is not configured or `code` is empty:
- Timer is not started
- Only initial billing is applied

### 2. Manual Billing During Timer
If user adds billing manually:
- Timer detects extra billing
- Timer stops automatically
- Log: "Manual billing detected, stopping followup timer"

### 3. Reaching Max Count
When max followup billings are applied:
- Timer stops automatically
- Log: "Reached max followup billings (15), stopping timer"

### 4. Timer Already Running
If `startFollowupTimer()` is called while timer is running:
- Existing timer is cancelled
- New timer starts with reset counters

### 5. Invalid Configuration
- `after/every/max` not a number → defaults to 1
- Missing or empty `code` → timer not started
- Followup code not found in Tardoc → logged as error

## Thread Safety

- Timer runs in background thread (daemon)
- Timer task catches all exceptions to prevent crashes
- UI updates are posted via EventService

## Memory Management

- Timer is cancelled when stopped
- Timer is daemon thread (doesn't prevent JVM exit)
- Timer reference is nulled after cancellation

## Testing Scenarios

### Test 1: Normal Followup Billing
**Setup**: after=5, every=1, max=3
**Steps**:
1. Start timer
2. Wait 5 minutes
3. Observe billing every minute
4. After 3 followup billings, timer stops

**Expected**:
- Initial billing at t=0
- Followup #1 at t=5
- Followup #2 at t=6
- Followup #3 at t=7
- Timer stops

### Test 2: Manual Billing Stops Timer
**Setup**: after=5, every=1, max=15
**Steps**:
1. Start timer
2. Wait 6 minutes (1 followup billing applied)
3. Add billing manually
4. Wait more time

**Expected**:
- Initial billing at t=0
- Followup #1 at t=5
- Manual billing at t=6
- Timer stops
- No more automatic billings

### Test 3: No Followup Configuration
**Setup**: No followup_billing in config
**Steps**:
1. Start timer

**Expected**:
- Only initial billing applied
- No timer started
- No followup billings

### Test 4: Max Limit Reached
**Setup**: after=1, every=1, max=2
**Steps**:
1. Start timer
2. Wait 3 minutes

**Expected**:
- Initial billing at t=0
- Followup #1 at t=1
- Followup #2 at t=2
- Timer stops (max reached)
- Log: "Reached max followup billings (2)"

## Integration Points

### When to Call autostart()
- When timer is started in the UI
- After verifying no existing billings
- When encounter is opened/created

### When to Call stopTimer()
- When user closes the encounter
- When user manually stops the timer
- When switching to different encounter
- On view disposal/cleanup

### Example Usage

```java
// In TardocKonsView or similar

// When timer starts
billingsManager.autostart();

// When user manually adds billing
// The timer will detect this automatically and stop

// When closing encounter or stopping timer
billingsManager.stopTimer();

// To check status
int count = billingsManager.getFollowupBillingCount();
System.out.println("Applied " + count + " followup billings");
```

## Best Practices

1. **Always call stopTimer() on cleanup**
   ```java
   @Override
   public void dispose() {
       if (billingsManager != null) {
           billingsManager.stopTimer();
       }
       super.dispose();
   }
   ```

2. **Check followup count for UI feedback**
   ```java
   int count = billingsManager.getFollowupBillingCount();
   updateStatusLabel("Followup billings: " + count);
   ```

3. **Listen to encounter update events**
   The timer posts update events after each billing application

4. **Configure reasonable limits**
   - `after`: Not too short (give user time)
   - `every`: Not too frequent (avoid spam)
   - `max`: Reasonable limit (e.g., 15 for max 15 minutes)

## Future Enhancements

1. **Pause/Resume**: Add ability to pause and resume timer
2. **Custom intervals**: Support different intervals at different times
3. **UI indicator**: Show timer status and countdown in UI
4. **Persistence**: Save timer state across application restarts
5. **Smart detection**: More sophisticated manual billing detection

## Troubleshooting

### Problem: Followup billing not starting
**Check**:
1. Is followup_billing configured in config.json?
2. Is followup_billing.code not empty?
3. Check console for "Starting followup timer" message
4. Verify initial billing was applied successfully

### Problem: Timer stops unexpectedly
**Check**:
1. Did user add billing manually?
2. Check console for "Manual billing detected" message
3. Was max count reached?
4. Check for exceptions in console

### Problem: Too many/few followup billings
**Check**:
1. Verify `max` value in config.json
2. Check elapsed time calculation
3. Verify `every` interval is correct
4. Check console logs for timing information

## Conclusion

The followup billing timer provides automatic, configuration-driven billing that monitors the timer and responds to user actions. The implementation is robust, handles edge cases, and provides comprehensive logging for debugging.
