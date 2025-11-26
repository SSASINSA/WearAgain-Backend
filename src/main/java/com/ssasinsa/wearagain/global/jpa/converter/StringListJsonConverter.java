package com.ssasinsa.wearagain.global.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * List<String>을 JSON 문자열로 직렬화/역직렬화하기 위한 컨버터.
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("pickupLocations 직렬화에 실패했습니다.", exception);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return List.of();
        }
        try {
            List<String> values = OBJECT_MAPPER.readValue(dbData, new TypeReference<List<String>>() { });
            return new ArrayList<>(values);
        } catch (Exception exception) {
            throw new IllegalArgumentException("pickupLocations 역직렬화에 실패했습니다.", exception);
        }
    }
}
