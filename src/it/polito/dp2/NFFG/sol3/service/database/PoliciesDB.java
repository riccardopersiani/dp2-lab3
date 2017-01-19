package it.polito.dp2.NFFG.sol3.service.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PoliciesDB {
	// The key is the NffgName
	private static Map<String, List<PolicyInfo>> policiesMap = new ConcurrentHashMap <String, List<PolicyInfo>>();
	private static List<PolicyInfo> policyList = new ArrayList<PolicyInfo>();

	// Returns the Map that contains <NffgName, List<PolicyInfo>>
	public Map<String, List<PolicyInfo>> getPoliciesMap() {
		return policiesMap;
	}

	public List<PolicyInfo> getPoliciesList(String NffgName) {
		return policiesMap.get(NffgName);
	}


	// Add an element to the list inside the map <NffgName, List<PolicyInfo>>
	public static void addNewPolicy(String nffgName, PolicyInfo policyInfo){
		if(nffgName == null)
			return;
		//If List inside map is not initialized
		if(policiesMap.get(nffgName) == null){
			// Initialize the list and add the first item
			policyList.add(policyInfo);
			policiesMap.put(nffgName, policyList);
		}
		// If list already exist inside map add the PolicyInfo element to it	
		policiesMap.get(nffgName).add(policyInfo);
	}


	// Get a policyInfo element from a list of the map
	public static PolicyInfo getPolicy(String policyName, String nffgName){
		List<PolicyInfo> policyInfoList = new ArrayList<PolicyInfo>(); 
		policyInfoList = policiesMap.get(nffgName);
		for(PolicyInfo pi : policyInfoList){
			if(pi.getName() != null && pi.getName().contains(policyName));
			return pi;	
		}
		return null;
	}

	// Delete the specified policy element from the list, given by the nffg name, inside the map
	public static void deletePolicy(String policyName, String NffgName){
		policiesMap.get(NffgName).remove(policyName);
	}

	// Delete all the policy elements from the list, given by the nffg name, inside the map
	public static void deleteNffgPolicies(String nffgName){
		policiesMap.remove(nffgName);
	}

	// Delete the specified policy element from the list, given by the nffg name, inside the map
	public static void deleteAll(){
		policiesMap.clear();
	}

	//Print all the PolicyInfo elements in
	public static void printDB(){
		
	}



}
