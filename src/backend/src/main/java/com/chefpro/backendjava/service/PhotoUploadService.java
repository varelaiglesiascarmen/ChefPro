package com.chefpro.backendjava.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoUploadService {

  /**
   * Convierte el fichero recibido a Base64 y lo guarda en chefs.photo.
   * @return la cadena Base64 con el prefijo data URI para uso directo en <img src="">
   */
  String uploadChefPhoto(MultipartFile file, Authentication authentication);

  /**
   * Convierte el fichero recibido a Base64 y lo guarda en chefs.cover_photo.
   * @return la cadena Base64 con el prefijo data URI para uso directo en <img src="">
   */
  String uploadChefCoverPhoto(MultipartFile file, Authentication authentication);
}
