using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Profile;

/// <summary>
/// ViewModel para editar perfil conectado directamente con el endpoint PUT /api/auth/perfil.
/// </summary>
public partial class EditProfileViewModel : BaseViewModel
{
    private readonly IAuthRepository _authRepository;

    [ObservableProperty]
    private string nombre = string.Empty;

    [ObservableProperty]
    private string apellido = string.Empty;

    [ObservableProperty]
    private string telefono = string.Empty;

    [ObservableProperty]
    private string login = string.Empty;

    [ObservableProperty]
    private bool guardadoExitoso = false;

    public bool BackendActualizacionDisponible => true;

    public EditProfileViewModel(IAuthRepository authRepository)
    {
        _authRepository = authRepository;
        Title = "Editar Perfil";
    }

    [RelayCommand]
    public async Task CargarDatosAsync()
    {
        if (IsBusy) return;
        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;
            var usuario = await _authRepository.GetCurrentUserAsync();
            if (usuario != null)
            {
                var partes = usuario.NombreCompleto?.Split(' ', 2, StringSplitOptions.RemoveEmptyEntries) ?? [];
                Nombre = partes.Length > 0 ? partes[0] : string.Empty;
                Apellido = partes.Length > 1 ? partes[1] : string.Empty;
                Telefono = usuario.Telefono ?? string.Empty;
                Login = usuario.Login ?? string.Empty;
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = "No se pudo cargar tu información: " + ex.Message;
            System.Diagnostics.Debug.WriteLine($"[EditProfileVM] {ex.Message}");
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task GuardarCambiosAsync()
    {
        if (IsBusy) return;

        if (string.IsNullOrWhiteSpace(Nombre))
        {
            ErrorMessage = "El nombre no puede estar vacío.";
            return;
        }
        if (string.IsNullOrWhiteSpace(Telefono))
        {
            ErrorMessage = "El teléfono no puede estar vacío.";
            return;
        }

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            var actualizado = await _authRepository.ActualizarPerfilAsync(Nombre, Apellido, Telefono, Login);

            GuardadoExitoso = true;
            if (Application.Current?.MainPage != null)
            {
                await Application.Current.MainPage.DisplayAlert(
                    "Perfil Actualizado",
                    "Tus datos han sido actualizados exitosamente en Shushine Studio.",
                    "Aceptar"
                );
            }

            await Shell.Current.GoToAsync("..");
        }
        catch (Exception ex)
        {
            ErrorMessage = "Error al actualizar perfil: " + ex.Message;
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
