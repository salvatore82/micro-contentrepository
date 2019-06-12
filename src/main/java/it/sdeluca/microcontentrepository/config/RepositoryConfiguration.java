package it.sdeluca.microcontentrepository.config;

import java.io.FileNotFoundException;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class RepositoryConfiguration {

	@Value("${jcr.rep.home}")
	private String jcrRepHome;
	@Value("${jcr.rep.config}")
	private String jcrRepConfig;

	/**
	 * Creates a JCR repository
	 * 
	 * @return Repository
	 * @throws FileNotFoundException
	 * @throws RepositoryException
	 */
	@Bean
	public Repository createRepositoryImpl() throws FileNotFoundException, RepositoryException {
		RepositoryConfig repositoryConfig = RepositoryConfig.create(ResourceUtils.getFile(jcrRepConfig).toURI(),
				jcrRepHome);
		return RepositoryImpl.create(repositoryConfig);
	}

}