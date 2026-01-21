package org.jasypt.hibernate6.encryptor.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CalengerAttributeConverter implements AttributeConverter<String, String> {

    private final AttributeEncryptionProvider attributeEncryptionProvider;

    public CalengerAttributeConverter() {
        attributeEncryptionProvider = AttributeEncryptionProvider.getInstance();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return (attribute == null) ? null : attributeEncryptionProvider.calenderEncryptor().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return (dbData == null) ? null : attributeEncryptionProvider.calenderEncryptor().decrypt(dbData);
    }
}