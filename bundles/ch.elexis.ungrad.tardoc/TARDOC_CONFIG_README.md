# TardocConfig Implementation Summary

## Overview
Successfully implemented TardocConfig class that reads and deserializes the contents of `rsc/config.json` using Gson.

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
- Added `TardocConfig config` field
- Constructor now calls `TardocConfig.load()` to read and parse config.json
- Added `getConfig()` method to access the configuration

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
    "autobilling_enabled": true,
    "initial_billing": "CA.00.0010",
    "followup_billing": {
      "code": "CA.00.0030",
      "after": "1",
      "every": "1"
    },
    "stop_timer": {
      "AA.05.0010": "Blutdruck/Puls: ...",
      // ... more codes
    },
    "continue_timer": {
      "AA.00.0060": "Besprechung: ...",
      // ... more codes
    }
  }
}
```

## Key Features

1. **Automatic Loading:** Configuration is automatically loaded when BillingsManager is instantiated
2. **Type Safety:** Provides typed access to all configuration properties
3. **Error Handling:** Gracefully handles missing or invalid configuration
4. **Extensible:** Easy to add more dignity codes or configuration properties
5. **Gson Integration:** Uses Gson (already available in project dependencies)

## Dependencies
- `com.google.gson` (version 2.12.1) - Already included in MANIFEST.MF

## Testing
To test the implementation:
1. Ensure `rsc/config.json` exists and is properly formatted
2. Create a BillingsManager instance
3. Call `getConfig()` to access the loaded configuration
4. Use the getter methods on DignityConfig to access specific values

## Error Handling
- If config.json cannot be loaded, `TardocConfig.load()` returns null
- Error messages are logged to System.err
- All getter methods handle null values gracefully
