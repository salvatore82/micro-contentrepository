/**
 * 
 */
package it.sdeluca.microcontentrepository.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.sdeluca.microcontentrepository.dto.Document;
import it.sdeluca.microcontentrepository.dto.MicroContentRepository;
import it.sdeluca.microcontentrepository.dto.MicroUser;
import it.sdeluca.microcontentrepository.exceptions.ContentrepoFileNotFoundException;

/**
 * @author S.DeLuca
 *
 */
@Service
public class ContentrepoServiceImpl implements ContentrepoService {

	private static final Logger log = LoggerFactory.getLogger(ContentrepoServiceImpl.class.getName());

	@Autowired
	private Repository repository;
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public String store(MultipartFile file) throws ContentrepoFileNotFoundException {
		try {
			Session session = repository.login(new SimpleCredentials("admin", "superSecret!".toCharArray()));
			ValueFactory factory = session.getValueFactory();
			Binary binary = factory.createBinary(file.getInputStream());
			Value value = factory.createValue(binary);
			Node root = session.getRootNode();
			Node uuidNode = root.addNode("jcr:uuid");
			Node fileNode = uuidNode.addNode("file", "nt:file");
			Node resourceNode = fileNode.addNode("jcr:content", "nt:resource");
			resourceNode.setProperty("jcr:data", value);
			uuidNode.addMixin("mix:title");
			uuidNode.setProperty("mix:title", file.getOriginalFilename());
			resourceNode.setProperty("jcr:mimeType", file.getContentType());
			session.save();
			return uuidNode.getIdentifier();
		} catch (RepositoryException | IOException e) {
			log.error("Error saving file", e);
			throw new ContentrepoFileNotFoundException(e.getMessage());
		}
	}

	@Override
	public Document load(String identifier) throws ContentrepoFileNotFoundException {
		try {
			Session session = repository.login(new SimpleCredentials("admin", "superSecret!".toCharArray()));
			Node root = session.getRootNode();
			Node userNode = root.getNode(identifier);
			Node fileNode = userNode.getNode("file");
			Node resourceNode = fileNode.getNode("jcr:content");
			Binary fileBinary = resourceNode.getProperty("jcr:data").getBinary();
			String title = userNode.getProperty("mix:title").getString();
			Document document = new Document(title, new InputStreamResource(fileBinary.getStream()));
			session.logout();
			return document;
		} catch (RepositoryException e) {
			log.error("Error retrieving file", e);
			throw new ContentrepoFileNotFoundException(e.getMessage());
		}
	}

	@JmsListener(destination = "microuser.topic")
	@SendTo("microcontentrepository.topic")
	public String listenForCreatedUser(final Message message) throws JMSException, JsonParseException,
			JsonMappingException, IOException, ContentrepoFileNotFoundException {
		if (message instanceof ActiveMQBytesMessage) {
			ActiveMQBytesMessage bytesMessage = (ActiveMQBytesMessage) message;
			MicroUser microUser = objectMapper.readValue(new String(bytesMessage.getContent().data), MicroUser.class);
			MicroContentRepository microContentRepository = new MicroContentRepository();
			try {
				Session session = repository.login(new SimpleCredentials("admin", "superSecret!".toCharArray()));
				ValueFactory factory = session.getValueFactory();
				Binary binary = factory.createBinary(new ByteArrayInputStream(microUser.getDocument()));
				Value value = factory.createValue(binary);
				Node root = session.getRootNode();
				Node uuidNode = root.addNode(microUser.getId().toString());
				Node fileNode = uuidNode.addNode("file", "nt:file");
				Node resourceNode = fileNode.addNode("jcr:content", "nt:resource");
				resourceNode.setProperty("jcr:data", value);
				uuidNode.addMixin("mix:title");
				uuidNode.setProperty("mix:title", microUser.getDocumentFileName());
				resourceNode.setProperty("jcr:mimeType", microUser.getDocumentMimeType());
				// TODO validation logic for file
				microContentRepository.setId(microUser.getId());
				microContentRepository.setUuid(uuidNode.getIdentifier());
				microContentRepository.setValid(Boolean.TRUE);
				session.save();
				session.logout();
				log.debug("Saved file for user ".concat(microUser.getId().toString()).concat(" with UUID: ")
						.concat(uuidNode.getIdentifier()));
			} catch (RepositoryException e) {
				log.error("Error saving file", e);
				throw new ContentrepoFileNotFoundException(e.getMessage());
			}
			return objectMapper.writeValueAsString(microContentRepository);
		} else {
			throw new JMSException("Failed reading message from topic");
		}
	}
}
