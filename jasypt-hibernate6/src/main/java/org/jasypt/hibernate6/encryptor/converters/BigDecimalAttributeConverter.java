package org.jasypt.hibernate6.encryptor.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class BigDecimalAttributeConverter implements AttributeConverter<BigDecimal, BigDecimal> {

    private final AttributeEncryptionProvider attributeEncryptionProvider;

    public BigDecimalAttributeConverter() {
        attributeEncryptionProvider = AttributeEncryptionProvider.getInstance();
    }

    @Override
    public BigDecimal convertToDatabaseColumn(BigDecimal attribute) {
        return (attribute == null) ? null : attributeEncryptionProvider.bigDecimalEncryptor().encrypt(attribute);
    }

    @Override
    public BigDecimal convertToEntityAttribute(BigDecimal dbData) {
        return (dbData == null) ? null : attributeEncryptionProvider.bigDecimalEncryptor().decrypt(dbData);
    }
}