namespace ShushineStudio.Mobile.Domain.Entities;

/// <summary>
/// Modelo de dominio que representa un elemento de notificación en el Centro de Notificaciones.
/// </summary>
public class NotificacionItem
{
    public string Id { get; set; } = string.Empty;
    public string Titulo { get; set; } = string.Empty;
    public string Mensaje { get; set; } = string.Empty;
    public string Categoria { get; set; } = "Citas"; // Citas, Mensajes, Solicitudes, Servicios
    public DateTime Fecha { get; set; } = DateTime.Now;
    public bool Leida { get; set; } = false;
    public string? RutaDestino { get; set; }
    public string? ParametroDestino { get; set; }
    public string Icono { get; set; } = "notifications";

    public string FechaTexto => Fecha.ToString("dd MMM, h:mm tt");
}
