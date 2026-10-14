using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Notifications;

/// <summary>
/// ViewModel para el Centro de Notificaciones.
/// Implementa los estados requeridos (Loading, Empty, Error, Backend Unavailable)
/// sin inventar notificaciones simuladas, respetando que el backend aún no cuenta con servicio de avisos.
/// </summary>
public partial class NotificationCenterViewModel : BaseViewModel
{
    [ObservableProperty]
    private ObservableCollection<NotificacionItem> notificaciones = new();

    [ObservableProperty]
    private ObservableCollection<NotificacionItem> notificacionesFiltradas = new();

    [ObservableProperty]
    private string categoriaSeleccionada = "Todas";

    [ObservableProperty]
    private int conteoNoLeidas = 0;

    [ObservableProperty]
    private bool tieneNotificaciones;

    [ObservableProperty]
    private bool estaVacio = true;

    // El backend no cuenta con sistema de notificaciones en la API actual
    public bool BackendDisponible => false;

    public NotificationCenterViewModel()
    {
        Title = "Notificaciones";
    }

    [RelayCommand]
    public async Task CargarNotificacionesAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            // Regla: No inventar notificaciones falsas de servidor ni datos mock.
            // Al no existir endpoint de notificaciones en Spring Boot, la colección queda vacía.
            Notificaciones.Clear();
            NotificacionesFiltradas.Clear();
            ConteoNoLeidas = 0;
            TieneNotificaciones = false;
            EstaVacio = true;
        }
        catch (Exception ex)
        {
            ErrorMessage = "No se pudieron sincronizar las notificaciones.";
            System.Diagnostics.Debug.WriteLine($"[NotificationCenterViewModel] Error: {ex.Message}");
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private void FiltrarCategoria(string categoria)
    {
        CategoriaSeleccionada = categoria;
        // Al estar vacía la fuente por ausencia de backend, se mantiene la colección vacía
        NotificacionesFiltradas.Clear();
    }

    [RelayCommand]
    private async Task MarcarTodasLeidasAsync()
    {
        if (Application.Current?.MainPage != null)
        {
            await Application.Current.MainPage.DisplayAlert(
                "Notificaciones",
                "Esta función estará disponible cuando el servicio de notificaciones esté conectado al servidor de Shunshine Studio.",
                "Entendido"
            );
        }
    }

    [RelayCommand]
    private async Task AbrirDetalleAsync(NotificacionItem item)
    {
        if (item == null) return;

        await Shell.Current.GoToAsync("NotificationDetailPage", new Dictionary<string, object>
        {
            { "notificacion", item }
        });
    }

    [RelayCommand]
    private async Task IrAPreferenciasAsync()
    {
        await Shell.Current.GoToAsync("PreferencesPage");
    }

    [RelayCommand]
    private async Task RegresarAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
