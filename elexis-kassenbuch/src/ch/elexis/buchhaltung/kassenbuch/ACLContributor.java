/*******************************************************************************
 * Copyright (c) 2007-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  G. Weirich 5.11.2016: Migrated from 1.4 to 3.ungrad
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AbstractAccessControl;
import ch.elexis.admin.IACLContributor;

/**
 * The ACLContributor defines, what rights should be configured to use this plugin
 * 
 * @author gerry
 *
 */
public class ACLContributor implements IACLContributor {
	public static final ACE KB = new ACE(ACE.ACE_ROOT, "Kassenbuch");
	public static final ACE BOOKING = new ACE(KB, "Buchung");
	public static final ACE STORNO = new ACE(KB, "Storno");
	public static final ACE VIEW = new ACE(KB, "Display");
	
	public String[] reject(String[] acl){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ACE[] getACL(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void initializeDefaults(AbstractAccessControl ac){
		ac.grant("users", KB);
		
	}
}
