package com.suivie_academique.suivie_academique.controllersTest;

import com.suivi_academique.controllers.SalleController;
import com.suivi_academique.dto.SalleDTO;
import com.suivi_academique.services.implementations.SalleService;
import com.suivi_academique.utils.RequestInfoExtractor;
import com.suivi_academique.utils.SalleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SalleController
 * Version corrigée avec mock de HttpServletRequest
 */
@ExtendWith(MockitoExtension.class)
class SalleControllerTest {

    @Mock
    private SalleService salleService;

    @Mock
    private RequestInfoExtractor requestInfoExtractor;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SalleController salleController;

    private SalleDTO salleDTO;

    @BeforeEach
    void setUp() {
        // Initialisation du DTO de test
        salleDTO = new SalleDTO();
        salleDTO.setCodeSalle("S001");
        salleDTO.setDescSalle("Salle de test");
        salleDTO.setContenance(50);
        salleDTO.setStatutSalle(SalleStatus.LIBRE);

        // ✅ Configuration par défaut du mock HttpServletRequest
        when(requestInfoExtractor.getClientIpAddress(any(HttpServletRequest.class)))
                .thenReturn("127.0.0.1");
        when(requestInfoExtractor.getBrowserName(any(HttpServletRequest.class)))
                .thenReturn("Postman");
        when(requestInfoExtractor.getBrowserVersion(any(HttpServletRequest.class)))
                .thenReturn("7.51");
        when(requestInfoExtractor.getOperatingSystem(any(HttpServletRequest.class)))
                .thenReturn("Windows 10");
        when(requestInfoExtractor.getDeviceType(any(HttpServletRequest.class)))
                .thenReturn("API Client");
        when(requestInfoExtractor.getUserAgent(any(HttpServletRequest.class)))
                .thenReturn("PostmanRuntime/7.51.0");
    }

    // ==================== TESTS POST /salle ====================

    @Test
    void testSave_Success() {
        // Given
        when(salleService.save(any(SalleDTO.class))).thenReturn(salleDTO);

        // When
        ResponseEntity<?> response = salleController.save(salleDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // Vérifier que la réponse contient "salle" et "requestInfo"
        assertTrue(response.getBody() instanceof Map);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("salle"));
        assertTrue(body.containsKey("requestInfo"));

        verify(salleService, times(1)).save(any(SalleDTO.class));
        verify(requestInfoExtractor, times(1)).getClientIpAddress(any(HttpServletRequest.class));
    }

