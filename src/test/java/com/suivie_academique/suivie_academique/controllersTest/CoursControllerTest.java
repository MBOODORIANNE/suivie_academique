package com.suivie_academique.suivie_academique.controllersTest;

import com.suivi_academique.controllers.CoursController;
import com.suivi_academique.dto.CoursDTO;
import com.suivi_academique.services.implementations.CoursService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursControllerTest {

    @Mock
    private CoursService coursService;

    @InjectMocks
    private CoursController coursController;

    private CoursDTO coursDTO;
    private List<CoursDTO> coursList;

    @BeforeEach
    void setUp() {
        // Initialisation du premier cours
        coursDTO = new CoursDTO();
        coursDTO.setCodeCours("C001");
        coursDTO.setLabelCours("Programmation Java");
        coursDTO.setNbCreditCours("4");
        coursDTO.setNbHeureCours("60");

        // Initialisation du deuxième cours
        CoursDTO cours2 = new CoursDTO();
        cours2.setCodeCours("C002");
        cours2.setLabelCours("Base de données");
        cours2.setNbCreditCours("3");
        cours2.setNbHeureCours("45");

        coursList = Arrays.asList(coursDTO, cours2);
    }

    // ========== Tests pour la méthode save() ==========

    @Test
    void save_ShouldReturnCreatedCours_WhenValidInput() {
        // Arrange
        when(coursService.save(any(CoursDTO.class))).thenReturn(coursDTO);

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(coursDTO, response.getBody());
        verify(coursService, times(1)).save(any(CoursDTO.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String errorMessage = "Erreur lors de la création du cours";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(coursService, times(1)).save(any(CoursDTO.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenCoursDTOIsNull() {
        // Arrange
        when(coursService.save(null))
                .thenThrow(new RuntimeException("CoursDTO ne peut pas être null"));

        // Act
        ResponseEntity<?> response = coursController.save(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(coursService, times(1)).save(null);
    }

    @Test
    void save_ShouldReturnBadRequest_WhenDuplicateLabelCours() {
        // Arrange
        String errorMessage = "Un cours avec ce label existe déjà";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void save_ShouldValidateCreditAndHours() {
        // Arrange
        coursDTO.setNbCreditCours("0");
        String errorMessage = "Le nombre de crédits doit être positif";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    // ========== Tests pour la méthode getAll() ==========

    @Test
    void getAll_ShouldReturnListOfCours_WhenSuccessful() {
        // Arrange
        when(coursService.getAll()).thenReturn(coursList);

        // Act
        ResponseEntity<List<CoursDTO>> response = coursController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(coursList, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(coursService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoCoursExist() {
        // Arrange
        when(coursService.getAll()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<CoursDTO>> response = coursController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(coursService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnCoursWithDifferentCredits() {
        // Arrange
        when(coursService.getAll()).thenReturn(coursList);

        // Act
        ResponseEntity<List<CoursDTO>> response = coursController.getAll();

        // Assert
        List<CoursDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals("4", result.get(0).getNbCreditCours());
        assertEquals("3", result.get(1).getNbCreditCours());
    }

    @Test
    void getAll_ShouldCallServiceOnce() {
        // Arrange
        when(coursService.getAll()).thenReturn(coursList);

        // Act
        coursController.getAll();

        // Assert
        verify(coursService, times(1)).getAll();
    }

    // ========== Tests pour la méthode update() ==========

    @Test
    void update_ShouldReturnUpdatedCours_WhenValidInput() {
        // Arrange
        String codeCours = "C001";
        CoursDTO updatedCours = new CoursDTO();
        updatedCours.setCodeCours(codeCours);
        updatedCours.setLabelCours("Programmation Java Avancée");
        updatedCours.setNbCreditCours("5");
        updatedCours.setNbHeureCours("75");

        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenReturn(updatedCours);

        // Act
        ResponseEntity<?> response = coursController.update(codeCours, coursDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedCours, response.getBody());
        verify(coursService, times(1)).update(eq(codeCours), any(CoursDTO.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenCoursNotFound() {
        // Arrange
        String codeCours = "C999";
        String errorMessage = "Cours non trouvé";
        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.update(codeCours, coursDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(coursService, times(1)).update(eq(codeCours), any(CoursDTO.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codeCours = "C001";
        String errorMessage = "Erreur lors de la mise à jour";
        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.update(codeCours, coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void update_ShouldHandleNullCodeCours() {
        // Arrange
        String errorMessage = "Code cours invalide";
        when(coursService.update(eq(null), any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.update(null, coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void update_ShouldAllowCreditChange() {
        // Arrange
        String codeCours = "C001";
        coursDTO.setNbCreditCours("6");

        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenReturn(coursDTO);

        // Act
        ResponseEntity<?> response = coursController.update(codeCours, coursDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CoursDTO result = (CoursDTO) response.getBody();
        assertEquals("6", result.getNbCreditCours());
    }

    // ========== Tests pour la méthode delete() ==========

    @Test
    void delete_ShouldReturnOk_WhenCoursIsDeleted() {
        // Arrange
        String codeCours = "C001";
        doNothing().when(coursService).delete(codeCours);

        // Act
        ResponseEntity<?> response = coursController.delete(codeCours);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(coursService, times(1)).delete(codeCours);
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenCoursNotFound() {
        // Arrange
        String codeCours = "C999";
        String errorMessage = "Cours non trouvé";
        doThrow(new RuntimeException(errorMessage))
                .when(coursService).delete(codeCours);

        // Act
        ResponseEntity<?> response = coursController.delete(codeCours);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(coursService, times(1)).delete(codeCours);
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codeCours = "C001";
        String errorMessage = "Erreur lors de la suppression";
        doThrow(new RuntimeException(errorMessage))
                .when(coursService).delete(codeCours);

        // Act
        ResponseEntity<?> response = coursController.delete(codeCours);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void delete_ShouldHandleNullCodeCours() {
        // Arrange
        String errorMessage = "Code cours invalide";
        doThrow(new RuntimeException(errorMessage))
                .when(coursService).delete(null);

        // Act
        ResponseEntity<?> response = coursController.delete(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void delete_ShouldPreventDeletionWhenCoursHasProgrammations() {
        // Arrange
        String codeCours = "C001";
        String errorMessage = "Impossible de supprimer: le cours a des programmations";
        doThrow(new RuntimeException(errorMessage))
                .when(coursService).delete(codeCours);

        // Act
        ResponseEntity<?> response = coursController.delete(codeCours);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    // ========== Tests pour la méthode get() (getById) ==========

    @Test
    void get_ShouldReturnCours_WhenCoursExists() {
        // Arrange
        String codeCours = "C001";
        when(coursService.getById(codeCours)).thenReturn(coursDTO);

        // Act
        ResponseEntity<?> response = coursController.get(codeCours);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(coursDTO, response.getBody());
        verify(coursService, times(1)).getById(codeCours);
    }

    @Test
    void get_ShouldReturnBadRequest_WhenCoursNotFound() {
        // Arrange
        String codeCours = "C999";
        String errorMessage = "Cours non trouvé";
        when(coursService.getById(codeCours))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.get(codeCours);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(coursService, times(1)).getById(codeCours);
    }

    @Test
    void get_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codeCours = "C001";
        String errorMessage = "Erreur lors de la récupération";
        when(coursService.getById(codeCours))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.get(codeCours);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void get_ShouldHandleNullCodeCours() {
        // Arrange
        String errorMessage = "Code cours invalide";
        when(coursService.getById(null))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.get(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void get_ShouldReturnCoursWithCorrectDetails() {
        // Arrange
        String codeCours = "C001";
        coursDTO.setLabelCours("Programmation Java");
        coursDTO.setNbCreditCours("4");
        coursDTO.setNbHeureCours("60");
        when(coursService.getById(codeCours)).thenReturn(coursDTO);

        // Act
        ResponseEntity<?> response = coursController.get(codeCours);

        // Assert
        CoursDTO result = (CoursDTO) response.getBody();
        assertNotNull(result);
        assertEquals("Programmation Java", result.getLabelCours());
        assertEquals("4", result.getNbCreditCours());
        assertEquals("60", result.getNbHeureCours());
    }

    // ========== Tests supplémentaires pour la couverture ==========

    @Test
    void constructor_ShouldInitializeController() {
        // Act
        CoursController controller = new CoursController(coursService);

        // Assert
        assertNotNull(controller);
    }

    @Test
    void save_ShouldHandleEmptyLabel() {
        // Arrange
        coursDTO.setLabelCours("");
        String errorMessage = "Le label du cours est obligatoire";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void save_ShouldHandleNegativeCredits() {
        // Arrange
        coursDTO.setNbCreditCours("-1");
        String errorMessage = "Le nombre de crédits doit être positif";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void getAll_ShouldReturnCoursInOrder() {
        // Arrange
        when(coursService.getAll()).thenReturn(coursList);

        // Act
        ResponseEntity<List<CoursDTO>> response = coursController.getAll();

        // Assert
        List<CoursDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals("C001", result.get(0).getCodeCours());
        assertEquals("C002", result.get(1).getCodeCours());
    }

    // ========== Tests d'intégration des scénarios complets ==========

    @Test
    void completeScenario_CreateUpdateAndDelete() {
        // Scenario: Créer un cours, le mettre à jour, puis le supprimer
        String codeCours = "C001";

        // 1. Création
        when(coursService.save(any(CoursDTO.class))).thenReturn(coursDTO);
        ResponseEntity<?> createResponse = coursController.save(coursDTO);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        // 2. Récupération
        when(coursService.getById(codeCours)).thenReturn(coursDTO);
        ResponseEntity<?> getResponse = coursController.get(codeCours);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // 3. Mise à jour
        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenReturn(coursDTO);
        ResponseEntity<?> updateResponse = coursController.update(codeCours, coursDTO);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // 4. Suppression
        doNothing().when(coursService).delete(codeCours);
        ResponseEntity<?> deleteResponse = coursController.delete(codeCours);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Vérifications
        verify(coursService, times(1)).save(any(CoursDTO.class));
        verify(coursService, times(1)).getById(codeCours);
        verify(coursService, times(1)).update(eq(codeCours), any(CoursDTO.class));
        verify(coursService, times(1)).delete(codeCours);
    }

    @Test
    void scenario_MultipleCoursDifferentCredits() {
        // Arrange
        CoursDTO cours1 = new CoursDTO();
        cours1.setCodeCours("C001");
        cours1.setLabelCours("Cours 1");
        cours1.setNbCreditCours("2");

        CoursDTO cours2 = new CoursDTO();
        cours2.setCodeCours("C002");
        cours2.setLabelCours("Cours 2");
        cours2.setNbCreditCours("4");

        CoursDTO cours3 = new CoursDTO();
        cours3.setCodeCours("C003");
        cours3.setLabelCours("Cours 3");
        cours3.setNbCreditCours("6");

        when(coursService.save(any(CoursDTO.class)))
                .thenReturn(cours1)
                .thenReturn(cours2)
                .thenReturn(cours3);

        // Act
        ResponseEntity<?> response1 = coursController.save(cours1);
        ResponseEntity<?> response2 = coursController.save(cours2);
        ResponseEntity<?> response3 = coursController.save(cours3);

        // Assert
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());
        verify(coursService, times(3)).save(any(CoursDTO.class));
    }

    @Test
    void update_ShouldAllowLabelChange() {
        // Arrange
        String codeCours = "C001";
        coursDTO.setLabelCours("Nouveau Label");

        when(coursService.update(eq(codeCours), any(CoursDTO.class)))
                .thenReturn(coursDTO);

        // Act
        ResponseEntity<?> response = coursController.update(codeCours, coursDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CoursDTO result = (CoursDTO) response.getBody();
        assertEquals("Nouveau Label", result.getLabelCours());
    }

    @Test
    void save_ShouldValidateNbHeureCours() {
        // Arrange
        coursDTO.setNbHeureCours("0");
        String errorMessage = "Le nombre d'heures doit être positif";
        when(coursService.save(any(CoursDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = coursController.save(coursDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}