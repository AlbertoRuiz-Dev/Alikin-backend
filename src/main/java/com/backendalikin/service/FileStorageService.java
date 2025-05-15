package com.backendalikin.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.storage.location:uploads}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(storageLocation);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el servicio de almacenamiento de archivos", e);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        // Normalizar el nombre del archivo
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Verificar si el nombre del archivo es válido
        if (originalFilename.contains("..")) {
            throw new RuntimeException("El nombre del archivo contiene una secuencia de ruta no válida: " + originalFilename);
        }
        
        // Generar un nombre único para evitar colisiones
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Crear directorio si no existe
        Path targetDir = rootLocation.resolve(subdirectory);
        Files.createDirectories(targetDir);
        
        // Copiar archivo al directorio objetivo
        Path targetLocation = targetDir.resolve(uniqueFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return subdirectory + "/" + uniqueFilename;
    }

    public byte[] loadFileAsBytes(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el archivo: " + filePath, e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + filePath, e);
        }
    }
}