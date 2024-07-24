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
package ch.elexis.ungrad.labview.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.PersistentObject;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.Util;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

/**
 * Collector for all Lab Items and Lab Results. This class does, for performance
 * reasons, deliberately not use the Elexis mechanism to retrieve and construct
 * PersistentObjects. Instead, it keeps LabItems cached and collects data for
 * the selected patient in a single database call.
 * 
 * @author gerry
 * 
 */
public class LabResultsSheet {
	Log log = Log.get("LabResultSheet");
	private static final String queryItems = "SELECT ID, titel,kuerzel,Gruppe,prio,RefMann,RefFrauOrTx,Typ, Einheit FROM LABORITEMS WHERE deleted='0'";
	private static final String queryResults = "SELECT ID, ItemID, Datum, Zeit, Resultat, Kommentar FROM LABORWERTE where PatientID=? AND deleted='0'";
	IPatient pat;

	TimeTool[] dateArray;
	Map<Item, Bucket> recently;
	Map<Item, Bucket> lastYear;
	Map<Item, Bucket> older;
	JdbcLink j;
	Map<String, Item> allItemsByID;
	Map<String, List<Item>> groups;
	SortedMap<Item, LabResultsRow> itemsWithResults = new TreeMap<Item, LabResultsRow>();
	SortedSet<TimeTool> resultDates = new TreeSet<>();
	List<IObserver> observers = new ArrayList<>();
	TimeTool oneMonth = new TimeTool();
	TimeTool oneYear = new TimeTool();

	@SuppressWarnings("deprecation")
	public LabResultsSheet() {
		j = PersistentObject.getConnection();
		oneMonth.addDays(-30);
		oneYear.addDays(-365);

	}

	/**
	 * Set the Patient.
	 * 
	 * @param pat patient or null
	 * @throws ElexisException
	 */
	public void setPatient(IPatient pat) throws ElexisException {
		this.pat = pat;
		loadItems(false);
		if (pat == null) {
			dateArray = null;
			itemsWithResults = null;
		} else {
			fetch();
		}
		for (IObserver o : observers) {
			o.signal(pat);
		}
	}

	public void reload() throws ElexisException {
		loadItems(true);
		setPatient(pat);
	}

	public String[] getAllGroups() {
		return groups.keySet().toArray(new String[0]);
	}

	/**
	 * Fetch all Items that belong to a group
	 * 
	 * @param group the group to query
	 * @return An Item[] which may be empty but is never null
	 */
	public Item[] getAllItemsForGroup(String group) {
		Util.require(group != null, "group must not be null");
		List<Item> items = groups.get(group);
		if (items == null) {
			return new Item[0];
		} else {
			Collections.sort(items);
			return items.toArray(new Item[0]);
		}
	}

	/**
	 * Register an Observer that will be notified when the LabResultSheet reloads
	 * 
	 * @param obs The Observer to register
	 */
	public void addObserver(IObserver obs) {
		Util.require(obs != null, "Observer must not be null");
		observers.add(obs);
	}

	/**
	 * Unregister a previously registered Obsever
	 * 
	 * @param obs
	 */
	public void removeObserver(IObserver obs) {
		Util.require(obs != null, "Observer must not be null");
		observers.remove(obs);
	}

	/**
	 * Retrieve all LabItems stored in the systems.
	 * 
	 * @return an ordered Array with all items
	 */
	public Item[] getItems() {
		if (allItemsByID == null) {
			try {
				loadItems(true);
			} catch (ElexisException e) {
				log.log(e, "could not load LabItems", Log.ERRORS);
			}
		}
		List<Item> list = new ArrayList<Item>(allItemsByID.size());
		for (Item it : allItemsByID.values()) {
			list.add(it);
		}
		Collections.sort(list);
		return list.toArray(new Item[0]);
	}

	/**
	 * Get all LabResults of the current patient
	 * 
	 * @return an Array with all LabResultsRows or null if no Patient is selected.
	 * @see LabResultsRow
	 */
	public LabResultsRow[] getLabResults() {
		return itemsWithResults == null ? null : itemsWithResults.values().toArray(new LabResultsRow[0]);
	}

	/**
	 * Fetch the Bucket with the latest results (less than a month old)
	 * 
	 * @param item The Item whose bucket is to retrieve
	 * @return the Bucket
	 * @see Bucket
	 */
	public Bucket getRecentBucket(Item item) {
		return recently.getOrDefault(item, new Bucket(item));
	}

	/**
	 * Fetch the Bucket with the somewhat older Results (more than a month but less
	 * than a year old)
	 * 
	 * @param item the Item whose Bucket is to retrieve
	 * @return the Bucket
	 * @see Bucket
	 */
	public Bucket getOneYearBucket(Item item) {
		return lastYear.getOrDefault(item, new Bucket(item));
	}

	/**
	 * Fetch the Bucket with the older Results (more than a year old)
	 * 
	 * @param item The Item whose Bucket is to retrieve
	 * @return the Bucket
	 */
	public Bucket getOlderBucket(Item item) {
		return older.getOrDefault(item, new Bucket(item));
	}

