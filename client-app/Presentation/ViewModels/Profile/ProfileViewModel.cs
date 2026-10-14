using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Profile;

/// <summary>
/// ViewModel para el perfil del cliente. Carga datos reales desde GET /api/auth/me.
/// Campos soportados por backend: nombre, apellido, telefono, login, rol, activo.
/// Campos NO soportados: foto, DUI, email real, contraseña, puntos/fidelidad, preferencias.
/// </summary>
public partial class ProfileViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;

    // ────────────────────────────────────────────────────────────
    // Datos reales — respaldados por GET /api/auth/me
    // ────────────────────────────────────────────────────────────
    [ObservableProperty]
    private string nombre = string.Empty;

    [ObservableProperty]
    private string apellido = string.Empty;

    [ObservableProperty]
    private string telefono = string.Empty;

    [ObservableProperty]
    private string login = string.Empty;

    [ObservableProperty]
    private string rol = "CLIENTE";

    [ObservableProperty]
    private string nivelFidelidad = "Bronce";

    [ObservableProperty]
    private int puntosAcumulados = 0;

    // Propiedad derivada: nombre completo para mostrar
    public string NombreCompleto => string.IsNullOrWhiteSpace(Apellido)
        ? Nombre
        : $"{Nombre} {Apellido}";

    // Iniciales para el avatar basado en nombre real
    public string Iniciales
    {
        get
        {
            var partes = NombreCompleto.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            return string.Concat(partes.Take(2).Select(p => p[0])).ToUpper();
        }
    }

    // Rol formateado
    public string RolDisplay => Rol?.Contains("ADMIN", StringComparison.OrdinalIgnoreCase) == true
        ? "Administrador"
        : "Cliente";

    public bool IsAdmin => Rol?.Contains("ADMIN", StringComparison.OrdinalIgnoreCase) == true;

    // ────────────────────────────────────────────────────────────
    // Constructor
    // ────────────────────────────────────────────────────────────
    public ProfileViewModel(IAuthRepository authRepository)
    {
        _authRepository = authRepository;
        Title = "Mi Perfil";
    }

    // ────────────────────────────────────────────────────────────
    // Carga de datos reales desde API
    // ────────────────────────────────────────────────────────────
    [RelayCommand]
    public async Task CargarPerfilAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            var usuario = await _authRepository.GetCurrentUserAsync();
            if (usuario != null)
            {
                // Separar nombre completo en nombre y apellido
                var partes = usuario.NombreCompleto?.Split(' ', 2, StringSplitOptions.RemoveEmptyEntries)
                             ?? [];
                Nombre = partes.Length > 0 ? partes[0] : string.Empty;
                Apellido = partes.Length > 1 ? partes[1] : string.Empty;
                Telefono = usuario.Telefono ?? string.Empty;
                Login = usuario.Login ?? string.Empty;
                Rol = usuario.Rol ?? "CLIENTE";
                NivelFidelidad = !string.IsNullOrWhiteSpace(usuario.NivelFidelidad) ? usuario.NivelFidelidad : "Bronce";
                PuntosAcumulados = usuario.PuntosAcumulados;

                OnPropertyChanged(nameof(NombreCompleto));
                OnPropertyChanged(nameof(Iniciales));
                OnPropertyChanged(nameof(RolDisplay));
                OnPropertyChanged(nameof(IsAdmin));
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = "No se pudo cargar el perfil. Verifica tu conexión.";
            System.Diagnostics.Debug.WriteLine($"[ProfileViewModel] Error: {ex.Message}");
        }
        finally
        {
            IsBusy = false;
        }
    }

    // ────────────────────────────────────────────────────────────
    // Navegación
    // ────────────────────────────────────────────────────────────
    [RelayCommand]
    private async Task IrAEditarPerfilAsync()
        => await Shell.Current.GoToAsync("EditProfilePage");

    [RelayCommand]
    private async Task IrASeguridadAsync()
        => await Shell.Current.GoToAsync("SecurityPage");

    [RelayCommand]
    private async Task IrACentroNotificacionesAsync()
        => await Shell.Current.GoToAsync("NotificationCenterPage");

    [RelayCommand]
    private async Task IrAPreferenciasAsync()
        => await Shell.Current.GoToAsync("PreferencesPage");

    [RelayCommand]
    private async Task IrAMisResenasAsync()
        => await Shell.Current.GoToAsync("MyReviewsPage");

    [RelayCommand]
    private async Task IrASolicitudesAsync()
        => await Shell.Current.GoToAsync("CustomRequestPage");

    [RelayCommand]
    private async Task IrAInfoAppAsync()
        => await Shell.Current.GoToAsync("AppInfoPage");

    [RelayCommand]
    private async Task IrAlPanelAdminAsync()
        => await Shell.Current.GoToAsync("AdminDashboardPage");

    // ────────────────────────────────────────────────────────────
    // Cerrar sesión — usa LogoutAsync() real de AuthRepository
    // ────────────────────────────────────────────────────────────
    [RelayCommand]
    private async Task CerrarSesionAsync()
    {
        if (Application.Current?.MainPage == null) return;

        bool confirm = await Application.Current.MainPage.DisplayAlert(
            "Cerrar Sesión",
            "¿Estás segura de que deseas salir de tu cuenta en Shunshine Studio?",
            "Sí, salir",
            "Cancelar"
        );

        if (confirm)
        {
            await _authRepository.LogoutAsync();
            if (Shell.Current is AppShell appShell)
            {
                appShell.SwitchToLogin();
            }
            else
            {
                await Shell.Current.GoToAsync("//LoginPage");
            }
        }
    }
}
