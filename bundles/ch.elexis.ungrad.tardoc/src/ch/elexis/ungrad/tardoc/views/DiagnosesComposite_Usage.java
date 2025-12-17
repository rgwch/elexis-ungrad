/**
 * Example usage of DiagnosesComposite
 * 
 * The DiagnosesComposite provides a complete UI for searching diagnoses
 * from ICD-10 and TI-Code systems.
 * 
 * Basic usage in a view:
 * 
 * public class MyDiagnosesView extends ViewPart {
 *     private DiagnosesComposite diagnosesComposite;
 *     
 *     @Override
 *     public void createPartControl(Composite parent) {
 *         diagnosesComposite = new DiagnosesComposite(parent, SWT.NONE);
 *         
 *         // Optional: Set the diagnoses viewer as selection provider
 *         getSite().setSelectionProvider(diagnosesComposite.getDiagnosesViewer());
 *     }
 *     
 *     @Override
 *     public void setFocus() {
 *         diagnosesComposite.setFocus();
 *     }
 * }
 * 
 * Features:
 * - Search field with real-time search
 * - Code system selector (All, ICD-10, TI-Code)
 * - Table viewer showing results in format: [CodeSystem] Code - Text
 * - Drag and drop support for diagnoses
 * - Status label showing search results count
 * 
 * The composite can be integrated into:
 * - Standalone views
 * - SashForms (like in TardocKonsView)
 * - Dialogs
 * - Other composite containers
 * 
 * Example integration into TardocKonsView:
 * 
 * private DiagnosesComposite diagnosesComposite;
 * 
 * // In createPartControl():
 * SashForm diagnosisSash = new SashForm(parent, SWT.HORIZONTAL);
 * diagnosesComposite = new DiagnosesComposite(diagnosisSash, SWT.NONE);
 * // ... other composites
 * 
 */
