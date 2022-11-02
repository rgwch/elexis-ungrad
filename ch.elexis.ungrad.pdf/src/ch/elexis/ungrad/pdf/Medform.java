/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
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

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class Medform {

  String form;
  Manager mgr = new Manager();

  Map<String, String> mapping = Stream.of(new String[][] {
      { "firstname", "topmostSubform[0].page1[0].patientS1Address[0].firstName[0]" },
      { "lastname", "topmostSubform[0].page1[0].patientS1Address[0].lastName[0]" },
      { "birthdate", "topmostSubform[0].page1[0].patientS1Address[0].birthDate[0]" },
      { "sex", "topmostSubform[0].page1[0].patientS1Address[0].sex[0]" },
      { "street", "topmostSubform[0].page1[0].patientS1Address[0].street[0]" },
      { "zip", "topmostSubform[0].page1[0].patientS1Address[0].zip[0]" },
      { "city", "topmostSubform[0].page1[0].patientS1Address[0].city[0]" },
      { "phone", "topmostSubform[0].page1[0].patientS1Address[0].phone[0]" },
      { "mail", "topmostSubform[0].page1[0].patientS1Address[0].email[0]" },
      { "date", "topmostSubform[0].page1[0].formS1Struct[0].modificationDate[0]" },
      { "mandatorPhone", "topmostSubform[0].page1[0].providerS1Address[0].phone[0]" },
      { "mandatorFax", "topmostSubform[0].page1[0].providerS1Address[0].fax[0]" },
      { "mandatorEAN", "topmostSubform[0].page1[0].providerS1Address[0].ean[0]" },
      { "mandatorZSR", "topmostSubform[0].page1[0].providerS1Address[0].zsr[0]" },
      { "mandatorNameLine", "topmostSubform[0].page2[0].providerS1Address[0].condensedName[0]" },
      { "mandatorStreet", "topmostSubform[0].page2[0].providerS1Address[0].street[0]" },
      { "mandatorZip", "topmostSubform[0].page2[0].providerS1Address[0].zip[0]" },
      { "mandatorCity", "topmostSubform[0].page2[0].providerS1Address[0].city[0]" },
      { "mandatorAddress", "topmostSubform[0].page1[0].providerS1Address[0].blockAddress[0]" }

  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

  public Medform(String formpath) {
    this.form = formpath;
  }

  public String create(String outPath, Patient pat) throws Exception {
    Map<String, String> m = new HashMap<String, String>();
    if (pat != null) {
      m.put(get("firstname"), pat.getVorname());
      m.put(get("lastname"), pat.getName());
      m.put(get("birthdate"), pat.getGeburtsdatum());
      m.put(get("sex"), pat.getGeschlecht());
      m.put(get("street"), pat.get(Kontakt.FLD_STREET));
      m.put(get("zip"), pat.get(Kontakt.FLD_ZIP));
      m.put(get("city"), pat.get(Kontakt.FLD_PLACE));
      m.put(get("phone"), pat.get(Kontakt.FLD_PHONE1));
      m.put(get("date"), new TimeTool().toString(TimeTool.DATE_GER));
    }
    Mandant mand=ElexisEventDispatcher.getSelectedMandator();
    if(mand!=null) {
      m.put(get("mandatorAddress"), mand.getPostAnschrift(true));
      m.put(get("mandatorNameLine"), mand.get("Bezeichnung1")+" "+mand.get("Bezeichnung2"));
      m.put(get("mandatorEAN"), mand.get("EAN"));
      m.put(get("mandatorZSR"),mand.get("KSK"));
      m.put(get("mandatorStreet"), mand.get("Strasse"));
      m.put(get("mandatorZIP"), mand.get("Plz"));
      m.put(get("mandatorCity"),mand.get("Ort"));
           
    }
    return mgr.fillForm(form, outPath, m);
  }

  public String get(String field) {
    String ret = mapping.get(field);
    if (ret == null) {
      return "";
    } else {
      return ret;
    }
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
 * topmostSubform[0].page2[0].endDate[0]
 * topmostSubform[0].page2[0].numWeeks[0]
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
 * topmostSubform[0].page2[0].deleteMe[0]
 * topmostSubform[0].page2[0].deleteMe[1]
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