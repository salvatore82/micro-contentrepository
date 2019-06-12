package it.sdeluca.microcontentrepository.service;

import org.springframework.web.multipart.MultipartFile;

import it.sdeluca.microcontentrepository.dto.Document;
import it.sdeluca.microcontentrepository.exceptions.ContentrepoFileNotFoundException;

public interface ContentrepoService {

	String store(MultipartFile file) throws ContentrepoFileNotFoundException;

	// Path load(String identifier);

	Document load(String identifier) throws ContentrepoFileNotFoundException;
}