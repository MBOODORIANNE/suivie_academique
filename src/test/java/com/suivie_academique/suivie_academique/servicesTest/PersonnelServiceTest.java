package com.suivie_academique.suivie_academique.servicesTest;

import com.suivi_academique.dto.PersonnelDTO;
import com.suivi_academique.entities.Personnel;
import com.suivi_academique.mappers.PersonnelMapper;
import com.suivi_academique.repositories.PersonnelRepository;
import com.suivi_academique.services.implementations.PersonnelService;
import com.suivi_academique.utils.CodeGenerator;
import com.suivi_academique.utils.RolePersonnel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonnelServiceTest {

    @Mock
    private PersonnelRepository personnelRepository;

    @Mock
    private PersonnelMapper personnelMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonnelService personnelService;

    private Personnel personnel;
    private PersonnelDTO personnelDTO;
    private List<Personnel> personnelList;
    private List<PersonnelDTO> personnelDTOList;

    @BeforeEach
    void setUp() {
        // Initialisation de l'entité Personnel
        personnel = new Personnel();
        personnel.setCodePersonnel("ENS001");
        personnel.setNomPersonnel("Jean Dupont");
        personnel.setLoginPersonnel("jean.dupont");
        personnel.setPadPersonnel("hashedPassword123");
        personnel.setSexePersonnel("M");
        personnel.setPhonePersonnel("690123456");
        personnel.setRolePersonnel(RolePersonnel.ENSEIGNANT);

        // Initialisation du DTO
        personnelDTO = new PersonnelDTO();
        personnelDTO.setCodePersonnel("ENS001");
        personnelDTO.setNomPersonnel("Jean Dupont");
        personnelDTO.setLoginPersonnel("jean.dupont");
        personnelDTO.setPadPersonnel("password123");
        personnelDTO.setSexePersonnel("M");
        personnelDTO.setPhonePersonnel("690123456");
        personnelDTO.setRolePersonnel(RolePersonnel.ENSEIGNANT);

        // Initialisation d'un deuxième personnel
        Personnel personnel2 = new Personnel();
        personnel2.setCodePersonnel("RA001");
        personnel2.setNomPersonnel("Marie Martin");
        personnel2.setLoginPersonnel("marie.martin");
        personnel2.setPadPersonnel("hashedPassword456");
        personnel2.setSexePersonnel("F");
        personnel2.setPhonePersonnel("691234567");
        personnel2.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        PersonnelDTO personnelDTO2 = new PersonnelDTO();
        personnelDTO2.setCodePersonnel("RA001");
        personnelDTO2.setNomPersonnel("Marie Martin");
        personnelDTO2.setLoginPersonnel("marie.martin");
        personnelDTO2.setPadPersonnel("password456");
        personnelDTO2.setSexePersonnel("F");
        personnelDTO2.setPhonePersonnel("691234567");
        personnelDTO2.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        personnelList = Arrays.asList(personnel, personnel2);
        personnelDTOList = Arrays.asList(personnelDTO, personnelDTO2);
    }

    // ========== Tests pour la méthode save() ==========

    @Test
    void save_ShouldReturnSavedPersonnelDTO_WhenValidInput() {
        // Arrange
        String generatedCode = "ENS001";
        when(codeGenerator.generate(anyString())).thenReturn(generatedCode);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        PersonnelDTO result = personnelService.save(personnelDTO);

        // Assert
        assertNotNull(result);
        assertEquals(personnelDTO.getNomPersonnel(), result.getNomPersonnel());
        assertEquals(personnelDTO.getLoginPersonnel(), result.getLoginPersonnel());
        verify(codeGenerator, times(1)).generate(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(personnelRepository, times(1)).save(any(Personnel.class));
        verify(personnelMapper, times(1)).toDTO(any(Personnel.class));
    }

    @Test
    void save_ShouldGenerateCodeBasedOnRole_WhenRoleIsENSEIGNANT() {
        // Arrange
        personnelDTO.setRolePersonnel(RolePersonnel.ENSEIGNANT);
        when(codeGenerator.generate("ENSEIGNANT")).thenReturn("ENS001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        PersonnelDTO result = personnelService.save(personnelDTO);

        // Assert
        assertNotNull(result);
        verify(codeGenerator, times(1)).generate("ENSEIGNANT");
    }

    @Test
    void save_ShouldGenerateCodeBasedOnRole_WhenRoleIsRESPONSABLE_ACADEMIQUE() {
        // Arrange
        personnelDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);
        when(codeGenerator.generate("RESPONSABLE_ACADEMIQUE")).thenReturn("RA001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        PersonnelDTO result = personnelService.save(personnelDTO);

        // Assert
        verify(codeGenerator, times(1)).generate("RESPONSABLE_ACADEMIQUE");
    }

    @Test
    void save_ShouldGenerateCodeBasedOnRole_WhenRoleIsRESPONSABLE_PERSONNEL() {
        // Arrange
        personnelDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_PERSONNEL);
        when(codeGenerator.generate("RESPONSABLE_PERSONNEL")).thenReturn("RP001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        PersonnelDTO result = personnelService.save(personnelDTO);

        // Assert
        verify(codeGenerator, times(1)).generate("RESPONSABLE_PERSONNEL");
    }

    @Test
    void save_ShouldThrowException_WhenCodeGenerationFails() {
        // Arrange
        when(codeGenerator.generate(anyString())).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> personnelService.save(personnelDTO));

        assertEquals("Impossible de générer un code pour le rôle: ENSEIGNANT", exception.getMessage());
        verify(codeGenerator, times(1)).generate(anyString());
        verify(personnelRepository, never()).save(any(Personnel.class));
    }

    @Test
    void save_ShouldEncodePassword_BeforeSaving() {
        // Arrange
        String plainPassword = "password123";
        String encodedPassword = "hashedPassword123";
        personnelDTO.setPadPersonnel(plainPassword);

        when(codeGenerator.generate(anyString())).thenReturn("ENS001");
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        personnelService.save(personnelDTO);

        // Assert
        verify(passwordEncoder, times(1)).encode(plainPassword);
        verify(personnelRepository, times(1)).save(argThat(p ->
                encodedPassword.equals(p.getPadPersonnel())
        ));
    }

    @Test
    void save_ShouldSetAllFields_WhenSaving() {
        // Arrange
        when(codeGenerator.generate(anyString())).thenReturn("ENS001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        personnelService.save(personnelDTO);

        // Assert
        verify(personnelRepository, times(1)).save(argThat(p ->
                "ENS001".equals(p.getCodePersonnel()) &&
                        "Jean Dupont".equals(p.getNomPersonnel()) &&
                        "jean.dupont".equals(p.getLoginPersonnel()) &&
                        "M".equals(p.getSexePersonnel()) &&
                        "690123456".equals(p.getPhonePersonnel()) &&
                        RolePersonnel.ENSEIGNANT.equals(p.getRolePersonnel())
        ));
    }

    @Test
    void save_ShouldHandleNullRole() {
        // Arrange
        personnelDTO.setRolePersonnel(null);
        when(codeGenerator.generate(anyString())).thenReturn("P001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> personnelService.save(personnelDTO));
    }

    // ========== Tests pour la méthode getAll() ==========

    @Test
    void getAll_ShouldReturnListOfPersonnelDTOs_WhenPersonnelsExist() {
        // Arrange
        when(personnelRepository.findAll()).thenReturn(personnelList);
        when(personnelMapper.toDTO(personnelList.get(0))).thenReturn(personnelDTOList.get(0));
        when(personnelMapper.toDTO(personnelList.get(1))).thenReturn(personnelDTOList.get(1));

        // Act
        List<PersonnelDTO> result = personnelService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ENS001", result.get(0).getCodePersonnel());
        assertEquals("RA001", result.get(1).getCodePersonnel());
        verify(personnelRepository, times(1)).findAll();
        verify(personnelMapper, times(2)).toDTO(any(Personnel.class));
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoPersonnelsExist() {
        // Arrange
        when(personnelRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PersonnelDTO> result = personnelService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(personnelRepository, times(1)).findAll();
        verify(personnelMapper, never()).toDTO(any(Personnel.class));
    }

    @Test
    void getAll_ShouldMapAllPersonnels() {
        // Arrange
        when(personnelRepository.findAll()).thenReturn(personnelList);
        when(personnelMapper.toDTO(any(Personnel.class)))
                .thenReturn(personnelDTOList.get(0))
                .thenReturn(personnelDTOList.get(1));

        // Act
        List<PersonnelDTO> result = personnelService.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(personnelMapper, times(personnelList.size())).toDTO(any(Personnel.class));
    }

    // ========== Tests pour la méthode getById() ==========

    @Test
    void getById_ShouldReturnPersonnelDTO_WhenPersonnelExists() {
        // Arrange
        String codePersonnel = "ENS001";
        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelMapper.toDTO(personnel)).thenReturn(personnelDTO);

        // Act
        PersonnelDTO result = personnelService.getById(codePersonnel);

        // Assert
        assertNotNull(result);
        assertEquals(codePersonnel, result.getCodePersonnel());
        assertEquals("Jean Dupont", result.getNomPersonnel());
        verify(personnelRepository, times(1)).findById(codePersonnel);
        verify(personnelMapper, times(1)).toDTO(personnel);
    }

    @Test
    void getById_ShouldThrowException_WhenPersonnelNotFound() {
        // Arrange
        String codePersonnel = "P999";
        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> personnelService.getById(codePersonnel));
        verify(personnelRepository, times(1)).findById(codePersonnel);
        verify(personnelMapper, never()).toDTO(any(Personnel.class));
    }

    @Test
    void getById_ShouldCallRepositoryOnce() {
        // Arrange
        String codePersonnel = "ENS001";
        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelMapper.toDTO(personnel)).thenReturn(personnelDTO);

        // Act
        personnelService.getById(codePersonnel);

        // Assert
        verify(personnelRepository, times(1)).findById(codePersonnel);
    }

    // ========== Tests pour la méthode update() ==========

    @Test
    void update_ShouldReturnUpdatedPersonnelDTO_WhenPersonnelExists() {
        // Arrange
        String codePersonnel = "ENS001";
        PersonnelDTO updateDTO = new PersonnelDTO();
        updateDTO.setNomPersonnel("Jean Dupont Modifié");
        updateDTO.setLoginPersonnel("jean.dupont.new");
        updateDTO.setPadPersonnel("newPassword");
        updateDTO.setSexePersonnel("M");
        updateDTO.setPhonePersonnel("690999999");
        updateDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(updateDTO);

        // Act
        PersonnelDTO result = personnelService.update(codePersonnel, updateDTO);

        // Assert
        assertNotNull(result);
        verify(personnelRepository, times(1)).findById(codePersonnel);
        verify(personnelRepository, times(1)).save(personnel);
        verify(personnelMapper, times(1)).toDTO(personnel);
    }

    @Test
    void update_ShouldUpdateAllFields() {
        // Arrange
        String codePersonnel = "ENS001";
        PersonnelDTO updateDTO = new PersonnelDTO();
        updateDTO.setNomPersonnel("Nouveau Nom");
        updateDTO.setLoginPersonnel("nouveau.login");
        updateDTO.setPadPersonnel("newPassword");
        updateDTO.setSexePersonnel("F");
        updateDTO.setPhonePersonnel("699999999");
        updateDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_PERSONNEL);

        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(updateDTO);

        // Act
        personnelService.update(codePersonnel, updateDTO);

        // Assert
        assertEquals("Nouveau Nom", personnel.getNomPersonnel());
        assertEquals("nouveau.login", personnel.getLoginPersonnel());
        assertEquals("newPassword", personnel.getPadPersonnel());
        assertEquals("F", personnel.getSexePersonnel());
        assertEquals("699999999", personnel.getPhonePersonnel());
        assertEquals(RolePersonnel.RESPONSABLE_PERSONNEL, personnel.getRolePersonnel());
    }

    @Test
    void update_ShouldThrowException_WhenPersonnelNotFound() {
        // Arrange
        String codePersonnel = "P999";
        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class,
                () -> personnelService.update(codePersonnel, personnelDTO));

        verify(personnelRepository, times(1)).findById(codePersonnel);
        verify(personnelRepository, never()).save(any(Personnel.class));
    }

    @Test
    void update_ShouldNotChangeCodePersonnel() {
        // Arrange
        String codePersonnel = "ENS001";
        PersonnelDTO updateDTO = new PersonnelDTO();
        updateDTO.setCodePersonnel("P999"); // Tentative de changer le code
        updateDTO.setNomPersonnel("Nouveau Nom");
        updateDTO.setLoginPersonnel("login");
        updateDTO.setPadPersonnel("password");
        updateDTO.setSexePersonnel("M");
        updateDTO.setPhonePersonnel("690000000");
        updateDTO.setRolePersonnel(RolePersonnel.ENSEIGNANT);

        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        personnelService.update(codePersonnel, updateDTO);

        // Assert
        assertEquals("ENS001", personnel.getCodePersonnel()); // Le code ne doit pas changer
    }

    @Test
    void update_ShouldAllowRoleChange() {
        // Arrange
        String codePersonnel = "ENS001";
        personnelDTO.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);

        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        personnelService.update(codePersonnel, personnelDTO);

        // Assert
        assertEquals(RolePersonnel.RESPONSABLE_ACADEMIQUE, personnel.getRolePersonnel());
        verify(personnelRepository, times(1)).save(personnel);
    }

    @Test
    void update_ShouldNotEncodePassword() {
        // Arrange
        String codePersonnel = "ENS001";
        String plainPassword = "newPassword";
        personnelDTO.setPadPersonnel(plainPassword);

        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        // Act
        personnelService.update(codePersonnel, personnelDTO);

        // Assert
        // Le mot de passe n'est PAS encodé lors de l'update (potentiel bug)
        assertEquals(plainPassword, personnel.getPadPersonnel());
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ========== Tests pour la méthode delete() ==========

    @Test
    void delete_ShouldDeletePersonnel_WhenPersonnelExists() {
        // Arrange
        String codePersonnel = "ENS001";
        when(personnelRepository.existsById(codePersonnel)).thenReturn(true);
        doNothing().when(personnelRepository).deleteById(codePersonnel);

        // Act
        personnelService.delete(codePersonnel);

        // Assert
        verify(personnelRepository, times(1)).existsById(codePersonnel);
        verify(personnelRepository, times(1)).deleteById(codePersonnel);
    }

    @Test
    void delete_ShouldThrowException_WhenPersonnelDoesNotExist() {
        // Arrange
        String codePersonnel = "P999";
        when(personnelRepository.existsById(codePersonnel)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> personnelService.delete(codePersonnel));

        assertEquals("Personnel inexistant.", exception.getMessage());
        verify(personnelRepository, times(1)).existsById(codePersonnel);
        verify(personnelRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldCallExistsBeforeDelete() {
        // Arrange
        String codePersonnel = "ENS001";
        when(personnelRepository.existsById(codePersonnel)).thenReturn(true);

        // Act
        personnelService.delete(codePersonnel);

        // Assert
        verify(personnelRepository, times(1)).existsById(codePersonnel);
        verify(personnelRepository, times(1)).deleteById(codePersonnel);
    }

    @Test
    void delete_ShouldNotDeleteIfNotExists() {
        // Arrange
        String codePersonnel = "P999";
        when(personnelRepository.existsById(codePersonnel)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> personnelService.delete(codePersonnel));
        verify(personnelRepository, never()).deleteById(codePersonnel);
    }

    // ========== Tests pour la méthode getByRole() ==========

    @Test
    void getByRole_ShouldReturnFirstPersonnelDTO_WhenPersonnelsWithRoleExist() {
        // Arrange
        String roleString = "ENSEIGNANT";
        when(personnelRepository.searchByRole(roleString)).thenReturn(personnelList);
        when(personnelMapper.toDTO(personnelList.get(0))).thenReturn(personnelDTOList.get(0));

        // Act
        PersonnelDTO result = personnelService.getByRole(roleString);

        // Assert
        assertNotNull(result);
        assertEquals("ENS001", result.getCodePersonnel());
        verify(personnelRepository, times(1)).searchByRole(roleString);
        verify(personnelMapper, times(1)).toDTO(personnelList.get(0));
    }

    @Test
    void getByRole_ShouldThrowException_WhenNoPersonnelsWithRoleExist() {
        // Arrange
        String roleString = "ENSEIGNANT";
        when(personnelRepository.searchByRole(roleString)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class,
                () -> personnelService.getByRole(roleString));

        verify(personnelRepository, times(1)).searchByRole(roleString);
    }

    @Test
    void getByRole_ShouldReturnFirstElement_WhenMultiplePersonnelsWithRoleExist() {
        // Arrange
        String roleString = "ENSEIGNANT";
        when(personnelRepository.searchByRole(roleString)).thenReturn(personnelList);
        when(personnelMapper.toDTO(personnelList.get(0))).thenReturn(personnelDTOList.get(0));

        // Act
        PersonnelDTO result = personnelService.getByRole(roleString);

        // Assert
        assertEquals(personnelDTOList.get(0).getCodePersonnel(), result.getCodePersonnel());
    }

    // ========== Tests d'intégration des scénarios complets ==========

    @Test
    void completeScenario_CreateUpdateAndDelete() {
        // Scenario: Créer un personnel, le mettre à jour, puis le supprimer
        String codePersonnel = "ENS001";

        // 1. Création
        when(codeGenerator.generate(anyString())).thenReturn(codePersonnel);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class))).thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class))).thenReturn(personnelDTO);

        PersonnelDTO created = personnelService.save(personnelDTO);
        assertNotNull(created);

        // 2. Mise à jour
        when(personnelRepository.findById(codePersonnel)).thenReturn(Optional.of(personnel));
        PersonnelDTO updated = personnelService.update(codePersonnel, personnelDTO);
        assertNotNull(updated);

        // 3. Suppression
        when(personnelRepository.existsById(codePersonnel)).thenReturn(true);
        assertDoesNotThrow(() -> personnelService.delete(codePersonnel));

        // Vérifications
        verify(personnelRepository, times(2)).save(any(Personnel.class)); // save + update
        verify(personnelRepository, times(1)).deleteById(codePersonnel);
    }

    @Test
    void scenario_CreatePersonnelsWithDifferentRoles() {
        // Arrange
        PersonnelDTO enseignant = new PersonnelDTO();
        enseignant.setRolePersonnel(RolePersonnel.ENSEIGNANT);
        enseignant.setNomPersonnel("Enseignant");
        enseignant.setLoginPersonnel("enseignant");
        enseignant.setPadPersonnel("pass");
        enseignant.setSexePersonnel("M");
        enseignant.setPhonePersonnel("690000000");

        PersonnelDTO responsable = new PersonnelDTO();
        responsable.setRolePersonnel(RolePersonnel.RESPONSABLE_ACADEMIQUE);
        responsable.setNomPersonnel("Responsable");
        responsable.setLoginPersonnel("responsable");
        responsable.setPadPersonnel("pass");
        responsable.setSexePersonnel("F");
        responsable.setPhonePersonnel("691111111");

        when(codeGenerator.generate("ENSEIGNANT")).thenReturn("ENS001");
        when(codeGenerator.generate("RESPONSABLE_ACADEMIQUE")).thenReturn("RA001");
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(personnelRepository.save(any(Personnel.class)))
                .thenReturn(personnel)
                .thenReturn(personnel);
        when(personnelMapper.toDTO(any(Personnel.class)))
                .thenReturn(enseignant)
                .thenReturn(responsable);

        // Act
        PersonnelDTO result1 = personnelService.save(enseignant);
        PersonnelDTO result2 = personnelService.save(responsable);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(codeGenerator, times(1)).generate("ENSEIGNANT");
        verify(codeGenerator, times(1)).generate("RESPONSABLE_ACADEMIQUE");
        verify(personnelRepository, times(2)).save(any(Personnel.class));
    }
}
