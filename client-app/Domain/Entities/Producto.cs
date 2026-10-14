using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Domain.Entities;

public class Producto
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("codigoProducto")]
    public string CodigoProducto { get; set; } = string.Empty;

    [JsonPropertyName("idCategoria")]
    public int IdCategoria { get; set; }

    [JsonPropertyName("nombreCategoria")]
    public string NombreCategoria { get; set; } = string.Empty;

    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("marca")]
    public string Marca { get; set; } = string.Empty;

    [JsonPropertyName("descripcion")]
    public string? Descripcion { get; set; }

    [JsonPropertyName("precio")]
    public decimal Precio { get; set; }

    [JsonPropertyName("stockActual")]
    public int StockActual { get; set; }

    [JsonPropertyName("stockMinimo")]
    public int StockMinimo { get; set; } = 5;

    [JsonPropertyName("imagenUrl")]
    public string? ImagenUrl { get; set; }

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;

    [JsonPropertyName("stockBajo")]
    public bool StockBajo { get; set; }
    public bool TieneMarca => !string.IsNullOrWhiteSpace(Marca);
    public bool TieneImagen => !string.IsNullOrWhiteSpace(ImagenUrl);
    public bool NoTieneImagen => !TieneImagen;
    public string ImagenValidaUrl
    {
        get
        {
            if (string.IsNullOrWhiteSpace(ImagenUrl)) return string.Empty;
            if (ImagenUrl.Contains("unsplash.com") && !ImagenUrl.Contains("?"))
            {
                return $"{ImagenUrl}?auto=format&fit=crop&w=400&q=80";
            }
            return ImagenUrl;
        }
    }

    public string PrecioTexto => $"${Precio:F2}";
    public string StockTexto => $"{StockActual} unidades";
    public string EstadoTexto => Activo ? "Activo" : "Inactivo";
    public string StockStatusColor => StockActual <= StockMinimo ? "#E53935" : "#2E7D32";
}
