package org.jasypt.hibernate6.encryptor.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ByteAttributeConverter implements AttributeConverter<byte[], byte[]> {

    private final AttributeEncryptionProvider attributeEncryptionProvider;

    public ByteAttributeConverter() {
        attributeEncryptionProvider = AttributeEncryptionProvider.getInstance();
    }

    @Override
    public byte[] convertToDatabaseColumn(byte[] attribute) {
        return (attribute == null) ? null : attributeEncryptionProvider.byteEncryptor().encrypt(attribute);
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] dbData) {
        return (dbData == null) ? null : attributeEncryptionProvider.byteEncryptor().decrypt(dbData);
    }
}