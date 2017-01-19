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
import it.polito.dp2.NFFG.sol3.service.jaxb.Nffgs;
import it.polito.dp2.NFFG.sol3.service.jaxb.NodeType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.service.neo4j.Labels;
import it.polito.dp2.NFFG.sol3.service.neo4j.Node;
import it.polito.dp2.NFFG.sol3.service.neo4j.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.neo4j.Path;
import it.polito.dp2.NFFG.sol3.service.neo4j.Paths;
import it.polito.dp2.NFFG.sol3.service.neo4j.Property;
import it.polito.dp2.NFFG.sol3.service.neo4j.Relationship;

public class NffgService {

	//private  List<TraversalPolicyType> policies = Policies.getAllPolicies(); 

	// Store the new policy in the DB
	public void addNewPolicy(ReachabilityPolicyType2 policy) {
		if(policy == null)
			return;
		PolicyInfo policyInfo = new PolicyInfo(policy.getName(), policy.getNffg(), policy.getSource(), policy.getDestination(), policy.isIsPositive());
		PoliciesDB.addNewPolicy(policy.getNffg(), policyInfo);
		if(policy.getVerification() != null)
			policyInfo.setVerification(policy.getVerification());
	}

	private WebTarget createTarget(){
		WebTarget target;

		Client c = ClientBuilder.newClient();	

		// If the property is not set, the default value
		if(System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL") == null)
			System.getProperty("http://localhost:8080/Neo4JXML/rest");
		// If the property is set
		target = c.target(System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL"));

		return target;
	}


	public void LoadOneNffgOnNeo4J(NFFG nffg){
		Map<String, String> nodesMap;
		Map<String, String> linksMap;
		Map<String, String> belongsMap;
		List<NodeType> nodes = new ArrayList<NodeType>();
		List<LinkType> links = new ArrayList<LinkType>();

		nodesMap = new HashMap<String, String>(); 
		linksMap = new HashMap<String, String>(); 
		belongsMap = new HashMap<String, String>(); 

		try{					
			WebTarget target = createTarget();

			/** Send the Nffg node to Neo4J **/
			Node nffg_node = new ObjectFactory().createNode();
			Property nffg_property = new ObjectFactory().createProperty();

			nffg_property.setName("name");
			nffg_property.setValue(nffg.getName());
			nffg_node.getProperty().add(nffg_property);

			System.out.println("Adding the nffg node...");

			Node response1 = target.path("resource")
					.path("node")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(nffg_node, MediaType.APPLICATION_XML),Node.class);

			// Save every node in my node map
			nodesMap.put(nffg.getName(),response1.getId());

			System.out.println("Adding all nodes and all belongs of the nffg "+response1.getId()+"...");

		}catch (Exception e){
			throw e;
		}
		try{
			System.out.println("Adding the nffg labels to the nffg "+nodesMap.get(nffg.getName())+"...");

			Labels nffg_label = new ObjectFactory().createLabels();

			nffg_label.getValue().add("NFFG");

			// Create new label in Neo4J
			WebTarget target = createTarget();
			target.path("resource")
			.path("node")
			.path(nodesMap.get(nffg.getName()))
			.path("label")
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(nffg_label, MediaType.APPLICATION_XML));

		}catch(Exception e){
			throw e;
		}
		try{

			/** Take the list of all nodes and links from the nffg **/
			nodes = nffg.getNodes().getNode();
			links = nffg.getLinks().getLink();

			/** Send every node and every belongs to Neo4J **/
			for(NodeType n : nodes){

				System.out.println("Sending single node storage request, node: "+n.getName()+"...");

				Node node = new ObjectFactory().createNode();
				Property property = new ObjectFactory().createProperty();

				property.setName("name");
				property.setValue(n.getName());
				node.getProperty().add(property);

				// Create new node in Neo4J
				WebTarget target = createTarget();
				Node response2 = target.path("resource")
						.path("node")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(node, MediaType.APPLICATION_XML),Node.class);

				// Save every node in my node map
				nodesMap.put(n.getName(),response2.getId());

				System.out.println("Adding the node labels to the node... ");

				Labels node_label = new ObjectFactory().createLabels();

				node_label.getValue().add(n.getService().name());

				// Create new label in Neo4J
				WebTarget target3 = createTarget();
				target3.path("resource")
				.path("node")
				.path(nodesMap.get(n.getName()))
				.path("label")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(node_label, MediaType.APPLICATION_XML));


				System.out.println("Sending single belong storage request, node "+nodesMap.get(n.getName())+"...");

				Relationship nffg_relationship = new ObjectFactory().createRelationship();

				// Set new relationship parameters
				nffg_relationship.setDstNode(nodesMap.get(n.getName()));
				nffg_relationship.setType("belongs");

				// Create new relationship in Neo4J
				WebTarget target2 = createTarget();
				Relationship response3 = target2.path("resource")
						.path("node")
						.path(nodesMap.get(nffg.getName()))
						.path("relationship")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(nffg_relationship, MediaType.APPLICATION_XML), Relationship.class);	