    @Test
    void testSave_WithInvalidData_ShouldReturnBadRequest() {
        // Given
        when(salleService.save(any(SalleDTO.class)))
                .thenThrow(new RuntimeException("La contenance doit être d'au moins 10 places"));

        // When
        ResponseEntity<?> response = salleController.save(salleDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La contenance doit être d'au moins 10 places", response.getBody());
        verify(salleService, times(1)).save(any(SalleDTO.class));
    }

    @Test
    void testSave_ShouldCaptureRequestInfo() {
        // Given
        when(salleService.save(any(SalleDTO.class))).thenReturn(salleDTO);

        // When
        ResponseEntity<?> response = salleController.save(salleDTO, httpServletRequest);

        // Then
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Map<String, Object> requestInfo = (Map<String, Object>) body.get("requestInfo");

        assertNotNull(requestInfo);
        assertEquals("127.0.0.1", requestInfo.get("ipAddress"));
        assertEquals("Postman 7.51", requestInfo.get("browser"));
        assertEquals("Windows 10", requestInfo.get("operatingSystem"));
        assertEquals("API Client", requestInfo.get("deviceType"));
    }

    // ==================== TESTS GET /salle ====================

    @Test
    void testGetAll_Success() {
        // Given
        List<SalleDTO> salles = Arrays.asList(salleDTO);
        when(salleService.getAll()).thenReturn(salles);

        // When
        ResponseEntity<List<SalleDTO>> response = salleController.getAll(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(salleService, times(1)).getAll();
    }

    @Test
    void testGetAll_EmptyList() {
        // Given
        when(salleService.getAll()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<SalleDTO>> response = salleController.getAll(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== TESTS GET /salle/{codeSalle} ====================

    @Test
    void testShow_Success() {
        // Given
        when(salleService.getById(anyString())).thenReturn(salleDTO);

        // When
        ResponseEntity<?> response = salleController.show("S001", httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(salleService, times(1)).getById("S001");
    }

    @Test
    void testShow_SalleNotFound() {
        // Given
        when(salleService.getById(anyString()))
                .thenThrow(new RuntimeException("Salle non trouvée: S999"));

        // When
        ResponseEntity<?> response = salleController.show("S999", httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Salle non trouvée: S999", response.getBody());
    }

    // ==================== TESTS PUT /salle/{codeSalle} ====================

    @Test
    void testUpdate_Success() {
        // Given
        SalleDTO updatedSalle = new SalleDTO();
        updatedSalle.setCodeSalle("S001");
        updatedSalle.setDescSalle("Salle modifiée");
        updatedSalle.setContenance(100);
        updatedSalle.setStatutSalle(SalleStatus.OCCUPE);

        when(salleService.update(anyString(), any(SalleDTO.class))).thenReturn(updatedSalle);

        // When
        ResponseEntity<?> response = salleController.update("S001", updatedSalle, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(salleService, times(1)).update("S001", updatedSalle);
    }

    @Test
    void testUpdate_SalleNotFound() {
        // Given
        when(salleService.update(anyString(), any(SalleDTO.class)))
                .thenThrow(new RuntimeException("Salle introuvable: S999"));

        // When
        ResponseEntity<?> response = salleController.update("S999", salleDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Salle introuvable: S999", response.getBody());
    }

    @Test
    void testUpdate_InvalidContenance() {
        // Given
        SalleDTO invalidSalle = new SalleDTO();
        invalidSalle.setCodeSalle("S001");
        invalidSalle.setContenance(5); // Moins de 10

        when(salleService.update(anyString(), any(SalleDTO.class)))
                .thenThrow(new RuntimeException("La contenance doit être d'au moins 10 places"));

        // When
        ResponseEntity<?> response = salleController.update("S001", invalidSalle, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== TESTS DELETE /salle/{codeSalle} ====================

    @Test
    void testDelete_Success() {
        // Given
        doNothing().when(salleService).delete(anyString());

        // When
        ResponseEntity<?> response = salleController.delete("S001", httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(salleService, times(1)).delete("S001");
    }

    @Test
    void testDelete_SalleNotFound() {
        // Given
        doThrow(new RuntimeException("Salle inexistante: S999"))
                .when(salleService).delete("S999");

        // When
        ResponseEntity<?> response = salleController.delete("S999", httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Salle inexistante: S999", response.getBody());
    }

    // ==================== TESTS DE LOGGING ====================

    @Test
    void testSave_ShouldLogRequestInfo() {
        // Given
        when(salleService.save(any(SalleDTO.class))).thenReturn(salleDTO);

        // When
        salleController.save(salleDTO, httpServletRequest);

        // Then
        verify(requestInfoExtractor, times(1)).getClientIpAddress(any(HttpServletRequest.class));
        verify(requestInfoExtractor, times(1)).getBrowserName(any(HttpServletRequest.class));
        verify(requestInfoExtractor, times(1)).getOperatingSystem(any(HttpServletRequest.class));
    }

    @Test
    void testUpdate_ShouldLogRequestInfo() {
        // Given
        when(salleService.update(anyString(), any(SalleDTO.class))).thenReturn(salleDTO);

        // When
        salleController.update("S001", salleDTO, httpServletRequest);

        // Then
        verify(requestInfoExtractor, times(1)).getClientIpAddress(any(HttpServletRequest.class));
        verify(requestInfoExtractor, times(1)).getBrowserName(any(HttpServletRequest.class));
    }

    @Test
    void testDelete_ShouldLogRequestInfo() {
        // Given
        doNothing().when(salleService).delete(anyString());

        // When
        salleController.delete("S001", httpServletRequest);

        // Then
        verify(requestInfoExtractor, times(1)).getClientIpAddress(any(HttpServletRequest.class));
        verify(requestInfoExtractor, times(1)).getBrowserName(any(HttpServletRequest.class));
    }

    // ==================== TESTS DE VALIDATION ====================

    @Test
    void testSave_WithNullCodeSalle_ShouldReturnBadRequest() {
        // Given
        SalleDTO invalidSalle = new SalleDTO();
        invalidSalle.setCodeSalle(null);
        invalidSalle.setContenance(50);

        when(salleService.save(any(SalleDTO.class)))
                .thenThrow(new RuntimeException("Le code salle est obligatoire"));

        // When
        ResponseEntity<?> response = salleController.save(invalidSalle, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSave_WithContenanceLessThan10_ShouldReturnBadRequest() {
        // Given
        SalleDTO invalidSalle = new SalleDTO();
        invalidSalle.setCodeSalle("S001");
        invalidSalle.setContenance(5);

        when(salleService.save(any(SalleDTO.class)))
                .thenThrow(new RuntimeException("La contenance doit être d'au moins 10 places (reçu: 5)"));

        // When
        ResponseEntity<?> response = salleController.save(invalidSalle, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("10 places"));
    }
}