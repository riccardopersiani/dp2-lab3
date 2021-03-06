package it.polito.dp2.NFFG.sol3.service.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;



public class NffgInfo {
	private String name;
	private String id;
	private Map<String,String> nodes = new ConcurrentHashMap<String,String>();
	private Map<String,String> links = new ConcurrentHashMap<String,String>();
	private Map<String,String> belongs = new ConcurrentHashMap<String,String>();
	private NFFG nffg;


	public NffgInfo(String name, String id, NFFG nffg, Map<String,String> nodes, Map<String,String> links,Map<String,String> belongs){
		this.name = name;
		this.id = id;
		this.nffg = nffg;
		this.nodes = nodes;
		this.links = links;
		this.belongs = belongs;
	}

	public String getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public NFFG getNffg(){
		return nffg;
	}

	public Map<String,String> getNodesMap(){
		return nodes;
	}

	public Map<String,String> getLinksMap(){
		return links;
	}

	public Map<String,String> getBelongsMap(){
		return belongs;
	}

	public ReachabilityPolicyType getReachabilityPolicyFromNffg(String policyName){
		for(int i=0; i<nffg.getPolicies().getReachabilityPolicy().size(); i++){
			if(nffg.getPolicies().getReachabilityPolicy().get(i).getName().equals(policyName)){
				return nffg.getPolicies().getReachabilityPolicy().get(i);
			}
		}
		return null;
	}
	
	public TraversalPolicyType getTraversalPolicyFromNffg(String policyName){
		for(int i=0; i<nffg.getPolicies().getTraversalPolicy().size(); i++){
			if(nffg.getPolicies().getTraversalPolicy().get(i).getName().equals(policyName)){
				return nffg.getPolicies().getTraversalPolicy().get(i);
			}
		}
		return null;
	}
	
	public void deleteReachabilityPolicyFromNffg(String policyName){
		for(int i=0; i<nffg.getPolicies().getReachabilityPolicy().size(); i++){
			if(nffg.getPolicies().getReachabilityPolicy().get(i).getName().equals(policyName)){
				nffg.getPolicies().getReachabilityPolicy().remove(i);
			}
		}
	}

	public void deleteTraversalPolicyFromNffg(String policyName){
		for(int i=0; i<nffg.getPolicies().getTraversalPolicy().size(); i++){
			if(nffg.getPolicies().getTraversalPolicy().get(i).getName().equals(policyName)){
				nffg.getPolicies().getTraversalPolicy().remove(i);
			}
		}
	}
	
	public boolean isReachabilityPolicy(String policyName){
		for(int i=0; i<nffg.getPolicies().getReachabilityPolicy().size();i++){
			if(nffg.getPolicies().getReachabilityPolicy().get(i).getName().equals(policyName)){
				return true;
			}
		}	
		return false;
	}
	
	public boolean isTraversalPolicy(String policyName){
		for(int i=0; i<nffg.getPolicies().getTraversalPolicy().size();i++){
			if(nffg.getPolicies().getTraversalPolicy().get(i).getName().equals(policyName)){
				return true;
			}
		}	
		return false;
	}

	public void printInfos(){
		System.out.println("Printing NffgInfo...");
		System.out.println("Name: "+this.name+", ID: "+this.id);

		Set<String> list = new HashSet<String>();
		list  = this.nodes.keySet();
		Iterator<String> iter = list.iterator();

		while(iter.hasNext()) {
			String key = iter.next();
			System.out.println("Node:"+this.nodes.get(key));
		}

		Set<String> list2 = new HashSet<String>();
		list2  = this.links.keySet();
		Iterator<String> iter2 = list2.iterator();

		while(iter2.hasNext()) {
			String key2 = iter2.next();
			System.out.println("Link:"+this.links.get(key2));
		}
	}

}
