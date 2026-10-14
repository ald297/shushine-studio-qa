using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Notifications;

/// <summary>
/// ViewModel para el Detalle de Notificación.
/// Muestra los datos de la notificación recibida e informa su estado de integración.
/// </summary>
public partial class NotificationDetailViewModel : BaseViewModel, IQueryAttributable
{
    [ObservableProperty]
    private NotificacionItem? notificacion;

    [ObservableProperty]
    private string titulo = "Detalle de Notificación";

    [ObservableProperty]
    private string mensaje = "Las notificaciones estarán disponibles cuando el servicio de notificaciones esté conectado.";

    [ObservableProperty]
    private string categoria = "Atelier";

    [ObservableProperty]
    private string fechaTexto = string.Empty;

    public bool BackendDisponible => false;

    public NotificationDetailViewModel()
    {
        Title = "Detalle";
    }

    public void ApplyQueryAttributes(IDictionary<string, object> query)
    {
        if (query.TryGetValue("notificacion", out var obj) && obj is NotificacionItem item)
        {
            Notificacion = item;
            Titulo = item.Titulo;
            Mensaje = item.Mensaje;
            Categoria = item.Categoria;
            FechaTexto = item.FechaTexto;
        }
    }

    [RelayCommand]
    private async Task RegresarAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
