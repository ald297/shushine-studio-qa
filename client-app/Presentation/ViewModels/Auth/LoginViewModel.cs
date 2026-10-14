using System.Text.RegularExpressions;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Auth;

/// <summary>
/// ViewModel reactivo para el inicio de sesión de clientes (US-2.02 / Wireframe Pág. 6).
/// Valida formato de credenciales, autentica contra la Web API y gestiona sesión segura.
/// </summary>
public partial class LoginViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;

    [ObservableProperty]
    private string email = string.Empty;

    [ObservableProperty]
    private string password = string.Empty;

    [ObservableProperty]
    private bool isPasswordHidden = true;

    public LoginViewModel(IAuthRepository authRepository)
    {
        _authRepository = authRepository;
        Title = "Iniciar Sesión";
    }

    [RelayCommand]
    private void TogglePasswordVisibility()
    {
        IsPasswordHidden = !IsPasswordHidden;
    }

    [RelayCommand]
    private async Task LoginAsync()
    {
        if (IsBusy) return;

        // 1. Validar campos no vacíos
        if (string.IsNullOrWhiteSpace(Email) || string.IsNullOrWhiteSpace(Password))
        {
            await ShowAlertAsync(
                "Campos Incompletos", 
                "Por favor, ingresa tu usuario o correo electrónico y tu contraseña."
            );
            return;
        }

        try
        {
            IsBusy = true;
            ErrorMessage = null;

            var success = await _authRepository.LoginAsync(Email.Trim(), Password);
            if (success)
            {
                // Limpiar contraseña de memoria tras autenticación exitosa
                Password = string.Empty;

                // Obtener usuario autenticado para determinar el rol oficial
                var currentUser = await _authRepository.GetCurrentUserAsync();
                var rol = currentUser?.Rol?.ToUpperInvariant() ?? "CLIENTE";

                if (Shell.Current is AppShell appShell)
                {
                    if (rol.Contains("ADMIN"))
                    {
                        appShell.SwitchToRole("ADMIN");
                    }
                    else
                    {
                        appShell.SwitchToRole("CLIENTE");
                    }
                }
            }
        }
        catch (TimeoutException tex)
        {
            ErrorMessage = tex.Message;
            await ShowAlertAsync("Servidor Iniciando", tex.Message);
        }
        catch (HttpRequestException hex)
        {
            ErrorMessage = hex.Message;
            await ShowAlertAsync("Aviso de Conexión", hex.Message);
        }
        catch (Exception ex)
        {
            if (ex is System.IO.IOException 
             || ex.GetType().Name.Contains("Socket", StringComparison.OrdinalIgnoreCase)
             || ex.Message.Contains("Socket", StringComparison.OrdinalIgnoreCase) 
             || ex.Message.Contains("closed", StringComparison.OrdinalIgnoreCase)
             || ex.Message.Contains("reset", StringComparison.OrdinalIgnoreCase))
            {
                var msg = "El servidor del salón está iniciando en la nube. Por favor presiona 'Iniciar Sesión' nuevamente.";
                ErrorMessage = msg;
                await ShowAlertAsync("Servidor Iniciando", msg);
            }
            else
            {
                ErrorMessage = ex.Message;
                await ShowAlertAsync("Aviso de Autenticación", ex.Message);
            }
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task ForgotPasswordAsync()
    {
        await ShowAlertAsync(
            "Recuperar Contraseña", 
            "Para restablecer tu contraseña, ingresa al portal web del salón o solicita asistencia en recepción al (+503) 2400-0000.", 
            "Entendido"
        );
    }

    [RelayCommand]
    private async Task NavigateToRegisterAsync()
    {
        await Shell.Current.GoToAsync("RegisterPage");
    }

    private static async Task ShowAlertAsync(string title, string message, string cancel = "Aceptar")
    {
        if (Application.Current?.MainPage != null)
        {
            await Application.Current.MainPage.DisplayAlert(title, message, cancel);
        }
    }
}
