package ch.elexis.ungrad.labview.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

public class LabResultsSheet {
	private static final String queryItems = "SELECT ID, titel,kuerzel,Gruppe,prio,RefMann,RefFrauOrTx FROM LABORITEMS WHERE deleted='0'";
	private static final String queryResults = "SELECT ItemID, Datum, Zeit, Resultat, Kommentar FROM LABORWERTE where PatientID=? AND deleted='0'";
	Patient pat;
	
	TimeTool[] dateArray;
	JdbcLink j;
	Map<String, Item> items;
	Map<String, SortedSet<Item>> groups;
	SortedMap<Item, LabResultsRow> rows;

	@SuppressWarnings("deprecation")
	public LabResultsSheet() {
		j = PersistentObject.getConnection();
	}

	public void setPatient(Patient pat) throws ElexisException{
		this.pat=pat;
		if(pat==null){
			dateArray=null;
			rows=null;
		}else{
			fetch();
		}
	}
	public int getColumnCount(){
		if(dateArray!=null){
			return dateArray.length;
		}else{
			return 0;
		}
	}
	
	public int getRowCount(){
		if(rows!=null){
			return rows.size();
		}else{
			return 0;
		}
	}
	
	public Result getValue(int nRow, int nColumn){
		if(nColumn<0 || nColumn>=dateArray.length){
			return null;
		}
		TimeTool date=dateArray[nColumn];
		LabResultsRow row=(LabResultsRow) rows.values().toArray()[nRow];
		for(Result res:row.results){
			if(res.date.equals(date.toString(TimeTool.DATE_COMPACT))){
				return res;
			}
		}
		return null;
	}
	
	public TimeTool[] getDates(){
		return dateArray;
	}
	
	private void loadItems(boolean bReload) throws ElexisException {
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
					Item item = new Item(res.getString(1), res.getString(2), res.getString(3), res.getString(4),
							res.getString(5), res.getString(6), res.getString(7));
					
					SortedSet<Item> itemsInGroup = groups.get(item.gruppe);
					if (itemsInGroup == null) {
						itemsInGroup = new TreeSet<Item>();
					}
					itemsInGroup.add(item);
					groups.put(item.gruppe, itemsInGroup);
					items.put(item.id, item);
				}

			} catch (SQLException ex) {
				throw new ElexisException("cant fetch Lab Items", ex);
			} finally {
				j.releasePreparedStatement(psItems);
			}
		}
	}

	private void fetch() throws ElexisException {
		loadItems(false);

		PreparedStatement ps = j.getPreparedStatement(queryResults);
		rows = new TreeMap<>();
		SortedSet<TimeTool> dates=new TreeSet<>();
		try {
			ps.setString(1, pat.getId());
			ResultSet res = ps.executeQuery();
			while (res.next()) {
				Result result = new Result(res.getString(1), res.getString(2), res.getString(3), res.getString(4),
						res.getString(5));
				Item item = items.get(result.itemId);
				if (item == null) {
					item = new Item("?", "?", "ZZZ", "999", "?", "?", "007");
				}
				dates.add(new TimeTool(result.date));
				LabResultsRow row = rows.get(item);
				if (row == null) {
					row = new LabResultsRow(item);
					rows.put(item, row);
				}
				row.add(result);
			}
			dateArray=(TimeTool[]) dates.toArray(new TimeTool[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ElexisException("error reading database", e);
		} finally {
			j.releasePreparedStatement(ps);
		}

	}
}
