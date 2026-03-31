package com.hainam.worksphere.shared.persistence.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static AesGcmStringEncryptor encryptor;

    @Autowired
    public void setEncryptor(AesGcmStringEncryptor encryptor) {
        EncryptedLocalDateConverter.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        if (encryptor == null) {
            throw new IllegalStateException("EncryptedLocalDateConverter is not initialized");
        }
        return encryptor.encrypt(attribute.toString());
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        if (encryptor == null) {
            throw new IllegalStateException("EncryptedLocalDateConverter is not initialized");
        }
        String plainDate = encryptor.decrypt(dbData);
        return LocalDate.parse(plainDate);
    }
}
