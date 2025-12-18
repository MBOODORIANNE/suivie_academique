package com.suivi_academique.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO contenant les informations de la requête HTTP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfoDTO {

    private String ipAddress;
    private String browserName;
    private String browserVersion;
    private String operatingSystem;
    private String deviceType;
    private String userAgent;
    private String httpMethod;
    private String requestUrl;
    private String referer;
    private String language;
    private LocalDateTime timestamp;

    /**
     * Génère un résumé lisible
     */
    public String getSummary() {
        return String.format(
                "IP: %s | Navigateur: %s %s | OS: %s | Appareil: %s",
                ipAddress,
                browserName,
                browserVersion,
                operatingSystem,
                deviceType
        );
    }
}