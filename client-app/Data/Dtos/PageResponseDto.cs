using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Data.Dtos;

/// <summary>
/// DTO genérico para mapear respuestas paginadas de Spring Boot (PageImpl / Pageable).
/// Permite compatibilidad total cuando se consumen endpoints paginados de la Web API.
/// </summary>
/// <typeparam name="T">Tipo del contenido de la página.</typeparam>
public class PageResponseDto<T>
{
    [JsonPropertyName("content")]
    public List<T>? Content { get; set; }

    [JsonPropertyName("totalElements")]
    public long TotalElements { get; set; }

    [JsonPropertyName("totalPages")]
    public int TotalPages { get; set; }

    [JsonPropertyName("size")]
    public int Size { get; set; }

    [JsonPropertyName("number")]
    public int Number { get; set; }

    [JsonPropertyName("first")]
    public bool First { get; set; }

    [JsonPropertyName("last")]
    public bool Last { get; set; }
}
