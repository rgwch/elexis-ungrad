package ch.elexis.ungrad.tardoc.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class DiagnosesManager {
	
	private static IModelService icd10ModelService;
	
	/**
	 * Get the ICD-10 IModelService, with fallback to direct OSGi lookup if injection
	 * hasn't happened yet.
	 */
	private IModelService getIcd10ModelService() {
		if (icd10ModelService != null) {
			return icd10ModelService;
		}
		
		// Fallback: try to get service directly from OSGi
		try {
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			if (context != null) {
				String filter = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.base.ch.icd10)";
				ServiceReference<?>[] refs = context.getServiceReferences(IModelService.class.getName(), filter);
				if (refs != null && refs.length > 0) {
					IModelService service = (IModelService) context.getService(refs[0]);
					icd10ModelService = service; // Cache it
					return service;
				}
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Find diagnoses matching the given pattern in the specified code system.
	 * @param codeSystem either "ICD-10" or "TI-Code" or "All"
	 * @param pattern the search pattern
	 * @return list of matching diagnoses
	 */
	public List<IDiagnosis> findDiagnoses(String codeSystem, String pattern) {
		if (pattern == null || pattern.trim().isEmpty()) {
			return Collections.emptyList();
		}
		
		List<IDiagnosis> results = new ArrayList<>();
		
		// if codeSystem is "ICD-10", search ICD10 
		if ("ICD-10".equals(codeSystem)) {
			results.addAll(searchIcd10(pattern));
		}
		// if codeSystem is "TI-Code", search TessinerCode
		else if ("TI-Code".equals(codeSystem)) {
			results.addAll(searchTessinerCode(pattern));
		}
		// if codeSystem is "All", search both code systems and combine results
		else if ("All".equals(codeSystem)) {
			results.addAll(searchIcd10(pattern));
			results.addAll(searchTessinerCode(pattern));
		}
		
		return results;
	}
	
	/**
	 * Search for ICD-10 diagnoses matching the pattern
	 * @param pattern the search pattern (searches in code and text)
	 * @return list of matching ICD-10 diagnoses
	 */
	private List<IDiagnosis> searchIcd10(String pattern) {
		IModelService service = getIcd10ModelService();
		if (service == null) {
			System.err.println("DiagnosesManager: ICD-10 IModelService is not available");
			return Collections.emptyList();
		}
		
		try {
			IQuery<IDiagnosisTree> query = service.getQuery(IDiagnosisTree.class);
			
			// Create OR condition: search in both code and text fields
			query.startGroup();
			query.or("code", COMPARATOR.LIKE, "%" + pattern + "%", true);
			query.or("text", COMPARATOR.LIKE, "%" + pattern + "%", true);
			query.andJoinGroups();
			
			// Order by code
			query.orderBy("code", IQuery.ORDER.ASC);
			
			List<IDiagnosisTree> results = query.execute();
			
			// Convert IDiagnosisTree to IDiagnosis
			return results.stream()
					.map(d -> (IDiagnosis) d)
					.collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	/**
	 * Search for TI-Code diagnoses matching the pattern
	 * @param pattern the search pattern (searches in code and text)
	 * @return list of matching TI-Code diagnoses
	 */
	private List<IDiagnosis> searchTessinerCode(String pattern) {
		List<IDiagnosis> results = new ArrayList<>();
		String searchPattern = pattern.toLowerCase();
		
		try {
			// Get all root nodes (chapters)
			// Use reflection or CodeElementService to access TI-Code diagnoses
			// Since TessinerCode has access restrictions, we'll query through the model service
			
			// Try to get TI-Code model service
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			if (context != null) {
				try {
					String filter = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.base.ch.ticode)";
					ServiceReference<?>[] refs = context.getServiceReferences(IModelService.class.getName(), filter);
					if (refs != null && refs.length > 0) {
						IModelService ticodeService = (IModelService) context.getService(refs[0]);
						
						// Query all TI-Code diagnoses
						IQuery<IDiagnosisTree> query = ticodeService.getQuery(IDiagnosisTree.class);
						List<IDiagnosisTree> allDiagnoses = query.execute();
						
						// Filter by pattern
						for (IDiagnosisTree diagnosis : allDiagnoses) {
							String code = diagnosis.getCode();
							String text = diagnosis.getText();
							
							if ((code != null && code.toLowerCase().contains(searchPattern)) ||
							    (text != null && text.toLowerCase().contains(searchPattern))) {
								results.add((IDiagnosis) diagnosis);
							}
						}
					}
				} catch (InvalidSyntaxException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * Check if a TessinerCode matches the search pattern
	 * @param code the TessinerCode to check
	 * @param pattern the search pattern (lowercase)
	 * @return true if code or text contains the pattern
	 */
	private boolean matches(IDiagnosisTree diagnosis, String pattern) {
		if (diagnosis == null) {
			return false;
		}
		
		String codeStr = diagnosis.getCode();
		String text = diagnosis.getText();
		
		return (codeStr != null && codeStr.toLowerCase().contains(pattern)) ||
		       (text != null && text.toLowerCase().contains(pattern));
	}
}
