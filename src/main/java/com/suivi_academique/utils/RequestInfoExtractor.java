package com.suivi_academique.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire pour extraire les informations de la requête HTTP
 * Compatible Spring Boot 3+ (jakarta.servlet)
 * Version améliorée avec détection Postman
 */
@Slf4j
@Component
public class RequestInfoExtractor {

    /**
     * Extrait l'adresse IP du client
     * Prend en compte les proxies et load balancers
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Si plusieurs IPs (proxy chain), prendre la première
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback sur l'IP directe
        String ip = request.getRemoteAddr();

        // Convertir localhost IPv6 en IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * Extrait le User-Agent complet
     */
    public String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * Extrait le nom du navigateur (version améliorée)
     */
    public String getBrowserName(HttpServletRequest request) {
        String userAgent = getUserAgent(request);

        // ✅ Détection améliorée de Postman
        if (userAgent.contains("PostmanRuntime") || userAgent.contains("Postman")) {
            return "Postman";
        } else if (userAgent.contains("Insomnia")) {
            return "Insomnia";
        } else if (userAgent.contains("curl")) {
            return "cURL";
        } else if (userAgent.contains("Edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            return "Google Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown Browser";
        }
    }

    /**
     * Extrait la version du navigateur (version améliorée)
     */
    public String getBrowserVersion(HttpServletRequest request) {
        String userAgent = getUserAgent(request);

        try {
            // ✅ Détection version Postman
            if (userAgent.contains("PostmanRuntime/")) {
                return extractVersion(userAgent, "PostmanRuntime/");
            } else if (userAgent.contains("Postman/")) {
                return extractVersion(userAgent, "Postman/");
            } else if (userAgent.contains("Edg/")) {
                return extractVersion(userAgent, "Edg/");
            } else if (userAgent.contains("Chrome/")) {
                return extractVersion(userAgent, "Chrome/");
            } else if (userAgent.contains("Firefox/")) {
                return extractVersion(userAgent, "Firefox/");
            } else if (userAgent.contains("Version/")) {
                return extractVersion(userAgent, "Version/");
            } else if (userAgent.contains("curl/")) {
                return extractVersion(userAgent, "curl/");
            }
        } catch (Exception e) {
            log.debug("Erreur extraction version navigateur: {}", e.getMessage());
        }

        return "";
    }

    /**
     * Extrait le système d'exploitation (version améliorée)
     */
    public String getOperatingSystem(HttpServletRequest request) {
        String userAgent = getUserAgent(request);

        // ✅ Détection OS pour Postman
        if (userAgent.contains("PostmanRuntime")) {
            // Postman tourne sur Windows/Mac/Linux mais ne l'indique pas toujours
            if (userAgent.contains("Win")) {
                return "Windows";
            } else if (userAgent.contains("Mac")) {
                return "macOS";
            } else if (userAgent.contains("Linux")) {
                return "Linux";
            }
            // Par défaut, on suppose que c'est le même OS que le serveur
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                return "Windows (serveur)";
            } else if (osName.contains("mac")) {
                return "macOS (serveur)";
            } else if (osName.contains("linux")) {
                return "Linux (serveur)";
            }
            return "Unknown (API Client)";
        }

        if (userAgent.contains("Windows NT 10.0")) {
            return "Windows 10/11";
        } else if (userAgent.contains("Windows NT 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("Windows NT 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            return "Mac OS X";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        } else {
            return "Unknown OS";
        }
    }

    /**
     * Extrait le type d'appareil
     */
    public String getDeviceType(HttpServletRequest request) {
        String userAgent = getUserAgent(request);

        // ✅ Postman est toujours un client API (Desktop)
        if (userAgent.contains("PostmanRuntime") || userAgent.contains("Postman")
                || userAgent.contains("Insomnia") || userAgent.contains("curl")) {
            return "API Client";
        }

        if (userAgent.contains("Mobile") || userAgent.contains("Android")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * Extrait la méthode HTTP
     */
    public String getHttpMethod(HttpServletRequest request) {
        return request.getMethod();
    }

    /**
     * Extrait l'URL complète de la requête
     */
    public String getRequestUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    /**
     * Extrait le referer (page d'origine)
     */
    public String getReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "Direct access";
    }

    /**
     * Extrait la langue préférée
     */
    public String getPreferredLanguage(HttpServletRequest request) {
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            // Prendre la première langue
            String[] languages = acceptLanguage.split(",");
            return languages[0].trim();
        }
        return "Unknown";
    }

    /**
     * Extrait l'heure de la requête
     */
    public String getRequestTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Méthode helper pour extraire la version (version améliorée)
     */
    private String extractVersion(String userAgent, String marker) {
        int startIndex = userAgent.indexOf(marker);
        if (startIndex == -1) return "";

        startIndex += marker.length();
        int endIndex = userAgent.indexOf(" ", startIndex);

        if (endIndex == -1) {
            endIndex = userAgent.length();
        }

        String version = userAgent.substring(startIndex, endIndex);

        // Extraire seulement la version majeure.mineure
        if (version.contains(".")) {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                return parts[0] + "." + parts[1];
            }
            return parts[0];
        }

        return version;
    }

    /**
     * Génère un résumé complet des informations de la requête
     */
    public String getRequestSummary(HttpServletRequest request) {
        String browserVersion = getBrowserVersion(request);
        String browserInfo = getBrowserName(request);
        if (!browserVersion.isEmpty()) {
            browserInfo += " " + browserVersion;
        }

        return String.format(
                "IP: %s | Client: %s | OS: %s | Type: %s | Méthode: %s",
                getClientIpAddress(request),
                browserInfo,
                getOperatingSystem(request),
                getDeviceType(request),
                getHttpMethod(request)
        );
    }

    /**
     * Log complet des informations de requête
     */
    public void logRequestInfo(HttpServletRequest request, String action) {
        log.info("========================================");
        log.info("Action: {}", action);
        log.info("Date/Heure: {}", getRequestTime());
        log.info("Adresse IP: {}", getClientIpAddress(request));
        log.info("Client: {} {}", getBrowserName(request), getBrowserVersion(request));
        log.info("Système d'exploitation: {}", getOperatingSystem(request));
        log.info("Type: {}", getDeviceType(request));
        log.info("User-Agent: {}", getUserAgent(request));
        log.info("Méthode HTTP: {}", getHttpMethod(request));
        log.info("URL: {}", getRequestUrl(request));
        log.info("Referer: {}", getReferer(request));
        log.info("Langue: {}", getPreferredLanguage(request));
        log.info("========================================");
    }
}