package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NffgVerifierException;

public class NffgVerifierFactory extends it.polito.dp2.NFFG.NffgVerifierFactory{

	@Override
	public NffgVerifier newNffgVerifier() throws NffgVerifierException {
		NffgVerifier myNffgVerifier = null;
		try {
			myNffgVerifier = new NffgVerifier();
		} catch (Exception e) {
			throw new NffgVerifierException();
		}		
		return myNffgVerifier;
	}
}
