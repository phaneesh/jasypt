package org.jasypt.hibernate6.encryptor.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringAttributeConverter implements AttributeConverter<String, String> {

    private final AttributeEncryptionProvider attributeEncryptionProvider;

    public StringAttributeConverter() {
        attributeEncryptionProvider = AttributeEncryptionProvider.getInstance();
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        return (attribute == null) ? null : attributeEncryptionProvider.stringEncryptor().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return (dbData == null) ? null : attributeEncryptionProvider.stringEncryptor().decrypt(dbData);
    }
}