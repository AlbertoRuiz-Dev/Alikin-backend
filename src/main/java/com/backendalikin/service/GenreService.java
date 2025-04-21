package com.backendalikin.service;

import com.backendalikin.dto.request.GenreRequest;
import com.backendalikin.dto.response.GenreResponse;
import com.backendalikin.entity.GenreEntity;
import com.backendalikin.mapper.GenreMapper;
import com.backendalikin.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        List<GenreEntity> genres = genreRepository.findAll();
        return genres.stream()
                .map(genreMapper::toGenreResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenreResponse getGenreById(Long id) {
        GenreEntity genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Género no encontrado"));
        return genreMapper.toGenreResponse(genre);
    }

    @Transactional
    public GenreResponse createGenre(GenreRequest genreRequest) {
        if (genreRepository.existsByNameIgnoreCase(genreRequest.getName())) {
            throw new RuntimeException("Ya existe un género con ese nombre");
        }
        
        GenreEntity genre = genreMapper.toEntity(genreRequest);
        GenreEntity savedGenre = genreRepository.save(genre);
        return genreMapper.toGenreResponse(savedGenre);
    }

    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest genreRequest) {
        GenreEntity genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Género no encontrado"));
        
        if (!genre.getName().equalsIgnoreCase(genreRequest.getName()) && 
            genreRepository.existsByNameIgnoreCase(genreRequest.getName())) {
            throw new RuntimeException("Ya existe un género con ese nombre");
        }
        
        genre.setName(genreRequest.getName());
        GenreEntity updatedGenre = genreRepository.save(genre);
        return genreMapper.toGenreResponse(updatedGenre);
    }

    @Transactional
    public void deleteGenre(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new RuntimeException("Género no encontrado");
        }
        genreRepository.deleteById(id);
    }
}