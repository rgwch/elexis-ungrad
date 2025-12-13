# Automatic Billing of CA00.0010 on Timer Start

## Overview
This feature automatically adds the Tardoc service CA00.0010 (consultation) to the billing when the timer is started, but only if specific conditions are met.

## Feature Description

When the user clicks the "Start" button on the timer, the system checks if the consultation service (CA00.0010) should be automatically added to the billing.

## Conditions

The CA00.0010 service is automatically added ONLY if ALL of the following conditions are met:

1. **Timer is being started fresh** (not resumed from pause)
2. **Active encounter exists** (`actEncounter` is not null)
3. **No existing billings** - The encounter has no billed items yet
4. **Mandator has dignity 3010** - The current mandator has the Tardoc dignity code "3010"
5. **CA00.0010 exists** - The Tardoc service CA00.0010 can be found in the database

## Implementation Details

### TardocManager.java - New Methods

#### `getLeistungByCode(String code, BundleContext context)`
- Queries for a specific Tardoc service by its code
- Parameters:
  - `code`: The Tardoc code (e.g., "CA00.0010")
  - `context`: Optional BundleContext for OSGi service lookup
- Returns: The ITardocLeistung if found, null otherwise

#### `mandatorHasDignity(String dignityCode)`
- Checks if the current mandator has a specific dignity code
- Parameters:
  - `dignityCode`: The dignity code to check (e.g., "3010")
- Returns: true if the mandator has this dignity, false otherwise
- Uses `ArzttarifeUtil.getMandantTardocSepcialist()` to get mandator dignities

### TardocKonsView.java - Modified Methods

#### `startTimer()`
- Modified to call `checkAndBillConsultation()` when starting fresh (not resuming)
- Only adds CA00.0010 on fresh start, not when resuming from pause

#### `checkAndBillConsultation()` (NEW)
- Private method that implements the auto-billing logic
- Checks all conditions sequentially (fail-fast)
- Uses `BillingServiceHolder.get().bill()` to add the service
- Posts `EVENT_UPDATE` to refresh the display
- Steps:
  1. Check if encounter exists
  2. Check if encounter already has billings
  3. Get TardocManager instance
  4. Check if mandator has dignity 3010
  5. Query for CA00.0010
  6. Add to billing if found
  7. Trigger display refresh

## User Experience

### Scenario 1: Auto-billing happens
1. User opens a consultation for a patient
2. No services have been billed yet
3. Current mandator has dignity 3010
4. User clicks "Start" on the timer
5. **Result**: CA00.0010 is automatically added to the billing

### Scenario 2: Auto-billing does NOT happen (already has billings)
1. User opens a consultation that already has some billed items
2. User clicks "Start" on the timer
3. **Result**: Nothing is auto-billed (encounter already has billings)

### Scenario 3: Auto-billing does NOT happen (no dignity)
1. User's mandator does not have dignity 3010
2. User clicks "Start" on the timer
3. **Result**: Nothing is auto-billed (dignity requirement not met)

### Scenario 4: Resume from pause
1. User had already started the timer
2. User clicked "Pause"
3. User clicks "Start" again to resume
4. **Result**: Nothing is auto-billed (resuming, not a fresh start)

## Technical Notes

- The feature uses the existing billing infrastructure (`BillingServiceHolder`)
- The display is automatically refreshed via the E4 event system
- No user confirmation dialog is shown - the billing is added silently
- If any condition fails, the method returns early without error messages
- The dignity check uses the Tardoc-specific dignity codes from ArzttarifeUtil

## Benefits

1. **Saves time**: Automatically adds the most common consultation service
2. **Reduces errors**: Ensures consultation is billed when appropriate
3. **Smart conditions**: Only adds when it makes sense (no existing billings)
4. **Respects mandator rights**: Only for mandators with appropriate dignity
5. **Non-intrusive**: Works silently in the background

## Future Enhancements

Potential improvements:
- Configuration option to enable/disable auto-billing
- Configuration to specify different default services per mandator
- User notification (e.g., toast message) when auto-billing occurs
- Support for different dignity codes triggering different default services
- Undo functionality for auto-billed items
