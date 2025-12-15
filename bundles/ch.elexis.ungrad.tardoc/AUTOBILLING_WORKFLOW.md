# Autobilling Implementation - Complete Workflow

## Overview
The BillingsManager now checks if the current mandator has one of the dignities configured in `rsc/config.json`. If autobilling is enabled for that dignity, it automatically applies the initial billing code when the timer starts.

## Workflow Steps

### 1. Initialization (Constructor)
When `BillingsManager` is created:

```java
public BillingsManager(TardocKonsView view) {
    this.tkv = view;
    // Load configuration from rsc/config.json
    this.config = TardocConfig.load();
    // Determine which dignity the current mandator has
    updateActiveDignityConfig();
}
```

The `updateActiveDignityConfig()` method:
- Iterates through all dignity codes in config.json (e.g., "3010")
- Checks if the current mandator has each dignity using `TardocManager.mandatorHasDignity()`
- Caches the first matching dignity configuration
- Logs the result: "Active dignity 3010, autobilling enabled"

### 2. Timer Start (autostart method)
When the timer is started via `autostart()`:

```java
public void autostart() {
    // 1. Check preconditions
    if (this.kons == null) return;
    if (this.activeDignityConfig == null) return;
    
    // 2. Check if encounter already has billings
    List<IBilled> billed = this.kons.getBilled();
    if (billed != null && !billed.isEmpty()) return;
    
    // 3. Check if autobilling is enabled
    if (!activeDignityConfig.isAutobillingEnabled()) return;
    
    // 4. Get initial billing code from config
    String initialBillingCode = activeDignityConfig.getInitialBilling();
    
    // 5. Look up the ITardocLeistung
    ITardocLeistung initialBilling = manager.getLeistungByCode(initialBillingCode, ...);
    
    // 6. Bill the initial code
    BillingServiceHolder.get().bill(initialBilling, this.kons, 1);
    
    // 7. Trigger UI update
    ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);
}
```

## Configuration Example

```json
{
  "3010": {
    "autobilling_enabled": true,
    "initial_billing": "CA.00.0010",
    "followup_billing": {
      "code": "CA.00.0030",
      "after": "1",
      "every": "1"
    },
    "stop_timer": {
      "AA.05.0010": "Blutdruck/Puls: ..."
    },
    "continue_timer": {
      "AA.00.0060": "Besprechung: ..."
    }
  }
}
```

## Code Flow Diagram

```
┌─────────────────────────────────────────┐
│ BillingsManager Constructor             │
├─────────────────────────────────────────┤
│ 1. Load rsc/config.json                 │
│ 2. Check mandator's dignities           │
│ 3. Find matching dignity in config      │
│ 4. Cache active dignity config          │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ autostart() called                      │
├─────────────────────────────────────────┤
│ 1. Has active dignity config?           │
│    └─ No → Exit                         │
│ 2. Encounter has billings?              │
│    └─ Yes → Exit                        │
│ 3. Autobilling enabled?                 │
│    └─ No → Exit                         │
│ 4. Get initial billing code             │
│ 5. Look up ITardocLeistung              │
│ 6. Bill the code                        │
│ 7. Update UI                            │
└─────────────────────────────────────────┘
```

## Key Design Decisions

### 1. Caching Active Dignity
The active dignity configuration is determined once in the constructor and cached. This avoids repeated lookups and improves performance.

**Pros:**
- Fast access during autostart
- Clear separation of concerns
- Easy to access from other methods

**Cons:**
- If mandator changes, BillingsManager needs to be recreated
- Consider adding a refresh method if needed

### 2. Early Return Pattern
The autostart method uses early returns for clarity:
```java
if (condition_not_met) return;
// Continue with normal flow
```

This makes the logic easy to follow and debug.

