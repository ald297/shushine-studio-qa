using System.Net.Http.Json;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class ProductoRepository : IProductoRepository
{
    private readonly HttpClient _httpClient;

    public ProductoRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<List<Producto>> GetProductosActivosAsync()
    {
        var productos = await _httpClient.GetFromJsonAsync<List<Producto>>("productos/lista");
        return productos ?? new List<Producto>();
    }

    public async Task<List<Producto>> GetProductosAdminAsync()
    {
        var productos = await _httpClient.GetFromJsonAsync<List<Producto>>("admin/productos/lista");
        return productos ?? new List<Producto>();
    }

    public async Task<Producto?> GetProductoPorIdAsync(int id)
    {
        try
        {
            return await _httpClient.GetFromJsonAsync<Producto>($"productos/{id}");
        }
        catch
        {
            return await _httpClient.GetFromJsonAsync<Producto>($"admin/productos/{id}");
        }
    }

    public async Task<Producto> CrearProductoAsync(Producto producto)
    {
        var body = new
        {
            codigoProducto = producto.CodigoProducto,
            idCategoria = producto.IdCategoria > 0 ? producto.IdCategoria : 1,
            nombre = producto.Nombre,
            marca = producto.Marca,
            descripcion = producto.Descripcion,
            precio = producto.Precio,
            stockActual = producto.StockActual,
            stockMinimo = producto.StockMinimo,
            imagenUrl = producto.ImagenUrl,
            activo = producto.Activo
        };

        var response = await _httpClient.PostAsJsonAsync("admin/productos", body);
        response.EnsureSuccessStatusCode();

        var creado = await response.Content.ReadFromJsonAsync<Producto>();
        return creado ?? throw new InvalidOperationException("No se pudo deserializar el producto creado.");
    }

    public async Task<Producto> ModificarProductoAsync(int id, Producto producto)
    {
        var body = new
        {
            codigoProducto = producto.CodigoProducto,
            idCategoria = producto.IdCategoria > 0 ? producto.IdCategoria : 1,
            nombre = producto.Nombre,
            marca = producto.Marca,
            descripcion = producto.Descripcion,
            precio = producto.Precio,
            stockActual = producto.StockActual,
            stockMinimo = producto.StockMinimo,
            imagenUrl = producto.ImagenUrl,
            activo = producto.Activo
        };

        var response = await _httpClient.PutAsJsonAsync($"admin/productos/{id}", body);
        response.EnsureSuccessStatusCode();

        var modificado = await response.Content.ReadFromJsonAsync<Producto>();
        return modificado ?? throw new InvalidOperationException("No se pudo deserializar el producto modificado.");
    }

    public async Task<Producto> ActualizarStockAsync(int id, int nuevoStock)
    {
        var body = new { stock = nuevoStock };
        var response = await _httpClient.PatchAsJsonAsync($"admin/productos/{id}/stock", body);
        response.EnsureSuccessStatusCode();

        var actualizado = await response.Content.ReadFromJsonAsync<Producto>();
        return actualizado ?? throw new InvalidOperationException("No se pudo actualizar el stock del producto.");
    }

    public async Task<bool> CambiarEstadoAsync(int id, bool activo)
    {
        var response = await _httpClient.PutAsync($"admin/productos/{id}/estado?activo={activo}", null);
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> EliminarProductoAsync(int id)
    {
        var response = await _httpClient.DeleteAsync($"admin/productos/{id}");
        return response.IsSuccessStatusCode;
    }
}
