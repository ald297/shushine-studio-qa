using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Data.Dtos;

/// <summary>
/// DTO oficial para PATCH /api/citas/{id}/estado en Spring Boot.
/// </summary>
public class CitaCambiarEstadoRequestDto
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("nuevoEstado")]
    public string NuevoEstado { get; set; } = string.Empty; // 'Confirmed', 'InProgress', 'Completed', 'Cancelled', 'Pending'

    [JsonPropertyName("motivoCancelacion")]
    public string? MotivoCancelacion { get; set; }
}