	public Result getValue(int nRow, int nColumn) {
		if (nColumn < 0 || dateArray == null || nColumn >= dateArray.length) {
			return null;
		}
		TimeTool date = dateArray[nColumn];
		LabResultsRow row = (LabResultsRow) itemsWithResults.values().toArray()[nRow];
		for (Result res : row.results) {
			if (res.get("datum").equals(date.toString(TimeTool.DATE_COMPACT))) {
				return res;
			}
		}
		return null;
	}

	/**
	 * Fetch all dates with at least one Lab test for the current patient.
	 * 
	 * @return an ordered Array with alld ates
	 */
	public TimeTool[] getDates() {
		return dateArray;
	}

	/**
	 * load all LabItems defined in the system
	 * 
	 * @param bReload force reload from the database even if Items are loaded
	 *                already
	 * @throws ElexisException database errors
	 */
	private void loadItems(boolean bReload) throws ElexisException {
		if (bReload) {
			allItemsByID = null;
		}
		if (allItemsByID == null) {
			PreparedStatement psItems = j.getPreparedStatement(queryItems);
			try {
				ResultSet res = psItems.executeQuery();
				allItemsByID = new HashMap<String, Item>();
				groups = new TreeMap<String, List<Item>>();
				while (res.next()) {
					Item item = new Item(res);

					List<Item> itemsInGroup = groups.get(item.get("gruppe"));
					if (itemsInGroup == null) {
						itemsInGroup = new ArrayList<Item>();
					}
					itemsInGroup.add(item);
					groups.put(item.get("gruppe"), itemsInGroup);
					allItemsByID.put(item.get("id"), item);
				}

			} catch (SQLException ex) {
				throw new ElexisException("can't fetch Lab Items", ex);
			} finally {
				j.releasePreparedStatement(psItems);
			}
		}
	}

	/**
	 * Load all LabResults of the current patient
	 * 
	 * @throws ElexisException database errors
	 */
	private void fetch() throws ElexisException {
		loadItems(false);
		resultDates.clear();
		PreparedStatement ps = j.getPreparedStatement(queryResults);
		itemsWithResults = new TreeMap<>();
		recently = new TreeMap<Item, Bucket>();
		lastYear = new TreeMap<Item, Bucket>();
		older = new TreeMap<Item, Bucket>();
		if (pat != null) {
			try {
				ps.setString(1, pat.getId());
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					Result result = new Result(res);
					addResult(result);
				}
				dateArray = (TimeTool[]) resultDates.toArray(new TimeTool[0]);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new ElexisException("error reading database", e);
			} finally {
				j.releasePreparedStatement(ps);
			}
		}

	}

	public void addResult(Result result) {
		Item item = allItemsByID.get(result.get("itemId"));
		if (item == null) {
			item = new Item("?");
		}
		TimeTool when = new TimeTool(result.get("datum"));
		resultDates.add(when);
		Bucket bucket;
		if (when.isBefore(oneYear)) {
			bucket = older.get(item);
			if (bucket == null) {
				bucket = new Bucket(item);
				older.put(item, bucket);
			}
		} else if (when.isBeforeOrEqual(oneMonth)) {
			bucket = lastYear.get(item);
			if (bucket == null) {
				bucket = new Bucket(item);
				lastYear.put(item, bucket);
			}
		} else {
			bucket = recently.get(item);
			if (bucket == null) {
				bucket = new Bucket(item);
				recently.put(item, bucket);
			}
		}
		bucket.addResult(result);
		LabResultsRow row = itemsWithResults.get(item);
		if (row == null && pat != null) {
			row = new LabResultsRow(item, pat);
			itemsWithResults.put(item, row);
		}
		row.add(result);

	}

	/**
	 * get a list of Item-Groups for which Results exist for the current Patient
	 */
	public Object[] getGroups() {
		SortedSet<String> groups = new TreeSet<String>();
		itemsWithResults.keySet().forEach(key -> {
			String grp = key.get("gruppe");
			groups.add(grp != null ? grp : "-");
		});
		return groups.toArray();
	}

	/**
	 * Get Results of a given group in the current context
	 * 
	 * @param group
	 * @return
	 */
	@NonNull
	public Object[] getRowsForGroup(String group) {
		Util.require(group != null, "group must not be null");
		List<LabResultsRow> results = new ArrayList<LabResultsRow>();
		itemsWithResults.keySet().forEach(key -> {
			if (key.get("gruppe").equals(group)) {
				LabResultsRow r = itemsWithResults.get(key);
				if (r == null) {
					log.log("Null value for " + key.get("Titel"), Log.ERRORS);
				} else {
					results.add(itemsWithResults.get(key));
				}
			}
		});
		Collections.sort(results);
		return results.toArray();
	}

	public String getNormRange(Item item) {
		if (!pat.getGender().equals(Gender.MALE)) {
			return item.get("refFrauOrTx");
		} else {
			return item.get("refMann");
		}
	}

	public boolean isPathologic(Item item, Result result) {
		if (item == null || result == null || pat == null) {
			return false;
		}
		return item.isPathologic(pat, result.get("resultat"));
	}

	public Result getResultForDate(Item item, TimeTool date) {
		LabResultsRow row = itemsWithResults.get(item);
		if (row == null) {
			return null;
		} else {
			return row.get(date);
		}
	}

}
