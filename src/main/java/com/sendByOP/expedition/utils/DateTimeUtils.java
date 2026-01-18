package com.sendByOP.expedition.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utilitaire pour les conversions entre java.util.Date et java.time.LocalDateTime
 */
public class DateTimeUtils {
    
    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Convertit un Date en LocalDateTime
     * @param date le Date à convertir
     * @return le LocalDateTime correspondant, ou null si date est null
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * Convertit un LocalDateTime en Date
     * @param localDateTime le LocalDateTime à convertir
     * @return le Date correspondant, ou null si localDateTime est null
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
    
    /**
     * Retourne le LocalDateTime actuel
     * @return LocalDateTime.now()
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Retourne le Date actuel
     * @return new Date()
     */
    public static Date nowAsDate() {
        return new Date();
    }
}
