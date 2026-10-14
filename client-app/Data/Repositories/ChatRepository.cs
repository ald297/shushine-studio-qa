using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using ShushineStudio.Mobile.Data.Services;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class ChatRepository : IChatRepository
{
    private readonly HttpClient _httpClient;
    private readonly ITokenStorageService _tokenStorageService;

    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public ChatRepository(
        HttpClient httpClient,
        ITokenStorageService? tokenStorageService = null)
    {
        _httpClient = httpClient;
        _tokenStorageService = tokenStorageService;
    }

    // =========================================================================
    // CLIENTE
    // =========================================================================

    public async Task<ConversacionChat?> ObtenerOCrearConversacionClienteAsync()
    {
        try
        {
            var response = await _httpClient.GetAsync("chat/conversacion");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ConversacionChat>(JsonOptions);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en ObtenerOCrearConversacionClienteAsync: {ex.Message}");
        }
        return null;
    }

    public async Task<List<ChatMessageItem>> ObtenerMensajesClienteAsync(int page = 0, int size = 50)
    {
        try
        {
            var response = await _httpClient.GetAsync($"chat/mensajes?page={page}&size={size}");
            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);

                JsonElement arrayElement;
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = doc.RootElement;
                }
                else if (doc.RootElement.TryGetProperty("content", out var contentElem) && contentElem.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = contentElem;
                }
                else
                {
                    return new List<ChatMessageItem>();
                }

                return JsonSerializer.Deserialize<List<ChatMessageItem>>(arrayElement.GetRawText(), JsonOptions) ?? new List<ChatMessageItem>();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en ObtenerMensajesClienteAsync: {ex.Message}");
        }
        return new List<ChatMessageItem>();
    }

    public async Task<ChatMessageItem?> EnviarMensajeClienteAsync(string contenido, string? imagenUrl = null)
    {
        try
        {
            var payload = new { contenido, imagenUrl };
            var response = await _httpClient.PostAsJsonAsync("chat/mensajes", payload, JsonOptions);
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ChatMessageItem>(JsonOptions);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en EnviarMensajeClienteAsync: {ex.Message}");
        }
        return null;
    }

    public async Task<bool> MarcarMensajesLeidosClienteAsync()
    {
        try
        {
            var response = await _httpClient.PutAsync("chat/mensajes/leidos", null);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en MarcarMensajesLeidosClienteAsync: {ex.Message}");
            return false;
        }
    }

    // =========================================================================
    // ADMINISTRADOR
    // =========================================================================

    public async Task<List<ConversacionChat>> ObtenerConversacionesAdminAsync(int page = 0, int size = 50)
    {
        try
        {
            var response = await _httpClient.GetAsync($"admin/chat/conversaciones?page={page}&size={size}");
            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);

                JsonElement arrayElement;
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = doc.RootElement;
                }
                else if (doc.RootElement.TryGetProperty("content", out var contentElem) && contentElem.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = contentElem;
                }
                else
                {
                    return new List<ConversacionChat>();
                }

                return JsonSerializer.Deserialize<List<ConversacionChat>>(arrayElement.GetRawText(), JsonOptions) ?? new List<ConversacionChat>();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en ObtenerConversacionesAdminAsync: {ex.Message}");
        }
        return new List<ConversacionChat>();
    }

    public async Task<ConversacionChat?> ObtenerConversacionPorIdAdminAsync(int conversacionId)
    {
        try
        {
            var response = await _httpClient.GetAsync($"admin/chat/conversaciones/{conversacionId}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ConversacionChat>(JsonOptions);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en ObtenerConversacionPorIdAdminAsync: {ex.Message}");
        }
        return null;
    }

    public async Task<List<ChatMessageItem>> ObtenerMensajesAdminAsync(int conversacionId, int page = 0, int size = 50)
    {
        try
        {
            var response = await _httpClient.GetAsync($"admin/chat/conversaciones/{conversacionId}/mensajes?page={page}&size={size}");
            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);

                JsonElement arrayElement;
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = doc.RootElement;
                }
                else if (doc.RootElement.TryGetProperty("content", out var contentElem) && contentElem.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = contentElem;
                }
                else
                {
                    return new List<ChatMessageItem>();
                }

                return JsonSerializer.Deserialize<List<ChatMessageItem>>(arrayElement.GetRawText(), JsonOptions) ?? new List<ChatMessageItem>();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en ObtenerMensajesAdminAsync: {ex.Message}");
        }
        return new List<ChatMessageItem>();
    }

    public async Task<ChatMessageItem?> EnviarMensajeAdminAsync(int conversacionId, string contenido, string? imagenUrl = null)
    {
        try
        {
            var payload = new { contenido, imagenUrl };
            var response = await _httpClient.PostAsJsonAsync($"admin/chat/conversaciones/{conversacionId}/mensajes", payload, JsonOptions);
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ChatMessageItem>(JsonOptions);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en EnviarMensajeAdminAsync: {ex.Message}");
        }
        return null;
    }

    public async Task<bool> MarcarMensajesLeidosAdminAsync(int conversacionId)
    {
        try
        {
            var response = await _httpClient.PutAsync($"admin/chat/conversaciones/{conversacionId}/mensajes/leidos", null);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error en MarcarMensajesLeidosAdminAsync: {ex.Message}");
            return false;
        }
    }

    public async Task<string?> SubirImagenChatAsync(Stream stream, string nombreArchivo)
    {
        try
        {
            using var content = new MultipartFormDataContent();
            var streamContent = new StreamContent(stream);
            var extension = Path.GetExtension(nombreArchivo).ToLowerInvariant();
            var mime = extension switch
            {
                ".png" => "image/png",
                ".webp" => "image/webp",
                _ => "image/jpeg"
            };
            streamContent.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(mime);
            content.Add(streamContent, "archivo", nombreArchivo);

            var response = await _httpClient.PostAsync("archivos/subir", content);
            if (!response.IsSuccessStatusCode) return null;

            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);
            if (doc.RootElement.TryGetProperty("url", out var urlProp))
            {
                return urlProp.GetString();
            }
            return null;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ChatRepository] Error al subir imagen de chat: {ex.Message}");
            return null;
        }
    }
}
