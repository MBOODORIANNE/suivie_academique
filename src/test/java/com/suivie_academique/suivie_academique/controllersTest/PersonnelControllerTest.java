package com.suivie_academique.suivie_academique.controllersTest;

import com.suivi_academique.controllers.PersonnelController;
import com.suivi_academique.dto.PersonnelDTO;
import com.suivi_academique.services.implementations.PersonnelService;
import com.suivi_academique.utils.RolePersonnel;
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
class PersonnelControllerTest {

    @Mock
    private PersonnelService personnelService;

    @InjectMocks
    private PersonnelController personnelController;

    private PersonnelDTO personnelDTO;
    private List<PersonnelDTO> personnelList;

    @BeforeEach
    void setUp() {
        // Initialisation du premier personnel
        personnelDTO = new PersonnelDTO();
        personnelDTO.setCodePersonnel("P001");
        personnelDTO.setNomPersonnel("Jean Dupont");
        personnelDTO.setLoginPersonnel("jean.dupont");
        personnelDTO.setPadPersonnel("password123");
        personnelDTO.setSexePersonnel("M");
        personnelDTO.setPhonePersonnel("690123456");
        personnelDTO.setRolePersonnel(RolePersonnel.ENSEIGNANT);

        // Initialisation du deuxième personnel
        PersonnelDTO personnel2 = new PersonnelDTO();
        personnel2.setCodePersonnel("P002");
        personnel2.setNomPersonnel("Marie Martin");
        personnel2.setLoginPersonnel("marie.martin");
        personnel2.setPadPersonnel("password456");
        personnel2.setSexePersonnel("F");
        personnel2.setPhonePersonnel("691234567");
        personnel2.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        personnelList = Arrays.asList(personnelDTO, personnel2);
    }

    // ========== Tests pour la méthode save() ==========

