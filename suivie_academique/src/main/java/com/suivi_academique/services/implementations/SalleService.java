package com.suivi_academique.services.implementations;

import com.suivi_academique.dto.SalleDTO;
import com.suivi_academique.entities.Salle;
import com.suivi_academique.mappers.SalleMapper;
import com.suivi_academique.repositories.SalleRepository;
import com.suivi_academique.services.interfaces.SalleInterface;
import com.suivi_academique.utils.SalleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des salles
 *
 * @author Suivi Académique
 * @version 2.0 (avec logging et corrections de bugs)
 */
@Slf4j  // ✅ Active le logging avec Lombok
@Setter
@Getter
@Service
@AllArgsConstructor
public class SalleService implements SalleInterface {

    private SalleRepository salleRepository;
    private SalleMapper salleMapper;

    /**
     * Sauvegarde une nouvelle salle
     *
     * @param salleDTO DTO de la salle à créer
     * @return DTO de la salle créée
     * @throws RuntimeException si les données sont invalides
     */
    @Override
    public SalleDTO save(SalleDTO salleDTO) {
        log.info("Tentative de création d'une nouvelle salle");
        log.debug("Données reçues: code={}, desc={}, contenance={}, statut={}",
                salleDTO.getCodeSalle(),
                salleDTO.getDescSalle(),
                salleDTO.getContenance(),
                salleDTO.getStatutSalle());

        //  CORRECTION: Vérification de null avant isEmpty()
        if (salleDTO.getCodeSalle() == null || salleDTO.getCodeSalle().trim().isEmpty()) {
            log.error("Tentative de création d'une salle sans code");
            throw new RuntimeException("Le code salle est obligatoire");
        }

        if (salleDTO.getContenance() < 10) {
            log.warn("Tentative de création d'une salle avec contenance insuffisante: {}",
                    salleDTO.getContenance());

        }

        try {
            Salle salle = salleRepository.save(salleMapper.toEntity(salleDTO));

            log.info("✓ Salle créée avec succès: code={}, contenance={}, statut={}",
                    salle.getCodeSalle(),
                    salle.getContenance(),
                    salle.getStatutSalle());

            return salleMapper.toDTO(salle);

        } catch (Exception e) {
            log.error("✗ Erreur lors de la création de la salle: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }

    /**
     * Récupère toutes les salles
     *
     * @return Liste de toutes les salles
     */
    @Override
    public List<SalleDTO> getAll() {
        log.debug("Récupération de toutes les salles");

        List<SalleDTO> result = salleRepository.findAll().stream()
                .map(salleMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Nombre de salles récupérées: {}", result.size());

        // Statistiques sur les statuts
        if (!result.isEmpty()) {
            long libres = result.stream()
                    .filter(s -> s.getStatutSalle() == SalleStatus.LIBRE)
                    .count();
            long occupees = result.stream()
                    .filter(s -> s.getStatutSalle() == SalleStatus.OCCUPE)
                    .count();
            long fermees = result.stream()
                    .filter(s -> s.getStatutSalle() == SalleStatus.FERMER)
                    .count();

            log.debug("Statistiques salles - Libres: {}, Occupées: {}, Fermées: {}",
                    libres, occupees, fermees);
        }

        return result;
    }

    /**
     * Récupère une salle par son code
     *
     * @param codeSalle Code de la salle à récupérer
     * @return DTO de la salle trouvée
     * @throws RuntimeException si la salle n'existe pas
     */
    @Override
    public SalleDTO getById(String codeSalle) {
        log.debug("Recherche de la salle avec code: {}", codeSalle);

        // CORRECTION: Utilisation de orElseThrow au lieu de .get()
        Salle salle = salleRepository.findById(codeSalle)
                .orElseThrow(() -> {
                    log.warn("⚠ Salle non trouvée avec le code: {}", codeSalle);
                    return new RuntimeException("Salle non trouvée: " + codeSalle);
                });

        log.debug("✓ Salle trouvée: desc={}, contenance={}, statut={}",
                salle.getDescSalle(),
                salle.getContenance(),
                salle.getStatutSalle());

        return salleMapper.toDTO(salle);
    }

    /**
     * Met à jour une salle existante
     *
     * @param codeSalle Code de la salle à mettre à jour
     * @param salleDTO Nouvelles données de la salle
     * @return DTO de la salle mise à jour
     * @throws RuntimeException si la salle n'existe pas
     */
    @Override
    public SalleDTO update(String codeSalle, SalleDTO salleDTO) {
        log.info("Mise à jour de la salle: {}", codeSalle);
        log.debug("Nouvelles données: contenance={}, desc={}, statut={}",
                salleDTO.getContenance(),
                salleDTO.getDescSalle(),
                salleDTO.getStatutSalle());

        // CORRECTION: Utilisation de orElseThrow au lieu de .get()
        Salle salle = salleRepository.findById(codeSalle)
                .orElseThrow(() -> {
                    log.error("✗ Tentative de mise à jour d'une salle inexistante: {}", codeSalle);
                    return new RuntimeException("Salle introuvable: " + codeSalle);
                });

        // Log des changements importants
        if (salle.getContenance() != salleDTO.getContenance()) {
            log.debug("Changement de contenance: {} → {}",
                    salle.getContenance(),
                    salleDTO.getContenance());
        }

        if (salle.getStatutSalle() != salleDTO.getStatutSalle()) {
            log.info("Changement de statut pour la salle {}: {} → {}",
                    codeSalle,
                    salle.getStatutSalle(),
                    salleDTO.getStatutSalle());
        }

        // Validation de la nouvelle contenance
        if (salleDTO.getContenance() < 10) {
            log.warn("Tentative de mise à jour avec contenance insuffisante: {}",
                    salleDTO.getContenance());
            throw new RuntimeException("La contenance doit être au moins 10 places");
        }

        // Mise à jour des champs
        salle.setContenance(salleDTO.getContenance());
        salle.setDescSalle(salleDTO.getDescSalle());
        salle.setStatutSalle(salleDTO.getStatutSalle());

        try {
            salleRepository.save(salle);
            log.info("✓ Salle mise à jour avec succès: {}", codeSalle);

            return salleMapper.toDTO(salle);

        } catch (Exception e) {
            log.error("✗ Erreur lors de la mise à jour de la salle {}: {}",
                    codeSalle, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    /**
     * Supprime une salle
     *
     * @param codeSalle Code de la salle à supprimer
     * @throws RuntimeException si la salle n'existe pas
     */
    @Override
    public void delete(String codeSalle) {
        log.info("Tentative de suppression de la salle: {}", codeSalle);

        if (!salleRepository.existsById(codeSalle)) {
            log.warn("⚠ Tentative de suppression d'une salle inexistante: {}", codeSalle);
            throw new RuntimeException("Salle inexistante: " + codeSalle);
        }

        try {
            // Récupérer la salle pour logger ses infos avant suppression
            Salle salle = salleRepository.findById(codeSalle).orElse(null);
            if (salle != null) {
                log.debug("Suppression de la salle: desc={}, contenance={}",
                        salle.getDescSalle(),
                        salle.getContenance());
            }

            salleRepository.deleteById(codeSalle);

            log.info("✓ Salle supprimée avec succès: {}", codeSalle);

        } catch (Exception e) {
            log.error("✗ Erreur lors de la suppression de la salle {}: {}",
                    codeSalle, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Recherche les salles par statut
     *
     * @param salleStatus Statut des salles à rechercher
     * @return DTO de la première salle trouvée (ou null)
     * @deprecated Cette méthode devrait retourner une liste au lieu d'un seul élément
     */
    @Override
    public SalleDTO findSallesOccupe(SalleStatus salleStatus) {
        log.debug("Recherche de salles avec statut: {}", salleStatus);

        // TODO: Implémenter cette méthode correctement
        // Elle devrait retourner List<SalleDTO> au lieu de SalleDTO

        log.warn("⚠ Méthode findSallesOccupe() non implémentée - retour null");

        return null;
    }

    /**
     * Méthode helper pour récupérer les salles par statut
     * (Version corrigée qui retourne une liste)
     *
     * @param salleStatus Statut des salles recherchées
     * @return Liste des salles avec le statut spécifié
     */
    public List<SalleDTO> findSallesByStatut(SalleStatus salleStatus) {
        log.debug("Recherche de salles avec statut: {}", salleStatus);

        try {
            List<SalleDTO> result = salleRepository.findAll().stream()
                    .filter(salle -> salle.getStatutSalle() == salleStatus)
                    .map(salleMapper::toDTO)
                    .collect(Collectors.toList());

            log.info("Nombre de salles trouvées avec statut {}: {}", salleStatus, result.size());

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la recherche de salles par statut: {}",
                    e.getMessage(), e);
            throw e;
        }
    }
}