using CommunityToolkit.Mvvm.Input;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Profile;

/// <summary>
/// ViewModel para la pantalla de información de la aplicación Shunshine Studio.
/// </summary>
public partial class AppInfoViewModel : BaseViewModel
{
    public string AppName => "Shunshine Studio";
    public string AppVersion => "v1.0.0";
    public string BuildNumber => "2026.1";
    public string SalonAddress => "Sonsonate, El Salvador";
    public string HorarioAtencion => "Lun - Sáb: 8:00 AM - 6:00 PM";
    public string Institucion => "ESFE AGAPE / MEGATEC";
    public string Equipo => "Alex Fernando Alfaro & Camila Antonia Calderon";

    public AppInfoViewModel()
    {
        Title = "Información de la App";
    }

    [RelayCommand]
    private async Task RegresarAsync()
        => await Shell.Current.GoToAsync("..");

    [RelayCommand]
    private async Task VerTerminosAsync()
    {
        if (Application.Current?.MainPage != null)
        {
            await Application.Current.MainPage.DisplayAlert(
                "Términos del Servicio",
                "Las reservas realizadas mediante Shunshine Studio quedan sujetas a confirmación y disponibilidad del salón.",
                "Entendido"
            );
        }
    }

    [RelayCommand]
    private async Task VerPrivacidadAsync()
    {
        if (Application.Current?.MainPage != null)
        {
            await Application.Current.MainPage.DisplayAlert(
                "Política de Privacidad",
                "Tus datos de cuenta y reservas se tratan con estricta confidencialidad para la gestión interna de citas.",
                "Entendido"
            );
        }
    }
}
