package com.shushinestudio.controladores;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/archivos")
@Tag(name = "Archivos y Multimedia", description = "Endpoints para subida persistente de imágenes a Supabase Storage")
public class ArchivoController {

    @Value("${supabase.url:https://acikahicfjtojuvqcvxv.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.storage.bucket:shushine-media}")
    private String storageBucket;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Subir imagen adjunta", description = "Sube la imagen a Supabase Storage y retorna su URL pública accesible para vincular a chats, solicitudes o perfiles.")
    public ResponseEntity<Map<String, Object>> subirArchivo(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se proporcionó ningún archivo válido"));
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten archivos de imagen (JPEG, PNG, WEBP)"));
        }

        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            System.err.println("[ArchivoController] Error de configuración: SUPABASE_SERVICE_ROLE_KEY ausente.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", 503,
                    "title", "Service Unavailable",
                    "error", "El servicio de almacenamiento seguro (Supabase Storage) no está configurado en el servidor."
            ));
        }

        try {
            byte[] bytes = archivo.getBytes();
            String originalName = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "imagen.jpg";
            String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String cleanName = UUID.randomUUID() + extension.toLowerCase();

            try {
                String uploadUrl = supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/" + storageBucket + "/" + cleanName;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uploadUrl))
                        .header("Authorization", "Bearer " + serviceRoleKey)
                        .header("apiKey", serviceRoleKey)
                        .header("Content-Type", contentType)
                        .timeout(Duration.ofSeconds(15))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    String publicUrl = supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/public/" + storageBucket + "/" + cleanName;
                    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                            "url", publicUrl,
                            "nombreOriginal", originalName,
                            "tamanoBytes", archivo.getSize(),
                            "contentType", contentType,
                            "storage", "supabase"
                    ));
                } else {
                    System.err.println("[ArchivoController] Supabase Storage respondió con status " + response.statusCode());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                            "status", 502,
                            "error", "Supabase Storage respondió con status " + response.statusCode()
                    ));
                }
            } catch (Exception ex) {
                System.err.println("[ArchivoController] Excepción al contactar Supabase Storage: " + ex.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "status", 503,
                        "error", "Error de comunicación con Supabase Storage: " + ex.getMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo: " + e.getMessage()));
        }
    }
}
