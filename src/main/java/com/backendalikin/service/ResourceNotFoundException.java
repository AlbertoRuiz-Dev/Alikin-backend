package com.backendalikin.service;

public class ResourceNotFoundException extends RuntimeException { // O extiende Exception si prefieres una excepción verificada

    public ResourceNotFoundException(String message) {
        super(message); // Llama al constructor de la clase padre (RuntimeException) con el mensaje
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause); // Constructor opcional para encadenar excepciones
    }
}
