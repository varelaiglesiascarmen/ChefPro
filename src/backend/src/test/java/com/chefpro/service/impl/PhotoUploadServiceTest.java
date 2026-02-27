package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.service.PhotoUploadService;
import com.chefpro.backendjava.service.impl.PhotoUploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhotoUploadServiceTest {

    private PhotoUploadService photoUploadService;

    private ChefRepository chefRepository;
    private Authentication authentication;

    private Chef chef;

    @BeforeEach
    void setUp() {
        chefRepository = mock(ChefRepository.class);
        authentication = mock(Authentication.class);

        photoUploadService = new PhotoUploadServiceImpl(chefRepository);

        chef = mock(Chef.class);
        when(authentication.getName()).thenReturn("mario@example.com");
        when(chefRepository.findByUser_Username("mario@example.com")).thenReturn(Optional.of(chef));
    }

    // Helper que crea un MultipartFile válido
    private MultipartFile validImageFile() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn("fake-image-bytes".getBytes());
        return file;
    }

    // ─── uploadChefPhoto ─────────────────────────────────────────────────────

    @Test
    void uploadChefPhoto_success_savesAndReturnsBase64() throws IOException {
        MultipartFile file = validImageFile();

        String result = photoUploadService.uploadChefPhoto(file, authentication);

        assertNotNull(result);
        assertTrue(result.startsWith("data:image/jpeg;base64,"));
        verify(chef).setPhoto(result);
        verify(chefRepository).save(chef);
    }

    @Test
    void uploadChefPhoto_emptyFile_throwsIllegalArgumentException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> photoUploadService.uploadChefPhoto(file, authentication));
        verify(chefRepository, never()).save(any());
    }

    @Test
    void uploadChefPhoto_chefNotFound_throwsNoSuchElementException() throws IOException {
        MultipartFile file = validImageFile();
        when(chefRepository.findByUser_Username("mario@example.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> photoUploadService.uploadChefPhoto(file, authentication));
        verify(chefRepository, never()).save(any());
    }

    @Test
    void uploadChefPhoto_nonImageFile_throwsIllegalArgumentException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(IllegalArgumentException.class,
            () -> photoUploadService.uploadChefPhoto(file, authentication));
        verify(chefRepository, never()).save(any());
    }

    @Test
    void uploadChefPhoto_fileTooLarge_throwsIllegalArgumentException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6 MB > límite de 5 MB

        assertThrows(IllegalArgumentException.class,
            () -> photoUploadService.uploadChefPhoto(file, authentication));
        verify(chefRepository, never()).save(any());
    }

    // ─── uploadChefCoverPhoto ────────────────────────────────────────────────

    @Test
    void uploadChefCoverPhoto_success_savesAndReturnsBase64() throws IOException {
        MultipartFile file = validImageFile();

        String result = photoUploadService.uploadChefCoverPhoto(file, authentication);

        assertNotNull(result);
        assertTrue(result.startsWith("data:image/jpeg;base64,"));
        verify(chef).setCoverPhoto(result);
        verify(chefRepository).save(chef);
    }

    @Test
    void uploadChefCoverPhoto_emptyFile_throwsIllegalArgumentException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> photoUploadService.uploadChefCoverPhoto(file, authentication));
        verify(chefRepository, never()).save(any());
    }
}
