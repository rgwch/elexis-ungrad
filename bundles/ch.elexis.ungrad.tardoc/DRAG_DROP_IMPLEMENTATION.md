# Drag and Drop Implementation for ITardocLeistung

## Overview
This document describes the drag and drop functionality implemented to allow users to drag ITardocLeistung elements from the billingPositionsViewer to the VerrechnungsDisplay (billed items view), with automatic refresh to show newly added items.

## Implementation Details

### 1. Added Drag Support to BillingPositionsComposite

The following changes were made to `BillingPositionsComposite.java`:

#### Added Imports
- `org.eclipse.e4.core.di.annotations.Optional`
- `org.eclipse.e4.ui.di.UIEventTopic`
- `org.eclipse.swt.dnd.DND`
- `org.eclipse.swt.dnd.DragSource`
- `org.eclipse.swt.dnd.DragSourceAdapter`
- `org.eclipse.swt.dnd.DragSourceEvent`
- `org.eclipse.swt.dnd.Transfer`
- `ch.elexis.core.common.ElexisEventTopics`
- `ch.elexis.core.data.service.StoreToStringServiceHolder`

#### New Method: addDragSupport()
A private method that configures drag support for the billing positions table viewer:

```java
private void addDragSupport() {
    int operations = DND.DROP_COPY;
    Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };

    DragSource dragSource = new DragSource(billingPositionsViewer.getTable(), operations);
    dragSource.setTransfer(transferTypes);
    dragSource.addDragListener(new DragSourceAdapter() {
        @Override
        public void dragSetData(DragSourceEvent event) {
            // Converts ITardocLeistung to string using StoreToStringService
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            // Validates that a selection exists
        }
    });
}
```

#### New Field: currentEncounter
Stores the current encounter to compare against event updates.

#### New Method: updateEncounter()
An E4 event handler that listens for encounter updates and refreshes the billed display:

```java
@Optional
@jakarta.inject.Inject
public void updateEncounter(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter) {
    if (encounter != null && encounter.equals(currentEncounter) && billed != null) {
        // Refresh the billed display to show newly added items
        billed.setEncounter(encounter);
    }
}
```

#### Updated Method: setKons()
Now stores the current encounter for event comparison:

```java
void setKons(IEncounter k) {
    currentEncounter = k;
    billed.setEncounter(k);
}
```

### 2. Enabled E4 Dependency Injection in TardocKonsView

The following changes were made to `TardocKonsView.java`:

#### Updated Method: createBillingPositionsSection()
Injects the E4 context to enable event handling:

```java
private void createBillingPositionsSection(Composite parent) {
    billingPositionsComposite = new BillingPositionsComposite(parent, SWT.NONE, this);
    
    // Inject E4 context to enable event handling
    CoreUiUtil.injectServices(billingPositionsComposite);
    
    // Set selection provider for the site
    getSite().setSelectionProvider(billingPositionsComposite.getBillingPositionsViewer());
}
```

### 3. How It Works

1. **User selects** an ITardocLeistung item in the billingPositionsViewer table
2. **User drags** the item (click and hold, then move the mouse)
3. **dragStart** is triggered - validates that there's a selection
4. **dragSetData** is triggered - converts the ITardocLeistung object to a string representation using `StoreToStringServiceHolder.getStoreToString()`
5. **User drops** the item onto the VerrechnungsDisplay
6. **VerrechnungsDisplay** receives the drop, converts the string back to ITardocLeistung using its internal `GenericObjectDropTarget` and `DropReceiver`
7. **Billing is processed** - the ITardocLeistung is added to the current encounter's billed items
8. **Event is posted** - VerrechnungsDisplay posts an `EVENT_UPDATE` event with the encounter
9. **Event is received** - BillingPositionsComposite's `updateEncounter()` method receives the event
10. **Display refreshes** - The billed display is refreshed to show the newly added item

### 4. Compatibility

The implementation is compatible with the existing VerrechnungsDisplay drop target because:

- ITardocLeistung extends IService, which extends ICodeElement
- VerrechnungsDisplay's DropReceiver accepts ICodeElement, IService, and IBillable objects
- The StoreToStringService provides bidirectional conversion between objects and strings
- E4 event system ensures proper communication between components

### 5. User Experience

Users can now:
- Select any Tardoc billing position from the search results
- Drag it to the billed items section (VerrechnungsDisplay)
- Drop it to add the service to the current encounter
- **See the newly added item immediately in the billed display** (automatic refresh)
- The service will be billed using the standard Elexis billing processor

## Testing Recommendations

1. Open a consultation view with Tardoc billing positions
2. Search for a Tardoc service
3. Select a result in the billing positions table
4. Drag the item to the VerrechnungsDisplay area
5. **Verify that the service is added to the billed items immediately**
6. Verify that the billed display shows the updated list
7. Verify that appropriate billing rules are applied

## Future Enhancements

Potential improvements:
- Support for multi-selection drag (dragging multiple items at once)
- Visual feedback during drag (cursor changes, highlighting)
- Drag preview showing what's being dragged
- Undo functionality for dropped items
