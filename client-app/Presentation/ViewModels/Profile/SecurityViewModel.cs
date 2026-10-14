using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Profile;

/// <summary>
/// ViewModel de Seguridad con persistencia y validación real de contraseña en la Web API de Shushine Studio.
/// </summary>
public partial class SecurityViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;

    [ObservableProperty]
    private string contrasenaActual = string.Empty;

    [ObservableProperty]
    private string contrasenaNueva = string.Empty;

    [ObservableProperty]
    private string contrasenaConfirmar = string.Empty;

    [ObservableProperty]
    private bool mostrarContrasenaActual = false;

    [ObservableProperty]
    private bool mostrarContrasenaNueva = false;

    [ObservableProperty]
    private bool mostrarContrasenaConfirmar = false;

    public bool BackendCambioContrasenaDisponible => true;

    public SecurityViewModel(IAuthRepository authRepository)
    {
        _authRepository = authRepository;
        Title = "Seguridad";
    }

    [RelayCommand]
    private void ToggleMostrarActual() => MostrarContrasenaActual = !MostrarContrasenaActual;

    [RelayCommand]
    private void ToggleMostrarNueva() => MostrarContrasenaNueva = !MostrarContrasenaNueva;

    [RelayCommand]
    private void ToggleMostrarConfirmar() => MostrarContrasenaConfirmar = !MostrarContrasenaConfirmar;

    [RelayCommand]
    private async Task CambiarContrasenaAsync()
    {
        if (IsBusy) return;

        // Validaciones de entrada
        if (string.IsNullOrWhiteSpace(ContrasenaActual) ||
            string.IsNullOrWhiteSpace(ContrasenaNueva) ||
            string.IsNullOrWhiteSpace(ContrasenaConfirmar))
        {
            ErrorMessage = "Todos los campos son requeridos.";
            return;
        }

        if (ContrasenaNueva != ContrasenaConfirmar)
        {
            ErrorMessage = "Las contraseñas nuevas no coinciden.";
            return;
        }

        if (ContrasenaNueva.Length < 6)
        {
            ErrorMessage = "La nueva contraseña debe tener al menos 6 caracteres.";
            return;
        }

        if (ContrasenaActual == ContrasenaNueva)
        {
            ErrorMessage = "La nueva contraseña debe ser diferente a la actual.";
            return;
        }

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            await _authRepository.CambiarClaveAsync(ContrasenaActual, ContrasenaNueva, ContrasenaConfirmar);

            ContrasenaActual = string.Empty;
            ContrasenaNueva = string.Empty;
            ContrasenaConfirmar = string.Empty;

            if (Application.Current?.MainPage != null)
            {
                await Application.Current.MainPage.DisplayAlert(
                    "Contraseña Actualizada",
                    "Tu contraseña ha sido cambiada exitosamente en el servidor.",
                    "Aceptar"
                );
            }

            await Shell.Current.GoToAsync("..");
        }
        catch (Exception ex)
        {
            ErrorMessage = ex.Message.Contains("incorrecta") 
                ? "La contraseña actual no es correcta." 
                : "No se pudo actualizar la contraseña: " + ex.Message;
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task RegresarAsync()
        => await Shell.Current.GoToAsync("..");
}
