package it.polito.dp2.NFFG.sol3.client2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.TraversalPolicyReader;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.sol3.client2.nffgservice.NFFG;
import it.polito.dp2.NFFG.sol3.client2.nffgservice.Nffgs;
import it.polito.dp2.NFFG.sol3.client2.nffgservice.ObjectFactory;
import it.polito.dp2.NFFG.sol3.client2.nffgservice.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.client2.nffgservice.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.client2.readers.NffgReaderCode;
import it.polito.dp2.NFFG.sol3.client2.readers.ReachabilityPolicyReaderCode;
import it.polito.dp2.NFFG.sol3.client2.readers.TraversalPolicyReaderCode;

public class NffgVerifier implements it.polito.dp2.NFFG.NffgVerifier {

	private Set<NffgReader> nffgReaders; //Contains all the Nffgs
	private Set<PolicyReader> policyReaders; // Contains all the Policies

	private Set<PolicyReader> oneNffgPolicies; //Contains the policy for one nffg
	private Map<NffgReader,Set<PolicyReader>> nffgPoliciesMap; // Contains the map of all policies for every nffg


	public NffgVerifier() throws ServiceException{
		nffgReaders = new HashSet<NffgReader>();
		policyReaders = new HashSet<PolicyReader>();
		nffgPoliciesMap = new HashMap<NffgReader,Set<PolicyReader>>();

		Nffgs nffgs = new ObjectFactory().createNffgs();
		try{
			// Perform the GET to NffgService in order to obtains all the Nffgs
			WebTarget target = Util.createClient2Target();	
			Response responseNffgs =  target.path("nffgs")
					.request()
					.get(Response.class);
			
			if(responseNffgs.getStatus() >= 400){
				System.err.println("NffgVerifer - Error: " + responseNffgs.getStatus() + "throw new ServiceException()");
				throw new NffgVerifierException();
			}
			
			nffgs = responseNffgs.readEntity(Nffgs.class);

			for(NFFG nffg : nffgs.getNFFG()){
				oneNffgPolicies = new HashSet<PolicyReader>();
				NffgReader nffgReader = new NffgReaderCode(nffg);
				nffgReaders.add(nffgReader);

				for(ReachabilityPolicyType reachabilityPolicy: nffg.getPolicies().getReachabilityPolicy()){
					ReachabilityPolicyReader reachabilityReader = new ReachabilityPolicyReaderCode(nffg, nffgReader, reachabilityPolicy);
					policyReaders.add(reachabilityReader);
					oneNffgPolicies.add(reachabilityReader);
				}

				for(TraversalPolicyType traversalPolicy: nffg.getPolicies().getTraversalPolicy()){
					TraversalPolicyReader traversalReader = new TraversalPolicyReaderCode(nffg, nffgReader, traversalPolicy);
					policyReaders.add(traversalReader);
					oneNffgPolicies.add(traversalReader);
				}

				nffgPoliciesMap.put(nffgReader, oneNffgPolicies);
			}	
		} catch (Exception e){
			System.err.println("NffgVerifer - RuntimeException - throw new ServiceException()");
			throw new ServiceException();
		}
	}

	@Override
	public NffgReader getNffg(String arg0) {
		for(NffgReader nffgReader: nffgReaders){
			if(nffgReader.getName().equals(arg0)){
				return nffgReader;
			}
		}
		return null;
	}

	@Override
	public Set<NffgReader> getNffgs() {
		return nffgReaders;
	}

	@Override
	public Set<PolicyReader> getPolicies() {
		return policyReaders;
	}

	@Override
	public Set<PolicyReader> getPolicies(String arg0) {
		for(NffgReader nffgReader: nffgReaders){
			if(nffgReader.getName().equals(arg0)){
				return nffgPoliciesMap.get(nffgReader);
			}
		}
		return null;
	}

	@Override
	public Set<PolicyReader> getPolicies(Calendar arg0) {
		Set<PolicyReader> policyAfter = new HashSet<PolicyReader>();
		for(PolicyReader policyReader: policyReaders){
			if(policyReader.getResult().getVerificationTime().after(arg0)){
				policyAfter.add(policyReader);
			}
		}
		return policyAfter;
	}

}
