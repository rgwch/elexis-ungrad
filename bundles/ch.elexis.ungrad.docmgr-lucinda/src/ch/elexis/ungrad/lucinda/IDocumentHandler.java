/*******************************************************************************
 * Copyright (c) 2016-2020 by G. Weirich
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
 ******************************************************************************/

package ch.elexis.ungrad.lucinda;

import org.eclipse.jface.action.IAction;

import ch.elexis.ungrad.lucinda.controller.Controller;

public interface IDocumentHandler{
	public IAction getSyncAction(final Controller controller);
	public IAction getFilterAction(final Controller controller);
}
