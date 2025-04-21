package com.backendalikin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String url;
    private String coverImageUrl;
    private int duration;
    private LocalDateTime uploadedAt;
    private UserBasicResponse uploader;
    private List<GenreResponse> genres;
}