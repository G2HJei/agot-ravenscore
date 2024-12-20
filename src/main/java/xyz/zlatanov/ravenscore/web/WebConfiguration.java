package xyz.zlatanov.ravenscore.web;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebConfiguration {

	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("ui-messages");
		return messageSource;
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder -> builder
				.modules(new JavaTimeModule())
				.featuresToEnable(INDENT_OUTPUT)
				.serializationInclusion(NON_NULL)
				.failOnUnknownProperties(false);
	}
}