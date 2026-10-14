using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Domain.Entities;

/// <summary>
/// Entidad de Dominio que modela una Reseña / Calificación en Shushine Studio.
/// Alineada al esquema relacional de la tabla 'public.resenas'.
/// </summary>
public class Resena
{
    [JsonPropertyName("id")]
    public long Id { get; set; }

    [JsonPropertyName("idCita")]
    public long CitaId { get; set; }

    [JsonPropertyName("codigoCita")]
    public string CodigoCita { get; set; } = string.Empty;

    [JsonPropertyName("idCliente")]
    public long ClienteId { get; set; }

    [JsonPropertyName("nombreCliente")]
    public string ClienteNombre { get; set; } = string.Empty;

    [JsonPropertyName("idEstilista")]
    public long EstilistaId { get; set; }

    [JsonPropertyName("nombreEstilista")]
    public string EstilistaNombre { get; set; } = string.Empty;

    [JsonPropertyName("servicioNombre")]
    public string ServicioNombre { get; set; } = string.Empty;

    [JsonPropertyName("estrellas")]
    public int Estrellas { get; set; } = 5;

    [JsonPropertyName("comentario")]
    public string Comentario { get; set; } = string.Empty;

    [JsonPropertyName("visiblePublica")]
    public bool VisiblePublica { get; set; } = true;

    [JsonPropertyName("fechaEmision")]
    public DateTime FechaEmision { get; set; } = DateTime.Now;

    public string EstrellasTexto => new string('★', Math.Clamp(Estrellas, 1, 5)) +
                                   new string('☆', 5 - Math.Clamp(Estrellas, 1, 5));

    public string FechaFormateada => FechaEmision.ToString("dd MMM yyyy");

    public string NivelTexto => Estrellas switch
    {
        1 => "Por Mejorar",
        2 => "Oportunidad de Mejora",
        3 => "Buena Experiencia",
        4 => "Gran Experiencia",
        5 => "Experiencia Excepcional",
        _ => "Sin Calificar"
    };
}
