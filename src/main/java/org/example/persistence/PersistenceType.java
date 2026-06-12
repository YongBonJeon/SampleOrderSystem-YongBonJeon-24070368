package org.example.persistence;

public enum PersistenceType {
    MEMORY, FILE, JSON, DATABASE;

    public static PersistenceType fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
