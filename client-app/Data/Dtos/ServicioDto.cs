using System.Text.Json.Serialization;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Data.Dtos;

public class ServicioDto
{
    [JsonPropertyName("id")]
    public long Id { get; set; }

    [JsonPropertyName("codigoServicio")]
    public string? CodigoServicio { get; set; }

    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("descripcion")]
    public string? Descripcion { get; set; }

    [JsonPropertyName("precioBase")]
    public decimal PrecioBase { get; set; }

    [JsonPropertyName("precio")]
    public decimal? Precio { get; set; }

    [JsonPropertyName("esPrecioVariable")]
    public bool? EsPrecioVariable { get; set; }

    [JsonPropertyName("duracionMinutos")]
    public int DuracionMinutos { get; set; }

    [JsonPropertyName("categoriaId")]
    public long? CategoriaId { get; set; }

    [JsonPropertyName("categoriaNombre")]
    public string? CategoriaNombre { get; set; }

    [JsonPropertyName("imagenUrl")]
    public string? ImagenUrl { get; set; }

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;

    [JsonPropertyName("protocolo")]
    public string? Protocolo { get; set; }

    public decimal PrecioFinal => PrecioBase > 0 ? PrecioBase : (Precio ?? 0);

    public Servicio ToEntity() => new()
    {
        Id = Id,
        CodigoServicio = CodigoServicio,
        Nombre = Nombre,
        Descripcion = Descripcion,
        Precio = PrecioFinal,
        DuracionMinutos = DuracionMinutos,
        CategoriaId = CategoriaId,
        CategoriaNombre = CategoriaNombre ?? "General",
        ImagenUrl = SanitizarUrlPublica(ImagenUrl),
        Activo = Activo,
        Protocolo = Protocolo
    };

    private static string? SanitizarUrlPublica(string? url)
    {
        if (string.IsNullOrWhiteSpace(url)) return null;
        var trimmed = url.Trim();
        if (trimmed.StartsWith("http://", StringComparison.OrdinalIgnoreCase) ||
            trimmed.StartsWith("https://", StringComparison.OrdinalIgnoreCase))
        {
            return trimmed;
        }
        return null;
    }
}

public class ServicioGuardarDto
{
    [JsonPropertyName("codigoServicio")]
    public string? CodigoServicio { get; set; }

    [JsonPropertyName("categoriaId")]
    public int CategoriaId { get; set; } = 1;

    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("descripcion")]
    public string? Descripcion { get; set; }

    [JsonPropertyName("precioBase")]
    public decimal PrecioBase { get; set; }

    [JsonPropertyName("esPrecioVariable")]
    public bool EsPrecioVariable { get; set; } = false;

    [JsonPropertyName("duracionMinutos")]
    public int DuracionMinutos { get; set; } = 45;

    [JsonPropertyName("intervaloSeguimientoDias")]
    public int IntervaloSeguimientoDias { get; set; } = 30;

    [JsonPropertyName("imagenUrl")]
    public string? ImagenUrl { get; set; }

    [JsonPropertyName("costoInsumos")]
    public decimal CostoInsumos { get; set; } = 0;
}

public class ServicioModificarDto : ServicioGuardarDto
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;
}

