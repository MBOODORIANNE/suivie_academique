package com.suivie_academique.suivie_academique.servicesTest;

import com.suivi_academique.dto.SalleDTO;
import com.suivi_academique.entities.Salle;
import com.suivi_academique.mappers.SalleMapper;
import com.suivi_academique.repositories.SalleRepository;
import com.suivi_academique.services.implementations.SalleService;
import com.suivi_academique.utils.SalleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalleServiceTest {

    @Mock
    private SalleRepository salleRepository;

    @Mock
    private SalleMapper salleMapper;

    @InjectMocks
    private SalleService salleService;

    private Salle salle;
    private SalleDTO salleDTO;
    private List<Salle> salleList;
    private List<SalleDTO> salleDTOList;

    @BeforeEach
    void setUp() {
        // Initialisation de l'entité Salle
        salle = new Salle();
        salle.setCodeSalle("S001");
        salle.setDescSalle("Salle de cours principale");
        salle.setContenance(50);
        salle.setStatutSalle(SalleStatus.LIBRE);

        // Initialisation du DTO
        salleDTO = new SalleDTO();
        salleDTO.setCodeSalle("S001");
        salleDTO.setDescSalle("Salle de cours principale");
        salleDTO.setContenance(50);
        salleDTO.setStatutSalle(SalleStatus.LIBRE);

        // Initialisation d'une deuxième salle pour les tests de liste
        Salle salle2 = new Salle();
        salle2.setCodeSalle("S002");
        salle2.setDescSalle("Salle de TP");
        salle2.setContenance(30);
        salle2.setStatutSalle(SalleStatus.OCCUPE);

        SalleDTO salleDTO2 = new SalleDTO();
        salleDTO2.setCodeSalle("S002");
        salleDTO2.setDescSalle("Salle de TP");
        salleDTO2.setContenance(30);
        salleDTO2.setStatutSalle(SalleStatus.OCCUPE);

        salleList = Arrays.asList(salle, salle2);
        salleDTOList = Arrays.asList(salleDTO, salleDTO2);
    }

    // ========== Tests pour la méthode save() ==========

    @Test
    void save_ShouldReturnSavedSalleDTO_WhenValidInput() {
        // Arrange
        when(salleMapper.toEntity(any(SalleDTO.class))).thenReturn(salle);
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(salleDTO);

        // Act
        SalleDTO result = salleService.save(salleDTO);

        // Assert
        assertNotNull(result);
        assertEquals(salleDTO.getCodeSalle(), result.getCodeSalle());
        assertEquals(salleDTO.getDescSalle(), result.getDescSalle());
        assertEquals(salleDTO.getContenance(), result.getContenance());
        verify(salleMapper, times(1)).toEntity(any(SalleDTO.class));
        verify(salleRepository, times(1)).save(any(Salle.class));
        verify(salleMapper, times(1)).toDTO(any(Salle.class));
    }

    @Test
    void save_ShouldThrowException_WhenCodeSalleIsEmpty() {
        // Arrange
        salleDTO.setCodeSalle("");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("Données incorret", exception.getMessage());
        verify(salleRepository, never()).save(any(Salle.class));
    }

    @Test
    void save_ShouldThrowException_WhenContenanceIsLessThan10() {
        // Arrange
        salleDTO.setContenance(5);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("Données incorret", exception.getMessage());
        verify(salleRepository, never()).save(any(Salle.class));
    }

    @Test
    void save_ShouldThrowException_WhenContenanceIsExactly9() {
        // Arrange
        salleDTO.setContenance(9);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("Données incorret", exception.getMessage());
    }

    @Test
    void save_ShouldSaveSuccessfully_WhenContenanceIsExactly10() {
        // Arrange
        salleDTO.setContenance(10);
        salle.setContenance(10);

        when(salleMapper.toEntity(any(SalleDTO.class))).thenReturn(salle);
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(salleDTO);

        // Act
        SalleDTO result = salleService.save(salleDTO);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getContenance());
        verify(salleRepository, times(1)).save(any(Salle.class));
    }

    @Test
    void save_ShouldThrowException_WhenCodeSalleIsNull() {
        // Arrange
        salleDTO.setCodeSalle(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> salleService.save(salleDTO));
        verify(salleRepository, never()).save(any(Salle.class));
    }

    // ========== Tests pour la méthode getAll() ==========

    @Test
    void getAll_ShouldReturnListOfSalleDTOs_WhenSallesExist() {
        // Arrange
        when(salleRepository.findAll()).thenReturn(salleList);
        when(salleMapper.toDTO(salleList.get(0))).thenReturn(salleDTOList.get(0));
        when(salleMapper.toDTO(salleList.get(1))).thenReturn(salleDTOList.get(1));

        // Act
        List<SalleDTO> result = salleService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("S001", result.get(0).getCodeSalle());
        assertEquals("S002", result.get(1).getCodeSalle());
        verify(salleRepository, times(1)).findAll();
        verify(salleMapper, times(2)).toDTO(any(Salle.class));
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoSallesExist() {
        // Arrange
        when(salleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<SalleDTO> result = salleService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(salleRepository, times(1)).findAll();
        verify(salleMapper, never()).toDTO(any(Salle.class));
    }

    @Test
    void getAll_ShouldHandleMapperCorrectly() {
        // Arrange
        when(salleRepository.findAll()).thenReturn(salleList);
        when(salleMapper.toDTO(any(Salle.class)))
                .thenReturn(salleDTOList.get(0))
                .thenReturn(salleDTOList.get(1));

        // Act
        List<SalleDTO> result = salleService.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(salleMapper, times(salleList.size())).toDTO(any(Salle.class));
    }

    // ========== Tests pour la méthode getById() ==========

    @Test
    void getById_ShouldReturnSalleDTO_WhenSalleExists() {
        // Arrange
        String codeSalle = "S001";
        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleMapper.toDTO(salle)).thenReturn(salleDTO);

        // Act
        SalleDTO result = salleService.getById(codeSalle);

        // Assert
        assertNotNull(result);
        assertEquals(codeSalle, result.getCodeSalle());
        assertEquals("Salle de cours principale", result.getDescSalle());
        verify(salleRepository, times(1)).findById(codeSalle);
        verify(salleMapper, times(1)).toDTO(salle);
    }

    @Test
    void getById_ShouldThrowException_WhenSalleNotFound() {
        // Arrange
        String codeSalle = "S999";
        when(salleRepository.findById(codeSalle)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> salleService.getById(codeSalle));
        verify(salleRepository, times(1)).findById(codeSalle);
        verify(salleMapper, never()).toDTO(any(Salle.class));
    }

    @Test
    void getById_ShouldCallRepositoryOnce() {
        // Arrange
        String codeSalle = "S001";
        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleMapper.toDTO(salle)).thenReturn(salleDTO);

        // Act
        salleService.getById(codeSalle);

        // Assert
        verify(salleRepository, times(1)).findById(codeSalle);
    }

    // ========== Tests pour la méthode update() ==========

    @Test
    void update_ShouldReturnUpdatedSalleDTO_WhenSalleExists() {
        // Arrange
        String codeSalle = "S001";
        SalleDTO updateDTO = new SalleDTO();
        updateDTO.setContenance(60);
        updateDTO.setDescSalle("Salle rénovée");
        updateDTO.setStatutSalle(SalleStatus.OCCUPE);

        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(updateDTO);

        // Act
        SalleDTO result = salleService.update(codeSalle, updateDTO);

        // Assert
        assertNotNull(result);
        verify(salleRepository, times(1)).findById(codeSalle);
        verify(salleRepository, times(1)).save(salle);
        verify(salleMapper, times(1)).toDTO(salle);
    }

    @Test
    void update_ShouldUpdateAllFields() {
        // Arrange
        String codeSalle = "S001";
        SalleDTO updateDTO = new SalleDTO();
        updateDTO.setContenance(75);
        updateDTO.setDescSalle("Nouvelle description");
        updateDTO.setStatutSalle(SalleStatus.LIBRE);

        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(updateDTO);

        // Act
        salleService.update(codeSalle, updateDTO);

        // Assert
        assertEquals(75, salle.getContenance());
        assertEquals("Nouvelle description", salle.getDescSalle());
        // ✅ CORRECTION: Vérifie LIBRE au lieu de FERMER
        assertEquals(SalleStatus.LIBRE, salle.getStatutSalle());
    }

    @Test
    void update_ShouldThrowException_WhenSalleNotFound() {
        // Arrange
        String codeSalle = "S999";
        when(salleRepository.findById(codeSalle)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class,
                () -> salleService.update(codeSalle, salleDTO));

        verify(salleRepository, times(1)).findById(codeSalle);
        verify(salleRepository, never()).save(any(Salle.class));
    }

    @Test
    void update_ShouldNotChangeCodeSalle() {
        // Arrange
        String codeSalle = "S001";
        SalleDTO updateDTO = new SalleDTO();
        updateDTO.setCodeSalle("S999"); // Tentative de changer le code
        updateDTO.setContenance(60);
        updateDTO.setDescSalle("Nouvelle salle");
        updateDTO.setStatutSalle(SalleStatus.LIBRE);

        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(salleDTO);

        // Act
        salleService.update(codeSalle, updateDTO);

        // Assert
        assertEquals("S001", salle.getCodeSalle()); // Le code ne doit pas changer
    }

    @Test
    void update_ShouldUpdateStatutSalle() {
        // Arrange
        String codeSalle = "S001";
        SalleDTO updateDTO = new SalleDTO();
        updateDTO.setContenance(50);
        updateDTO.setDescSalle("Salle de cours");
        updateDTO.setStatutSalle(SalleStatus.OCCUPE);

        when(salleRepository.findById(codeSalle)).thenReturn(Optional.of(salle));
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(updateDTO);

        // Act
        salleService.update(codeSalle, updateDTO);

        // Assert
        assertEquals(SalleStatus.OCCUPE, salle.getStatutSalle());
        verify(salleRepository, times(1)).save(salle);
    }

    // ========== Tests pour la méthode delete() ==========

    @Test
    void delete_ShouldDeleteSalle_WhenSalleExists() {
        // Arrange
        String codeSalle = "S001";
        when(salleRepository.existsById(codeSalle)).thenReturn(true);
        doNothing().when(salleRepository).deleteById(codeSalle);

        // Act
        salleService.delete(codeSalle);

        // Assert
        verify(salleRepository, times(1)).existsById(codeSalle);
        verify(salleRepository, times(1)).deleteById(codeSalle);
    }

    @Test
    void delete_ShouldThrowException_WhenSalleDoesNotExist() {
        // Arrange
        String codeSalle = "S999";
        when(salleRepository.existsById(codeSalle)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.delete(codeSalle));

        assertEquals("Salle inexistante", exception.getMessage());
        verify(salleRepository, times(1)).existsById(codeSalle);
        verify(salleRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldCallExistsBeforeDelete() {
        // Arrange
        String codeSalle = "S001";
        when(salleRepository.existsById(codeSalle)).thenReturn(true);

        // Act
        salleService.delete(codeSalle);

        // Assert
        verify(salleRepository, times(1)).existsById(codeSalle);
        verify(salleRepository, times(1)).deleteById(codeSalle);
    }

    @Test
    void delete_ShouldNotDeleteIfNotExists() {
        // Arrange
        String codeSalle = "S999";
        when(salleRepository.existsById(codeSalle)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> salleService.delete(codeSalle));
        verify(salleRepository, never()).deleteById(codeSalle);
    }

    // ========== Tests pour la méthode findSallesOccupe() ==========

    @Test
    void findSallesOccupe_ShouldReturnNull() {
        // Act
        SalleDTO result = salleService.findSallesOccupe(SalleStatus.OCCUPE);

        // Assert
        assertNull(result);
    }

    @Test
    void findSallesOccupe_ShouldReturnNull_ForAnyStatus() {
        // Act
        SalleDTO resultLibre = salleService.findSallesOccupe(SalleStatus.LIBRE);
        SalleDTO resultOccupee = salleService.findSallesOccupe(SalleStatus.OCCUPE);
        SalleDTO resultFermer = salleService.findSallesOccupe(SalleStatus.FERMER);

        // Assert
        assertNull(resultLibre);
        assertNull(resultOccupee);
        assertNull(resultFermer);
    }

    // ========== Tests des getters et setters (pour couverture) ==========

    @Test
    void testGettersAndSetters() {
        // Test des getters
        assertNotNull(salleService.getSalleRepository());
        assertNotNull(salleService.getSalleMapper());

        // Test des setters
        SalleRepository newRepository = mock(SalleRepository.class);
        SalleMapper newMapper = mock(SalleMapper.class);

        salleService.setSalleRepository(newRepository);
        salleService.setSalleMapper(newMapper);

        assertEquals(newRepository, salleService.getSalleRepository());
        assertEquals(newMapper, salleService.getSalleMapper());
    }

    // ========== Tests d'intégration des scénarios complets ==========

    @Test
    void completeScenario_CreateUpdateAndDelete() {
        // Scenario: Créer une salle, la mettre à jour, puis la supprimer

        // 1. Création
        when(salleMapper.toEntity(any(SalleDTO.class))).thenReturn(salle);
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(salleDTO);

        SalleDTO created = salleService.save(salleDTO);
        assertNotNull(created);

        // 2. Mise à jour
        when(salleRepository.findById("S001")).thenReturn(Optional.of(salle));
        SalleDTO updated = salleService.update("S001", salleDTO);
        assertNotNull(updated);

        // 3. Suppression
        when(salleRepository.existsById("S001")).thenReturn(true);
        assertDoesNotThrow(() -> salleService.delete("S001"));

        // Vérifications
        verify(salleRepository, times(2)).save(any(Salle.class)); // save + update
        verify(salleRepository, times(1)).deleteById("S001");
    }

    @Test
    void save_ShouldValidateAllRequiredFields() {
        // Test avec contenance limite basse
        salleDTO.setContenance(10);
        when(salleMapper.toEntity(any(SalleDTO.class))).thenReturn(salle);
        when(salleRepository.save(any(Salle.class))).thenReturn(salle);
        when(salleMapper.toDTO(any(Salle.class))).thenReturn(salleDTO);

        assertDoesNotThrow(() -> salleService.save(salleDTO));

        // Test avec contenance invalide
        salleDTO.setContenance(9);
        assertThrows(RuntimeException.class, () -> salleService.save(salleDTO));
    }
}