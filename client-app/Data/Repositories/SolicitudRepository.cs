using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class SolicitudRepository : ISolicitudRepository
{
    private readonly HttpClient _httpClient;

    public SolicitudRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<SolicitudDiseno> CrearSolicitudAsync(string notasCliente, string? servicioDeseado, string? imagenesReferenciaUrls)
    {
        var body = new
        {
            notasCliente = notasCliente,
            servicioDeseado = servicioDeseado ?? "Diseño Personalizado",
            imagenesReferenciaUrls = imagenesReferenciaUrls
        };

        var response = await _httpClient.PostAsJsonAsync("solicitudes", body);
        response.EnsureSuccessStatusCode();

        var creada = await response.Content.ReadFromJsonAsync<SolicitudDiseno>();
        return creada ?? throw new InvalidOperationException("No se pudo deserializar la solicitud creada.");
    }

    public async Task<List<SolicitudDiseno>> GetMisSolicitudesAsync()
    {
        var solicitudes = await _httpClient.GetFromJsonAsync<List<SolicitudDiseno>>("solicitudes/mis-solicitudes");
        return solicitudes ?? new List<SolicitudDiseno>();
    }

    public async Task<SolicitudDiseno?> GetSolicitudPorIdClienteAsync(int id)
    {
        return await _httpClient.GetFromJsonAsync<SolicitudDiseno>($"solicitudes/{id}");
    }

    public async Task<List<SolicitudDiseno>> GetSolicitudesAdminAsync()
    {
        var solicitudes = await _httpClient.GetFromJsonAsync<List<SolicitudDiseno>>("admin/solicitudes");
        return solicitudes ?? new List<SolicitudDiseno>();
    }

    public async Task<SolicitudDiseno?> GetSolicitudPorIdAdminAsync(int id)
    {
        return await _httpClient.GetFromJsonAsync<SolicitudDiseno>($"admin/solicitudes/{id}");
    }

    public async Task<SolicitudDiseno> CotizarSolicitudAdminAsync(int idSolicitud, decimal precioPropuesto, string descripcionTrabajo)
    {
        var body = new
        {
            precioPropuesto = precioPropuesto,
            descripcionTrabajo = descripcionTrabajo
        };

        var response = await _httpClient.PostAsJsonAsync($"admin/solicitudes/{idSolicitud}/cotizar", body);
        response.EnsureSuccessStatusCode();

        var actualizada = await response.Content.ReadFromJsonAsync<SolicitudDiseno>();
        return actualizada ?? throw new InvalidOperationException("No se pudo deserializar la respuesta de cotización.");
    }

    public async Task<SolicitudDiseno> ResponderCotizacionAsync(int idSolicitud, bool aceptar)
    {
        var body = new { aceptar = aceptar };
        var response = await _httpClient.PutAsJsonAsync($"solicitudes/{idSolicitud}/responder-cotizacion", body);
        response.EnsureSuccessStatusCode();

        var actualizada = await response.Content.ReadFromJsonAsync<SolicitudDiseno>();
        return actualizada ?? throw new InvalidOperationException("No se pudo deserializar la respuesta de la solicitud.");
    }

    public async Task<string?> SubirImagenAsync(Stream stream, string nombreArchivo)
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
            streamContent.Headers.ContentType = new MediaTypeHeaderValue(mime);
            content.Add(streamContent, "archivo", nombreArchivo);

            var response = await _httpClient.PostAsync("archivos/subir", content);
            if (!response.IsSuccessStatusCode)
            {
                var errContent = await response.Content.ReadAsStringAsync();
                System.Diagnostics.Debug.WriteLine($"[SolicitudRepository] Error al subir imagen (Status {(int)response.StatusCode}): {errContent}");
                return null;
            }

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
            System.Diagnostics.Debug.WriteLine($"[SolicitudRepository] Excepción al subir imagen: {ex.Message}");
            return null;
        }
    }
}
