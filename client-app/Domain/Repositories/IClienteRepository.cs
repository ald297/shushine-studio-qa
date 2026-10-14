using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IClienteRepository
{
    Task<List<Cliente>> ObtenerClientesAsync(int page = 0, int size = 50);
    Task<Cliente?> ObtenerClientePorIdAsync(long id);
    Task<List<Reserva>> ObtenerHistorialCitasClienteAsync(long clienteId);
}
