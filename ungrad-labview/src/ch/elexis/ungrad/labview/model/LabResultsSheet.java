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
package ch.elexis.ungrad.labview.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.Util;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

/**
 * Collector for all Lab Items and Lab Results. This class does, for performance reasons,
 * deliberately not use the Elexis mechanism to retrieve and construct PersistentObjects. Instead,
 * it keeps LabItems cached and collects data for the selected patient in a single database call.
 * 
 * @author gerry
 * 		
 */
public class LabResultsSheet {
	Log log = Log.get("LabResultSheet");
	private static final String queryItems =
		"SELECT ID, titel,kuerzel,Gruppe,prio,RefMann,RefFrauOrTx,Typ, Einheit FROM LABORITEMS WHERE deleted='0'";
	private static final String queryResults =
		"SELECT ItemID, Datum, Zeit, Resultat, Kommentar FROM LABORWERTE where PatientID=? AND deleted='0'";
	Patient pat;
	
	TimeTool[] dateArray;
	Item[] itemArray;
	Map<Item, Bucket> recently;
	Map<Item, Bucket> lastYear;
	Map<Item, Bucket> older;
	JdbcLink j;
	Map<String, Item> items;
	Map<String, SortedSet<Item>> groups;
	SortedMap<Item, LabResultsRow> rows = new TreeMap<Item, LabResultsRow>();
	List<IObserver> observers = new ArrayList<>();
	
	@SuppressWarnings("deprecation")
	public LabResultsSheet(){
		j = PersistentObject.getConnection();
	}
	
	/**
	 * Set the Patient.
	 * 
	 * @param pat
	 *            patient or null
	 * @throws ElexisException
	 */
	public void setPatient(Patient pat) throws ElexisException{
		this.pat = pat;
		loadItems(false);
		if (pat == null) {
			dateArray = null;
			rows = null;
		} else {
			fetch();
		}
		for (IObserver o : observers) {
			o.signal(pat);
		}
	}
	
	public String[] getAllGroups(){
		return groups.keySet().toArray(new String[0]);
	}
	
	public Item[] getAllItemsForGroup(String group){
		Util.require(group != null, "group must not be null");
		Set<Item> items = groups.get(group);
		return (items == null) ? new Item[0] : items.toArray(new Item[0]);
	}
	
	public void addObserver(IObserver obs){
		Util.require(obs != null, "Observer must not be null");
		observers.add(obs);
	}
	
	public void removeObserver(IObserver obs){
		Util.require(obs != null, "Observer must not be null");
		observers.remove(obs);
	}
	
	/**
	 * Retrieve all LabItems stored in the systems.
	 * 
	 * @return an ordered Array with all items
	 */
	public Item[] getItems(){
		if (itemArray == null) {
			try {
				loadItems(true);
			} catch (ElexisException e) {
				log.log(e, "could not load LabItems", Log.ERRORS);
			}
		}
		return itemArray;
	}
	
	/**
	 * Get all LabResults of the current patient
	 * 
	 * @return an Array with all LabResultsRows or null if no Patient is selected.
	 * @see LabResultsRow
	 */
	public LabResultsRow[] getLabResults(){
		return rows == null ? null : rows.values().toArray(new LabResultsRow[0]);
	}
	
	/**
	 * Fetch the Bucket with the latest results (less than a month old)
	 * 
	 * @param item
	 *            The Item whose bucket is to retrieve
	 * @return the Bucket
	 * @see Bucket
	 */
	public Bucket getRecentBucket(Item item){
		return recently.getOrDefault(item, new Bucket(item));
	}
	
	/**
	 * Fetch the Bucket with the somewhat older Results (more than a month but less than a year old)
	 * 
	 * @param item
	 *            the Item whose Bucket is to retrieve
	 * @return the Bucket
	 * @see Bucket
	 */
	public Bucket getOneYearBucket(Item item){
		return lastYear.getOrDefault(item, new Bucket(item));
	}
	
	/**
	 * Fetch the Bucket with the older Results (more than a year old)
	 * 
	 * @param item
	 *            The Item whose Bucket is to retrieve
	 * @return the Bucket
	 */
	public Bucket getOlderBucket(Item item){
		return older.getOrDefault(item, new Bucket(item));
	}
	
