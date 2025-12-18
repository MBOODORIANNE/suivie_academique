package com.suivi_academique.controllers;

import com.suivi_academique.dto.SalleDTO;
import com.suivi_academique.services.implementations.SalleService;
import com.suivi_academique.utils.RequestInfoExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des salles
 * Capture automatiquement les informations de requête HTTP
 */
@Slf4j
@RestController
@RequestMapping("/salle")
public class SalleController {

    private final SalleService salleService;
    private final RequestInfoExtractor requestInfoExtractor;

    public SalleController(SalleService salleService, RequestInfoExtractor requestInfoExtractor) {
        this.salleService = salleService;
        this.requestInfoExtractor = requestInfoExtractor;
    }

    /**
     * Crée une nouvelle salle
     * Capture et retourne les informations de la requête HTTP
     */
    @PostMapping
    public ResponseEntity<?> save(@RequestBody SalleDTO salleDTO, HttpServletRequest request) {

        //  Extraction des informations de la requête
        String ipAddress = requestInfoExtractor.getClientIpAddress(request);
        String browserName = requestInfoExtractor.getBrowserName(request);
        String browserVersion = requestInfoExtractor.getBrowserVersion(request);
        String os = requestInfoExtractor.getOperatingSystem(request);
        String deviceType = requestInfoExtractor.getDeviceType(request);
        String userAgent = requestInfoExtractor.getUserAgent(request);

        // Logging détaillé
        log.info("========================================");
        log.info("CRÉATION D'UNE NOUVELLE SALLE");
        log.info("========================================");
        log.info("Adresse IP: {}", ipAddress);
        log.info(" Navigateur: {} {}", browserName, browserVersion);
        log.info("Système d'exploitation: {}", os);
        log.info("Type d'appareil: {}", deviceType);
        log.info(" User-Agent: {}", userAgent);
        log.info("Date/Heure: {}", LocalDateTime.now());
        log.info(" Données salle: code={}, desc={}, contenance={}",
                salleDTO.getCodeSalle(),
                salleDTO.getDescSalle(),
                salleDTO.getContenance());
        log.info("========================================");

        try {
            SalleDTO savedSalle = salleService.save(salleDTO);

            //  Créer la réponse avec les informations de requête
            Map<String, Object> response = new HashMap<>();
            response.put("salle", savedSalle);
            response.put("requestInfo", Map.of(
                    "ipAddress", ipAddress,
                    "browser", browserName + " " + browserVersion,
                    "operatingSystem", os,
                    "deviceType", deviceType,
                    "userAgent", userAgent,
                    "timestamp", LocalDateTime.now()
            ));

            log.info("Salle créée avec succès: {}", savedSalle.getCodeSalle());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error(" Erreur lors de la création de la salle: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère toutes les salles
     */
    @GetMapping
    public ResponseEntity<List<SalleDTO>> getAll(HttpServletRequest request) {

        log.info("Récupération de toutes les salles depuis IP: {}, Navigateur: {}",
                requestInfoExtractor.getClientIpAddress(request),
                requestInfoExtractor.getBrowserName(request));

        try {
            return new ResponseEntity<>(salleService.getAll(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des salles: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Met à jour une salle existante
     */
    @PutMapping("/{codeSalle}")
    public ResponseEntity<?> update(
            @PathVariable String codeSalle,
            @RequestBody SalleDTO salleDTO,
            HttpServletRequest request) {

        log.info("Mise à jour salle {} depuis IP: {}, Navigateur: {}",
                codeSalle,
                requestInfoExtractor.getClientIpAddress(request),
                requestInfoExtractor.getBrowserName(request));

        try {
            return new ResponseEntity<>(salleService.update(codeSalle, salleDTO), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la salle {}: {}", codeSalle, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime une salle
     */
    @DeleteMapping("/{codeSalle}")
    public ResponseEntity<?> delete(@PathVariable String codeSalle, HttpServletRequest request) {

        log.warn("SUPPRESSION salle {} depuis IP: {}, Navigateur: {}",
                codeSalle,
                requestInfoExtractor.getClientIpAddress(request),
                requestInfoExtractor.getBrowserName(request));

        try {
            salleService.delete(codeSalle);

            log.info("Salle {} supprimée avec succès", codeSalle);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la salle {}: {}", codeSalle, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère une salle par son code
     */
    @GetMapping("/{codeSalle}")
    public ResponseEntity<?> show(@PathVariable String codeSalle, HttpServletRequest request) {

        log.info("Consultation salle {} depuis IP: {}",
                codeSalle,
                requestInfoExtractor.getClientIpAddress(request));

        try {
            return new ResponseEntity<>(salleService.getById(codeSalle), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la salle {}: {}", codeSalle, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}