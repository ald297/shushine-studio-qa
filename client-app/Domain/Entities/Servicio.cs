namespace ShushineStudio.Mobile.Domain.Entities;

public class Servicio
{
    public long Id { get; set; }
    public string? CodigoServicio { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public decimal Precio { get; set; }
    public int DuracionMinutos { get; set; }
    public long? CategoriaId { get; set; }
    public string? CategoriaNombre { get; set; }
    public string? ImagenUrl { get; set; }
    public bool Activo { get; set; } = true;
    public string? Protocolo { get; set; }

    public string DuracionFormateada => $"{DuracionMinutos} min";
    public string PrecioFormateado => $"${Precio:N2}";
    public bool TieneImagen => !string.IsNullOrWhiteSpace(ImagenUrl);
    public bool NoTieneImagen => string.IsNullOrWhiteSpace(ImagenUrl);

    public string EstadoTexto => Activo ? "ACTIVO" : "INACTIVO";
    public string EstadoColor => Activo ? "#2E7D32" : "#78716C";
    public string EstadoBgColor => Activo ? "#E8F5E9" : "#F5F5F4";
}