	public Result getValue(int nRow, int nColumn){
		if (nColumn < 0 || dateArray == null || nColumn >= dateArray.length) {
			return null;
		}
		TimeTool date = dateArray[nColumn];
		LabResultsRow row = (LabResultsRow) rows.values().toArray()[nRow];
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
	public TimeTool[] getDates(){
		return dateArray;
	}
	
	private void loadItems(boolean bReload) throws ElexisException{
		if (bReload) {
			items = null;
		}
		if (items == null) {
			PreparedStatement psItems = j.getPreparedStatement(queryItems);
			try {
				ResultSet res = psItems.executeQuery();
				items = new HashMap<String, Item>();
				groups = new TreeMap<String, SortedSet<Item>>();
				while (res.next()) {
					Item item = new Item(res);
					
					SortedSet<Item> itemsInGroup = groups.get(item.get("gruppe"));
					if (itemsInGroup == null) {
						itemsInGroup = new TreeSet<Item>();
					}
					itemsInGroup.add(item);
					groups.put(item.get("gruppe"), itemsInGroup);
					items.put(item.get("id"), item);
				}
				List<Item> allItems = new LinkedList<Item>();
				for (Set<Item> set : groups.values()) {
					for (Item item : set) {
						allItems.add(item);
					}
				}
				itemArray = allItems.toArray(new Item[0]);
				
			} catch (SQLException ex) {
				throw new ElexisException("can't fetch Lab Items", ex);
			} finally {
				j.releasePreparedStatement(psItems);
			}
		}
	}
	
	private void fetch() throws ElexisException{
		loadItems(false);
		
		PreparedStatement ps = j.getPreparedStatement(queryResults);
		rows = new TreeMap<>();
		SortedSet<TimeTool> dates = new TreeSet<>();
		recently = new TreeMap<Item, Bucket>();
		lastYear = new TreeMap<Item, Bucket>();
		older = new TreeMap<Item, Bucket>();
		TimeTool oneMonth = new TimeTool();
		oneMonth.addDays(-30);
		TimeTool oneYear = new TimeTool();
		oneYear.addDays(-365);
		try {
			ps.setString(1, pat.getId());
			ResultSet res = ps.executeQuery();
			while (res.next()) {
				Result result = new Result(res);
				Item item = items.get(result.get("itemId"));
				if (item == null) {
					item = new Item("?");
				}
				TimeTool when = new TimeTool(result.get("datum"));
				dates.add(when);
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
				LabResultsRow row = rows.get(item);
				if (row == null) {
					row = new LabResultsRow(item, pat);
					rows.put(item, row);
				}
				row.add(result);
			}
			dateArray = (TimeTool[]) dates.toArray(new TimeTool[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ElexisException("error reading database", e);
		} finally {
			j.releasePreparedStatement(ps);
		}
		
	}
	
	/**
	 * get a list of Item-Groups for which Results exist in the current context
	 */
	public Object[] getGroups(){
		SortedSet<String> groups = new TreeSet<String>();
		rows.keySet().forEach(key -> groups.add(key.get("gruppe")));
		return groups.toArray();
	}
	
	/**
	 * Ger Results of a given group in the current context
	 * 
	 * @param group
	 * @return
	 */
	@NonNull
	public Object[] getRowsForGroup(String group){
		Util.require(group != null, "group must not be null");
		SortedSet<LabResultsRow> results = new TreeSet<LabResultsRow>();
		rows.keySet().forEach(key -> {
			if (key.get("gruppe").equals(group)) {
				results.add(rows.get(key));
			}
		});
		return results.toArray();
	}
	
	public String getNormRange(Item item){
		if (pat.getGender() == Gender.FEMALE) {
			return item.get("refFrauOrTx");
		} else {
			return item.get("refMann");
		}
	}
	
	public Result getResultForDate(Item item, TimeTool date){
		LabResultsRow row = rows.get(item);
		if (row == null) {
			return null;
		} else {
			return row.get(date);
		}
	}
}
