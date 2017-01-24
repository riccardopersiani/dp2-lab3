package it.polito.dp2.NFFG.sol3.service.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PoliciesDB {
	// The key is the PolicyID
	private static Map<String, PolicyInfo> policiesMap = new ConcurrentHashMap <String, PolicyInfo>();

	// Returns the Map that contains <NffgName, List<PolicyInfo>>
	public static Map<String, PolicyInfo> getPoliciesMap() {
		return PoliciesDB.policiesMap;
	}

	public static PolicyInfo getPolicy(String policyName) {
		System.out.println("Get policy:" + policyName);
		return PoliciesDB.policiesMap.get(policyName);
	}

	// Add an element to the list inside the map <NffgName, List<PolicyInfo>>
	public static void addNewPolicy(String policyName, PolicyInfo policyInfo){
		if(policyName == null)
			return;
		//System.out.println("Adding policy:" + policyName);
		PoliciesDB.policiesMap.put(policyName, policyInfo);
	}

	// Delete the specified policy element from the list, given by the nffg name, inside the map
	public static void deletePolicy(String policyName){
		System.out.println("Delete policy: " + policyName);
		PoliciesDB.policiesMap.remove(policyName);
	}

	// Delete the specified policy element from the list, given by the nffg name, inside the map
	public static void deleteAll(){
		System.out.println("Delete all policies...");
		PoliciesDB.policiesMap.clear();
	}

	//Print all the PolicyInfo elements in
	public static void printDB(){
		
	}



}
