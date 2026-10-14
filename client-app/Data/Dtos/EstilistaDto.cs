using System.Text.Json.Serialization;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Data.Dtos;

public class EstilistaDto
{
    [JsonPropertyName("id")]
    public long Id { get; set; }

    [JsonPropertyName("nombreCompleto")]
    public string NombreCompleto { get; set; } = string.Empty;

    [JsonPropertyName("especialidadPrincipal")]
    public string EspecialidadPrincipal { get; set; } = string.Empty;

    [JsonPropertyName("biografia")]
    public string? Biografia { get; set; }

    [JsonPropertyName("avatarUrl")]
    public string? AvatarUrl { get; set; }

    [JsonPropertyName("colorAgenda")]
    public string? ColorAgenda { get; set; }

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;

    public Estilista ToEntity() => new()
    {
        Id = Id,
        NombreCompleto = NombreCompleto,
        EspecialidadPrincipal = EspecialidadPrincipal,
        Biografia = Biografia,
        AvatarUrl = AvatarUrl,
        ColorAgenda = ColorAgenda ?? "#E91E63",
        Activo = Activo
    };
}

public class FranjaHorariaDto
{
    [JsonPropertyName("horaInicio")]
    public string HoraInicio { get; set; } = string.Empty;

    [JsonPropertyName("horaFin")]
    public string HoraFin { get; set; } = string.Empty;

    [JsonPropertyName("disponible")]
    public bool Disponible { get; set; }

    [JsonPropertyName("motivoNoDisponible")]
    public string? MotivoNoDisponible { get; set; }

    public FranjaHoraria ToEntity() => new()
    {
        HoraInicio = HoraInicio,
        HoraFin = HoraFin,
        Disponible = Disponible,
        MotivoNoDisponible = MotivoNoDisponible
    };
}

public class DisponibilidadSalidaDto
{
    [JsonPropertyName("estilistaId")]
    public long EstilistaId { get; set; }

    [JsonPropertyName("estilistaNombre")]
    public string EstilistaNombre { get; set; } = string.Empty;

    [JsonPropertyName("fecha")]
    public string Fecha { get; set; } = string.Empty;

    [JsonPropertyName("franjas")]
    public List<FranjaHorariaDto> Franjas { get; set; } = new();

    public DisponibilidadEstilista ToEntity() => new()
    {
        EstilistaId = EstilistaId,
        EstilistaNombre = EstilistaNombre,
        Fecha = Fecha,
        Franjas = Franjas?.Select(f => f.ToEntity()).ToList() ?? new List<FranjaHoraria>()
    };
}
