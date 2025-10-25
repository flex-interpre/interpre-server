package com.flex.interpre.global.config;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VectorConverter implements AttributeConverter<float[], Object> {
    // Java float[] -> DB의 vector 타입으로 변환
    @Override
    public Object convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) return null;
        return new PGvector(attribute);
    }

    // DB의 vector 타입 -> float[] 타입으로 변환
    @Override
    public float[] convertToEntityAttribute(Object dbData) {
        if (dbData == null) return null;
        return ((PGvector) dbData).toArray();
    }
}