package it.polito.dp2.NFFG.sol3.service.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFFG.sol3.service.jaxb.Policy;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;


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

	public static void updatePolicy(Policy policy){
		ReachabilityPolicyType2 rp = policy.getReachabilityPolicy();
		if(rp != null ){
			PolicyInfo policyInfo = new PolicyInfo(rp.getName(),rp.getNffg(),rp.getSource(),rp.getDestination(),rp.isIsPositive(),rp.getVerification());
			//policyInfo.setVerification(rp.getVerification());
			String policyName = rp.getName();
			PoliciesDB.policiesMap.put(policyName, policyInfo);
		}
		else{
			TraversalPolicyType2 tp = policy.getTraversalPolicy();
			PolicyInfo policyInfo = new PolicyInfo(tp.getName(),tp.getNffg(),tp.getSource(),tp.getDestination(),tp.isIsPositive(),tp.getDevices(),tp.getVerification());
			//policyInfo.setVerification(tp.getVerification());
			String policyName = tp.getName();
			PoliciesDB.policiesMap.put(policyName, policyInfo);
		}
	}

}
