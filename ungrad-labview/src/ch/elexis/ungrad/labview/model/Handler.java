package ch.elexis.ungrad.labview.model;

import java.util.List;

public interface Handler {
	public void signal(List<LabResultsRow> result);
}