				System.out.println("Storing data in maps...");

				// Save every belongs in my belongs map
				belongsMap.put(n.getName(),response3.getId());
			}

			System.out.println("Adding all links of the nffg...");

			/** Send every link to Neo4J **/
			for(LinkType l: links){

				System.out.println("Sending single link storage request, link: "+l.getName()+"...");

				Relationship relationship = new ObjectFactory().createRelationship();

				// Set new relationship parameters
				relationship.setDstNode(nodesMap.get(l.getDestination()));
				relationship.setType("Link");

				// Create new relationship in Neo4J
				WebTarget target = createTarget();
				Relationship response4 = target.path("resource")
						.path("node")
						.path(nodesMap.get(l.getSource()))
						.path("relationship")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(relationship, MediaType.APPLICATION_XML), Relationship.class);	

				// Save every link in my link map
				linksMap.put(l.getName(), response4.getId());
			}				

			// Create the NffgInfo Object
			NffgInfo nffgInfo = new NffgInfo(nffg.getName(),nodesMap.get(nffg.getName()),nffg,nodesMap, linksMap, belongsMap);
			// Add the nffg to the nffg database
			NffgsDB.addNewNffg(nffg.getName(), nffgInfo);	

			System.out.println("Adding to DB nffg: "+nffg.getName()+" and its NffginfoID: "+nffgInfo.getId());

			NffgsDB.printDB();

			System.out.println("Element added to the map, the actual map size is: " + NffgsDB.getNffgMap().size());

			/** Store locally the reachability policies **/
			for(ReachabilityPolicyType rp : nffg.getPolicies().getReachabilityPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(rp.getName(),
						nffg.getName(),
						rp.getSource(),
						rp.getDestination(),
						rp.isIsPositive());
				PoliciesDB.addNewPolicy(nffg.getName(), policyInfo);
				if(rp.getVerification() != null)
					policyInfo.setVerification(rp.getVerification());
				policyInfo.printInfos();
			}
			
			/** Store locally the policies **/
			for(TraversalPolicyType tp : nffg.getPolicies().getTraversalPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(tp.getName(),
						nffg.getName(),
						tp.getSource(),
						tp.getDestination(),
						tp.isIsPositive(),
						tp.getDevices().getDevice());
				PoliciesDB.addNewPolicy(nffg.getName(), policyInfo);
				if(tp.getVerification() != null)
					policyInfo.setVerification(tp.getVerification());
				policyInfo.printInfos();
			}

		}catch(Exception e){
			throw e;
		}
	}

	public void sendPolicyVerification(String policy_name, String nffg_name){
		try{

			WebTarget target = createTarget();

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
			throw e;
		}
	}

	public void updatePolicy(ReachabilityPolicyType2 policy) {
		PoliciesDB.deletePolicy(policy.getName(), policy.getNffg());
		PolicyInfo policyInfo = new PolicyInfo(policy.getName(),
				policy.getName(),
				policy.getSource(),
				policy.getDestination(),
				policy.isIsPositive());
		PoliciesDB.addNewPolicy(policy.getNffg(), policyInfo);
		if(policy.getVerification() != null)
			policyInfo.setVerification(policy.getVerification());
		policyInfo.printInfos();
	}

	public void removeAllPolicies(){

	}


	public Nffgs getAllNffgs() {
		Nffgs nffgs = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createNffgs();

		Set<String> list = new HashSet<String>();
		list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> iter = list.iterator();

		System.out.println("Before the while, size: " + NffgsDB.getNffgMap().size());

		/** Send the list of all nffgs **/
		while(iter.hasNext()) {
			System.out.println("Inside the while");
			String key = iter.next();
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
			nffgs.getNFFG().add(nffgInfo.getNffg());
		}
		return nffgs;
	}

	public void deleteOneNffg(String nffg_name) {

		System.out.println("Delete Nffg: "+nffg_name);

		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(nffg_name);
		nffgInfo.printInfos();
		String nffg_id = nffgInfo.getId();

		Set<String> link_list = new HashSet<String>();
		link_list  = nffgInfo.getLinksMap().keySet();
		Iterator<String> link_iter = link_list.iterator();

		/** Remove the nffg links from Neo4J **/
		while(link_iter.hasNext()) {
			String link_key = link_iter.next();
			String linkID = nffgInfo.getLinksMap().get(link_key);
			try{

				System.out.println("Delete Link: "+link_key+" with LinkID:"+linkID);

				// Delete Links			
				WebTarget target = createTarget();
				target.path("resource")
				.path("relationship")
				.path(linkID)
				.request()
				.delete();
			}catch(Exception e){
				throw e;
			}
		}

		Set<String> belong_list = new HashSet<String>();
		belong_list  = nffgInfo.getBelongsMap().keySet();
		Iterator<String> belong_iter = belong_list.iterator();

		/** Remove the nffg belongs from Neo4J **/
		while(belong_iter.hasNext()) {
			String belong_key = belong_iter.next();
			String belongID = nffgInfo.getBelongsMap().get(belong_key);
			try{

				System.out.println("Delete Belong: "+belong_key+" with BelongID:"+belongID);

				// Delete Belongs			
				WebTarget target = createTarget();
				target.path("resource")
				.path("relationship")
				.path(belongID)
				.request()
				.delete();
			}catch(Exception e){
				throw e;
			}
		}

		Set<String> node_list = new HashSet<String>();
		node_list  = nffgInfo.getNodesMap().keySet();
		Iterator<String> node_iter = node_list.iterator();

		/** Remove the nffg nodes from Neo4J **/
		while(node_iter.hasNext()) {
			String node_key = node_iter.next();
			String nodeID = nffgInfo.getNodesMap().get(node_key);
			try{
				if(nodeID != nffg_id){
					System.out.println("Delete Node: "+node_key+" with NodeID: "+nodeID);
					// Delete Nodes	different from nffg node		
					WebTarget target = createTarget();
					target.path("resource")
					.path("node")
					.path(nodeID)
					.request(MediaType.APPLICATION_XML)
					.delete();
				}

			}catch(Exception e){
				throw e;
			}
		}

		try{
			System.out.println("Delete NffgNode: "+nffg_name+" with NffgNodeID: "+nffg_id);

			// Delete Nffg node
			WebTarget target = createTarget();
			target.path("resource")
			.path("node")
			.path(nffg_id)
			.request(MediaType.APPLICATION_XML)
			.delete();
		}catch(Exception e){
			throw e;
		}

		/** Remove the policies of the nffg from the local cache **/
		PoliciesDB.deleteNffgPolicies(nffg_name);
		/** Remove the nffg from the local cache **/
		NffgsDB.getNffgMap().remove(nffg_name);
	}

	public void deleteAllNffgs(){
		
		NffgsDB.printDB();
		
		Set<String> nffg_list = new HashSet<String>();
		nffg_list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> nffg_iter = nffg_list.iterator();

		/** Remove all the nffg nodes from Neo4J **/
		while(nffg_iter.hasNext()) {
			String nffg_name = nffg_iter.next();
			this.deleteOneNffg(nffg_name);
			System.out.println("Deleted "+nffg_name+"...");
		}
	}

	public void printNffgsMap(){
		Set<String> list = new HashSet<String>();
		list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> iter = list.iterator();

		System.out.println("Map Size: " + NffgsDB.getNffgMap().size());

		/** Send the list of all nffgs **/
		while(iter.hasNext()) {
			System.out.println("Inside the while");
			String key = iter.next();
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
			System.out.println("NffgName: "+NffgsDB.getNffgMap().get(key)+", NffgID: "+nffgInfo.getId());
		}
	}



}
