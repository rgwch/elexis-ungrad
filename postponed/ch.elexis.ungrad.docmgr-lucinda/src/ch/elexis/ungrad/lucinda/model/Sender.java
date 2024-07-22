/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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

package ch.elexis.ungrad.lucinda.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.PersistentObject;
import ch.elexis.ungrad.lucinda.Lucinda;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result.SEVERITY;

/**
 * The Sender takes a List of PersistentObjects from its creating customer and
 * sends them asynchroneously to Lucinda. Each Object gets "specified" by the
 * customer, i.e. the customer transforms the PersistentObject into a map of
 * properties to persist.
 * 
 * @author gerry
 * 
 */
public class Sender extends Job {
	private List<? extends PersistentObject> toDo;
	private Customer customer;
	private Lucinda lucinda;
	private Logger log = LoggerFactory.getLogger(getClass());
	private boolean bCopy = false;

	/**
	 * Create a new Sender. It will run immediately after creation
	 * 
	 * @param customer the customer who is able to specify documents to import to
	 *                 lucinda
	 * @param list     List of PersistenObjects to send
	 * @param bCopy    if false, Documents are only indexed. If true, Documents are
	 *                 imported to Lucinda
	 * @throws IOException
	 */
	public Sender(Customer customer, List<? extends PersistentObject> list, boolean bCopy) {
		super("indiziere");
		toDo = list;
		this.customer = customer;
		this.bCopy = bCopy;
		lucinda = new Lucinda();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		// SubMonitor sub = SubMonitor.convert(monitor, toDo.size());
		int size = toDo.size();
		monitor.beginTask("Indiziere Konsultationen", size);
		for (PersistentObject po : toDo) {
			if (po.exists()) {
				Map order = customer.specify(po);
				if (order == null) {
					break;
				} else {
					byte[] contents = (byte[]) order.get("payload"); //$NON-NLS-1$
					if (contents != null) { // skip empty files - no point in
											// indexing null
						String title = (String) order.get("title");
						monitor.subTask(title + "(noch " + size-- + ")");
						String type = (String) order.get("type");
						log.debug(title);
						try {
							Map result = lucinda.addToIndex(po.getId(), title == null ? "?" : title, //$NON-NLS-1$
									type == null ? "" : type, order, contents, bCopy); //$NON-NLS-1$
						} catch (Exception ex) {
							ExHandler.handle(ex);
							return new Status(SEVERITY.ERROR.ordinal(), "ch.ungrad.lucinda", ex.getMessage(), ex);
						}
					} else { // Skipped empty file -> Advance to next
						log.warn("Skipping empty Document " + order.get("title"));
					}
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
			// sub.split(1);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.worked(1);
		}
		return Status.OK_STATUS;
	}
}