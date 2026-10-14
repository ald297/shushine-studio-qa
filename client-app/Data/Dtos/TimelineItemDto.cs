using System.Text.Json.Serialization;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Data.Dtos;

/// <summary>
/// DTO oficial que mapea la respuesta de GET /api/citas/timeline del backend Spring Boot.
/// </summary>
public class TimelineItemDto
{
    [JsonPropertyName("citaId")]
    public int CitaId { get; set; }

    [JsonPropertyName("codigoCita")]
    public string CodigoCita { get; set; } = string.Empty;

    [JsonPropertyName("clienteId")]
    public int? ClienteId { get; set; }

    [JsonPropertyName("clienteNombre")]
    public string? ClienteNombre { get; set; }

    [JsonPropertyName("clienteTelefono")]
    public string? ClienteTelefono { get; set; }

    [JsonPropertyName("estilistaId")]
    public int? EstilistaId { get; set; }

    [JsonPropertyName("estilistaNombre")]
    public string? EstilistaNombre { get; set; }

    [JsonPropertyName("fechaCita")]
    public string? FechaCita { get; set; }

    [JsonPropertyName("horaInicio")]
    public string? HoraInicio { get; set; }

    [JsonPropertyName("horaFin")]
    public string? HoraFin { get; set; }

    [JsonPropertyName("estado")]
    public string Estado { get; set; } = "Confirmed";

    [JsonPropertyName("total")]
    public decimal Total { get; set; }

    [JsonPropertyName("esWalkin")]
    public bool? EsWalkin { get; set; }

    [JsonPropertyName("notas")]
    public string? Notas { get; set; }

    [JsonPropertyName("servicios")]
    public List<string>? Servicios { get; set; }

    public Reserva ToEntity()
    {
        // Limpiar hora si viene con segundos ("09:00:00" -> "09:00")
        var horaIni = HoraInicio ?? string.Empty;
        if (horaIni.Length > 5) horaIni = horaIni[..5];

        var horaF = HoraFin ?? string.Empty;
        if (horaF.Length > 5) horaF = horaF[..5];

        return new Reserva
        {
            Id = CitaId,
            CodigoCita = CodigoCita,
            ClienteId = ClienteId ?? 0,
            ClienteNombre = !string.IsNullOrWhiteSpace(ClienteNombre) ? ClienteNombre : (EsWalkin == true ? "Cliente Presencial" : "Cliente General"),
            ClienteTelefono = ClienteTelefono,
            EstilistaId = EstilistaId ?? 0,
            EstilistaNombre = EstilistaNombre ?? "Estilista Asignada",
            FechaCita = FechaCita ?? DateTime.Today.ToString("yyyy-MM-dd"),
            HoraInicio = horaIni,
            HoraFin = horaF,
            Estado = Estado,
            Total = Total,
            Notas = Notas,
            ServiciosNombres = Servicios ?? new List<string>()
        };
    }
}
