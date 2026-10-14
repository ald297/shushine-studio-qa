using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IProductoRepository
{
    Task<List<Producto>> GetProductosActivosAsync();
    Task<List<Producto>> GetProductosAdminAsync();
    Task<Producto?> GetProductoPorIdAsync(int id);
    Task<Producto> CrearProductoAsync(Producto producto);
    Task<Producto> ModificarProductoAsync(int id, Producto producto);
    Task<Producto> ActualizarStockAsync(int id, int nuevoStock);
    Task<bool> CambiarEstadoAsync(int id, bool activo);
    Task<bool> EliminarProductoAsync(int id);
}
