package it.polito.dp2.NFFG.sol3.service.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;

public class NffgInfo {
	private String name;
	private String id;
	private NFFG nffg;
	private static Map<String,String> nodes = new ConcurrentHashMap<String,String>();
	private static Map<String,String> links = new ConcurrentHashMap<String,String>();

	public NffgInfo(String name, String id, NFFG nffg, Map<String,String> nodes, Map<String,String> links){
		this.name = name;
		this.id = id;
		this.nffg = nffg;
		NffgInfo.nodes = nodes;
		NffgInfo.links = links;
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

}
