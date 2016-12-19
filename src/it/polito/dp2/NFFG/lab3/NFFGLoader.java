package it.polito.dp2.NFFG.lab3;

public class NFFGLoader {

	public NFFGLoader() {
	}

	public static void main(String[] args) {
		NFFGClientFactory factory = NFFGClientFactory.newInstance();
		try {
			NFFGClient client = factory.newNFFGClient();
			client.loadAll();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
}
