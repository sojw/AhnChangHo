package com.sojw.ahnchangho.core.util;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
	public static final String JSON_EXTENSION = ".json";
	private static ObjectMapper mapper = ObjectMapperFactory.mapper();

	/**
	 * Converts the object to a JSON string
	 */
	public static String toJSON(Object value) {
		try {
			StringWriter writer = new StringWriter();

			//mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.writeValue(writer, value);

			return mapper.writeValueAsString(value);
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		return StringUtils.EMPTY;
	}

	/**
	 * Converts the JSON string to an Object (either Map or List)
	 */
	public static Object fromJSON(String value) {
		return fromJSON(value, Object.class);
	}

	/**
	 * Converts the JSON string to a typed object via a TypeReference The main
	 * complication is handling of Generic types: if they are used, one has to
	 * use TypeReference object, to work around Java Type Erasure.
	 * 
	 * ex: return JSONUtils.fromJSON(this.answersJson, new
	 * TypeReference<List<StanzaAnswer>>(){});
	 */
	public static <T> T fromJSON(String value, TypeReference<T> type) {
		try {
			return mapper.readValue(value, type);
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		return null;
	}

	/**
	 * Converts the JSON String to a typed Object
	 * 
	 * ex > 
	 *   JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(SuccessResponse.class, Event.class); 
	 *   SuccessResponse<Event> response = JsonUtils.fromJSON(responseString, type);
	 * 
	 */
	public static <T> T fromJSON(String value, JavaType valueType) {
		try {
			return mapper.readValue(value, valueType);
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		return null;
	}

	/**
	 * Converts the JSON string to a typed object (or Map/List if Object.class
	 * is passed in)
	 * 
	 * ex > 
	 * 	JsonUtils.fromJSON(responseString, new TypeReference<SuccessResponse<Event>>() { });
	 */
	public static <T> T fromJSON(String value, Class<T> type) {
		//		if ("{}".equals(value)) {
		//			return null;
		//		}

		try {
			return mapper.readValue(value, type);
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		return null;
	}

	/**
	 * Convert value.
	 *
	 * @param <T> the generic type
	 * @param fromValue the from value
	 * @param toValueTypeRef the to value type ref
	 * @return the t
	 */
	public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
		try {
			return mapper.convertValue(fromValue, toValueTypeRef);
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		return null;
	}
}