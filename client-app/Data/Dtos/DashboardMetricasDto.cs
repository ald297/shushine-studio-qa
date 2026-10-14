using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Data.Dtos;

/// <summary>
/// DTO oficial que mapea la respuesta de GET /api/admin/dashboard del backend Spring Boot.
/// </summary>
public class DashboardMetricasDto
{
    [JsonPropertyName("totalCitasHoy")]
    public long TotalCitasHoy { get; set; }

    [JsonPropertyName("totalCitasSemana")]
    public long TotalCitasSemana { get; set; }

    [JsonPropertyName("ingresosHoy")]
    public decimal IngresosHoy { get; set; }

    [JsonPropertyName("ingresosMes")]
    public decimal IngresosMes { get; set; }

    [JsonPropertyName("citasEnProceso")]
    public long CitasEnProceso { get; set; }

    [JsonPropertyName("citasCompletadas")]
    public long CitasCompletadas { get; set; }

    [JsonPropertyName("citasCanceladas")]
    public long CitasCanceladas { get; set; }

    [JsonPropertyName("estilistasActivos")]
    public long EstilistasActivos { get; set; }
}