### 3. Logging
All significant actions are logged:
- Dignity detection: "Active dignity 3010, autobilling enabled"
- Autobilling disabled: "Autobilling disabled for dignity 3010"
- Success: "Applied initial billing CA.00.0010 for dignity 3010"
- Errors: "Initial billing code CA.00.0010 not found in Tardoc"

### 4. Error Handling
The implementation is defensive:
- Null checks for config, encounter, dignity config
- Empty string checks for codes
- Try-catch in TardocConfig.load()
- Fallback values in FollowupBilling.getAfter()/getEvery()

## Public API

### BillingsManager Methods

```java
// Get the full configuration
TardocConfig getConfig()

// Get active dignity config for current mandator
TardocConfig.DignityConfig getActiveDignityConfig()

// Get active dignity code (e.g., "3010")
String getActiveDignityCode()

// Trigger autobilling
void autostart()
```

### TardocConfig.DignityConfig Methods

```java
// Check if autobilling is enabled
boolean isAutobillingEnabled()

// Get initial billing code
String getInitialBilling()

// Get followup billing configuration
FollowupBilling getFollowupBilling()

// Get stop timer codes and their default text
Map<String, String> getStopTimer()

// Get continue timer codes and their default text
Map<String, String> getContinueTimer()
```

## Usage Example

```java
// In TardocKonsView or similar
BillingsManager manager = new BillingsManager(this);

// Check what's configured
if (manager.getActiveDignityConfig() != null) {
    String code = manager.getActiveDignityCode();
    String initialBilling = manager.getActiveDignityConfig().getInitialBilling();
    System.out.println("Mandator has dignity " + code + 
                      ", will bill " + initialBilling);
}

// When timer starts
manager.autostart(); // Will apply initial billing if configured
```

## Testing Scenarios

### Test 1: Happy Path
- Mandator has dignity "3010"
- "3010" is configured in config.json
- autobilling_enabled = true
- Encounter has no billings
- **Expected:** Initial billing code is applied

### Test 2: Autobilling Disabled
- Mandator has dignity "3010"
- "3010" is configured with autobilling_enabled = false
- **Expected:** No billing applied, log message shown

### Test 3: No Matching Dignity
- Mandator has dignity "9999"
- Only "3010" is configured in config.json
- **Expected:** activeDignityConfig is null, no billing applied

### Test 4: Existing Billings
- Mandator has dignity "3010"
- Encounter already has billings
- **Expected:** No additional billing applied

### Test 5: Invalid Initial Code
- Mandator has dignity "3010"
- initial_billing = "INVALID.CODE"
- **Expected:** Error logged, no billing applied

## Future Enhancements

### 1. Followup Billing
Implement automatic followup billing based on:
- `followup_billing.after`: Days after initial
- `followup_billing.every`: Repeat interval

### 2. Stop/Continue Timer Logic
Use the stop_timer and continue_timer maps to:
- Insert default text when codes are billed
- Stop/continue timer based on code type

### 3. Multiple Dignities
Handle cases where mandator has multiple configured dignities:
- Priority system
- Allow multiple active configs

### 4. Dynamic Refresh
Add method to refresh active dignity config when mandator changes:
```java
public void refreshActiveDignityConfig() {
    updateActiveDignityConfig();
}
```

## Troubleshooting

### Problem: Autobilling not working
**Check:**
1. Is config.json properly formatted?
2. Does mandator have a configured dignity?
3. Is autobilling_enabled = true?
4. Does encounter already have billings?
5. Check console logs for error messages

### Problem: Wrong code being billed
**Check:**
1. Verify initial_billing in config.json
2. Check if code exists in Tardoc database
3. Verify mandator's dignity is correct

### Problem: Config not loading
**Check:**
1. Is rsc/config.json in the bundle?
2. Check console for "Error loading config.json"
3. Verify JSON syntax (no trailing commas, proper quotes)

## Conclusion

The autobilling implementation provides a flexible, configuration-driven approach to automatically billing initial consultation codes based on mandator dignities. The system is extensible for future enhancements like followup billing and timer management.
