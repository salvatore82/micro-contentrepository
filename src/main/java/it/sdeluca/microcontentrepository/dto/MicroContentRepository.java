package it.sdeluca.microcontentrepository.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The persistent class for the USER database table.
 * 
 */
@Data
@NoArgsConstructor
public class MicroContentRepository {
	private String uuid;
	private Integer id;
	private Boolean valid;
}