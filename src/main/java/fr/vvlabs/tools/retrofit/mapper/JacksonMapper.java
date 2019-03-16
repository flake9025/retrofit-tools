package fr.vvlabs.tools.retrofit.mapper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author vvillain
 * The Jackson Object Mapper using most common parameters.
 */
public class JacksonMapper extends ObjectMapper {

    private static final long serialVersionUID = 5844456449874879552L;

    /**
     * Instantiates a new asset jackson mapper.
     */
    public JacksonMapper() {
        this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        this.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        this.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        this.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        this.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        setSerializationInclusion(Include.NON_NULL);
    }
}
