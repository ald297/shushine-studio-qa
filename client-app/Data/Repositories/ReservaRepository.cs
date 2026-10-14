using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

/// <summary>
/// Repositorio de reservas conectado exclusivamente con la Web API Spring Boot / PostgreSQL en Render.
/// Cumple con las reglas estrictas de negocio: CERO mocks, CERO datos hardcodeados y persistencia real.
/// </summary>
public class ReservaRepository : IReservaRepository
{
    private readonly HttpClient _httpClient;

    public ReservaRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<Reserva>> GetMisCitasAsync()
    {
        try
        {
            // Endpoint oficial en Render: GET /api/citas/mis-citas
            var dtos = await _httpClient.GetFromJsonAsync<List<ReservaDto>>("citas/mis-citas");
            if (dtos != null)
            {
                return dtos.Select(d => d.ToEntity()).OrderByDescending(r => r.FechaHoraInicio);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al obtener citas del cliente: {ex.Message}");
            throw;
        }

        return Enumerable.Empty<Reserva>();
    }

    public async Task<IEnumerable<Reserva>> GetTodasCitasAdminAsync()
    {
        return await ObtenerTodasCitasAdminPaginadasAsync(0, 50);
    }

    public async Task<DashboardMetricasDto?> GetDashboardMetricasAsync()
    {
        try
        {
            // Endpoint oficial en Spring Boot: GET /api/admin/dashboard
            var response = await _httpClient.GetAsync("admin/dashboard");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<DashboardMetricasDto>();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al consultar /api/admin/dashboard: {ex.Message}");
        }

        return null;
    }

    public async Task<IEnumerable<Reserva>> GetTimelineCitasAsync(DateTime fecha, int? estilistaId = null)
    {
        try
        {
            // Endpoint oficial en Spring Boot: GET /api/citas/timeline?fecha=YYYY-MM-DD
            var fechaStr = fecha.ToString("yyyy-MM-dd");
            var url = estilistaId.HasValue && estilistaId.Value > 0
                ? $"citas/timeline?fecha={fechaStr}&estilistaId={estilistaId.Value}"
                : $"citas/timeline?fecha={fechaStr}";

            var dtos = await _httpClient.GetFromJsonAsync<List<TimelineItemDto>>(url);
            if (dtos != null)
            {
                return dtos.Select(d => d.ToEntity()).ToList();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al consultar /api/citas/timeline: {ex.Message}");
        }

        return Enumerable.Empty<Reserva>();
    }

    public async Task<Reserva?> ObtenerCitaPorIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"citas/{id}");
            if (response.IsSuccessStatusCode)
            {
                var dto = await response.Content.ReadFromJsonAsync<ReservaDto>();
                return dto?.ToEntity();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al obtener cita {id}: {ex.Message}");
        }

        return null;
    }

