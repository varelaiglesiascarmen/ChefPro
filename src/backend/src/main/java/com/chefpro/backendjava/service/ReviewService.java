package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import org.springframework.security.core.Authentication;

public interface ReviewService {

  void createReview(ReviewCReqDto dto, Authentication authentication);
}
