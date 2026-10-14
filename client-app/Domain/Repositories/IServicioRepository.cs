using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IServicioRepository
{
    Task<IEnumerable<Servicio>> GetServiciosAsync(long? categoriaId = null);
    Task<Servicio?> GetServicioByIdAsync(long id);
    Task<IEnumerable<string>> GetCategoriasAsync();
    Task<Servicio?> CrearServicioAsync(Servicio servicio);
    Task<bool> ActualizarServicioAsync(Servicio servicio);
    Task<bool> EliminarServicioAsync(long id);
    Task<string?> SubirImagenAsync(Stream stream, string nombreArchivo);
}

