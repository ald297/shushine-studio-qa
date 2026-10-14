using System.Text.RegularExpressions;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Auth;

/// <summary>
/// ViewModel reactivo para el registro de nuevos clientes (US-2.01 / Wireframe Pág. 5).
/// Implementa validaciones de negocio en cliente y delega la creación en IAuthRepository.
/// </summary>
public partial class RegisterViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;

    [ObservableProperty]
    private string nombre = string.Empty;

    [ObservableProperty]
    private string email = string.Empty;

    [ObservableProperty]
    private string telefono = string.Empty;

    [ObservableProperty]
    private string password = string.Empty;

    [ObservableProperty]
    private string confirmPassword = string.Empty;

    [ObservableProperty]
    private bool isPasswordHidden = true;

    [ObservableProperty]
    private bool acceptTerms = true;

    public RegisterViewModel(IAuthRepository authRepository)
    {
        _authRepository = authRepository;
        Title = "Crear Cuenta";
    }

    [RelayCommand]
    private void TogglePasswordVisibility()
    {
        IsPasswordHidden = !IsPasswordHidden;
    }

    [RelayCommand]
    private async Task RegisterAsync()
    {
        if (IsBusy) return;

        // 1. Validar campos obligatorios
        if (string.IsNullOrWhiteSpace(Nombre) || 
            string.IsNullOrWhiteSpace(Email) || 
            string.IsNullOrWhiteSpace(Password) || 
            string.IsNullOrWhiteSpace(ConfirmPassword))
        {
            await ShowAlertAsync("Campos Incompletos", "Por favor, completa todos los campos obligatorios.");
            return;
        }

        // 2. Validar formato de correo electrónico
        var emailRegex = new Regex(@"^[^@\s]+@[^@\s]+\.[^@\s]+$", RegexOptions.IgnoreCase);
        if (!emailRegex.IsMatch(Email.Trim()))
        {
            await ShowAlertAsync("Correo Inválido", "Por favor ingresa un correo electrónico con formato válido (ej. nombre@correo.com).");
            return;
        }

        // 3. Validar longitud de contraseña
        if (Password.Length < 6)
        {
            await ShowAlertAsync("Contraseña Muy Corta", "La contraseña debe contener al menos 6 caracteres.");
            return;
        }

        // 4. Validar coincidencia de contraseñas
        if (Password != ConfirmPassword)
        {
            await ShowAlertAsync("Contraseñas No Coinciden", "Las contraseñas ingresadas no coinciden. Por favor, verifícalas.");
            return;
        }

        // 5. Validar aceptación de términos
        if (!AcceptTerms)
        {
            await ShowAlertAsync("Términos Requeridos", "Debes aceptar los Términos y Condiciones y Políticas de Privacidad para continuar.");
            return;
        }

        try
        {
            IsBusy = true;
            ErrorMessage = null;

            var success = await _authRepository.RegisterAsync(
                Nombre.Trim(), 
                Email.Trim(), 
                Password, 
                Telefono?.Trim() ?? string.Empty
            );

            if (success)
            {
                await ShowAlertAsync(
                    "¡Bienvenida a Shushine!", 
                    "Tu cuenta ha sido creada exitosamente. Disfruta tu experiencia en Shushine Studio.", 
                    "Continuar"
                );

                // Redirigir limpiamente usando la navegación central de AppShell
                if (Shell.Current is AppShell appShell)
                {
                    appShell.SwitchToRole("CLIENTE");
                }
                else
                {
                    await Shell.Current.GoToAsync("//CatalogPage");
                }
            }
            else
            {
                await ShowAlertAsync(
                    "No se pudo crear la cuenta",
                    "Verifica que el correo o usuario no esté registrado previamente y que los datos sean correctos.",
                    "Aceptar"
                );
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = ex.Message;
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task NavigateBackToLoginAsync()
    {
        await Shell.Current.GoToAsync("..");
    }

    private static async Task ShowAlertAsync(string title, string message, string cancel = "Aceptar")
    {
        if (Application.Current?.MainPage != null)
        {
            await Application.Current.MainPage.DisplayAlert(title, message, cancel);
        }
    }
}
