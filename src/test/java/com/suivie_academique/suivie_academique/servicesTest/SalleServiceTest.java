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

import java.util.*;

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
        salle = new Salle();
        salle.setCodeSalle("S001");
        salle.setDescSalle("Salle de cours principale");
        salle.setContenance(50);
        salle.setStatutSalle(SalleStatus.LIBRE);

        salleDTO = new SalleDTO();
        salleDTO.setCodeSalle("S001");
        salleDTO.setDescSalle("Salle de cours principale");
        salleDTO.setContenance(50);
        salleDTO.setStatutSalle(SalleStatus.LIBRE);

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

    // ================= save() =================

    @Test
    void save_ShouldReturnSavedSalleDTO_WhenValidInput() {
        when(salleMapper.toEntity(any())).thenReturn(salle);
        when(salleRepository.save(any())).thenReturn(salle);
        when(salleMapper.toDTO(any())).thenReturn(salleDTO);

        SalleDTO result = salleService.save(salleDTO);

        assertNotNull(result);
        assertEquals("S001", result.getCodeSalle());
        verify(salleRepository).save(any());
    }

    @Test
    void save_ShouldThrowException_WhenCodeSalleIsEmpty() {
        salleDTO.setCodeSalle("");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("Le code salle est obligatoire", exception.getMessage());
        verify(salleRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowException_WhenCodeSalleIsNull() {
        salleDTO.setCodeSalle(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("Le code salle est obligatoire", exception.getMessage());
    }

    @Test
    void save_ShouldThrowException_WhenContenanceLessThan10() {
        salleDTO.setContenance(9);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.save(salleDTO));

        assertEquals("La contenance minimale est 10", exception.getMessage());
    }

    @Test
    void save_ShouldPass_WhenContenanceIsExactly10() {
        salleDTO.setContenance(10);
        salle.setContenance(10);

        when(salleMapper.toEntity(any())).thenReturn(salle);
        when(salleRepository.save(any())).thenReturn(salle);
        when(salleMapper.toDTO(any())).thenReturn(salleDTO);

        assertDoesNotThrow(() -> salleService.save(salleDTO));
    }

    // ================= getAll() =================

    @Test
    void getAll_ShouldReturnList_WhenExists() {
        when(salleRepository.findAll()).thenReturn(salleList);
        when(salleMapper.toDTO(any())).thenReturn(salleDTOList.get(0), salleDTOList.get(1));

        List<SalleDTO> result = salleService.getAll();

        assertEquals(2, result.size());
        verify(salleMapper, times(2)).toDTO(any());
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNone() {
        when(salleRepository.findAll()).thenReturn(Collections.emptyList());

        List<SalleDTO> result = salleService.getAll();

        assertTrue(result.isEmpty());
    }

    // ================= getById() =================

    @Test
    void getById_ShouldReturnSalleDTO_WhenExists() {
        when(salleRepository.findById("S001")).thenReturn(Optional.of(salle));
        when(salleMapper.toDTO(salle)).thenReturn(salleDTO);

        SalleDTO result = salleService.getById("S001");

        assertNotNull(result);
        assertEquals("S001", result.getCodeSalle());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        when(salleRepository.findById("S999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.getById("S999"));

        assertEquals("Salle inexistante", exception.getMessage());
    }

    // ================= update() =================

    @Test
    void update_ShouldUpdateSalle_WhenExists() {
        SalleDTO updateDTO = new SalleDTO();
        updateDTO.setDescSalle("Salle rénovée");
        updateDTO.setContenance(60);
        updateDTO.setStatutSalle(SalleStatus.OCCUPE);

        when(salleRepository.findById("S001")).thenReturn(Optional.of(salle));
        when(salleRepository.save(any())).thenReturn(salle);
        when(salleMapper.toDTO(any())).thenReturn(updateDTO);

        SalleDTO result = salleService.update("S001", updateDTO);

        assertEquals(60, salle.getContenance());
        assertEquals(SalleStatus.OCCUPE, salle.getStatutSalle());
        assertNotNull(result);
    }

    @Test
    void update_ShouldThrowException_WhenSalleNotFound() {
        when(salleRepository.findById("S999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.update("S999", salleDTO));

        assertEquals("Salle inexistante", exception.getMessage());
    }

    // ================= delete() =================

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(salleRepository.existsById("S001")).thenReturn(true);

        assertDoesNotThrow(() -> salleService.delete("S001"));
        verify(salleRepository).deleteById("S001");
    }

    @Test
    void delete_ShouldThrowException_WhenNotExists() {
        when(salleRepository.existsById("S999")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salleService.delete("S999"));

        assertEquals("Salle inexistante", exception.getMessage());
    }

    // ================= findSallesOccupe() =================

    @Test
    void findSallesOccupe_ShouldReturnNull() {
        assertNull(salleService.findSallesOccupe(SalleStatus.OCCUPE));
    }
}
