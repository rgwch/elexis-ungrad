/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.tardoc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.tardoc.messages"; //$NON-NLS-1$

	// TardocKonsView
	public static String TardocKonsView_ToggleAdditions_Tooltip;
	public static String TardocKonsView_SwitchToDiagnoses_Tooltip;
	public static String TardocKonsView_SwitchToBillingPositions_Tooltip;

	// TimerComposite
	public static String TimerComposite_StartTimer_Tooltip;
	public static String TimerComposite_PauseTimer_Tooltip;
	public static String TimerComposite_ResetTimer_Tooltip;

	// BillingPositionsComposite
	public static String BillingPositionsComposite_Search_Label;
	public static String BillingPositionsComposite_Search_Placeholder;

	// DiagnosesComposite
	public static String DiagnosesComposite_CodeSystem_Label;
	public static String DiagnosesComposite_CodeSystem_All;
	public static String DiagnosesComposite_CodeSystem_ICD10;
	public static String DiagnosesComposite_CodeSystem_TICode;
	public static String DiagnosesComposite_Search_Label;
	public static String DiagnosesComposite_Search_Placeholder;

	// CasesComposite
	public static String CasesComposite_NoCase_Selected;
	public static String CasesComposite_Closed_Suffix;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
