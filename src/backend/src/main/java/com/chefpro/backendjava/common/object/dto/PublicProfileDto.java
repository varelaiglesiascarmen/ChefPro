package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileDto {
    private Long id;
    private String name;
    private String lastname;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String photo;
    private String bio;
    private String location;
    private String languages;
    private Double rating;
    private Long reviewsCount;
    private List<ReviewSummaryDto> reviews;
}