    @Test
    void save_ShouldReturnCreatedPersonnel_WhenValidInput() {
        // Arrange
        when(personnelService.save(any(PersonnelDTO.class))).thenReturn(personnelDTO);

        // Act
        ResponseEntity<?> response = personnelController.save(personnelDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(personnelDTO, response.getBody());
        verify(personnelService, times(1)).save(any(PersonnelDTO.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String errorMessage = "Erreur lors de la création du personnel";
        when(personnelService.save(any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.save(personnelDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(personnelService, times(1)).save(any(PersonnelDTO.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenPersonnelDTOIsNull() {
        // Arrange
        when(personnelService.save(null))
                .thenThrow(new RuntimeException("PersonnelDTO ne peut pas être null"));

        // Act
        ResponseEntity<?> response = personnelController.save(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(personnelService, times(1)).save(null);
    }

    @Test
    void save_ShouldReturnBadRequest_WhenDuplicateCodePersonnel() {
        // Arrange
        String errorMessage = "Un personnel avec ce code existe déjà";
        when(personnelService.save(any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.save(personnelDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void save_ShouldHandleAllRoles() {
        // Test pour chaque rôle
        for (RolePersonnel role : RolePersonnel.values()) {
            personnelDTO.setRolePersonnel(role);
            when(personnelService.save(any(PersonnelDTO.class))).thenReturn(personnelDTO);

            ResponseEntity<?> response = personnelController.save(personnelDTO);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            PersonnelDTO responseBody = (PersonnelDTO) response.getBody();
            assertNotNull(responseBody);
            assertEquals(role, responseBody.getRolePersonnel());
        }
    }

    // ========== Tests pour la méthode getAll() ==========

    @Test
    void getAll_ShouldReturnListOfPersonnel_WhenSuccessful() {
        // Arrange
        when(personnelService.getAll()).thenReturn(personnelList);

        // Act
        ResponseEntity<List<PersonnelDTO>> response = personnelController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(personnelList, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(personnelService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoPersonnelExist() {
        // Arrange
        when(personnelService.getAll()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<PersonnelDTO>> response = personnelController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(personnelService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnAllPersonnelWithDifferentRoles() {
        // Arrange
        when(personnelService.getAll()).thenReturn(personnelList);

        // Act
        ResponseEntity<List<PersonnelDTO>> response = personnelController.getAll();

        // Assert
        List<PersonnelDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(RolePersonnel.ENSEIGNANT, result.get(0).getRolePersonnel());
        assertEquals(RolePersonnel.RESPONSABLE_ACADEMIQUE, result.get(1).getRolePersonnel());
    }

    @Test
    void getAll_ShouldCallServiceOnce() {
        // Arrange
        when(personnelService.getAll()).thenReturn(personnelList);

        // Act
        personnelController.getAll();

        // Assert
        verify(personnelService, times(1)).getAll();
    }

    // ========== Tests pour la méthode update() ==========

    @Test
    void update_ShouldReturnUpdatedPersonnel_WhenValidInput() {
        // Arrange
        String codePersonnel = "P001";
        PersonnelDTO updatedPersonnel = new PersonnelDTO();
        updatedPersonnel.setCodePersonnel(codePersonnel);
        updatedPersonnel.setNomPersonnel("Jean Dupont Modifié");
        updatedPersonnel.setLoginPersonnel("jean.dupont.new");
        updatedPersonnel.setPadPersonnel("newpassword");
        updatedPersonnel.setSexePersonnel("M");
        updatedPersonnel.setPhonePersonnel("690999999");
        updatedPersonnel.setRolePersonnel(RolePersonnel.RESPONSABLE_PERSONNEL);

        when(personnelService.update(eq(codePersonnel), any(PersonnelDTO.class)))
                .thenReturn(updatedPersonnel);

        // Act
        ResponseEntity<?> response = personnelController.update(codePersonnel, personnelDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedPersonnel, response.getBody());
        verify(personnelService, times(1)).update(eq(codePersonnel), any(PersonnelDTO.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenPersonnelNotFound() {
        // Arrange
        String codePersonnel = "P999";
        String errorMessage = "Personnel non trouvé";
        when(personnelService.update(eq(codePersonnel), any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.update(codePersonnel, personnelDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(personnelService, times(1)).update(eq(codePersonnel), any(PersonnelDTO.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codePersonnel = "P001";
        String errorMessage = "Erreur lors de la mise à jour";
        when(personnelService.update(eq(codePersonnel), any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.update(codePersonnel, personnelDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void update_ShouldHandleNullCodePersonnel() {
        // Arrange
        String errorMessage = "Code personnel invalide";
        when(personnelService.update(eq(null), any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.update(null, personnelDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void update_ShouldAllowRoleChange() {
        // Arrange
        String codePersonnel = "P001";
        personnelDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        when(personnelService.update(eq(codePersonnel), any(PersonnelDTO.class)))
                .thenReturn(personnelDTO);

        // Act
        ResponseEntity<?> response = personnelController.update(codePersonnel, personnelDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PersonnelDTO result = (PersonnelDTO) response.getBody();
        assertEquals(RolePersonnel.RESPONSABLE_ACADEMIQUE, result.getRolePersonnel());
    }

    // ========== Tests pour la méthode delete() ==========

    @Test
    void delete_ShouldReturnOk_WhenPersonnelIsDeleted() {
        // Arrange
        String codePersonnel = "P001";
        doNothing().when(personnelService).delete(codePersonnel);

        // Act
        ResponseEntity<?> response = personnelController.delete(codePersonnel);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(personnelService, times(1)).delete(codePersonnel);
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenPersonnelNotFound() {
        // Arrange
        String codePersonnel = "P999";
        String errorMessage = "Personnel non trouvé";
        doThrow(new RuntimeException(errorMessage))
                .when(personnelService).delete(codePersonnel);

        // Act
        ResponseEntity<?> response = personnelController.delete(codePersonnel);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(personnelService, times(1)).delete(codePersonnel);
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codePersonnel = "P001";
        String errorMessage = "Erreur lors de la suppression";
        doThrow(new RuntimeException(errorMessage))
                .when(personnelService).delete(codePersonnel);

        // Act
        ResponseEntity<?> response = personnelController.delete(codePersonnel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void delete_ShouldHandleNullCodePersonnel() {
        // Arrange
        String errorMessage = "Code personnel invalide";
        doThrow(new RuntimeException(errorMessage))
                .when(personnelService).delete(null);

        // Act
        ResponseEntity<?> response = personnelController.delete(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    // ========== Tests pour la méthode get() (getById) ==========

    @Test
    void get_ShouldReturnPersonnel_WhenPersonnelExists() {
        // Arrange
        String codePersonnel = "P001";
        when(personnelService.getById(codePersonnel)).thenReturn(personnelDTO);

        // Act
        ResponseEntity<?> response = personnelController.get(codePersonnel);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(personnelDTO, response.getBody());
        verify(personnelService, times(1)).getById(codePersonnel);
    }

    @Test
    void get_ShouldReturnBadRequest_WhenPersonnelNotFound() {
        // Arrange
        String codePersonnel = "P999";
        String errorMessage = "Personnel non trouvé";
        when(personnelService.getById(codePersonnel))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.get(codePersonnel);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(personnelService, times(1)).getById(codePersonnel);
    }

    @Test
    void get_ShouldReturnBadRequest_WhenExceptionOccurs() {
        // Arrange
        String codePersonnel = "P001";
        String errorMessage = "Erreur lors de la récupération";
        when(personnelService.getById(codePersonnel))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.get(codePersonnel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void get_ShouldHandleNullCodePersonnel() {
        // Arrange
        String errorMessage = "Code personnel invalide";
        when(personnelService.getById(null))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.get(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void get_ShouldReturnPersonnelWithCorrectRole() {
        // Arrange
        String codePersonnel = "P001";
        personnelDTO.setRolePersonnel(RolePersonnel.ENSEIGNANT);
        when(personnelService.getById(codePersonnel)).thenReturn(personnelDTO);

        // Act
        ResponseEntity<?> response = personnelController.get(codePersonnel);

        // Assert
        PersonnelDTO result = (PersonnelDTO) response.getBody();
        assertNotNull(result);
        assertEquals(RolePersonnel.ENSEIGNANT, result.getRolePersonnel());
    }

    // ========== Tests supplémentaires pour la couverture ==========

    @Test
    void constructor_ShouldInitializeController() {
        // Act
        PersonnelController controller = new PersonnelController(personnelService);

        // Assert
        assertNotNull(controller);
    }

    @Test
    void save_ShouldHandleEmptyFields() {
        // Arrange
        PersonnelDTO emptyPersonnel = new PersonnelDTO();
        emptyPersonnel.setCodePersonnel("P001");
        String errorMessage = "Champs obligatoires manquants";
        when(personnelService.save(any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.save(emptyPersonnel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void getAll_ShouldReturnPersonnelInOrder() {
        // Arrange
        when(personnelService.getAll()).thenReturn(personnelList);

        // Act
        ResponseEntity<List<PersonnelDTO>> response = personnelController.getAll();

        // Assert
        List<PersonnelDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals("P001", result.get(0).getCodePersonnel());
        assertEquals("P002", result.get(1).getCodePersonnel());
    }

    // ========== Tests d'intégration des scénarios complets ==========

    @Test
    void completeScenario_CreateUpdateAndDelete() {
        // Scenario: Créer un personnel, le mettre à jour, puis le supprimer
        String codePersonnel = "P001";

        // 1. Création
        when(personnelService.save(any(PersonnelDTO.class))).thenReturn(personnelDTO);
        ResponseEntity<?> createResponse = personnelController.save(personnelDTO);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        // 2. Récupération
        when(personnelService.getById(codePersonnel)).thenReturn(personnelDTO);
        ResponseEntity<?> getResponse = personnelController.get(codePersonnel);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // 3. Mise à jour
        when(personnelService.update(eq(codePersonnel), any(PersonnelDTO.class)))
                .thenReturn(personnelDTO);
        ResponseEntity<?> updateResponse = personnelController.update(codePersonnel, personnelDTO);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // 4. Suppression
        doNothing().when(personnelService).delete(codePersonnel);
        ResponseEntity<?> deleteResponse = personnelController.delete(codePersonnel);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Vérifications
        verify(personnelService, times(1)).save(any(PersonnelDTO.class));
        verify(personnelService, times(1)).getById(codePersonnel);
        verify(personnelService, times(1)).update(eq(codePersonnel), any(PersonnelDTO.class));
        verify(personnelService, times(1)).delete(codePersonnel);
    }

    @Test
    void scenario_MultiplePersonnelWithDifferentRoles() {
        // Arrange
        PersonnelDTO enseignant = new PersonnelDTO();
        enseignant.setCodePersonnel("P001");
        enseignant.setRolePersonnel(RolePersonnel.ENSEIGNANT);

        PersonnelDTO responsableAcademique = new PersonnelDTO();
        responsableAcademique.setCodePersonnel("P002");
        responsableAcademique.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        PersonnelDTO responsablePersonnel = new PersonnelDTO();
        responsablePersonnel.setCodePersonnel("P003");
        responsablePersonnel.setRolePersonnel(RolePersonnel.RESPONSABLE_PERSONNEL);

        when(personnelService.save(any(PersonnelDTO.class)))
                .thenReturn(enseignant)
                .thenReturn(responsableAcademique)
                .thenReturn(responsablePersonnel);

        // Act
        ResponseEntity<?> response1 = personnelController.save(enseignant);
        ResponseEntity<?> response2 = personnelController.save(responsableAcademique);
        ResponseEntity<?> response3 = personnelController.save(responsablePersonnel);

        // Assert
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());
        verify(personnelService, times(3)).save(any(PersonnelDTO.class));
    }

    @Test
    void save_ShouldValidatePhoneNumberFormat() {
        // Arrange
        personnelDTO.setPhonePersonnel("invalid_phone");
        String errorMessage = "Format de téléphone invalide";
        when(personnelService.save(any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.save(personnelDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void save_ShouldValidateLoginUniqueness() {
        // Arrange
        String errorMessage = "Login déjà utilisé";
        when(personnelService.save(any(PersonnelDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = personnelController.save(personnelDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}