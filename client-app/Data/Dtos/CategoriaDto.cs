using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Data.Dtos;

public class CategoriaDto
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("descripcion")]
    public string? Descripcion { get; set; }

    [JsonPropertyName("iconoUrl")]
    public string? IconoUrl { get; set; }

    [JsonPropertyName("tipo")]
    public string? Tipo { get; set; }

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;
}
