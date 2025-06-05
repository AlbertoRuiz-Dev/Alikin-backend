package com.backendalikin.poblate;

import com.backendalikin.entity.*;
import com.backendalikin.model.enums.Role;
import com.backendalikin.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataSeederConfig {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final CommunityRepository communityRepository;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) return;

            // Crear usuarios
            UserEntity rivas = createUser("rivas@alikin.com", "Rivas", "rivas");
            UserEntity jose = createUser("joseluis@alikin.com", "Jose Luis", "joseluis");
            UserEntity ango = createUser("ango@alikin.com", "Ango", "ango");
            UserEntity alberto = createUser("alberto@alikin.com", "Alberto", "alberto");
            rivas.setRole(Role.USER);
            jose.setRole(Role.USER);
            ango.setRole(Role.USER);
            alberto.setRole(Role.USER);

            userRepository.saveAll(List.of(rivas, jose, ango, alberto));

            // Crear géneros
            GenreEntity rock = new GenreEntity(); rock.setName("Rock");
            GenreEntity pop = new GenreEntity(); pop.setName("Pop");
            GenreEntity jazz = new GenreEntity(); jazz.setName("Jazz");
            GenreEntity electronic = new GenreEntity(); electronic.setName("Electrónica");
            genreRepository.saveAll(List.of(rock, pop, jazz, electronic));

            // Crear canciones
            SongEntity song1 = new SongEntity();
            song1.setTitle("Canción Uno");
            song1.setArtist("Artista 1");
            song1.setAlbum("Álbum 1");
            song1.setUrl("songs/song1.mp3");
            song1.setCoverImageUrl("cover1.jpg");
            song1.setDuration(210);
            song1.setUploadedAt(LocalDateTime.now());
            song1.setUploader(rivas);
            song1.setGenres(Set.of(rock));

            SongEntity song2 = new SongEntity();
            song2.setTitle("Melodía Azul");
            song2.setArtist("Artista 2");
            song2.setAlbum("Álbum Azul");
            song2.setUrl("songs/song2.mp3");
            song2.setCoverImageUrl("cover2.jpg");
            song2.setDuration(180);
            song2.setUploadedAt(LocalDateTime.now());
            song2.setUploader(jose);
            song2.setGenres(Set.of(pop));

            SongEntity song3 = new SongEntity();
            song3.setTitle("Jazz en el Alma");
            song3.setArtist("Artista 3");
            song3.setAlbum("Jazz Collection");
            song3.setUrl("songs/song3.mp3");
            song3.setCoverImageUrl("cover3.jpg");
            song3.setDuration(240);
            song3.setUploadedAt(LocalDateTime.now());
            song3.setUploader(ango);
            song3.setGenres(Set.of(jazz));

            SongEntity song4 = new SongEntity();
            song4.setTitle("Beat Drop");
            song4.setArtist("DJ X");
            song4.setAlbum("Electro Vibes");
            song4.setUrl("songs/song4.mp3");
            song4.setCoverImageUrl("cover4.jpg");
            song4.setDuration(200);
            song4.setUploadedAt(LocalDateTime.now());
            song4.setUploader(alberto);
            song4.setGenres(Set.of(electronic));

            SongEntity song5 = new SongEntity();
            song5.setTitle("Rock Total");
            song5.setArtist("Band Z");
            song5.setAlbum("Rocking Now");
            song5.setUrl("songs/song5.mp3");
            song5.setCoverImageUrl("cover5.jpg");
            song5.setDuration(230);
            song5.setUploadedAt(LocalDateTime.now());
            song5.setUploader(rivas);
            song5.setGenres(Set.of(rock));

            songRepository.saveAll(List.of(song1, song2, song3, song4, song5));

            // Crear comunidades
            CommunityEntity general = new CommunityEntity();
            general.setName("General");
            general.setDescription("Comunidad general para todos");
            general.setMembers(new HashSet<>(List.of(rivas, jose)));

            CommunityEntity musica = new CommunityEntity();
            musica.setName("Música");
            musica.setDescription("Comparte tus canciones favoritas");
            musica.setMembers(new HashSet<>(List.of(ango, alberto)));

            communityRepository.saveAll(List.of(general, musica));

            // Crear playlists
            PlaylistEntity lista1 = new PlaylistEntity();
            lista1.setName("Top Canciones");
            lista1.setOwner(rivas);
            lista1.setSongs(List.of(song1, song2));

            PlaylistEntity lista2 = new PlaylistEntity();
            lista2.setName("Jazz Chill");
            lista2.setOwner(jose);
            lista2.setSongs(List.of(song3));

            playlistRepository.saveAll(List.of(lista1, lista2));

            // Crear posts con imágenes aleatorias
            PostEntity post1 = new PostEntity();
            post1.setContent("Me encanta esta canción!");
            post1.setSong(song1);
            post1.setUser(rivas);
            post1.setCreatedAt(LocalDateTime.now());
            post1.setVoteCount(0);
            post1.setImageUrl(downloadRandomImage("rock", "post1.jpg"));

            PostEntity post2 = new PostEntity();
            post2.setContent("¿Alguien más ama el jazz?");
            post2.setSong(song3);
            post2.setUser(jose);
            post2.setCommunity(general);
            post2.setCreatedAt(LocalDateTime.now());
            post2.setVoteCount(0);
            post2.setImageUrl(downloadRandomImage("jazz", "post2.jpg"));

            PostEntity post3 = new PostEntity();
            post3.setContent("Foto del concierto anoche");
            post3.setUser(alberto);
            post3.setCommunity(musica);
            post3.setCreatedAt(LocalDateTime.now());
            post3.setVoteCount(0);
            post3.setImageUrl(downloadRandomImage("concert", "post3.jpg"));

            PostEntity post4 = new PostEntity();
            post4.setContent("Mi playlist favorita");
            post4.setSong(song2);
            post4.setUser(ango);
            post4.setCreatedAt(LocalDateTime.now());
            post4.setVoteCount(0);
            post4.setImageUrl(downloadRandomImage("pop", "post4.jpg"));

            PostEntity post5 = new PostEntity();
            post5.setContent("Rockeando la tarde");
            post5.setSong(song5);
            post5.setUser(rivas);
            post5.setCommunity(general);
            post5.setCreatedAt(LocalDateTime.now());
            post5.setVoteCount(0);
            post5.setImageUrl(downloadRandomImage("rock", "post5.jpg"));

            postRepository.saveAll(List.of(post1, post2, post3, post4, post5));

            // Crear comentarios
            CommentEntity c1 = new CommentEntity();
            c1.setContent("Totalmente de acuerdo!");
            c1.setUser(jose);
            c1.setPost(post1);
            c1.setCreatedAt(LocalDateTime.now());

            CommentEntity c2 = new CommentEntity();
            c2.setContent("¡Esa canción es fuego!");
            c2.setUser(alberto);
            c2.setPost(post1);
            c2.setCreatedAt(LocalDateTime.now());

            CommentEntity c3 = new CommentEntity();
            c3.setContent("Me encanta ese estilo");
            c3.setUser(rivas);
            c3.setPost(post2);
            c3.setCreatedAt(LocalDateTime.now());

            CommentEntity c4 = new CommentEntity();
            c4.setContent("¡Qué buenos recuerdos!");
            c4.setUser(ango);
            c4.setPost(post3);
            c4.setCreatedAt(LocalDateTime.now());

            CommentEntity c5 = new CommentEntity();
            c5.setContent("¡Gracias por compartir!");
            c5.setUser(jose);
            c5.setPost(post5);
            c5.setCreatedAt(LocalDateTime.now());

            commentRepository.saveAll(List.of(c1, c2, c3, c4, c5));
        };
    }

    private UserEntity createUser(String email, String name, String nickname) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password10"));
        user.setName(name);
        user.setNickname(nickname);
        user.setProfilePictureUrl("");
        return user;
    }

    public String downloadRandomImage(String keyword, String filename) {
        try {
            String unsplashAccessKey = "SPdJXWnHDsWcZT6V1O2sMB8iAzK8c2Mrfbu5pGgpY5U"; // Reemplaza con tu clave
            String unsplashUrl = "https://api.unsplash.com/photos/random?query=" + keyword +
                    "&client_id=" + unsplashAccessKey;

            String jsonResponse = restTemplate.getForObject(unsplashUrl, String.class);
            Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
            Map<String, String> urls = (Map<String, String>) response.get("urls");
            String imageUrl = urls.get("regular");

            String filePath = "/app/uploads/" + filename;
            URL url = new URL(imageUrl);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();

            return filename;
        } catch (Exception e) {
            System.err.println("Error downloading image for keyword " + keyword + ": " + e.getMessage());
            return "/uploads/images/default.jpg";
        }

    }
}