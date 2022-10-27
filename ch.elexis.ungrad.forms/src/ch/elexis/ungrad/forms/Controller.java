package ch.elexis.ungrad.forms;

import ch.elexis.core.model.IPatient;
import ch.elexis.data.Patient;

public class Controller {
	private Patient currentPatient;
	
	void changePatient(Patient pat){
		currentPatient=pat;
	}
}
