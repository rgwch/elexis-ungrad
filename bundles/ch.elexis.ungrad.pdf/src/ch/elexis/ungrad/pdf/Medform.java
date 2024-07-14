/*******************************************************************************
 * Copyright (c) 2022-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;

import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IContextService;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Handle medForm- forms (@see http://medforms.ch)
 * 
 * @author gerry
 *
 */
public class Medform {

	String form;
	Manager mgr = new Manager();
	IContextService contextService=ContextServiceHolder.get();
	
	Map<String, String> mapping = Stream
			.of(new String[][] { { "patAddress", "topmostSubform[0].page1[0].patientS1Address[0].blockAddress[0]" },
					{ "patNameLine", "topmostSubform[0].page1[0].patientS1Address[0].condensedName[0]" },
					{ "patFirstname", "topmostSubform[0].page1[0].patientS1Address[0].firstName[0]" },
					{ "patLastname", "topmostSubform[0].page1[0].patientS1Address[0].lastName[0]" },
					{ "patBirthdate", "topmostSubform[0].page1[0].patientS1Address[0].birthDate[0]" },
					{ "patSex", "topmostSubform[0].page1[0].patientS1Address[0].sex[0]" },
					{ "patStreet", "topmostSubform[0].page1[0].patientS1Address[0].street[0]" },
					{ "patZip", "topmostSubform[0].page1[0].patientS1Address[0].zip[0]" },
					{ "patCity", "topmostSubform[0].page1[0].patientS1Address[0].city[0]" },
					{ "patPhone1", "topmostSubform[0].page1[0].patientS1Address[0].phone[0]" },
					{ "patPhone2", "topmostSubform[0].page1[0].patientS1Address[0].phone[1]" },
					{ "patMail", "topmostSubform[0].page1[0].patientS1Address[0].email[0]" },
					{ "docDate", "topmostSubform[0].page1[0].formS1Struct[0].modificationDate[0]" },
					{ "mandatorPhone1", "topmostSubform[0].page1[0].providerS1Address[0].phone[0]" },
					{ "mandatorMail", "topmostSubform[0].page1[0].providerS1Address[0].email[0]" },
					{ "mandatorFax", "topmostSubform[0].page1[0].providerS1Address[0].fax[0]" },
					{ "mandatorEAN", "topmostSubform[0].page1[0].providerS1Address[0].ean[0]" },
					{ "mandatorZSR", "topmostSubform[0].page1[0].providerS1Address[0].zsr[0]" },
					{ "mandatorNameLine", "topmostSubform[0].page2[0].providerS1Address[0].condensedName[0]" },
					{ "mandatorStreet", "topmostSubform[0].page2[0].providerS1Address[0].street[0]" },
					{ "mandatorZip", "topmostSubform[0].page2[0].providerS1Address[0].zip[0]" },
					{ "mandatorCity", "topmostSubform[0].page2[0].providerS1Address[0].city[0]" },
					{ "mandatorAddress", "topmostSubform[0].page1[0].providerS1Address[0].blockAddress[0]" },
					{ "insuranceName", "topmostSubform[0].page1[0].insuranceS1Address[0].companyName[0]" },
					{ "insuranceStreet", "topmostSubform[0].page1[0].insuranceS1Address[0].street[0]" },
					{ "insuranceZip", "topmostSubform[0].page1[0].insuranceS1Address[0].zip[0]" },
					{ "insuranceCity", "topmostSubform[0].page1[0].insuranceS1Address[0].city[0]" },
					{ "insuranceMail", "topmostSubform[0].page1[0].insuranceS1Address[0].email[0]" },
					{ "insuranceCaseNr", "topmostSubform[0].page1[0].lawS1Struct[0].insuredID[0]" },
					{ "insuranceLaw", "topmostSubform[0].page1[0].lawS1Struct[0].type[0]" },
					{ "receiverMail", "topmostSubform[0].page1[0].consumerS1Address[0].email[0]" }

			}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	/**
	 * 
	 * @param formpath full path to the medForm to fill
	 */
	public Medform(String formpath) {
		this.form = formpath;
	}

	/**
	 * Check if the currentliy loaded pdf is a medForm. Read the oid form-field.
	 * 
	 * @return true if an OID form field was found, and if its value starts with
	 *         "medforms".
	 */
	public boolean isMedform() {
		try {
			String oid = mgr.getFieldContents(form, "topmostSubform[0].page1[0].formS1Struct[0].oid[0]");
			return (oid != null) && oid.startsWith("medforms");
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}

	/**
   * Create a PDF file from formPath with prefilled fields
   * 
   * @param outPath
   *                Path for the newly created PDF
   * @param pat
   *                Patient for which the form should be prefilled
   * @return The Path to the filled form
   * @throws Exception
   */
  public String create(String outPath, IPatient pat) throws Exception {
    Map<String, String> m = new HashMap<String, String>();
    if (pat != null) {
      m.put(get("patAddress"), pat.getPostalAddress());  // .getPostAnschrift(true));
      m.put(get("patFirstname"), pat.getFirstName());
      m.put(get("patLastname"), pat.getLastName());
      m.put(get("patBirthdate"), pat.getDateOfBirth().toLocalDate().toString());
      m.put(get("patSex"), pat.getGender().toString());
      m.put(get("patStreet"), pat.getStreet());
      m.put(get("patZip"), pat.getZip());
      m.put(get("patCity"), pat.getCity());
      m.put(get("patPhone1"), pat.getPhone1());
      m.put(get("patPhone2"), pat.getPhone2());
      m.put(get("patMail"), pat.getEmail());
      m.put(get("docDate"), new TimeTool().toString(TimeTool.DATE_GER));
    }
    // Mandant mand = ElexisEventDispatcher.getSelectedMandator();
	IMandator mand = contextService.getActiveMandator().orElseThrow();
    if (mand != null) {
      m.put(get("mandatorAddress"), mand.getPostalAddress());
      m.put(get("mandatorNameLine"), mand.getDescription1()+" "+mand.getDescription2());
      m.put(get("mandatorEAN"), (String) mand.getExtInfo("EAN"));
      String ksk = (String) mand.getExtInfo("KSK");
      if (ksk==null) {
        ksk = (String) mand.getExtInfo("ZSR");
        if (ksk==null) {
          ksk = "";
        }
      }
      m.put(get("mandatorZSR"), ksk);
      m.put(get("mandatorStreet"), mand.getStreet());
      m.put(get("mandatorZip"), mand.getZip());
      m.put(get("mandatorCity"), mand.getCity());
      m.put(get("mandatorPhone1"), mand.getPhone1());
      m.put(get("mandatorMail"), mand.getEmail());
    }
    // Konsultation kons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
    ICoverage cov=contextService.getActiveCoverage().orElseThrow();
          if (cov != null) {
        IContact cb = cov.getCostBearer();
        if (cb != null) {
          m.put(get("insuranceName"), cb.getDescription1() + " " + cb.getDescription2());
          m.put(get("insuranceStreet"), StringTool.unNull(cb.getStreet()));
          m.put(get("insuranceZip"), StringTool.unNull(cb.getZip()));
          m.put(get("insuranceCity"), StringTool.unNull(cb.getCity()));
          m.put(get("insuranceMail"), StringTool.unNull(cb.getEmail()));

        }
        BillingLaw bl = cov.getBillingSystem().getLaw();
        String nr = cov.getInsuranceNumber();
        if (StringTool.isNothing(nr)) {
          nr = (String) cov.getExtInfo("Versicherungsnummer");
        }
        if (StringTool.isNothing(nr)) {
          nr = (String) cov.getExtInfo("Unfallnummer");
        }
        m.put(get("insuranceCaseNr"), StringTool.unNull(nr));
        m.put(get("insuranceLaw"), bl.toString());
      }
    

    return mgr.fillForm(form, outPath, m);
  }

	private String[] getPhones(Kontakt k) {
		String p1 = k.get(Kontakt.FLD_PHONE1);
		String p2 = k.get(Kontakt.FLD_PHONE2);
		String p3 = k.get(Kontakt.FLD_MOBILEPHONE);
		String[] ret = new String[2];
		if (!StringTool.isNothing(p3)) {
			ret[0] = p3;
			if (!StringTool.isNothing(p1) && !p1.equals(p3)) {
				ret[1] = p1;
			} else {
				if (!StringTool.isNothing(p2) && !p2.equals(p3)) {
					ret[1] = p2;
				} else {
					ret[1] = "";
				}
			}
		} else {
			if (!StringTool.isNothing(p1)) {
				ret[0] = p1;
				if (!StringTool.isNothing(p2) && !p1.equals(p2)) {
					ret[1] = p2;
				} else {
					ret[1] = "";
				}
			} else {
				ret[1] = "";
				if (!StringTool.isNothing(p3)) {
					ret[0] = p3;
				} else {
					ret[0] = "";
				}
			}
		}
		return ret;
	}

	public String get(String field) {
		String ret = mapping.get(field);
		if (ret == null) {
			return "";
		} else {
			return ret;
		}
	}

	/**
	 * Retrieve the Value of a field
	 * 
	 * @param name
	 * @return
	 */
	public String getFieldValue(String name) {
		String medformsField = get(name);
		if (medformsField != null) {
			try {
				return mgr.getFieldContents(this.form, medformsField);
			} catch (Exception e) {
				ExHandler.handle(e);
			}
		}
		return "";
	}
}
/*
 * topmostSubform[0].#pageSet[0].page[0].footer[0].pageNumber[0]
 * topmostSubform[0].#pageSet[0].page[1].footer[0].pageNumber[0]
 * topmostSubform[0].#pageSet[0].page[2].footer[0].pageNumber[0]
 * topmostSubform[0].page1[0].lawS1Struct[0].caseDate[0]
 * topmostSubform[0].page1[0].lawS1Struct[0].caseID[0]
 * topmostSubform[0].page1[0].lawS1Struct[0].type[0]
 * topmostSubform[0].page1[0].lawS1Struct[0].input[0]
 * topmostSubform[0].page1[0].accidentDetails[0]
 * topmostSubform[0].page1[0].javascriptAvailability[0]
 * topmostSubform[0].page1[0].helpSystem[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].condensedName[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].condensedAddress[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].ean[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].email[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].phone[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].fax[0]
 * topmostSubform[0].page1[0].consumerS1Address[0].input[0]
 * topmostSubform[0].page1[0].recipientAddress[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].condensedName[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].condensedAddress[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].ean[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].email[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].phone[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].fax[0]
 * topmostSubform[0].page1[0].insuranceS1Address[0].input[0]
 * topmostSubform[0].page1[0].employerS1Address[0].condensedName[0]
 * topmostSubform[0].page1[0].employerS1Address[0].street[0]
 * topmostSubform[0].page1[0].employerS1Address[0].zip[0]
 * topmostSubform[0].page1[0].employerS1Address[0].city[0]
 * topmostSubform[0].page1[0].employerS1Address[0].nif[0]
 * topmostSubform[0].page1[0].employerS1Address[0].input[0]
 * topmostSubform[0].page1[0].patientS1Address[0].firstName[0]
 * topmostSubform[0].page1[0].patientS1Address[0].lastName[0]
 * topmostSubform[0].page1[0].patientS1Address[0].street[0]
 * topmostSubform[0].page1[0].patientS1Address[0].zip[0]
 * topmostSubform[0].page1[0].patientS1Address[0].city[0]
 * topmostSubform[0].page1[0].patientS1Address[0].ssn[0]
 * topmostSubform[0].page1[0].patientS1Address[0].birthDate[0]
 * topmostSubform[0].page1[0].patientS1Address[0].sex[0]
 * topmostSubform[0].page1[0].patientS1Address[0].profession[0]
 * topmostSubform[0].page1[0].patientS1Address[0].input[0]
 * topmostSubform[0].page1[0].treatmentS1Struct[0].beginDate[0]
 * topmostSubform[0].page1[0].treatmentS1Struct[0].where[0]
 * topmostSubform[0].page1[0].treatmentS1Struct[0].when[0]
 * topmostSubform[0].page1[0].treatmentS1Struct[0].input[0]
 * topmostSubform[0].page1[0].generalConditionStruct[0].ask4specialPerception[0]
 * topmostSubform[0].page1[0].generalConditionStruct[0].specialPerception[0]
 * topmostSubform[0].page1[0].anamnesisStruct[0].morphologicalFinding[0]
 * topmostSubform[0].page1[0].anamnesisStruct[0].functionalFinding[0]
 * topmostSubform[0].page1[0].anamnesisStruct[0].xray[0]
 * topmostSubform[0].page1[0].diagnosisS1Struct[0].name[0]
 * topmostSubform[0].page1[0].diagnosisS1Struct[0].code[0]
 * topmostSubform[0].page1[0].diagnosisS1Struct[0].type[0]
 * topmostSubform[0].page1[0].diagnosisS1Struct[0].input[0]
 * topmostSubform[0].page1[0].formDescription[0]
 * topmostSubform[0].page1[0].pad_medforms_form_id[0]
 * topmostSubform[0].page1[0].calcP1Pad_medfor)ms_form_id[0]
 * topmostSubform[0].page2[0].endDate[0] topmostSubform[0].page2[0].numWeeks[0]
 * topmostSubform[0].page2[0].therapyStruct[0].ask4Hospitalization[0]
 * topmostSubform[0].page2[0].therapyStruct[0].procedure[0]
 * topmostSubform[0].page2[0].hospitalS1Address[0].condensedName[0]
 * topmostSubform[0].page2[0].hospitalS1Address[0].condensedAddress[0]
 * topmostSubform[0].page2[0].hospitalS1Address[0].ean[0]
 * topmostSubform[0].page2[0].hospitalS1Address[0].input[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].percentageNum[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].dailyAttendance[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].dailyResilience[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].beginDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].endDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[0].input[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].percentageNum[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].dailyAttendance[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].dailyResilience[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].beginDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].endDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[1].input[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].percentageNum[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].dailyAttendance[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].dailyResilience[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].beginDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].endDate[0]
 * topmostSubform[0].page2[0].unemployabilityS1Struct[2].input[0]
 * topmostSubform[0].page2[0].deleteMe[0] topmostSubform[0].page2[0].deleteMe[1]
 * topmostSubform[0].page2[0].deleteMe[2]
 * topmostSubform[0].page2[0].formRemark[0]
 * topmostSubform[0].page2[0].formS1Struct[0].creationDate[0]
 * topmostSubform[0].page2[0].formS1Struct[0].modificationDate[0]
 * topmostSubform[0].page2[0].formS1Struct[0].oid[0]
 * topmostSubform[0].page2[0].formS1Struct[0].guid[0]
 * topmostSubform[0].page2[0].formS1Struct[0].language[0]
 * topmostSubform[0].page2[0].formS1Struct[0].version[0]
 * topmostSubform[0].page2[0].formS1Struct[0].serialNum[0]
 * topmostSubform[0].page2[0].formS1Struct[0].supervisorData[0]
 * topmostSubform[0].page2[0].formS1Struct[0].instructions[0]
 * topmostSubform[0].page2[0].providerS1Address[0].condensedName[0]
 * topmostSubform[0].page2[0].providerS1Address[0].street[0]
 * topmostSubform[0].page2[0].providerS1Address[0].zip[0]
 * topmostSubform[0].page2[0].providerS1Address[0].city[0]
 * topmostSubform[0].page2[0].providerS1Address[0].email[0]
 * topmostSubform[0].page2[0].providerS1Address[0].ean[0]
 * topmostSubform[0].page2[0].providerS1Address[0].zsr[0]
 * topmostSubform[0].page2[0].providerS1Address[0].phone[0]
 * topmostSubform[0].page2[0].providerS1Address[0].input[0]
 * topmostSubform[0].page2[0].submitLog[0]
 * topmostSubform[0].page2[0].calcSubmitLog[0]
 * topmostSubform[0].page2[0].dataSubmit[0]
 * topmostSubform[0].page2[0].formDescription[0]
 * topmostSubform[0].page2[0].fillerName[0]
 * topmostSubform[0].page2[0].calcNextPage[0]
 * topmostSubform[0].page2[0].calcP2Pad_medforms_form_id[0]
 * topmostSubform[0].page2[0].production_modus[0]
 * topmostSubform[0].page2[0].docOpenTime[0]
 * topmostSubform[0].page2[0].openAttach[0]
 * topmostSubform[0].page2[0].ask4Plausibility[0]
 * topmostSubform[0].page3[0].calcNextPage[0]
 * topmostSubform[0].page3[0].formDescription[0]
 * topmostSubform[0].page3[0].calcP3Pad_medforms_form_id[0]
 * topmostSubform[0].page3[0].addendum[0]
 * 
 * topmostSubform[0].page1[0].providerS1Address[0].blockAddress[0]->Eisenbeiss
 * topmostSubform[0].page1[0].providerS1Address[0].phone[0]->123
 * topmostSubform[0].page1[0].providerS1Address[0].fax[0]->456
 * topmostSubform[0].page1[0].providerS1Address[0].ean[0]->789
 * topmostSubform[0].page1[0].providerS1Address[0].zsr[0]->012
 * topmostSubform[0].page1[0].providerS1Address[0].input[0]->
 */