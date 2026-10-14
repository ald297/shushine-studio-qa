using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface ISolicitudRepository
{
    Task<SolicitudDiseno> CrearSolicitudAsync(string notasCliente, string? servicioDeseado, string? imagenesReferenciaUrls);
    Task<List<SolicitudDiseno>> GetMisSolicitudesAsync();
    Task<SolicitudDiseno?> GetSolicitudPorIdClienteAsync(int id);
    Task<List<SolicitudDiseno>> GetSolicitudesAdminAsync();
    Task<SolicitudDiseno?> GetSolicitudPorIdAdminAsync(int id);
    Task<SolicitudDiseno> CotizarSolicitudAdminAsync(int idSolicitud, decimal precioPropuesto, string descripcionTrabajo);
    Task<SolicitudDiseno> ResponderCotizacionAsync(int idSolicitud, bool aceptar);
    Task<string?> SubirImagenAsync(Stream stream, string nombreArchivo);
}
