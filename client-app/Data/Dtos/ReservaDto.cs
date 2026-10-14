using System.Text.Json.Serialization;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Data.Dtos;

public class ReservaDto
{
    [JsonPropertyName("id")]
    public long Id { get; set; }

    [JsonPropertyName("codigoCita")]
    public string CodigoCita { get; set; } = string.Empty;

    [JsonPropertyName("codigoReserva")]
    public string? CodigoReservaFallback { get; set; }

    [JsonPropertyName("clienteId")]
    public long ClienteId { get; set; }

    [JsonPropertyName("clienteNombre")]
    public string? ClienteNombre { get; set; }

    [JsonPropertyName("clienteTelefono")]
    public string? ClienteTelefono { get; set; }

    [JsonPropertyName("estilistaId")]
    public long EstilistaId { get; set; }

    [JsonPropertyName("estilistaNombre")]
    public string? EstilistaNombre { get; set; }

    [JsonPropertyName("fechaCita")]
    public string FechaCita { get; set; } = string.Empty;

    [JsonPropertyName("horaInicio")]
    public string HoraInicio { get; set; } = string.Empty;

    [JsonPropertyName("horaFin")]
    public string HoraFin { get; set; } = string.Empty;

    [JsonPropertyName("estado")]
    public string Estado { get; set; } = "PENDIENTE";

    [JsonPropertyName("subtotal")]
    public decimal Subtotal { get; set; }

    [JsonPropertyName("iva")]
    public decimal Iva { get; set; }

    [JsonPropertyName("total")]
    public decimal Total { get; set; }

    [JsonPropertyName("metodoPagoPreferente")]
    public string? MetodoPagoPreferente { get; set; }

    [JsonPropertyName("estadoPago")]
    public string? EstadoPago { get; set; }

    [JsonPropertyName("notasCliente")]
    public string? NotasCliente { get; set; }

    [JsonPropertyName("notas")]
    public string? NotasFallback { get; set; }

    [JsonPropertyName("serviciosNombres")]
    public List<string>? ServiciosNombres { get; set; }

    public Reserva ToEntity() => new()
    {
        Id = Id,
        CodigoCita = !string.IsNullOrEmpty(CodigoCita) ? CodigoCita : (CodigoReservaFallback ?? string.Empty),
        ClienteId = ClienteId,
        ClienteNombre = ClienteNombre ?? string.Empty,
        ClienteTelefono = ClienteTelefono,
        EstilistaId = EstilistaId,
        EstilistaNombre = EstilistaNombre ?? string.Empty,
        FechaCita = FechaCita,
        HoraInicio = HoraInicio,
        HoraFin = HoraFin,
        Estado = Estado,
        Subtotal = Subtotal,
        Iva = Iva,
        Total = Total,
        MetodoPagoPreferente = MetodoPagoPreferente ?? "Efectivo",
        EstadoPago = EstadoPago ?? "PENDIENTE",
        Notas = NotasCliente ?? NotasFallback,
        ServiciosNombres = ServiciosNombres ?? new List<string>()
    };
}

public class CrearReservaRequestDto
{
    [JsonPropertyName("estilistaId")]
    public long EstilistaId { get; set; }

    [JsonPropertyName("fechaCita")]
    public string FechaCita { get; set; } = string.Empty; // "yyyy-MM-dd"

    [JsonPropertyName("horaInicio")]
    public string HoraInicio { get; set; } = string.Empty; // "HH:mm"

    [JsonPropertyName("servicioIds")]
    public List<int> ServicioIds { get; set; } = new();

    [JsonPropertyName("notasCliente")]
    public string? NotasCliente { get; set; }

    [JsonPropertyName("metodoPagoPreferente")]
    public string MetodoPagoPreferente { get; set; } = "Efectivo";
}
