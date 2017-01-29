package it.polito.dp2.NFFG.sol3.service.validation;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;

@Provider
@Consumes({"application/xml","text/xml"})
public class NffgsValidationProvider implements MessageBodyReader<NFFG> {
	String responseBodyTemplate;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return NFFG.class.equals(type);
	}

	@Override
	public NFFG readFrom(Class<NFFG> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		final String jaxbPackage = "it.polito.dp2.NFFG.sol3.service.jaxb";
		Unmarshaller unmarshaller;
		try {				
			InputStream schemaStream = NffgsValidationProvider.class.getResourceAsStream("/xsd/nffgVerifier.xsd");
			if (schemaStream == null) {
				throw new IOException();
			}
			JAXBContext jc = JAXBContext.newInstance( jaxbPackage );
			unmarshaller = jc.createUnmarshaller();
			SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new StreamSource(schemaStream));
			unmarshaller.setSchema(schema);

		} catch (SAXException | JAXBException | IOException se) {
			System.err.println("exception");
			throw new IOException();
		}
		try {
			return (NFFG) unmarshaller.unmarshal(entityStream);
		} catch (JAXBException ex) {
			throw new BadRequestException("Request body validation error");
		}
	}

}
