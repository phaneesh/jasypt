package org.jasypt.hibernate6.encryptor.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigInteger;

@Converter
public class BigIntegerAttributeConverter implements AttributeConverter<BigInteger, BigInteger> {

    private final AttributeEncryptionProvider attributeEncryptionProvider;

    public BigIntegerAttributeConverter() {
        attributeEncryptionProvider = AttributeEncryptionProvider.getInstance();
    }

    @Override
    public BigInteger convertToDatabaseColumn(BigInteger attribute) {
        return (attribute == null) ? null : attributeEncryptionProvider.bigIntegerEncryptor().encrypt(attribute);
    }

    @Override
    public BigInteger convertToEntityAttribute(BigInteger dbData) {
        return (dbData == null) ? null : attributeEncryptionProvider.bigIntegerEncryptor().decrypt(dbData);
    }
}