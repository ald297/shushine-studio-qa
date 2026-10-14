using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Profile;

/// <summary>
/// ViewModel de Preferencias. No existe tabla de preferencias ni endpoint en el backend.
/// Los switches son locales usando Preferences de MAUI (no persisten en servidor).
/// Esta funcionalidad es UI preparada — se indica explícitamente en la vista.
/// </summary>
public partial class PreferencesViewModel : BaseViewModel
{
    private const string KeyRecordatorios = "pref_recordatorios";
    private const string KeyMensajes = "pref_mensajes";
    private const string KeyCambiosCita = "pref_cambios_cita";
    private const string KeyPromociones = "pref_promociones";

    [ObservableProperty]
    private bool recordatoriosCita = true;

    [ObservableProperty]
    private bool mensajesChat = true;

    [ObservableProperty]
    private bool cambiosDeCita = true;

    [ObservableProperty]
    private bool promocionesOfertas = false;

    // No existe backend de preferencias
    public bool BackendPreferenciasDisponible => false;

    public PreferencesViewModel()
    {
        Title = "Preferencias";
        CargarPreferenciasLocales();
    }

    private void CargarPreferenciasLocales()
    {
        // Preferencias guardadas localmente en el dispositivo (no en servidor)
        RecordatoriosCita = Preferences.Default.Get(KeyRecordatorios, true);
        MensajesChat = Preferences.Default.Get(KeyMensajes, true);
        CambiosDeCita = Preferences.Default.Get(KeyCambiosCita, true);
        PromocionesOfertas = Preferences.Default.Get(KeyPromociones, false);
    }

    partial void OnRecordatoriosCitaChanged(bool value)
        => Preferences.Default.Set(KeyRecordatorios, value);

    partial void OnMensajesChatChanged(bool value)
        => Preferences.Default.Set(KeyMensajes, value);

    partial void OnCambiosDeCitaChanged(bool value)
        => Preferences.Default.Set(KeyCambiosCita, value);

    partial void OnPromocionesOfertasChanged(bool value)
        => Preferences.Default.Set(KeyPromociones, value);

    [RelayCommand]
    private async Task RegresarAsync()
        => await Shell.Current.GoToAsync("..");
}
