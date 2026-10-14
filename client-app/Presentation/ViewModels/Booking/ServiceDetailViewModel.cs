using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Booking;

/// <summary>
/// ViewModel para la ficha técnica y detalle de servicio (US-3.02 / Wireframe Pág. 8).
/// Permite revisar el protocolo paso a paso y avanzar a la selección de estilista.
/// </summary>
[QueryProperty(nameof(ServicioId), "servicioId")]
public partial class ServiceDetailViewModel : BaseViewModel
{
    private readonly IServicioRepository _servicioRepository;

    [ObservableProperty]
    private long servicioId;

    [ObservableProperty]
    private Servicio? servicio;

    public ServiceDetailViewModel(IServicioRepository servicioRepository)
    {
        _servicioRepository = servicioRepository;
        Title = "Detalle del Tratamiento";
    }

    async partial void OnServicioIdChanged(long value)
    {
        if (value > 0)
        {
            await LoadDetalleAsync(value);
        }
    }

    public async Task LoadDetalleAsync(long id)
    {
        try
        {
            IsBusy = true;
            Servicio = await _servicioRepository.GetServicioByIdAsync(id);
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task ContinuarAReservaAsync()
    {
        if (Servicio == null) return;

        // Navegar a selección de estilista transmitiendo el servicioId
        await Shell.Current.GoToAsync($"StylistSelectionPage?servicioId={Servicio.Id}");
    }

    [RelayCommand]
    private async Task VolverAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
