using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IResenaRepository
{
    Task<Resena> CrearResenaAsync(long idCita, int estrellas, string comentario, bool visiblePublica = true);
    Task<List<Resena>> GetMisResenasAsync();
    Task<List<Resena>> GetResenasAdminAsync();
    Task<Resena?> GetResenaPorIdAsync(long id);
}
