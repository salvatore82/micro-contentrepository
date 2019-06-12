package it.sdeluca.microcontentrepository.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.sdeluca.microcontentrepository.dto.Document;
import it.sdeluca.microcontentrepository.exceptions.ContentrepoFileNotFoundException;
import it.sdeluca.microcontentrepository.service.ContentrepoService;

@RestController
@RequestMapping("/api/v1/files")
@Api(value = "Repository controller")
public class ContentrepoController {

	@Autowired
	private ContentrepoService storageService;

	@GetMapping("/{identifier}")
	@ApiOperation("Download the file")
	public ResponseEntity<Resource> serveFile(
			@ApiParam(value = "File name identifier", required = true) @PathVariable("identifier") String identifier)
			throws ContentrepoFileNotFoundException {
		Document file = storageService.load(identifier);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file.getResource());
	}

	@PostMapping
	@ApiOperation("Upload the file")
	public String handleFileUpload(
			@ApiParam(value = "The file to upload", required = true) @RequestParam("file") MultipartFile file)
			throws ContentrepoFileNotFoundException {
		return storageService.store(file);
	}

	@ExceptionHandler(ContentrepoFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(ContentrepoFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}