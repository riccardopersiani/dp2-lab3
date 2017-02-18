package it.polito.dp2.NFFG.sol3.client1;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.NFFG.FunctionalType;
import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.TraversalPolicyReader;
import it.polito.dp2.NFFG.VerificationResultReader;
import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.DevicesListType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.LinkType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.LinksType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.NFFG;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.NodeType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.NodesType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.ObjectFactory;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.Policies;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.PoliciesToBeVerified;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.PoliciesVerified;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.Policy;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.ServiceType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.TraversalPolicyType2;
import it.polito.dp2.NFFG.sol3.client1.nffgservice.VerificationType;

public class NFFGClient implements it.polito.dp2.NFFG.lab3.NFFGClient {

	//private Set<NodeReader> nodeSet; 
	private Set<NffgReader> nffgSet;

	private NffgVerifier monitor;
	private NFFG NFFG;

	public NFFGClient(){
		nffgSet = new HashSet<>();
	}

	@Override
	public void loadNFFG(String name)throws UnknownNameException, AlreadyLoadedException, ServiceException {
		it.polito.dp2.NFFG.NffgVerifierFactory factory = it.polito.dp2.NFFG.NffgVerifierFactory.newInstance();
		try {					
			monitor = factory.newNffgVerifier();

			createNffgStructure(name);

			// Send the post nffg request to the NffgService
			WebTarget target = Util.createClientTarget();		
			Response response = target.path("nffgs")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(NFFG, MediaType.APPLICATION_XML));

			if(response.getStatus() == 409){
				System.err.println("loadNFFG - response status 409: AlreadyLoadedException");
				throw new AlreadyLoadedException();
			}
			if(response.getStatus() == 404){	
				System.err.println("loadNFFG - response status 404: UnknownNameException");
				throw new UnknownNameException();
			}
			if(response.getStatus() >= 400){
				System.err.println("loadNFFG - response status >= 400: ServiceException! Neo4j could be not running");
				throw new ServiceException();
			}
			
		} catch (NffgVerifierException e) {
			System.err.println("loadNFFG - NffgVerifierException");
			throw new ServiceException();
		} catch (RuntimeException e){
			System.err.println("loadNFFG - RuntimeException");
			throw new ServiceException();
		}

	}

	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
		NffgVerifier monitor;
		it.polito.dp2.NFFG.NffgVerifierFactory factory = it.polito.dp2.NFFG.NffgVerifierFactory.newInstance();
		try {					
			monitor = factory.newNffgVerifier();			
			nffgSet = monitor.getNffgs();

			for(NffgReader nfr : nffgSet ){
				try {
					loadNFFG(nfr.getName());
				} catch (UnknownNameException e) {
					System.err.println("loadAll - ServiceException");
					throw new ServiceException();
				} catch (AlreadyLoadedException e) {
					System.err.println("loadAll - AlreadyLoadedException");
					throw new AlreadyLoadedException();
				}			
			}
		}catch(NffgVerifierException e){
			System.err.println("loadAll - NffgVerifierException");
			throw new ServiceException();
		} 
	}

	@Override
	public void loadReachabilityPolicy(String name, String nffgName, boolean isPositive, String srcNodeName,
			String dstNodeName) throws UnknownNameException, ServiceException {

		Policy policy = new ObjectFactory().createPolicy();

		ReachabilityPolicyType2 rp = new ObjectFactory().createReachabilityPolicyType2();
		rp.setName(name);
		rp.setNffg(nffgName);
		rp.setSource(srcNodeName);
		rp.setDestination(dstNodeName);
		rp.setIsPositive(isPositive);

		policy.setReachabilityPolicy(rp);	

		// Send the POST request to the NffgService in order to load the policy
		WebTarget target = Util.createClientTarget();		
		Response response =  target.path("policies")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(policy, MediaType.APPLICATION_XML));

		if(response.getStatus() == 404){	
			System.err.println("loadReachabilityPolicy response 404 - UnknownNameException");
			throw new UnknownNameException();
		}
		
		if(response.getStatus() == 409){
			WebTarget target2 = Util.createClientTarget();		
			Response response2 =  target2.path("policies")
					.request(MediaType.APPLICATION_XML)
					.put(Entity.entity(policy, MediaType.APPLICATION_XML));

			if(response2.getStatus() == 404){		
				System.err.println("loadReachabilityPolicy response2 404 - UnknownNameException");
				throw new UnknownNameException();
			}
			
			if(response2.getStatus() >= 400){	
				System.err.println("loadReachabilityPolicy response2 >= 400 - ServiceException");
				throw new ServiceException();
			}
		}
		
		if(response.getStatus() >= 400){	
			System.err.println("loadReachabilityPolicy response >= 400 - ServiceException");
			throw new ServiceException();
		}
	}

	@Override
	public void unloadReachabilityPolicy(String name) throws UnknownNameException, ServiceException {

		try{
			// Send the DELETE request to the NffgService in order to delete the policy
			WebTarget target = Util.createClientTarget();
			Response response = target.path("policies")
					.path(name)
					.request()
					.delete();
			
			if(response.getStatus() == 404){
				System.err.println("unloadReachabilityPolicy response 404 - UnknownNameException");
				throw new UnknownNameException();
			}
			
			if(response.getStatus() >= 400){
				System.err.println("unloadReachabilityPolicy response >= 400 - ServiceException");
				throw new ServiceException();
			}
			
		}catch(RuntimeException e){
			System.err.println("unloadReachabilityPolicy - RuntimeException");
			throw new ServiceException();
		}
		
	}

	@Override
	public boolean testReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		try{
			PoliciesToBeVerified policy_to_be_verified = new ObjectFactory().createPoliciesToBeVerified();
			policy_to_be_verified.getName().add(name);

			// Send the PUT request to the NffgService in order to test the reachability policy
			WebTarget target = Util.createClientTarget();		
			Response response1 = target.path("verification")
					.request(MediaType.APPLICATION_XML)
					.put(Entity.entity(policy_to_be_verified, MediaType.APPLICATION_XML),Response.class);
			
			// Check if the server reply is correct and if neo4j is running
			if(response1.getStatus() >= 400){
				System.err.println("testReachabilityPolicy - response1 status:" + response1.getStatus());
				throw new ServiceException();
			}
			
			PoliciesVerified response = response1.readEntity(PoliciesVerified.class);
			
			// Traversal Policy
			if(response.getTraversalPolicy().isEmpty() == false){
				TraversalPolicyType2 tp = new ObjectFactory().createTraversalPolicyType2();
				tp = response.getTraversalPolicy().get(0);
					if(tp.getVerification().isResult() == true){
						return true;
					} else{
						return false;
					}
			}
			// Reachability Policy
			else{
				if(response.getReachabilityPolicy().isEmpty() == true){
					throw new UnknownNameException();
				}
				ReachabilityPolicyType2 rp = response.getReachabilityPolicy().get(0);
					if(rp.getVerification().isResult() == true){
						return true;
					} else{
						return false;
					}				
			}
		}catch(RuntimeException e){
			System.err.println("testReachabilityPolicy - RuntimeException");
			throw new ServiceException();
		}
	}

	private void createPoliciesStructure(NFFG NFFG) {
		// Get the list of policies of the NFFG with name nffgName
		Set<PolicyReader> policySet = monitor.getPolicies(NFFG.getName());

		/*** Create Policies ***/
		Policies Policies = new ObjectFactory().createPolicies();

		// For each policy print related data
		for (PolicyReader pr: policySet) {
			try{
				/*** Create TraversalPolicy ***/
				TraversalPolicyType TraversalPolicy = new ObjectFactory().createTraversalPolicyType();			    
				// Set a single traversal policy
				TraversalPolicyReader policy = (TraversalPolicyReader) pr;
				// Get the list of traversed devices in the single policy
				Set<FunctionalType> FunctionalSet = policy.getTraversedFuctionalTypes();
				/*** Create Devices ***/
				DevicesListType Devices = new ObjectFactory().createDevicesListType();

				// Select each device traversed by the policy 
				for(FunctionalType f: FunctionalSet) {
					ServiceType Service = Util.covertFunctionalToService(f);
					Devices.getDevice().add(Service.value());
				}	

				/* Setting Traversal Policy elements */
				TraversalPolicy.setName(policy.getName());
				TraversalPolicy.setSource(policy.getSourceNode().getName());
				TraversalPolicy.setDestination(policy.getDestinationNode().getName());
				TraversalPolicy.setIsPositive(policy.isPositive());
				TraversalPolicy.setDevices(Devices);

				VerificationResultReader result = policy.getResult();

				if (result == null) {
				} else{
					VerificationType Verification = new ObjectFactory().createVerificationType();

					// Set <attribute name="last_update_time">
					Calendar verificationTime = result.getVerificationTime();
					XMLGregorianCalendar verificationTimeXGC;
					verificationTimeXGC = Util.calendarToXMLGregorianCalendar(verificationTime);

					if (result.getVerificationResult()){
						Verification.setResult(true);
						Verification.setTime(verificationTimeXGC);
						Verification.setMessage(result.getVerificationResultMsg());
					} else{
						Verification.setResult(false);
						Verification.setTime(verificationTimeXGC);
						Verification.setMessage(result.getVerificationResultMsg());
					}
					// Add Verification infos to the actual Traversal Policy
					TraversalPolicy.setVerification(Verification);
				}
				//Add the actual Traversal Policy to the Policies container
				Policies.getTraversalPolicy().add(TraversalPolicy);
			} 
			catch(ClassCastException e2){
				/*** Create ReachabilityPolicy ***/
				ReachabilityPolicyType ReachabilityPolicy = new ObjectFactory().createReachabilityPolicyType();			    
				// Set a single "potential" traversal policy
				ReachabilityPolicyReader policy = (ReachabilityPolicyReader) pr;
				/* Setting Reachability Policy elements */
				ReachabilityPolicy.setName(policy.getName());
				ReachabilityPolicy.setSource(policy.getSourceNode().getName());
				ReachabilityPolicy.setDestination(policy.getDestinationNode().getName());
				ReachabilityPolicy.setIsPositive(policy.isPositive());

				VerificationType Verification = new ObjectFactory().createVerificationType();
				VerificationResultReader result = policy.getResult();
				if (result != null){
					// Set <attribute name="last_update_time">
					Calendar verificationTime = result.getVerificationTime();
					XMLGregorianCalendar verificationTimeXGC;
					verificationTimeXGC = Util.calendarToXMLGregorianCalendar(verificationTime);

					if (result.getVerificationResult()){
						Verification.setResult(true);
						Verification.setTime(verificationTimeXGC);
						Verification.setMessage(result.getVerificationResultMsg());
					} else{
						Verification.setResult(false);
						Verification.setTime(verificationTimeXGC);
						Verification.setMessage(result.getVerificationResultMsg());
					}
					// Add Verification infos to the actual Traversal Policy
					ReachabilityPolicy.setVerification(Verification);	
				}
				//Add the actual Reachability Policy to the Policies container
				Policies.getReachabilityPolicy().add(ReachabilityPolicy);
			}
		}
		NFFG.setPolicies(Policies);
	}

	private void createNffgStructure(String name) {
		// Get the list of NFFGs
		NffgReader nffg_r = monitor.getNffg(name);

		// For each NFFG print related data
		/*** Create one NFFG ***/
		NFFG = new ObjectFactory().createNFFG();
		// Set <attribute name="name">
		NFFG.setName(nffg_r.getName());
		// Set <attribute name="last_update_time">
		Calendar lastUpdateTime = nffg_r.getUpdateTime();
		XMLGregorianCalendar lastUpdateTimeXGC = Util.calendarToXMLGregorianCalendar(lastUpdateTime);
		NFFG.setLastUpdateTime(lastUpdateTimeXGC);

		// Method that add polices to the actual NFFG
		createPoliciesStructure(NFFG);
		// Get the list of nodes
		Set<NodeReader> nodeSet = nffg_r.getNodes();
		/*** Create Nodes ***/
		NodesType Nodes = new ObjectFactory().createNodesType();
		/*** Create Links ***/
		LinksType Links = new ObjectFactory().createLinksType();
		// Add Nodes to the NFFG
		NFFG.setNodes(Nodes);
		// Add Links to the NFFG
		NFFG.setLinks(Links);

		for (NodeReader nr: nodeSet) {
			/*** Create a Node ***/
			NodeType Node= new ObjectFactory().createNodeType();
			// Set <attribute name="id">
			Node.setName(nr.getName());
			//System.out.println("nr.getName(): "+nr.getName());
			// Set <attribute name="Service">
			Node.setService(Util.covertFunctionalToService(nr.getFuncType()).value());
			// Add a single Node to the Nodes
			Nodes.getNode().add(Node);
			// Get the list of links
			Set<LinkReader> linkSet = nr.getLinks();

			for (LinkReader lr: linkSet){
				/*** Create a Link ***/
				LinkType Link = new ObjectFactory().createLinkType();
				// Set element Source
				Link.setSource(lr.getSourceNode().getName());
				// Set element Destination
				Link.setDestination(lr.getDestinationNode().getName());
				// Set <attribute name="id">
				Link.setName(lr.getName());
				//System.out.println("lr.getName(): "+lr.getName());
				// Add a single Link to the Links
				Links.getLink().add(Link);
			}// End for(Link)

		}// End for(Node)	
	}
}
