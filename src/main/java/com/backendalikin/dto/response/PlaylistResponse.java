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
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private boolean isPublic;
    private UserBasicResponse owner;
    private List<SongBasicResponse> songs;
}