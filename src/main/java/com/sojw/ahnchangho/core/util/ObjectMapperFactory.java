package com.sojw.ahnchangho.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * A factory for creating Mapper objects.
 */
public class ObjectMapperFactory {

	/**
	 * Mapper.
	 *
	 * @return the object mapper
	 */
	public static ObjectMapper mapper() {
		return new ObjectMapper() {
			{
				registerModule(new JodaModule());
				disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
				enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
			}
		};
	}

	/**
	 * Indented.
	 *
	 * @return the object mapper
	 */
	public static ObjectMapper indented() {
		ObjectMapper objectMapper = mapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return objectMapper;
	}
}