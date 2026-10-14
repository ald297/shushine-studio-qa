using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Home;

/// <summary>
/// ViewModel para la pantalla de inicio del cliente.
/// Carga datos reales: usuario actual, próxima cita agendada y accesos rápidos.
/// </summary>
public partial class ClientHomeViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;
    private readonly IReservaRepository _reservaRepository;

    [ObservableProperty]
    private string nombreCliente = "Clienta";

    [ObservableProperty]
    private string saludo = "¡Bienvenida!";

    [ObservableProperty]
    private bool tieneProximaCita;

    public bool NoTieneProximaCita => !TieneProximaCita;

    partial void OnTieneProximaCitaChanged(bool value)
    {
        OnPropertyChanged(nameof(NoTieneProximaCita));
    }

    [ObservableProperty]
    private Reserva? proximaCita;

    [ObservableProperty]
    private string proximaCitaFecha = string.Empty;

    [ObservableProperty]
    private string proximaCitaHora = string.Empty;

    [ObservableProperty]
    private string proximaCitaServicio = string.Empty;

    [ObservableProperty]
    private string proximaCitaEstilista = string.Empty;

    [ObservableProperty]
    private string proximaCitaEstado = string.Empty;

    [ObservableProperty]
    private string proximaCitaEstadoColor = "#C5A059";

    public ClientHomeViewModel(IAuthRepository authRepository, IReservaRepository reservaRepository)
    {
        _authRepository = authRepository;
        _reservaRepository = reservaRepository;
        Title = "Inicio";
        ActualizarSaludo();
    }

    private void ActualizarSaludo()
    {
        var hora = DateTime.Now.Hour;
        Saludo = hora switch
        {
            >= 5 and < 12 => "¡Buenos días!",
            >= 12 and < 19 => "¡Buenas tardes!",
            _ => "¡Buenas noches!"
        };
    }

    [RelayCommand]
    public async Task CargarDatosAsync()
    {
        ActualizarSaludo();

        // 1. Cargar nombre real del cliente
        try
        {
            var user = await _authRepository.GetCurrentUserAsync();
            if (user != null && !string.IsNullOrWhiteSpace(user.NombreCompleto))
            {
                var partes = user.NombreCompleto.Split(' ', StringSplitOptions.RemoveEmptyEntries);
                NombreCliente = partes.Length > 0 ? partes[0] : user.NombreCompleto;
            }
        }
        catch
        {
            // Fallback con valor por defecto
        }

        // 2. Cargar próxima cita real
        try
        {
            var citas = await _reservaRepository.GetMisCitasAsync();
            if (citas != null)
            {
                var listaCitas = citas.ToList();
                var proxima = listaCitas
                    .Where(c => c.Estado?.ToUpperInvariant() is not "CANCELADA" and not "CANCELLED" and not "COMPLETADA" and not "COMPLETED")
                    .OrderBy(c => c.FechaHoraInicio > DateTime.MinValue ? c.FechaHoraInicio : DateTime.MaxValue)
                    .FirstOrDefault();

                if (proxima != null)
                {
                    ProximaCita = proxima;
                    ProximaCitaServicio = !string.IsNullOrEmpty(proxima.ServiciosResumen) ? proxima.ServiciosResumen : "Servicio de Belleza";
                    ProximaCitaFecha = !string.IsNullOrEmpty(proxima.FechaCita) ? proxima.FechaCita : proxima.FechaHoraInicio.ToString("dd/MM/yyyy");
                    ProximaCitaHora = !string.IsNullOrEmpty(proxima.HorarioTexto) ? proxima.HorarioTexto : proxima.HoraInicio;
                    ProximaCitaEstilista = !string.IsNullOrEmpty(proxima.EstilistaNombre) ? proxima.EstilistaNombre : "Estilista asignado";
                    ProximaCitaEstado = proxima.EstadoDisplay;
                    ProximaCitaEstadoColor = proxima.EstadoColor;
                    TieneProximaCita = true;
                }
                else
                {
                    TieneProximaCita = false;
                    ProximaCita = null;
                }
            }
        }
        catch
        {
            TieneProximaCita = false;
        }
    }

    [RelayCommand]
    private async Task IrAAgendarCita()
    {
        await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
    }

    [RelayCommand]
    private async Task IrACatalogo()
    {
        await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
    }

    [RelayCommand]
    private async Task IrAProductos()
    {
        await Shell.Current.GoToAsync("ClientProductsPage");
    }

    [RelayCommand]
    private async Task IrACitas()
    {
        await Shell.Current.GoToAsync("//MainTabs/MyAppointmentsPage");
    }

    [RelayCommand]
    private async Task IrAChat()
    {
        await Shell.Current.GoToAsync("//MainTabs/ChatPage");
    }

    [RelayCommand]
    private async Task IrASolicitudes()
    {
        await Shell.Current.GoToAsync("CustomRequestPage");
    }
}
