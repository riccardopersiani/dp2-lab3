package it.polito.dp2.NFFG.sol3.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.NFFG.sol3.service.database.NffgInfo;
import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;
import it.polito.dp2.NFFG.sol3.service.database.PoliciesDB;
import it.polito.dp2.NFFG.sol3.service.database.PolicyInfo;
import it.polito.dp2.NFFG.sol3.service.jaxb.LinkType;
import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;
import it.polito.dp2.NFFG.sol3.service.jaxb.NodeType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.neo4j.Labels;
import it.polito.dp2.NFFG.sol3.service.neo4j.Node;
import it.polito.dp2.NFFG.sol3.service.neo4j.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.neo4j.Path;
import it.polito.dp2.NFFG.sol3.service.neo4j.Paths;
import it.polito.dp2.NFFG.sol3.service.neo4j.Property;
import it.polito.dp2.NFFG.sol3.service.neo4j.Relationship;

public class NffgService {
	private WebTarget target;
	private Map<String, String> nodesMap = new HashMap<String, String>(); 
	private Map<String, String> linksMap = new HashMap<String, String>(); 
	//private  List<TraversalPolicyType> policies = Policies.getAllPolicies(); 

	public void addNewNffg(NFFG nffg) {
		if(nffg == null)
			return;
		//nffgs.add(nffg);		
	}

	// Store the new policy in the DB
	public void addNewPolicy(ReachabilityPolicyType2 policy) {
		if(policy == null)
			return;
		PolicyInfo policyInfo = new PolicyInfo(policy.getName(), policy.getNffg(), policy.getSource(), policy.getDestination(), policy.isIsPositive(), policy.getVerification());
		PoliciesDB.addNewPolicy(policy.getNffg(), policyInfo);
	}


	private WebTarget createTarget(){	
		Client c = ClientBuilder.newClient();	
		
		// If the property is not set, the default value
		if(System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL") == null)
			System.getProperty("http://localhost:8080/Neo4JXML/rest");
		// If the property is set
		target = c.target(System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL"));
		
		return target;
	}
	
	public void LoadAllNffgsOnNeo4J(List<NFFG> nffgs){
		for(NFFG nffg : nffgs){
			LoadOneNffgOnNeo4J(nffg);
		}
	}

	public void LoadOneNffgOnNeo4J(NFFG nffg){
		List<NodeType> nodes = new ArrayList<NodeType>();
		List<LinkType> links = new ArrayList<LinkType>();

		try{			
			
			target = createTarget();

			/** Send the Nffg node to Neo4J **/
			Node nffg_node = new ObjectFactory().createNode();
			Property nffg_property = new ObjectFactory().createProperty();
			Labels nffg_label = new ObjectFactory().createLabels();

			nffg_property.setName("name");
			nffg_property.setValue(nffg.getName());
			nffg_label.getValue().add("NFFG");
			nffg_node.getProperty().add(nffg_property);

			Node response1 = target.path("resource")
					.path("node")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(nffg_node, MediaType.APPLICATION_XML),Node.class);

			/** Take the list of all nodes and links from the nffg **/
			nodes = nffg.getNodes().getNode();
			links = nffg.getLinks().getLink();


			/** Send every node to Neo4J **/
			for(NodeType n : nodes){
				Node node = new ObjectFactory().createNode();
				Property property = new ObjectFactory().createProperty();

				property.setName("name");
				property.setValue(n.getName());
				node.getProperty().add(property);

				Node response2 = target.path("resource")
						.path("node")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(node, MediaType.APPLICATION_XML),Node.class);
				
				// Save every node in my node map
				nodesMap.put(n.getName(),response2.getId());
			}


			/** Send every link to Neo4J **/
			for(NodeType n : nodes){
				for(LinkType l: links){
					Relationship relationship = new ObjectFactory().createRelationship();
					
					// Set new relationship parameters
					relationship.setDstNode(l.getDestination());
					relationship.setType("Link");
					
					// Create new relationship in Neo4J
					Relationship response3 = target.path("resource")
							.path("node")
							.path(n.getName())
							.path("relationship")
							.request(MediaType.APPLICATION_XML)
							.post(Entity.entity(relationship, MediaType.APPLICATION_XML), Relationship.class);	
					
					// Save every link in my link map
					linksMap.put(l.getName(), response3.getId());
				}				
			}
			
			// Create the NffgInfo Object
			NffgInfo nffgInfo = new NffgInfo(nffg.getName(),response1.getId(),nffg,nodesMap, linksMap);
			// Add the nffg to the nffg database
			NffgsDB.addNewNffg(nffg.getName(), nffgInfo);		
			
			/** Store locally the policies **/
			for(ReachabilityPolicyType rp : nffg.getPolicies().getReachabilityPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(rp.getName(),
													   nffg.getName(),
													   rp.getSource(),
													   rp.getDestination(),
												   	   rp.isIsPositive(),
													   rp.getVerification());
				PoliciesDB.addNewPolicy(nffg.getName(), policyInfo);
			}
			
		}catch(Exception e){
			e.getMessage();
		}
	}
	
	public void sendPolicyVerification(String policy_name, String nffg_name){
		try{
			
			target = createTarget();
			
			/** Take the list of all policies **/	
			PolicyInfo policyInfo = PoliciesDB.getPolicy(policy_name, nffg_name);
			
			/** Perform the GET to Neo4J in order to obtains the paths list**/
			Paths response3 = (Paths) target.path("resource")
					.path("node")
					.path(policyInfo.getSource())
					.path("paths")
					.queryParam("dst", policyInfo.getDestination())
					.request()
					.get(Paths.class);
			
			List<Path> pathList = response3.getPath();
			
			/** If there at least one path the two nodes are reachable **/
			if(!pathList.isEmpty()){
				System.out.println("List<Path> is not empty, reachability policy is verified");
				
				//TODO policyInfo.getVerification().setTime();			    
			    
			    if(policyInfo.getIsPositive() == true)
					policyInfo.getVerification().setResult(true);
			
				policyInfo.getVerification().setResult(false);
			}
			
		}catch(Exception e){
			e.getMessage();
		}
	}

	public void updatePolicy(ReachabilityPolicyType2 policy) {
		PoliciesDB.deletePolicy(policy.getName(), policy.getNffg());
		PolicyInfo policyInfo = new PolicyInfo(policy.getName(),
											   policy.getName(),
										   	   policy.getSource(),
											   policy.getDestination(),
											   policy.isIsPositive(),
											   policy.getVerification());
		PoliciesDB.addNewPolicy(policy.getNffg(), policyInfo);
	}
	
	public void removeAllPolicies(){
		
	}

	public List<NFFG> getAllNffgs() {
		List<NFFG> nffgList = new ArrayList<NFFG>();
		Set<String> list = new HashSet<String>();
		list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> iter = list.iterator();
		
		/** Send the list of policies to be verified **/
		while(iter.hasNext()) {
		     String key = iter.next();
		     NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
		     nffgList.add(nffgInfo.getNffg());
		}
		return nffgList;
	}

}
