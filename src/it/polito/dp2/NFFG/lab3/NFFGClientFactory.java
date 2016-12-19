/**
 * 
 */
package it.polito.dp2.NFFG.lab3;

import it.polito.dp2.NFFG.FactoryConfigurationError;

/**
 * Defines a factory API that enables applications to obtain one or more objects
 * implementing the {@link NFFGClient} interface.
 *
 */
public abstract class NFFGClientFactory {

	private static final String propertyName = "it.polito.dp2.NFFG.NFFGClientFactory";
	
	protected NFFGClientFactory() {}
	
	/**
	 * Obtain a new instance of a <tt>NFFGClientFactory</tt>.
	 * 
	 * <p>
	 * This static method creates a new factory instance. This method uses the
	 * <tt>it.polito.dp2.NFFG.NFFGClientFactory</tt> system property to
	 * determine the NFFGClientFactory implementation class to load.
	 * </p>
	 * <p>
	 * Once an application has obtained a reference to a
	 * <tt>NFFGClientFactory</tt> it can use the factory to obtain a new
	 * {@link NFFGClient} instance.
	 * </p>
	 * 
	 * @return a new instance of a <tt>NFFGClientFactory</tt>.
	 * 
	 * @throws FactoryConfigurationError if the implementation is not available 
	 * or cannot be instantiated.
	 */
	public static NFFGClientFactory newInstance() throws FactoryConfigurationError {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		if(loader == null) {
			loader = NFFGClientFactory.class.getClassLoader();
		}
		
		String className = System.getProperty(propertyName);
		if (className == null) {
			throw new FactoryConfigurationError("cannot create a new instance of a NFFGClientFactory"
												+ "since the system property '" + propertyName + "'"
												+ "is not defined");
		}
		
		try {
			Class<?> c = (loader != null) ? loader.loadClass(className) : Class.forName(className);
			return (NFFGClientFactory) c.newInstance();
		} catch (Exception e) {
			throw new FactoryConfigurationError(e, "error instantiatig class '" + className + "'.");
		}
	}
	
	
	/**
	 * Creates a new instance of a {@link NFFGClient} implementation.
	 * 
	 * @return a new instance of a {@link NFFGClient} implementation.
	 * @throws NFFGClientException if an implementation of {@link NFFGClient} cannot be created.
	 */
	public abstract NFFGClient newNFFGClient() throws NFFGClientException;
}