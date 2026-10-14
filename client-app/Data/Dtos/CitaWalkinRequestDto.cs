using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Data.Dtos;

/// <summary>
/// DTO oficial para POST /api/citas/walkin en Spring Boot.
/// </summary>
public class CitaWalkinRequestDto
{
    [JsonPropertyName("nombreCliente")]
    public string NombreCliente { get; set; } = string.Empty;

    [JsonPropertyName("telefonoCliente")]
    public string? TelefonoCliente { get; set; }

    [JsonPropertyName("estilistaId")]
    public int EstilistaId { get; set; }

    [JsonPropertyName("fechaCita")]
    public string FechaCita { get; set; } = string.Empty; // "yyyy-MM-dd"

    [JsonPropertyName("horaInicio")]
    public string HoraInicio { get; set; } = string.Empty; // "HH:mm" o "HH:mm:ss"

    [JsonPropertyName("servicioIds")]
    public List<int> ServicioIds { get; set; } = new();

    [JsonPropertyName("metodoPagoPreferente")]
    public string MetodoPagoPreferente { get; set; } = "Efectivo";

    [JsonPropertyName("notas")]
    public string? Notas { get; set; }
}
