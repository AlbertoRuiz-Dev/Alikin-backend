package com.backendalikin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongBasicResponse {
    private Long id;
    private String title;
    private String artist;
    private String coverImageUrl;
    private int duration;
}