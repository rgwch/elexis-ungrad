/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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

import java.util.Map;

/**
 * Created by gerry on 11.05.16.
 */
public interface Handler {
  public void signal(Map<String,Object> detail);
}
