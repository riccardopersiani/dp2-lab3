package it.polito.dp2.NFFG.sol3.service.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PoliciesDB {
	private static Map<String, PolicyInfo> policiesMap = new ConcurrentHashMap <String, PolicyInfo>();

	public static Map<String, PolicyInfo> getPoliciesMap() {
		return PoliciesDB.policiesMap;
	}

	public static PolicyInfo getPolicy(String policyName) {
		return PoliciesDB.policiesMap.get(policyName);
	}

	public static void addNewPolicy(String policyName, PolicyInfo policyInfo){
		PoliciesDB.policiesMap.put(policyName, policyInfo);
	}

	public static void deletePolicy(String policyName){
		PoliciesDB.policiesMap.remove(policyName);
	}

	public static void deleteAll(){
		PoliciesDB.policiesMap.clear();
	}

}
