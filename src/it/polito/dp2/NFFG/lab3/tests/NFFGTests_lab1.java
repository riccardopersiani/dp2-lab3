package it.polito.dp2.NFFG.lab3.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.dp2.NFFG.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class NFFGTests_lab1 {
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    
    class NamedEntityReaderComparator implements Comparator<NamedEntityReader> {
        public int compare(NamedEntityReader f0, NamedEntityReader f1) {
        	return f0.getName().compareTo(f1.getName());
        }
    }
    
	private static NffgVerifier referenceNffgVerifier;	// reference data generator
	private static NffgVerifier testNffgVerifier;		// implementation under test
	private static long testcase;
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	// Create reference data generator
        System.setProperty("it.polito.dp2.NFFG.NffgVerifierFactory", "it.polito.dp2.NFFG.Random.NffgVerifierFactoryImpl");

        referenceNffgVerifier = NffgVerifierFactory.newInstance().newNffgVerifier();

        // Create implementation under test
        System.setProperty("it.polito.dp2.NFFG.NffgVerifierFactory", "it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory");

        testNffgVerifier = NffgVerifierFactory.newInstance().newNffgVerifier();
        
        // read testcase property
        Long testcaseObj = Long.getLong("it.polito.dp2.NFFG.Random.testcase");
        if (testcaseObj == null)
        	testcase = 0;
        else
        	testcase = testcaseObj.longValue();
    }
    
    @Before
    public void setUp() throws Exception {
        assertNotNull("Internal tester error during test setup: null reference", referenceNffgVerifier);
        assertNotNull("Could not run tests: the implementation under test generated a null NffgVerifier", testNffgVerifier);
    }

	// method for comparing two non-null strings    
	private void compareString(String rs, String ts, String meaning) {
		assertNotNull("NULL "+meaning, ts);
        assertEquals("Wrong "+meaning, rs, ts);		
	}
	
	private void compareTime(Calendar rc, Calendar tc, String meaning) {
		if (testcase != 2) // no time checking in this case
			return;
		assertNotNull(rc);
		assertNotNull("Null "+meaning, tc);
		
		// Compute lower and upper bounds for checking with precision of 1 minute
		Calendar upperBound, lowerBound;
		upperBound = (Calendar)rc.clone();
		upperBound.add(Calendar.MINUTE, 1);
		lowerBound = (Calendar)rc.clone();
		lowerBound.add(Calendar.MINUTE, -1);
		
		// Compute the condition to be checked
		boolean condition = tc.after(lowerBound) && tc.before(upperBound);
		
		assertTrue("Wrong "+meaning, condition);
	}
	
    @Test
    public final void testGetNffgs() {
    		// call getWorkflows on the two implementations
			Set<NffgReader> rs = referenceNffgVerifier.getNffgs();
			Set<NffgReader> ts = testNffgVerifier.getNffgs();
			
			compareNffgSets(rs, ts);
    }

	private void compareNffgSets(Set<NffgReader> rs, Set<NffgReader> ts) {
		// if one of the two calls returned null while the other didn't return null, the test fails
		if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
		    fail("getNffgs returns null when it should return non-null or vice versa");
		    return;
		}

		// if both calls returned null, there are no nffgs, and the test passes
		if ((rs == null) && (ts == null)) {
		    assertTrue("There are no Nffgs!", true);
		    return;
		}
		
		// check that the number of nffgs matches
		assertEquals("Wrong Number of Nffgs", rs.size(), ts.size());
		
		// create treesets of nffgs, using the comparator for sorting, one for reference and one for impl. under test 
		TreeSet<NffgReader> rts = new TreeSet<NffgReader>(new NamedEntityReaderComparator());
		TreeSet<NffgReader> tts = new TreeSet<NffgReader>(new NamedEntityReaderComparator());
   
		rts.addAll(rs);
		tts.addAll(ts);
		
		// check that all nffgs match one by one
		Iterator<NffgReader> ri = rts.iterator();
		Iterator<NffgReader> ti = tts.iterator();

		while (ri.hasNext() && ti.hasNext()) {
			compareNffgReader(ri.next(),ti.next());
		}
	}

    // private method for comparing two non-null NffgReader objects
	private void compareNffgReader(NffgReader rwr, NffgReader twr) {
		// check the NffgReaders are not null
		assertNotNull("Internal tester error: null nffg reader", rwr);
        assertNotNull("Unexpected null nffg reader", twr);
        System.out.println("Comparing nffg "+rwr.getName());

        // check the NffgReaders return the same data
        compareString(rwr.getName(), twr.getName(), "nffg name");
        compareTime(rwr.getUpdateTime(), twr.getUpdateTime(), "update time");
        compareNodeSets(rwr.getNodes(), twr.getNodes());
	}

	private void compareNodeSets(Set<NodeReader> rs, Set<NodeReader> ts) {
		// if one of the two calls returned null while the other didn't return null, the test fails
		if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
		    fail("getNodes returns null when it should return non-null or vice versa");
		    return;
		}

        // if both calls returned null, there are no nodes, and the test passes
		if ((rs == null) && (ts == null)) {
		    assertTrue("There are no nodes!", true);
		    return;
		}
		
        // check that the number of nodes matches
		assertEquals("Wrong Number of nodes", rs.size(), ts.size());
		
        // create treesets of nodes, using the comparator for sorting, one for reference and one for impl. under test 
		TreeSet<NodeReader> rts = new TreeSet<NodeReader>(new NamedEntityReaderComparator());
		TreeSet<NodeReader> tts = new TreeSet<NodeReader>(new NamedEntityReaderComparator());
   
		rts.addAll(rs);
		tts.addAll(ts);
		
		Iterator<NodeReader> ri = rts.iterator();
		Iterator<NodeReader> ti = tts.iterator();

        // check that all nodes match one by one
		while (ri.hasNext() && ti.hasNext()) {
			compareNodeReader(ri.next(),ti.next());
		}
	}

    // private method for comparing two non-null NodeReader objects
	private void compareNodeReader(NodeReader rpr, NodeReader tpr) {
		assertNotNull("Internal tester error: null node reader", rpr);
        assertNotNull("A null NodeReader has been found", tpr);
        
        System.out.println("Comparing node " + rpr.getName());
        
        // check the NodeReaders return the same name and functional type and links
        compareString(rpr.getName(), tpr.getName(), "node name");       
        assertEquals("wrong functional type", rpr.getFuncType(), tpr.getFuncType());
        compareLinkReaderSets(rpr.getLinks(), tpr.getLinks(), "links");             
	}

	private void compareLinkReaderSets(Set<LinkReader> rs, Set<LinkReader> ts, String string) {
		// if one of the two calls returned null while the other didn't return null, the test fails
		if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
		    fail("getLinks returns null when it should return non-null or vice versa");
		    return;
		}

        // if both calls returned null, there are no nodes, and the test passes
		if ((rs == null) && (ts == null)) {
		    assertTrue("There are no links!", true);
		    return;
		}
		
        // check that the number of nodes matches
		assertEquals("Wrong Number of links", rs.size(), ts.size());
		
        // create treesets of nodes, using the comparator for sorting, one for reference and one for impl. under test 
		TreeSet<LinkReader> rts = new TreeSet<LinkReader>(new NamedEntityReaderComparator());
		TreeSet<LinkReader> tts = new TreeSet<LinkReader>(new NamedEntityReaderComparator());
   
		rts.addAll(rs);
		tts.addAll(ts);
		
		Iterator<LinkReader> ri = rts.iterator();
		Iterator<LinkReader> ti = tts.iterator();

        // check that all nodes match one by one
		while (ri.hasNext() && ti.hasNext()) {
			compareLinkReader(ri.next(),ti.next());
		}

	}

	private void compareLinkReader(LinkReader rlr, LinkReader tlr) {
        // check the LinkReaders return the same name, source and destination
        compareString(rlr.getName(), tlr.getName(), "node name");   
        compareString(rlr.getSourceNode().getName(), tlr.getSourceNode().getName(), "source node");
        compareString(rlr.getDestinationNode().getName(), tlr.getDestinationNode().getName(), "destination node");
	}
	
    @Test
    public final void testGetPolicies() {
    	// call getPolicies on the two implementations
    	Set<PolicyReader> rs = referenceNffgVerifier.getPolicies();
		Set<PolicyReader> ts = testNffgVerifier.getPolicies();
		
		// check the resulting policy sets are equal
	    comparePolicySets(rs, ts);
    }

    private void comparePolicySets(Set<PolicyReader> rs, Set<PolicyReader> ts) {
		// if one of the two calls returned null while the other didn't return null, the test fails
		if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
		    fail("getPolicies returns null when it should return non-null or vice versa");
		    return;
		}

        // if both calls returned null, there are no policies, and the test passes
		if ((rs == null) && (ts == null)) {
		    assertTrue("There are no policies!", true);
		    return;
		}
		
        // check that the number of policies matches
		assertEquals("Wrong Number of policies", rs.size(), ts.size());
		
        // create treesets of nodes, using the comparator for sorting, one for reference and one for impl. under test 
		TreeSet<PolicyReader> rts = new TreeSet<PolicyReader>(new NamedEntityReaderComparator());
		TreeSet<PolicyReader> tts = new TreeSet<PolicyReader>(new NamedEntityReaderComparator());
   
		rts.addAll(rs);
		tts.addAll(ts);
		
		Iterator<PolicyReader> ri = rts.iterator();
		Iterator<PolicyReader> ti = tts.iterator();

        // check that all nodes match one by one
		while (ri.hasNext() && ti.hasNext()) {
			comparePolicyReader(ri.next(),ti.next());
		}

	}
    
	private void comparePolicyReader(PolicyReader rpr, PolicyReader tpr) {
		assertNotNull("Internal tester error: null policy reader", rpr);
        assertNotNull("A null PolicyReader has been found", tpr);
        System.out.println("Comparing policy " + rpr.getName());
        compareString(rpr.getName(), tpr.getName(), "policy name");
        assertTrue("Wrong ispositive",rpr.isPositive()==tpr.isPositive());
        compareVerificationResultReader(rpr.getResult(),tpr.getResult());
	}

	private void compareVerificationResultReader(VerificationResultReader rr, VerificationResultReader tr) {
		if ((rr == null) && (tr != null) || (rr != null) && (tr == null)) {
		    fail("verification result is null when it should be non-null or vice versa");
		    return;
		}
		if ((rr == null) && (tr == null)) {
		    // System.out.println("No verification result to compare.");;
		    return;
		}
		assertNotNull("Internal tester error: null policy referenced by verification result", rr.getPolicy());
        assertNotNull("Verification result references null policy", tr.getPolicy());
        // System.out.println("Comparing verification result of policy "+rr.getPolicy().getName());
        compareString(rr.getPolicy().getName(),tr.getPolicy().getName(), "policy name");
        assertTrue("Wrong result",rr.getVerificationResult().equals(tr.getVerificationResult()));
        compareString(rr.getVerificationResultMsg(), tr.getVerificationResultMsg(), "verification result message");
        compareTime(rr.getVerificationTime(),tr.getVerificationTime(), "verification time");
	}

}
