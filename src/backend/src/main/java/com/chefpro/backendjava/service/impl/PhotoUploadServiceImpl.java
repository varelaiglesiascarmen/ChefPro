package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.service.PhotoUploadService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.NoSuchElementException;

@Component("photoUploadService")
public class PhotoUploadServiceImpl implements PhotoUploadService {

  private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

  private final ChefRepository chefRepository;

  public PhotoUploadServiceImpl(ChefRepository chefRepository) {
    this.chefRepository = chefRepository;
  }

  @Override
  @Transactional
  public String uploadChefPhoto(MultipartFile file, Authentication authentication) {
    Chef chef = getAuthenticatedChef(authentication);
    String base64 = toBase64DataUri(file);
    chef.setPhoto(base64);
    chefRepository.save(chef);
    return base64;
  }

  @Override
  @Transactional
  public String uploadChefCoverPhoto(MultipartFile file, Authentication authentication) {
    Chef chef = getAuthenticatedChef(authentication);
    String base64 = toBase64DataUri(file);
    chef.setCoverPhoto(base64);
    chefRepository.save(chef);
    return base64;
  }

  // -------------------------------------------------------

  private Chef getAuthenticatedChef(Authentication authentication) {
    return chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new NoSuchElementException("Chef not found for user: " + authentication.getName()));
  }

  private String toBase64DataUri(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("File must be an image (jpeg, png, webp...)");
    }

    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new IllegalArgumentException("File exceeds maximum allowed size of 5 MB");
    }

    try {
      byte[] bytes = file.getBytes();
      String base64 = Base64.getEncoder().encodeToString(bytes);
      // Formato data URI que el navegador puede usar directamente en <img src="">
      return "data:" + contentType + ";base64," + base64;
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file bytes", e);
    }
  }
}
