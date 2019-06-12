/**
 * 
 */
package it.sdeluca.microcontentrepository.dto;

import java.io.Serializable;

import org.springframework.core.io.Resource;

/**
 * @author S.DeLuca
 *
 */
public class Document implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filename;
	private transient Resource resource;

	public Document(String filename, Resource resource) {
		this.filename = filename;
		this.resource = resource;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
