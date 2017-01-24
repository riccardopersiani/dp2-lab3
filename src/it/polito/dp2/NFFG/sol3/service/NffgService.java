package it.polito.dp2.NFFG.sol3.service;

import java.util.ArrayList;
import java.util.Calendar;
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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.NFFG.sol3.service.database.NffgInfo;
import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;
import it.polito.dp2.NFFG.sol3.service.database.PoliciesDB;
import it.polito.dp2.NFFG.sol3.service.database.PolicyInfo;
import it.polito.dp2.NFFG.sol3.service.jaxb.LinkType;
import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;
import it.polito.dp2.NFFG.sol3.service.jaxb.Nffgs;
import it.polito.dp2.NFFG.sol3.service.jaxb.NodeType;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesToBeVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.Policy;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.ServiceType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;
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
	public void addNewPolicy(Policy policy_to_add) {
		System.out.println("Adding new policy...");

		if(policy_to_add.getTraversalPolicy() == null){

			if(policy_to_add.getReachabilityPolicy()== null){
				System.out.println("Policy is void...");
				//TODO
			}

			System.out.println("Reachability policy ready to be added...");
			ReachabilityPolicyType2 reachability_policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType2();
			reachability_policy = policy_to_add.getReachabilityPolicy();
			PolicyInfo policyInfo = new PolicyInfo(reachability_policy.getName(), reachability_policy.getNffg(), reachability_policy.getSource(), reachability_policy.getDestination(), reachability_policy.isIsPositive());
			PoliciesDB.addNewPolicy(reachability_policy.getName(), policyInfo);

			// Save the new policy inside the nffg
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(reachability_policy.getNffg());
			ReachabilityPolicyType reachability_policy_nffg = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType();
			reachability_policy_nffg.setDestination(reachability_policy.getDestination());
			reachability_policy_nffg.setName(reachability_policy.getName());
			reachability_policy_nffg.setSource(reachability_policy.getSource());
			reachability_policy_nffg.setIsPositive(reachability_policy.isIsPositive());

			if(reachability_policy.getVerification() != null){
				reachability_policy_nffg.setVerification(reachability_policy.getVerification());
			}
			nffgInfo.getNffg().getPolicies().getReachabilityPolicy().add(reachability_policy_nffg);

			/*if(reachability_policy.getVerification() != null){
				//TODO da correggere
				reachability_policy.getVerification().setTime(updateTime());
			}*/

			policyInfo.setVerification(reachability_policy.getVerification());
			policyInfo.printInfos();
		}
		else{
			System.out.println("Traversal policy ready to be added...");
			TraversalPolicyType2 traversal_policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();
			traversal_policy = policy_to_add.getTraversalPolicy();
			PolicyInfo policyInfo = new PolicyInfo(traversal_policy.getName(), traversal_policy.getNffg(), traversal_policy.getSource(), traversal_policy.getDestination(), traversal_policy.isIsPositive(),traversal_policy.getDevices());
			PoliciesDB.addNewPolicy(traversal_policy.getName(), policyInfo);

			// Save the new policy inside the nffg

			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(traversal_policy.getNffg());
			TraversalPolicyType traversal_policy_nffg = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType();
			traversal_policy_nffg.setDestination(traversal_policy.getDestination());
			traversal_policy_nffg.setName(traversal_policy.getName());
			traversal_policy_nffg.setSource(traversal_policy.getSource());
			traversal_policy_nffg.setIsPositive(traversal_policy.isIsPositive());

			if(traversal_policy.getVerification() != null){
				traversal_policy_nffg.setVerification(traversal_policy.getVerification());
			}
			nffgInfo.getNffg().getPolicies().getTraversalPolicy().add(traversal_policy_nffg);
			/*
			if(traversal_policy.getVerification() != null)
				traversal_policy.getVerification().setTime(updateTime());
			 */
			policyInfo.setVerification(traversal_policy.getVerification());
			policyInfo.printInfos();
		}
	}

	private WebTarget createTarget(){
		WebTarget target;
		String property_value = System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");

		Client c = ClientBuilder.newClient();

		// If the property is not set, the default value
		if(property_value == null)
			property_value = "http://localhost:8080/Neo4JXML/rest";
		// If the property is set
		target = c.target(property_value);

		return target;
	}


	public void LoadOneNffgOnNeo4J(NFFG nffg) throws Exception{
		System.out.println("Inside LOadOneNffgOnNeo4J(NFFG nffg)...");
		Map<String, String> nodesMap = new HashMap<String, String>(); 
		Map<String, String> linksMap = new HashMap<String, String>(); 
		Map<String, String> belongsMap = new HashMap<String, String>(); 

		List<NodeType> nodes = new ArrayList<NodeType>();
		List<LinkType> links = new ArrayList<LinkType>();

		if(NffgsDB.getNffgMap().containsKey(nffg.getName())){
			System.out.println("**** ALERT **** Nffg gia' presente...");
			throw new Exception("Nffg already stored");
		}

		try{			
			System.out.println("Creating Web Target...");
			WebTarget target = createTarget();

			/** Send the Nffg node to Neo4J **/
			Node nffg_node = new ObjectFactory().createNode();
			Property nffg_property = new ObjectFactory().createProperty();

			nffg_property.setName("name");
			nffg_property.setValue(nffg.getName());
			nffg_node.getProperty().add(nffg_property);

			//System.out.println("Adding the nffg node...");

			//System.out.println("NffgNode POST...");
			Node response1 = target.path("resource")
					.path("node")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(nffg_node, MediaType.APPLICATION_XML),Node.class);

			// Save every node in my node map
			nodesMap.put(nffg.getName(),response1.getId());

			//System.out.println("Adding all nodes and all belongs of the nffg "+response1.getId()+"...");

		}catch (Exception e){
			throw e;
		}
		try{
			System.out.println("Adding the nffg labels to the nffg "+nodesMap.get(nffg.getName())+"...");

			Labels nffg_label = new ObjectFactory().createLabels();

			nffg_label.getValue().add("NFFG");

			//System.out.println("NffgLabel POST...");
			// Create new label in Neo4J
			WebTarget target = createTarget();
			target.path("resource")
			.path("node")
			.path(nodesMap.get(nffg.getName()))
			.path("label")
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(nffg_label, MediaType.APPLICATION_XML));

			//System.out.println("NffgLabel POST DONE!!");

		}catch(Exception e){
			throw e;
		}
		try{
			/** Take the list of all nodes and links from the nffg **/
			nodes = nffg.getNodes().getNode();
			links = nffg.getLinks().getLink();

			/** Send every node and every belongs to Neo4J **/
			for(NodeType n : nodes){

				//System.out.println("Sending single node storage request, node: "+n.getName()+"...");

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

				Labels node_label = new ObjectFactory().createLabels();		

				ServiceType serviceType = n.getService();
				if(n.getService() == null){
					System.out.println("SERVICE E' NULLLLLLLLLLLLL.. ");
				}

				//String value = n.getService().value();
				//System.out.println("********BEFORE ADD VALUE SERCICAKLç DAS******: "+n.getService().value().toString());

				node_label.getValue().add(n.getService().value().toString());

				//System.out.println("Adding the node labels to the node BEFORE POST... ");
				// Create new label in Neo4J
				WebTarget target3 = createTarget();
				target3.path("resource")
				.path("node")
				.path(nodesMap.get(n.getName()))
				.path("label")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(node_label, MediaType.APPLICATION_XML));
				//System.out.println("Adding the node labels to the node AFTER POST... ");


				//System.out.println("Sending single belong storage request, node "+nodesMap.get(n.getName())+"...");

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

				//System.out.println("Storing data in maps...");

				// Save every belongs in my belongs map
				belongsMap.put(n.getName(),response3.getId());
			}

			//System.out.println("Adding all links of the nffg...");

			/** Send every link to Neo4J **/
			for(LinkType l: links){

				//System.out.println("Sending single link storage request, link: "+l.getName()+"...");

				Relationship relationship = new ObjectFactory().createRelationship();

				// Set new relationship parameters
				relationship.setDstNode(nodesMap.get(l.getDestination()));
				relationship.setType("Link");

				//System.out.println("Sending single link storage request BEFORE POST");
				// Create new relationship in Neo4J
				WebTarget target = createTarget();
				Relationship response4 = target.path("resource")
						.path("node")
						.path(nodesMap.get(l.getSource()))
						.path("relationship")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(relationship, MediaType.APPLICATION_XML), Relationship.class);	

				//System.out.println("Sending single link storage request AFTER POST");
				// Save every link in my link map
				linksMap.put(l.getName(), response4.getId());
			}				

			// Create the NffgInfo Object
			NffgInfo nffgInfo = new NffgInfo(nffg.getName(),nodesMap.get(nffg.getName()),nffg,nodesMap, linksMap, belongsMap);
			// Update the attribute last_update_time 
			nffgInfo.getNffg().setLastUpdateTime(updateTime());
			// Add the nffg to the nffg database
			NffgsDB.addNewNffg(nffg.getName(), nffgInfo);	

			//System.out.println("Adding to DB nffg: "+nffg.getName()+" and its NffginfoID: "+nffgInfo.getId());

			//NffgsDB.printDB();

			//System.out.println("NFFG added to the map, the actual map size is: " + NffgsDB.getNffgMap().size());

			if(nffg.getPolicies() == null){
				System.out.println("There are no policies");
			}
			else{
				System.out.println("There are:" + nffg.getPolicies().getReachabilityPolicy().size()+ " reach policies");
				System.out.println("There are:" + nffg.getPolicies().getTraversalPolicy().size()+ " trav policies");
				/** Store locally the reachability policies **/
				for(ReachabilityPolicyType rp : nffg.getPolicies().getReachabilityPolicy()){
					PolicyInfo policyInfo = new PolicyInfo(rp.getName(),
							nffg.getName(),
							rp.getSource(),
							rp.getDestination(),
							rp.isIsPositive());
					PoliciesDB.addNewPolicy(rp.getName(), policyInfo);
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
							tp.getDevices());
					PoliciesDB.addNewPolicy(tp.getName(), policyInfo);
					if(tp.getVerification() != null)
						policyInfo.setVerification(tp.getVerification());
					policyInfo.printInfos();
				}
			}

		}catch(Exception e){
			throw e;
		}
	}




	public void updatePolicy(Policy policy_to_update) {
		ReachabilityPolicyType2 policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();

		// If it is a reachability policy
		if(policy_to_update.getReachabilityPolicy() != null){
			System.out.println("Reachability policy ready to be udated...");
			policy = policy_to_update.getReachabilityPolicy();
			NffgsDB.getNffgMap().get(policy.getNffg()).deleteReachabilityPolicyFromNffg(policy.getName());
			// If it is a traversal policy
		} else{
			System.out.println("Traversal policy ready to be udated...");
			policy = policy_to_update.getTraversalPolicy();
			NffgsDB.getNffgMap().get(policy.getNffg()).deleteTraversalPolicyFromNffg(policy.getName());

		}
		PoliciesDB.deletePolicy(policy.getName());
		addNewPolicy(policy_to_update);
	}

	public void deleteOnePolicy(String policyName){
		boolean reachability = false;
		System.out.println("Delete function called...");

		PolicyInfo policyInfo = PoliciesDB.getPolicy(policyName);
		System.out.println("Delete function in nffg: ..."+policyInfo.getNffg());

		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(policyInfo.getNffg());

		for(int i =0; i<nffgInfo.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
			if(nffgInfo.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policyName)){
				reachability = true;
			}
		}

		if(reachability == true){
			System.out.println("Delete REACHABILITY...");
			NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteReachabilityPolicyFromNffg(policyName);
		}
		else{
			System.out.println("Delete TRAVERSAL...");
			NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteTraversalPolicyFromNffg(policyName);
		}
		PoliciesDB.deletePolicy(policyName);
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

	public NFFG getOneNffg(String nffgName) {
		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(nffgName);
		return nffgInfo.getNffg();
	}

	/*
	public void deleteOneNffg(String nffg_name) {

		System.out.println("Delete Nffg: "+nffg_name);

		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(nffg_name);
		nffgInfo.printInfos();
		String nffg_id = nffgInfo.getId();

		Set<String> link_list = new HashSet<String>();
		link_list  = nffgInfo.getLinksMap().keySet();
		Iterator<String> link_iter = link_list.iterator();

		// Remove the nffg links from Neo4J 
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

		// Remove the nffg belongs from Neo4J 
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

		// Remove the nffg nodes from Neo4J
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

		// Remove the policies of the nffg from the local cache 
		PoliciesDB.deleteNffgPolicies(nffg_name);
		// Remove the nffg from the local cache
		NffgsDB.getNffgMap().remove(nffg_name);
	}
	 */
	/*
	public void deleteAllNffgs(){	
		NffgsDB.printDB();

		Set<String> nffg_list = new HashSet<String>();
		nffg_list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> nffg_iter = nffg_list.iterator();

		// Remove all the nffg nodes from Neo4J
		while(nffg_iter.hasNext()) {
			String nffg_name = nffg_iter.next();
			this.deleteOneNffg(nffg_name);
			System.out.println("Deleted "+nffg_name+"...");
		}
	}
	 */

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

	public PoliciesVerified verifyPolicies(PoliciesToBeVerified policies) throws Exception {	
		PolicyInfo policyInfo;
		PoliciesVerified policies_to_be_returned = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createPoliciesVerified();
		System.out.println("verifyPolicies method called");
		List<String> policies_to_verify = policies.getName();
		System.out.println("scrolling the policies to verify, size: "+policies_to_verify.size());
		for(int i=0; i<policies_to_verify.size(); i++){
			System.out.println("inside for, policy: "+policies_to_verify.get(i));

			if(PoliciesDB.getPolicy(policies_to_verify.get(i)) != null){
				System.out.println("inside if");
				policyInfo = sendPolicyVerification(policies_to_verify.get(i));

				if(policyInfo.getDevices() == null){
					System.out.println("***REACHABILITY POLICY***");
					ReachabilityPolicyType2 rp = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType2();
					rp = policyInfo.getReachabilityPolicy();
					policies_to_be_returned.getReachabilityPolicy().add(rp);
				}
				else{
					System.out.println("***TRAVERSAL POLICY***");
					TraversalPolicyType2 rp = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();
					rp = policyInfo.getTraversalPolicy();
					policies_to_be_returned.getTraversalPolicy().add(rp);
				}
			}
			else{
				System.out.println("inside if");
				throw new Exception("Policy not stored in DB");
			}
		}
		return policies_to_be_returned;
	}

	public PolicyInfo sendPolicyVerification(String policy_name){
		try{
			System.out.println("----------------call sendPolicyVerification()");

			WebTarget target = createTarget();

			/** Take the list of all policies **/	
			PolicyInfo policyInfo = PoliciesDB.getPolicy(policy_name);
			System.out.println("Policy to be verified: "+policyInfo.getName());

			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(policyInfo.getNffg());
			String sourceNodeID = nffgInfo.getNodesMap().get(policyInfo.getSource());
			String destiantionNodeID =nffgInfo.getNodesMap().get(policyInfo.getDestination());

			/** Perform the GET to Neo4J in order to obtains the paths list**/
			Paths response3 = (Paths) target.path("resource")
					.path("node")
					.path(sourceNodeID)
					.path("paths")
					.queryParam("dst", destiantionNodeID)
					.request()
					.get(Paths.class);

			System.out.println("send request to neo4j");

			List<Path> pathList = response3.getPath();

			System.out.println("getting the path list neo4j with size" + pathList.size());

			/** If there at least one path the two nodes are reachable **/
			if(pathList.isEmpty() == false){
				System.out.println("List<Path> is not empty, policy is verified");

				if(policyInfo.getVerification() == null){
					System.out.println("Adding verification type");
					VerificationType verification = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createVerificationType();
					policyInfo.setVerification(verification);
					//Setting the new verification in the nffgDB
					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());
					//reachability
					System.out.println("BEFORE THE DB IF");

					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){
						System.out.println("IF REACHABILITY");

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
							System.out.println("FOR REACHABILITY scorro la policy: "+nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName());

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								System.out.println("POLICY FOUND IN LIST");
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).setVerification(verification);
								System.out.println("After Set VERIFICATION");
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setTime(updateTime());
								System.out.println("After Set Time");

								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Positive");
									System.out.println("After Set Message");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(true);
									System.out.println("After Set Result");

								}
								else{
									System.out.println("Policy negative DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}else{//traversal
						System.out.println("ELSE TRAVERSAL");
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){
							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).setVerification(verification);
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Positive");
								}else{
									System.out.println("Policy negative DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}
				}else{

					//Verification is not null 

					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());
					//reachability
					System.out.println("BEFORE THE DB IF");

					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){
						System.out.println("IF REACHABILITY");

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
							System.out.println("FOR REACHABILITY scorro la policy: "+nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName());

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								System.out.println("POLICY FOUND IN LIST");
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setTime(updateTime());
								System.out.println("After Set Time");

								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Positive");
									System.out.println("After Set Message");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(true);
									System.out.println("After Set Result");

								}
								else{
									System.out.println("Policy negative DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}else{//traversal
						System.out.println("ELSE TRAVERSAL");
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){
							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Positive");
								}else{
									System.out.println("Policy negative DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}

					policyInfo.getVerification().setTime(updateTime());			    

					if(policyInfo.getIsPositive() == true){
						policyInfo.getVerification().setResult(true);
						policyInfo.getVerification().setMessage("Ok");
						System.out.println("Policy positive");
					}
					else{
						policyInfo.getVerification().setResult(false);
						policyInfo.getVerification().setMessage("policy negative Ok");
						System.out.println("Policy negative");
					}
				}
				System.out.println("Ritorno policy info VERIFICA");
				return policyInfo;

			}
			else{
				System.out.println("List<Path> is empty, reachability policy is not verified");

				if(policyInfo.getVerification() == null){
					System.out.println("Adding verification type");
					VerificationType verification = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createVerificationType();
					policyInfo.setVerification(verification);

					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());
					//reachability
					System.out.println("BEFORE THE DB IF");
					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){
						System.out.println("IF REACHABILITY");

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
							System.out.println("FOR REACHABILITY scorro la policy: "+nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName());

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								System.out.println("POLICY FOUND IN LIST");
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).setVerification(verification);
								System.out.println("After Set VERIFICATION");
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setTime(updateTime());
								System.out.println("After Set Time");

								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Positive");
									System.out.println("After Set Message");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(false);
									System.out.println("After Set Result");

								}
								else{
									System.out.println("Policy negative DBBBB");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}else{//traversal
						System.out.println("ELSE TRAVERSAL");
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){
							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).setVerification(verification);
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									System.out.println("Policy positive DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Positive");
								}else{
									System.out.println("Policy negative DB TRAVERSAL");
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Negative");					
								}
							}
						}
					}
				}else{

					policyInfo.getVerification().setTime(updateTime());			    
					System.out.println("Policy not verified");
					if(policyInfo.getIsPositive() == true){
						policyInfo.getVerification().setResult(false);
						policyInfo.getVerification().setMessage("Policy positive but not reachable");
						System.out.println("Policy positive");
					}
					else{
						policyInfo.getVerification().setResult(false);
						policyInfo.getVerification().setMessage("Policy negative but Reachable");
						System.out.println("Policy negative");
					}
				}
				return policyInfo;

			}

		}catch(Exception e){
			System.out.println("EXCEPRION CATCHED!! SENDPOLICYVERIFICATION");
			throw e;
		}
	}

	public XMLGregorianCalendar calendarToXMLGregorianCalendar(Calendar calendar) {
		try {
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar();
			xgc.setYear(calendar.get(Calendar.YEAR));
			xgc.setMonth(calendar.get(Calendar.MONTH) + 1);
			xgc.setDay(calendar.get(Calendar.DAY_OF_MONTH));
			xgc.setHour(calendar.get(Calendar.HOUR_OF_DAY));
			xgc.setMinute(calendar.get(Calendar.MINUTE));
			xgc.setSecond(calendar.get(Calendar.SECOND));
			xgc.setMillisecond(calendar.get(Calendar.MILLISECOND));
			// Calendar ZONE_OFFSET and DST_OFFSET fields are in milliseconds.
			int offsetInMinutes = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
			xgc.setTimezone(offsetInMinutes);
			return xgc;
		} catch (DatatypeConfigurationException e) {
			System.out.print(e.getMessage());
			return null;
		}
	}

	public XMLGregorianCalendar updateTime(){
		XMLGregorianCalendar xmlGregorianCalendar = calendarToXMLGregorianCalendar(Calendar.getInstance());
		return xmlGregorianCalendar;
	}

	public Policy getPolicy(String policyID) throws Exception {
		PolicyInfo policyInfo = PoliciesDB.getPolicy(policyID);
		if(policyInfo == null){
			System.out.println("Policy does not exist");
			throw new Exception("Policy does not exist");
		}
		Policy policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createPolicy();
		if(policyInfo.getDevices() == null){
			policy.setReachabilityPolicy(policyInfo.getReachabilityPolicy());
		}
		else{
			policy.setTraversalPolicy(policyInfo.getTraversalPolicy());
		}
		return policy;
	}

}
