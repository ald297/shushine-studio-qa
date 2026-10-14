using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Domain.Entities;

public class Cotizacion
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("idSolicitud")]
    public int IdSolicitud { get; set; }

    [JsonPropertyName("precioPropuesto")]
    public decimal PrecioPropuesto { get; set; }

    [JsonPropertyName("descripcionTrabajo")]
    public string DescripcionTrabajo { get; set; } = string.Empty;

    [JsonPropertyName("estado")]
    public string Estado { get; set; } = "Propuesta"; // Propuesta, Aceptada, Rechazada

    [JsonPropertyName("fechaCotizacion")]
    public DateTime FechaCotizacion { get; set; } = DateTime.Now;

    [JsonPropertyName("fechaRespuesta")]
    public DateTime? FechaRespuesta { get; set; }

    public string PrecioTexto => $"${PrecioPropuesto:F2}";
}

public class SolicitudDiseno
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("idCliente")]
    public int IdCliente { get; set; }

    [JsonPropertyName("nombreCliente")]
    public string NombreCliente { get; set; } = string.Empty;

    [JsonPropertyName("telefonoCliente")]
    public string TelefonoCliente { get; set; } = string.Empty;

    [JsonPropertyName("servicioDeseado")]
    public string ServicioDeseado { get; set; } = string.Empty;

    [JsonPropertyName("notasCliente")]
    public string NotasCliente { get; set; } = string.Empty;

    [JsonPropertyName("imagenesReferenciaUrls")]
    public string? ImagenesReferenciaUrls { get; set; }

    [JsonPropertyName("estado")]
    public string Estado { get; set; } = "Pendiente"; // Pendiente, Cotizada, Aceptada, Rechazada

    [JsonPropertyName("fechaSolicitud")]
    public DateTime FechaSolicitud { get; set; } = DateTime.Now;

    [JsonPropertyName("cotizacion")]
    public Cotizacion? Cotizacion { get; set; }

    public bool EsCotizacionPropuesta => Cotizacion != null && string.Equals(Cotizacion.Estado, "Propuesta", StringComparison.OrdinalIgnoreCase);
    public bool TieneCotizacion => Cotizacion != null;
    public bool TieneFoto => !string.IsNullOrEmpty(ImagenesReferenciaUrls);
    public string FechaTexto => FechaSolicitud.ToString("dd MMM yyyy · h:mm tt");
    public string EstadoBadgeColor => Estado?.ToLowerInvariant() switch
    {
        "cotizada" => "#1976D2",
        "aceptada" => "#2E7D32",
        "rechazada" => "#C62828",
        _ => "#F57C00"
    };
}
