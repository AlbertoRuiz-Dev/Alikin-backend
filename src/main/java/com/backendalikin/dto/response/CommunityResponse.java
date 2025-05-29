
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
public class CommunityResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private UserBasicResponse leader;
    private int membersCount;
    private boolean isMember; // Si el usuario actual es miembro
    private String userRole; // Rol del usuario actual en la comunidad
    private PlaylistBasicResponse radioPlaylist;
    private String radioStationName;
    private String radioStreamUrl;
    private String radioStationLogoUrl;
}