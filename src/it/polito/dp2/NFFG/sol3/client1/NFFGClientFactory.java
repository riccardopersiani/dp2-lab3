package it.polito.dp2.NFFG.sol3.client1;

//Cancellato package
import it.polito.dp2.NFFG.lab3.NFFGClientException;

public class NFFGClientFactory extends it.polito.dp2.NFFG.lab3.NFFGClientFactory {

	@Override
	public NFFGClient newNFFGClient() throws NFFGClientException {
		NFFGClient myNFFGClient = null;
		try{
			myNFFGClient = new NFFGClient();
		}catch (Exception e) {
			throw new NFFGClientException();
		}
		return myNFFGClient;
	}
	
}
