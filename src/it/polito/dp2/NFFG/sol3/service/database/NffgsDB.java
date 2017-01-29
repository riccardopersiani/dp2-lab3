package it.polito.dp2.NFFG.sol3.service.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;

public class NffgsDB {

	private static Map<String, NffgInfo> nffgMap = new ConcurrentHashMap<String, NffgInfo>();

	public static Map<String, NffgInfo> getNffgMap() {
		return nffgMap;
	}

	public static void addNewNffg(String nffgName, NffgInfo nffgInfo) {
		nffgMap.put(nffgName, nffgInfo);
	}

	public static void deleteNffg(String nffgName){
		nffgMap.remove(nffgName);
	}

	public static void deleteAll(){
		PoliciesDB.deleteAll();
		nffgMap.clear();
	}

	public static void printDB(){		
		Set<String> list = new HashSet<String>();
		list  = nffgMap.keySet();
		Iterator<String> iter = list.iterator();

		// Send the list of all nffgs
		while(iter.hasNext()) {
			String key = iter.next();
			nffgMap.get(key).printInfos();
		}
	}

	public static void addReachabilityPolicy(ReachabilityPolicyType2 policy){
		String nffgName = policy.getNffg();
		// Get the old nffgInfo
		NffgInfo nffgInfo = nffgMap.get(nffgName);
		
		
		ReachabilityPolicyType rp = new ReachabilityPolicyType();
		rp.setDestination(policy.getDestination());
		rp.setName(policy.getName());
		rp.setSource(policy.getSource());
		rp.setIsPositive(policy.isIsPositive());
		rp.setVerification(policy.getVerification());
		
		nffgInfo.getNffg().getPolicies().getReachabilityPolicy().add(rp);
		
		//Updating the nffgDB
		nffgMap.put(nffgName, nffgInfo);

	}
	
	public static void addTraversalPolicy(TraversalPolicyType2 policy) {
		String nffgName = policy.getNffg();
		// Get the old nffgInfo
		NffgInfo nffgInfo = nffgMap.get(nffgName);
		
		
		TraversalPolicyType rp = new TraversalPolicyType();
		rp.setDestination(policy.getDestination());
		rp.setName(policy.getName());
		rp.setSource(policy.getSource());
		rp.setIsPositive(policy.isIsPositive());
		rp.setVerification(policy.getVerification());
		
		nffgInfo.getNffg().getPolicies().getTraversalPolicy().add(rp);
		
		//Updating the nffgDB
		nffgMap.put(nffgName, nffgInfo);
		
	}

	
	public static void updateReachabilityPolicy(ReachabilityPolicyType2 policy, String nffgName){
		// Get the old nffgInfo
		NffgInfo nffgInfo = nffgMap.get(nffgName);
		// Create the new nffgInfo
		NffgInfo nffgInfo2 = new NffgInfo(nffgInfo.getName(), nffgInfo.getId(), nffgInfo.getNffg(), nffgInfo.getNodesMap(), nffgInfo.getLinksMap(), nffgInfo.getBelongsMap());

		ReachabilityPolicyType rp = nffgInfo.getReachabilityPolicyFromNffg(policy.getName());
		rp.setDestination(policy.getDestination());
		rp.setName(policy.getName());
		rp.setSource(policy.getSource());
		rp.setIsPositive(policy.isIsPositive());
		rp.setVerification(policy.getVerification());

		nffgInfo.getNffg().getPolicies().getReachabilityPolicy().remove(policy.getName());
		//nffgInfo2.getNffg().getPolicies().getReachabilityPolicy().add(tp);

		//Updating the nffgDB
		nffgMap.put(nffgName, nffgInfo2);

	}

	public static void updateTraversalPolicy(TraversalPolicyType2 policy, String nffgName){
		// Get the old nffgInfo
		NffgInfo nffgInfo = nffgMap.get(nffgName);
		// Create the new nffgInfo
		NffgInfo nffgInfo2 = new NffgInfo(nffgInfo.getName(), nffgInfo.getId(), nffgInfo.getNffg(), nffgInfo.getNodesMap(), nffgInfo.getLinksMap(), nffgInfo.getBelongsMap());

		TraversalPolicyType tp = nffgInfo.getTraversalPolicyFromNffg(policy.getName());
		tp.setDestination(policy.getDestination());
		tp.setName(policy.getName());
		tp.setSource(policy.getSource());
		tp.setDevices(policy.getDevices());
		tp.setIsPositive(policy.isIsPositive());
		tp.setVerification(policy.getVerification());

		nffgInfo.getNffg().getPolicies().getTraversalPolicy().remove(policy.getName());
		//nffgInfo2.getNffg().getPolicies().getTraversalPolicy().add(tp);

		//Updating the nffgDB
		nffgMap.put(nffgName, nffgInfo2);
	}

}

