/**
 * 
 */
package it.sdeluca.microcontentrepository.service;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.sdeluca.microcontentrepository.dto.Document;
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
			Node uuidNode = session.getNodeByIdentifier(identifier);
			Node fileNode = uuidNode.getNode("file");
			Node resourceNode = fileNode.getNode("jcr:content");
			Binary fileBinary = resourceNode.getProperty("jcr:data").getBinary();
			String title = uuidNode.getProperty("mix:title").getString();
			return new Document(title, new InputStreamResource(fileBinary.getStream()));
		} catch (RepositoryException e) {
			log.error("Error retrieving file", e);
			throw new ContentrepoFileNotFoundException(e.getMessage());
		}
	}
}
