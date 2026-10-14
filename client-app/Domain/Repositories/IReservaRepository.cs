using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IReservaRepository
{
    Task<IEnumerable<Reserva>> GetMisCitasAsync();
    Task<IEnumerable<Reserva>> GetTodasCitasAdminAsync();
    Task<DashboardMetricasDto?> GetDashboardMetricasAsync();
    Task<IEnumerable<Reserva>> GetTimelineCitasAsync(DateTime fecha, int? estilistaId = null);
    Task<Reserva?> ObtenerCitaPorIdAsync(long id);
    Task<IEnumerable<Reserva>> ObtenerTodasCitasAdminPaginadasAsync(int page = 0, int size = 50);
    Task<Reserva?> CambiarEstadoCitaAsync(long citaId, string nuevoEstado, string? motivoCancelacion = null);
    Task<Reserva?> CrearCitaWalkinAsync(CitaWalkinRequestDto walkinDto);
    Task<Reserva?> CrearReservaAsync(long estilistaId, DateTime fechaCita, string horaInicio, List<int> servicioIds, string? notas, string metodoPago = "Efectivo");
    Task<Reserva?> CrearReservaAsync(long servicioId, long? estilistaId, DateTime fechaHora, string? notas);
    Task<bool> CancelarReservaAsync(long reservaId, string? motivo = null);
    Task<bool> RegistrarPagoAsync(long citaId, decimal monto, string metodoPago, string? referenciaPos = null);
    Task<string?> DescargarReporteCsvAsync();
}
