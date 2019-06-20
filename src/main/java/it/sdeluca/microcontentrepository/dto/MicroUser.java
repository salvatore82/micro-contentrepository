package it.sdeluca.microcontentrepository.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The persistent class for the USER database table.
 * 
 */
@Data
@NoArgsConstructor
public class MicroUser {
	private Integer id;
	private String lastName;
	private String name;
	private byte[] document;
	private String documentFileName;
	private String documentMimeType;
	private Boolean valid;
}