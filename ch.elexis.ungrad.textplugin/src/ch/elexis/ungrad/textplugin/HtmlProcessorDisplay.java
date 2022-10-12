/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.textplugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class HtmlProcessorDisplay extends Composite {
	private HtmlDoc doc;
	 
	
	public HtmlProcessorDisplay(Composite parent, HtmlDoc document) {
		super(parent,SWT.NONE);
		doc=document;
	}
}
