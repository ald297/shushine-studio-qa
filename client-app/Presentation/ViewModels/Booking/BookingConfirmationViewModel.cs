using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Booking;

/// <summary>
/// ViewModel para el comprobante de cita confirmada (US-4.02 / Wireframe Pág. 12).
/// Refleja el identificador real de la cita sin generar códigos ficticios ni inventar sucursales.
/// </summary>
[QueryProperty(nameof(Codigo), "codigo")]
[QueryProperty(nameof(ServicioNombre), "servicioNombre")]
[QueryProperty(nameof(EstilistaNombre), "estilistaNombre")]
[QueryProperty(nameof(Fecha), "fecha")]
[QueryProperty(nameof(Hora), "hora")]
[QueryProperty(nameof(Total), "total")]
public partial class BookingConfirmationViewModel : BaseViewModel
{
    [ObservableProperty]
    private string codigo = string.Empty;

    [ObservableProperty]
    private string servicioNombre = string.Empty;

    [ObservableProperty]
    private string estilistaNombre = string.Empty;

    [ObservableProperty]
    private string fecha = string.Empty;

    [ObservableProperty]
    private string hora = string.Empty;

    [ObservableProperty]
    private string total = string.Empty;

    public bool TieneCodigo => !string.IsNullOrWhiteSpace(Codigo);

    public BookingConfirmationViewModel()
    {
        Title = "¡Cita Confirmada!";
    }

    partial void OnCodigoChanged(string value)
    {
        OnPropertyChanged(nameof(TieneCodigo));
    }

    [RelayCommand]
    private async Task VerEnMisCitasAsync()
    {
        try
        {
            await Shell.Current.GoToAsync("//MainTabs/MyAppointmentsPage");
        }
        catch
        {
            await Shell.Current.GoToAsync("//MyAppointmentsPage");
        }
    }

    [RelayCommand]
    private async Task VolverAlInicioAsync()
    {
        try
        {
            await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
        }
        catch
        {
            await Shell.Current.GoToAsync("//CatalogPage");
        }
    }
}
