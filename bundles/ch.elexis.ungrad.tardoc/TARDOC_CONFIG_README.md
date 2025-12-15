# TardocConfig Implementation Summary

## Overview
Successfully implemented TardocConfig class that reads and deserializes the contents of `rsc/config.json` using Gson. The BillingsManager now automatically checks the current mandator's dignities against the configured dignities and applies initial billing if autobilling is enabled.

## Files Created/Modified

### 1. TardocConfig.java
**Location:** `src/ch/elexis/ungrad/tardoc/services/TardocConfig.java`

**Features:**
- Reads configuration from `rsc/config.json`
- Parses JSON using Gson with custom deserializer
- Provides structured access to billing configuration
- Supports multiple dignity codes with their specific configurations

**Main Classes:**
- `TardocConfig` - Main configuration holder
- `DignityConfig` - Configuration for specific dignity code (e.g., "3010")
- `FollowupBilling` - Followup billing rules
- `TardocConfigDeserializer` - Custom Gson deserializer (inner class)

**Usage:**
```java
TardocConfig config = TardocConfig.load();
TardocConfig.DignityConfig dignityConfig = config.getDignityConfig("3010");
if (dignityConfig != null) {
    String initialBilling = dignityConfig.getInitialBilling();
    boolean autoEnabled = dignityConfig.isAutobillingEnabled();
    // ... more operations
}
```

### 2. BillingsManager.java (Updated)
**Location:** `src/ch/elexis/ungrad/tardoc/services/BillingsManager.java`

**Changes:**
- Added `TardocConfig config` field to hold loaded configuration
- Added `activeDignityConfig` field to cache the current mandator's dignity configuration
- Added `activeDignityCode` field to track which dignity is active
- Constructor now:
  - Calls `TardocConfig.load()` to read and parse config.json
  - Calls `updateActiveDignityConfig()` to determine the active dignity
- Added `updateActiveDignityConfig()` method to check mandator's dignities against configured dignities
- Added `getActiveDignityConfig()` method to access the active dignity configuration
- Added `getActiveDignityCode()` method to get the active dignity code
- Updated `autostart()` method to:
  - Check if current mandator has one of the configured dignities
  - Verify autobilling is enabled for that dignity
  - Apply the initial billing code specified in the configuration
  - Log all actions for debugging

**New Behavior:**
The `autostart()` method now:
1. Checks if an active dignity configuration exists (mandator has one of the configured dignities)
2. Verifies no billings exist yet on the encounter
3. Checks if autobilling is enabled for the active dignity
4. Retrieves the initial billing code from the configuration
5. Applies the initial billing to the encounter
6. Triggers UI update

### 3. TardocConfigExample.java (New)
**Location:** `src/ch/elexis/ungrad/tardoc/services/TardocConfigExample.java`

**Purpose:**
- Demonstrates how to use TardocConfig
- Shows examples of accessing various configuration properties

## Configuration Structure

The `rsc/config.json` file is structured as follows:

```json
{
  "3010": {                              // Dignity code
    "autobilling_enabled": true,         // Enable/disable autobilling
    "initial_billing": "CA.00.0010",     // Code to bill when starting
    "followup_billing": {
      "code": "CA.00.0030",              // Code for followup
      "after": "1",                       // Days after initial
      "every": "1"                        // Repeat every N days
    },
    "stop_timer": {
      "AA.05.0010": "Blutdruck/Puls: ...",  // Codes that stop timer
      // ... more codes
    },
    "continue_timer": {
      "AA.00.0060": "Besprechung: ...",     // Codes that continue timer
      // ... more codes
    }
  }
}
```

## Autobilling Workflow

1. **Initialization:** When BillingsManager is created, it:
   - Loads the configuration from `rsc/config.json`
   - Checks which configured dignities the current mandator has
   - Caches the active dignity configuration

2. **Timer Start:** When `autostart()` is called:
   - Checks if there's an active dignity configuration
   - Verifies no billings exist yet
   - Checks if `autobilling_enabled` is true
   - Retrieves the `initial_billing` code
   - Bills the initial code to the encounter

3. **Logging:** All steps are logged to console for debugging:
   - Active dignity detection
   - Autobilling status
   - Billing application
   - Errors (missing codes, config issues, etc.)

## Key Features

1. **Automatic Dignity Detection:** BillingsManager automatically detects which configured dignity the current mandator has
2. **Cached Configuration:** Active dignity config is cached for performance
3. **Flexible Configuration:** Easy to add more dignities by editing config.json
4. **Type Safety:** Provides typed access to all configuration properties
5. **Error Handling:** Gracefully handles missing or invalid configuration
6. **Extensible:** Easy to add more configuration properties or billing rules
7. **Gson Integration:** Uses Gson (already available in project dependencies)

## Dependencies
- `com.google.gson` (version 2.12.1) - Already included in MANIFEST.MF
- `ch.elexis.base.ch.arzttarife.tardoc` - For ITardocLeistung
- `ch.elexis.base.ch.arzttarife.util` - For ArzttarifeUtil (dignity checking)

## Testing
To test the implementation:
1. Ensure `rsc/config.json` exists and is properly formatted
2. Configure a mandator with dignity "3010" (or another configured dignity)
3. Create a BillingsManager instance
4. Call `autostart()` to trigger autobilling
5. Check console logs for debugging information
6. Verify the initial billing code was added to the encounter

## Configuration Options

### autobilling_enabled
- Type: boolean
- Default: false
- When true, initial billing is automatically applied when starting the timer

### initial_billing
- Type: string
- The Tardoc code to bill initially (e.g., "CA.00.0010")

### followup_billing
- Object with:
  - `code`: Tardoc code for followup (e.g., "CA.00.0030")
  - `after`: Days after initial billing
  - `every`: Repeat interval in days

### stop_timer / continue_timer
- Type: Map<String, String>
- Keys: Tardoc codes
- Values: Default text to insert when code is billed

## Error Handling
- If config.json cannot be loaded, `TardocConfig.load()` returns null
- If no matching dignity is found, autobilling is skipped
- If initial billing code is not found in Tardoc, error is logged
- Error messages are logged to System.err
- All getter methods handle null values gracefully