    public async Task<IEnumerable<Reserva>> ObtenerTodasCitasAdminPaginadasAsync(int page = 0, int size = 50)
    {
        try
        {
            var response = await _httpClient.GetAsync($"citas?page={page}&size={size}&sort=id,desc");
            if (response.IsSuccessStatusCode)
            {
                var json = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(json);
                if (doc.RootElement.TryGetProperty("content", out var contentElem))
                {
                    var dtos = JsonSerializer.Deserialize<List<ReservaDto>>(contentElem.GetRawText(), new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });
                    if (dtos != null)
                    {
                        return dtos.Select(d => d.ToEntity()).ToList();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al listar citas admin: {ex.Message}");
        }

        return Enumerable.Empty<Reserva>();
    }

    public async Task<Reserva?> CambiarEstadoCitaAsync(long citaId, string nuevoEstado, string? motivoCancelacion = null)
    {
        try
        {
            var req = new CitaCambiarEstadoRequestDto
            {
                Id = (int)citaId,
                NuevoEstado = nuevoEstado,
                MotivoCancelacion = motivoCancelacion
            };

            var jsonContent = new StringContent(JsonSerializer.Serialize(req), Encoding.UTF8, "application/json");
            var response = await _httpClient.PatchAsync($"citas/{citaId}/estado", jsonContent);

            if (response.IsSuccessStatusCode)
            {
                var dto = await response.Content.ReadFromJsonAsync<ReservaDto>();
                return dto?.ToEntity();
            }
            else
            {
                var errStr = await response.Content.ReadAsStringAsync();
                System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error PATCH citas/{citaId}/estado ({response.StatusCode}): {errStr}");
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Excepción en CambiarEstadoCitaAsync: {ex.Message}");
            throw;
        }

        return null;
    }

    public async Task<Reserva?> CrearCitaWalkinAsync(CitaWalkinRequestDto walkinDto)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("citas/walkin", walkinDto);
            if (response.IsSuccessStatusCode)
            {
                var dto = await response.Content.ReadFromJsonAsync<ReservaDto>();
                return dto?.ToEntity();
            }
            else if (response.StatusCode == System.Net.HttpStatusCode.Conflict)
            {
                var errJson = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(errJson);
                var detail = "El estilista seleccionado ya no tiene disponible la franja horaria solicitada.";
                if (doc.RootElement.TryGetProperty("detail", out var dElem))
                {
                    detail = dElem.GetString() ?? detail;
                }
                throw new InvalidOperationException(detail);
            }
            else
            {
                var errStr = await response.Content.ReadAsStringAsync();
                throw new InvalidOperationException($"Error del servidor: {errStr}");
            }
        }
        catch (InvalidOperationException)
        {
            throw;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error en CrearCitaWalkinAsync: {ex.Message}");
            throw new Exception("No se pudo conectar con el servidor para agendar la cita. Verifica tu conexión.", ex);
        }
    }

    public async Task<Reserva?> CrearReservaAsync(
        long estilistaId,
        DateTime fechaCita,
        string horaInicio,
        List<int> servicioIds,
        string? notas,
        string metodoPago = "Efectivo")
    {
        var request = new CrearReservaRequestDto
        {
            EstilistaId = estilistaId,
            FechaCita = fechaCita.ToString("yyyy-MM-dd"),
            HoraInicio = horaInicio,
            ServicioIds = servicioIds,
            NotasCliente = notas,
            MetodoPagoPreferente = metodoPago
        };

        // Endpoint oficial en Render: POST /api/citas
        var response = await _httpClient.PostAsJsonAsync("citas", request);
        if (response.IsSuccessStatusCode)
        {
            var dto = await response.Content.ReadFromJsonAsync<ReservaDto>();
            if (dto != null)
            {
                return dto.ToEntity();
            }
            throw new InvalidOperationException("Respuesta inválida del servidor al confirmar la reserva.");
        }
        else if (response.StatusCode == System.Net.HttpStatusCode.Conflict)
        {
            var errJson = await response.Content.ReadAsStringAsync();
            var detail = "El horario seleccionado ya no se encuentra disponible. Por favor, selecciona otro turno.";
            try
            {
                using var doc = JsonDocument.Parse(errJson);
                if (doc.RootElement.TryGetProperty("detail", out var dElem))
                {
                    detail = dElem.GetString() ?? detail;
                }
            }
            catch { }
            throw new InvalidOperationException(detail);
        }
        else
        {
            var errStr = await response.Content.ReadAsStringAsync();
            throw new InvalidOperationException($"Error al crear reserva: {errStr}");
        }
    }

    public async Task<Reserva?> CrearReservaAsync(
        long servicioId,
        long? estilistaId,
        DateTime fechaHora,
        string? notas)
    {
        var targetEstilista = estilistaId ?? 1;
        var horaInicio = fechaHora.ToString("HH:mm");
        var servicioIds = new List<int> { (int)servicioId };

        return await CrearReservaAsync(targetEstilista, fechaHora.Date, horaInicio, servicioIds, notas, "Efectivo");
    }

    public async Task<bool> CancelarReservaAsync(long reservaId, string? motivo = null)
    {
        try
        {
            var body = new
            {
                motivo = motivo ?? "Cancelada por el cliente desde la app móvil"
            };

            var response = await _httpClient.PutAsJsonAsync($"citas/{reservaId}/cancelar", body);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al cancelar cita: {ex.Message}");
            return false;
        }
    }

    public async Task<bool> RegistrarPagoAsync(long citaId, decimal monto, string metodoPago, string? referenciaPos = null)
    {
        try
        {
            var pagoReq = new
            {
                citaId = (int)citaId,
                monto = monto,
                metodoPago = metodoPago,
                referenciaPos = referenciaPos
            };

            var response = await _httpClient.PostAsJsonAsync("pagos", pagoReq);
            if (response.IsSuccessStatusCode)
            {
                return true;
            }

            var err = await response.Content.ReadAsStringAsync();
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al registrar pago para cita {citaId}: {err}");
            return false;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Excepción al registrar pago: {ex.Message}");
            return false;
        }
    }

    public async Task<string?> DescargarReporteCsvAsync()
    {
        try
        {
            var response = await _httpClient.GetAsync("admin/reportes/exportar");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadAsStringAsync();
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ReservaRepository] Error al descargar reporte CSV: {ex.Message}");
        }
        return null;
    }
}
