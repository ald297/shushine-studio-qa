using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Booking;

public partial class EstilistaItem : ObservableObject
{
    public Estilista Estilista { get; set; } = new();

    [ObservableProperty]
    private bool seleccionado;
}

/// <summary>
/// ViewModel para la selección de estilista o auto-asignación (US-3.03 / Wireframe Pág. 9).
/// Permite elegir un profesional específico o la opción 'Cualquiera disponible'.
/// </summary>
[QueryProperty(nameof(ServicioId), "servicioId")]
public partial class StylistSelectionViewModel : BaseViewModel
{
    private readonly IServicioRepository _servicioRepository;
    private readonly IEstilistaRepository _estilistaRepository;

    [ObservableProperty]
    private long servicioId;

    [ObservableProperty]
    private Servicio? servicio;

    [ObservableProperty]
    private ObservableCollection<EstilistaItem> estilistas = new();

    [ObservableProperty]
    private Estilista? estilistaSeleccionado;

    [ObservableProperty]
    private long estilistaSeleccionadoId;

    [ObservableProperty]
    private bool esCualquieraDisponible = true;

    public string NombreProfesionalElegido => EsCualquieraDisponible || EstilistaSeleccionado == null 
        ? "Cualquier estilista disponible" 
        : EstilistaSeleccionado.NombreCompleto;

    public StylistSelectionViewModel(
        IServicioRepository servicioRepository,
        IEstilistaRepository estilistaRepository)
    {
        _servicioRepository = servicioRepository;
        _estilistaRepository = estilistaRepository;
        Title = "Selecciona tu Estilista";
        _ = CargarEstilistasAsync();
    }

    async partial void OnServicioIdChanged(long value)
    {
        if (value > 0)
        {
            Servicio = await _servicioRepository.GetServicioByIdAsync(value);
        }
    }

    partial void OnEsCualquieraDisponibleChanged(bool value)
    {
        OnPropertyChanged(nameof(NombreProfesionalElegido));
    }

    partial void OnEstilistaSeleccionadoChanged(Estilista? value)
    {
        OnPropertyChanged(nameof(NombreProfesionalElegido));
    }

    private async Task CargarEstilistasAsync()
    {
        try
        {
            IsBusy = true;
            ErrorMessage = null;
            var list = await _estilistaRepository.GetEstilistasAsync();
            if (list != null && list.Any())
            {
                Estilistas.Clear();
                foreach (var e in list)
                {
                    Estilistas.Add(new EstilistaItem
                    {
                        Estilista = e,
                        Seleccionado = !EsCualquieraDisponible && (e.Id == EstilistaSeleccionadoId)
                    });
                }
                return;
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

        CargarEstilistasFallback();
    }

    private void CargarEstilistasFallback()
    {
        Estilistas.Clear();
        var lista = new List<Estilista>
        {
            new Estilista
            {
                Id = 1,
                NombreCompleto = "Sofía Ramos",
                Especialidad = "Colorista Senior & Balayage",
                Disponible = true
            },
            new Estilista
            {
                Id = 2,
                NombreCompleto = "Valentina Gómez",
                Especialidad = "Master en Uñas Esculpidas & Spa",
                Disponible = true
            },
            new Estilista
            {
                Id = 3,
                NombreCompleto = "Andrea Morales",
                Especialidad = "Corte de Autor & Visagismo",
                Disponible = true
            }
        };

        foreach (var e in lista)
        {
            Estilistas.Add(new EstilistaItem
            {
                Estilista = e,
                Seleccionado = !EsCualquieraDisponible && (e.Id == EstilistaSeleccionadoId)
            });
        }
    }

    [RelayCommand]
    private void ElegirCualquiera()
    {
        EsCualquieraDisponible = true;
        EstilistaSeleccionado = null;
        EstilistaSeleccionadoId = 0;
        foreach (var item in Estilistas)
        {
            item.Seleccionado = false;
        }
    }

    [RelayCommand]
    private void SeleccionarEstilista(EstilistaItem item)
    {
        if (item == null) return;
        EsCualquieraDisponible = false;
        EstilistaSeleccionado = item.Estilista;
        EstilistaSeleccionadoId = item.Estilista.Id;
        foreach (var e in Estilistas)
        {
            e.Seleccionado = (e.Estilista.Id == item.Estilista.Id);
        }
    }

    [RelayCommand]
    private async Task VolverAsync()
    {
        await Shell.Current.GoToAsync("..");
    }

    [RelayCommand]
    private async Task ContinuarAFechaHoraAsync()
    {
        if (Servicio == null) return;

        var estilistaIdParam = EsCualquieraDisponible || EstilistaSeleccionado == null 
            ? "0" 
            : EstilistaSeleccionado.Id.ToString();

        var estilistaNombreParam = EsCualquieraDisponible || EstilistaSeleccionado == null 
            ? "Cualquier estilista disponible" 
            : EstilistaSeleccionado.NombreCompleto;

        await Shell.Current.GoToAsync(
            $"SlotSelectionPage?servicioId={Servicio.Id}&estilistaId={estilistaIdParam}&estilistaNombre={Uri.EscapeDataString(estilistaNombreParam)}"
        );
    }
}
