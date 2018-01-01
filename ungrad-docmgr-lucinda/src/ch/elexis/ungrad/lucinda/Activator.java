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
 *********************************************************************************/

package ch.elexis.ungrad.lucinda;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.core.data.util.Extensions;
import io.vertx.core.json.JsonObject;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.rgw.elexis.docmgr-lucinda"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private List<Handler> handlers = new ArrayList<>();
	private List<JsonObject> messages = new LinkedList<>();
	private List<IDocumentHandler> addons;
	
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		addons = Extensions.getClasses("ch.elexis.ungrad.lucinda.addon", "DocumentHandler");
	}

	public void addHandler(Handler handler) {
		handlers.add(handler);
	}

	public void removeHandler(Handler handler) {
		handlers.remove(handler);
	}

	public List<IDocumentHandler> getAddons() {
		return addons;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public void addMessage(JsonObject message) {
		messages.add(message);
	}

	public void addMessages(List<JsonObject> messages) {
		this.messages.addAll(messages);
	}

	public List<JsonObject> getMessages() {
		return messages;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
