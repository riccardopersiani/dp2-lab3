package it.polito.dp2.NFFG.lab3.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.dp2.NFFG.*;
import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.NFFGClientException;
import it.polito.dp2.NFFG.lab3.NFFGClientFactory;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;

import java.net.URL;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class NFFGTests {

	private static NffgVerifier referenceNffgVerifier;	// reference data generator
	private static NffgVerifier testNffgVerifier;	// data generator under test
	private static NFFGClient testNFFGClient;			// NFFGClient under test
	private static long testcase;
	private static URL serviceUrl; 
	private static NffgReader referenceNFFG;
	private static ReachabilityPolicyReader referencePolicy;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Create reference data generator
		System.setProperty("it.polito.dp2.NFFG.NffgVerifierFactory", "it.polito.dp2.NFFG.Random.NffgVerifierFactoryImpl");
		referenceNffgVerifier = NffgVerifierFactory.newInstance().newNffgVerifier();

		// set referenceNFFG
		if(referenceNffgVerifier.getNffgs().size()!=0){
			TreeSet<NffgReader> rts = new TreeSet<NffgReader>(new NamedEntityReaderComparator());
			rts.addAll(referenceNffgVerifier.getNffgs());
			Iterator<NffgReader> iter = rts.iterator();
			boolean found=false;
			// look for nffg with at least one policy
			while(iter.hasNext() && !found) {
				referenceNFFG = iter.next();
				Set<PolicyReader> policies = referenceNffgVerifier.getPolicies(referenceNFFG.getName());
				if (policies.size()>0) {
					TreeSet<PolicyReader> pts = new TreeSet<PolicyReader>(new NamedEntityReaderComparator());
					pts.addAll(policies);
					Iterator<PolicyReader> pIter = pts.iterator();
					referencePolicy = (ReachabilityPolicyReader) pIter.next();
					found=true;
				}
			}
			assertEquals("Tests cannot run. Please choose another seed.",found,true);
		}
		
		// read testcase property
		Long testcaseObj = Long.getLong("it.polito.dp2.NFFG.Random.testcase");
		if (testcaseObj == null)
			testcase = 0;
		else
			testcase = testcaseObj.longValue();

	}

	@Before
	public void setUp() throws Exception {
        assertNotNull("Internal tester error during test setup: null nffgverifier reference", referenceNffgVerifier);
        assertNotNull("Internal tester error during test setup: null reference NFFG", referenceNFFG);
        assertNotNull("Internal tester error during test setup: null reference policy", referencePolicy);
		assertNotNull("Internal error: reference Policy has null source node", referencePolicy.getSourceNode());
		assertNotNull("Internal error: reference Policy has null destination node", referencePolicy.getDestinationNode());
		assertNotNull("Internal error: reference Policy has null nffg", referencePolicy.getNffg());
	}

	// method for comparing two non-null strings    
	private void compareString(String rs, String ts, String meaning) {
		assertNotNull("NULL "+meaning, ts);
		assertEquals("Wrong "+meaning, rs, ts);		
	}

	private void createClient() throws NFFGClientException {
		// Create client under test
		try {
			testNFFGClient = NFFGClientFactory.newInstance().newNFFGClient();
		} catch (FactoryConfigurationError fce) {
			fce.printStackTrace();
		}
		assertNotNull("The implementation under test generated a null NFFGClient", testNFFGClient);
	}

	@Test
	public final void testLoadUnloadPolicy() {
		System.out.println("DEBUG: starting testloadunloadpolicy");
			int rightPolicyNumber = referenceNffgVerifier.getPolicies(referenceNFFG.getName()).size();
		try {
			
			// 1. Load a non-existing policy and check that the number of policies has been increased by 1
			// create client under test
				createClient();
			// load a non existing policy
			testNFFGClient.loadReachabilityPolicy("TestPolicy", referencePolicy.getNffg().getName(), referencePolicy.isPositive(), referencePolicy.getSourceNode().getName(), referencePolicy.getDestinationNode().getName());
			// Now there should be one more policy
			comparePolicyNumber(rightPolicyNumber+1, referenceNFFG.getName());

			// 2. Unload the policy previously loaded and check that the number of policies has been decreased by 1
			
			testNFFGClient.unloadReachabilityPolicy("TestPolicy");
			comparePolicyNumber(rightPolicyNumber, referenceNFFG.getName());
			
		} catch (NFFGClientException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (UnknownNameException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (ServiceException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (NffgVerifierException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (FactoryConfigurationError e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} 		

	}

	private void comparePolicyNumber(int expected, String NffgName) throws NffgVerifierException, FactoryConfigurationError {
		// create testNffgVerifier
		System.setProperty("it.polito.dp2.NFFG.NffgVerifierFactory", "it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory");
		testNffgVerifier = NffgVerifierFactory.newInstance().newNffgVerifier();
		assertNotNull("The implementation under test generated a null NffgVerifier", testNffgVerifier);
		
		// read policies and check their number is right
		Set<PolicyReader> tps = testNffgVerifier.getPolicies(NffgName);
		assertNotNull("Null policy set",tps);
		assertEquals("Wrong number of policies", expected ,tps.size());
	}

	@Test
	public final void testReachability() {
		System.out.println("DEBUG: starting testReachability");
		try {
			// create client under test
			createClient();
			
			// test reference reachability policy
			boolean result = testNFFGClient.testReachabilityPolicy(referencePolicy.getName());
			
			// check result of reachability policy has been set correctly in the service
			compareVerificationResult(referencePolicy, result);

		} catch (UnknownNameException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (ServiceException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (NFFGClientException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (NffgVerifierException e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		} catch (FactoryConfigurationError e) {
			fail("Unexpected exception thrown: "+e.getClass().getName());
		}
	}

	private void compareVerificationResult(ReachabilityPolicyReader rp, boolean result) throws NffgVerifierException, FactoryConfigurationError {
		// create testNffgVerifier
		System.setProperty("it.polito.dp2.NFFG.NffgVerifierFactory", "it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory");
		testNffgVerifier = NffgVerifierFactory.newInstance().newNffgVerifier();
		assertNotNull("The implementation under test generated a null NffgVerifier", testNffgVerifier);
		
		Set<PolicyReader> tps = testNffgVerifier.getPolicies(referenceNFFG.getName());
		for (PolicyReader pr: tps) {
			if (referencePolicy.getName().equals(pr.getName())) {		
				// Found policy: check it
				VerificationResultReader tvr = pr.getResult();
				assertNotNull("Null verification result reader", tvr);
				assertEquals("Wrong verification result", result, tvr.getVerificationResult());
				VerificationResultReader rvr = referencePolicy.getResult();
				if (rvr != null) {
					Calendar rc = referencePolicy.getResult().getVerificationTime();
					if (rc!= null)
						compareTimePassed(rc, tvr.getVerificationTime(), "verification time");
				}
				break;
			}
		}
	}
	
	private void compareTimePassed(Calendar rc, Calendar tc, String meaning) {
		assertNotNull(rc);
		assertNotNull("Null "+meaning, tc);
				
		// Compute the condition to be checked
		boolean condition = tc.after(rc);
		
		assertTrue("Wrong "+meaning, condition);
	}

}

class NamedEntityReaderComparator implements Comparator<NamedEntityReader> {
    public int compare(NamedEntityReader f0, NamedEntityReader f1) {
    	return f0.getName().compareTo(f1.getName());
    }
}
