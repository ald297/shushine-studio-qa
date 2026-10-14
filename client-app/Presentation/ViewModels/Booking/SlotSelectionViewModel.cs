using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;
using ShushineStudio.Mobile.Domain.UseCases;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Booking;

public partial class TimeSlotItem : ObservableObject
{
    [ObservableProperty]
    private string hora = string.Empty;

    [ObservableProperty]
    private bool disponible = true;

    [ObservableProperty]
    private bool seleccionado = false;

    [ObservableProperty]
    private string turno = "Mañana";

    public bool NoDisponible => !Disponible;
}

public partial class DayItem : ObservableObject
{
    [ObservableProperty]
    private DateTime fecha;

    [ObservableProperty]
    private string diaNombre = string.Empty;

    [ObservableProperty]
    private string diaNumero = string.Empty;

    [ObservableProperty]
    private bool seleccionado = false;
}

/// <summary>
/// ViewModel para la selección de fecha y bloques horarios disponibles (US-3.04 / Wireframe Pág. 10).
/// Genera slots dinámicos matutinos y vespertinos con filtrado de ocupados mediante GetDisponibilidadUseCase.
/// </summary>
[QueryProperty(nameof(ServicioId), "servicioId")]
[QueryProperty(nameof(EstilistaId), "estilistaId")]
[QueryProperty(nameof(EstilistaNombre), "estilistaNombre")]
public partial class SlotSelectionViewModel : BaseViewModel
{
    private readonly IServicioRepository _servicioRepository;
    private readonly GetDisponibilidadUseCase? _getDisponibilidadUseCase;

    [ObservableProperty]
    private long servicioId;

    [ObservableProperty]
    private string estilistaId = "0";

    [ObservableProperty]
    private string estilistaNombre = "Cualquier estilista disponible";

    [ObservableProperty]
    private Servicio? servicio;

    [ObservableProperty]
    private ObservableCollection<DayItem> diasSemana = new();

    [ObservableProperty]
    private DateTime fechaSeleccionada = DateTime.Today.AddDays(1);

    [ObservableProperty]
    private ObservableCollection<TimeSlotItem> slotsManana = new();

    [ObservableProperty]
    private ObservableCollection<TimeSlotItem> slotsTarde = new();

    [ObservableProperty]
    private string horaSeleccionada = string.Empty;

    public bool HasErrorMessage => !string.IsNullOrWhiteSpace(ErrorMessage);

    public bool TieneHoraSeleccionada => !string.IsNullOrWhiteSpace(HoraSeleccionada);

    public string MesAnioTexto => FechaSeleccionada.ToString("MMMM yyyy", new System.Globalization.CultureInfo("es-ES")).ToUpperInvariant();

    public string ResumenFechaHora => string.IsNullOrWhiteSpace(HoraSeleccionada)
        ? "Selecciona un horario disponible"
        : $"{FechaSeleccionada:dd/MM/yyyy} • {HoraSeleccionada}";

    public SlotSelectionViewModel(
        IServicioRepository servicioRepository,
        GetDisponibilidadUseCase? getDisponibilidadUseCase = null)
    {
        _servicioRepository = servicioRepository;
        _getDisponibilidadUseCase = getDisponibilidadUseCase;
        Title = "Fecha y Horario";
        GenerarDias();
        GenerarSlots();
    }

    async partial void OnServicioIdChanged(long value)
    {
        if (value > 0)
        {
            Servicio = await _servicioRepository.GetServicioByIdAsync(value);
            await CargarDisponibilidadAsync();
        }
    }

    async partial void OnEstilistaIdChanged(string value)
    {
        await CargarDisponibilidadAsync();
    }

    partial void OnHoraSeleccionadaChanged(string value)
    {
        OnPropertyChanged(nameof(TieneHoraSeleccionada));
        OnPropertyChanged(nameof(ResumenFechaHora));
    }

    partial void OnFechaSeleccionadaChanged(DateTime value)
    {
        OnPropertyChanged(nameof(MesAnioTexto));
        OnPropertyChanged(nameof(ResumenFechaHora));
    }

    private void GenerarDias()
    {
        DiasSemana.Clear();
        var baseDate = DateTime.Today;

        for (int i = 1; i <= 14; i++)
        {
            var date = baseDate.AddDays(i);
            DiasSemana.Add(new DayItem
            {
                Fecha = date,
                DiaNombre = date.ToString("ddd", new System.Globalization.CultureInfo("es-ES")).ToUpperInvariant().TrimEnd('.'),
                DiaNumero = date.Day.ToString(),
                Seleccionado = (i == 1)
            });
        }
    }

    [RelayCommand]
    public async Task CargarDisponibilidadAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = null;
            OnPropertyChanged(nameof(HasErrorMessage));

            long.TryParse(EstilistaId, out var idEstilista);

            if (_getDisponibilidadUseCase != null && idEstilista > 0 && ServicioId > 0)
            {
                var disp = await _getDisponibilidadUseCase.ExecuteAsync(idEstilista, FechaSeleccionada, ServicioId);
                if (disp != null && disp.Franjas != null && disp.Franjas.Any())
                {
                    SlotsManana.Clear();
                    SlotsTarde.Clear();

                    foreach (var f in disp.Franjas)
                    {
                        var horaInicio = f.HoraInicio;
                        bool esTarde = false;
                        if (TimeSpan.TryParse(horaInicio, out var ts))
                        {
                            esTarde = ts.Hours >= 12;
                        }
                        else if (horaInicio.Contains("PM", StringComparison.OrdinalIgnoreCase))
                        {
                            esTarde = true;
                        }

                        var item = new TimeSlotItem
                        {
                            Hora = f.HoraInicio,
                            Disponible = f.Disponible,
                            Turno = esTarde ? "Tarde" : "Mañana",
                            Seleccionado = (f.HoraInicio == HoraSeleccionada)
                        };

                        if (esTarde)
                            SlotsTarde.Add(item);
                        else
                            SlotsManana.Add(item);
                    }
                    return;
                }
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = ex.Message;
            OnPropertyChanged(nameof(HasErrorMessage));
        }
        finally
        {
            IsBusy = false;
        }

        GenerarSlots();
    }

    private void GenerarSlots()
    {
        SlotsManana.Clear();
        SlotsManana.Add(new TimeSlotItem { Hora = "09:00 AM", Disponible = true, Turno = "Mañana" });
        SlotsManana.Add(new TimeSlotItem { Hora = "10:30 AM", Disponible = false, Turno = "Mañana" }); // Ocupado
        SlotsManana.Add(new TimeSlotItem { Hora = "11:45 AM", Disponible = true, Turno = "Mañana" });

        SlotsTarde.Clear();
        SlotsTarde.Add(new TimeSlotItem { Hora = "02:00 PM", Disponible = true, Turno = "Tarde" });
        SlotsTarde.Add(new TimeSlotItem { Hora = "03:30 PM", Disponible = true, Turno = "Tarde" });
        SlotsTarde.Add(new TimeSlotItem { Hora = "05:00 PM", Disponible = false, Turno = "Tarde" }); // Ocupado
        SlotsTarde.Add(new TimeSlotItem { Hora = "06:15 PM", Disponible = true, Turno = "Tarde" });

        // Si la hora seleccionada previa sigue disponible en los nuevos slots, marcarla
        foreach (var s in SlotsManana)
            s.Seleccionado = (s.Hora == HoraSeleccionada);
        foreach (var s in SlotsTarde)
            s.Seleccionado = (s.Hora == HoraSeleccionada);
    }

    [RelayCommand]
    private async Task SeleccionarDiaAsync(DayItem day)
    {
        if (day == null) return;

        foreach (var d in DiasSemana)
        {
            d.Seleccionado = (d.Fecha == day.Fecha);
        }

        FechaSeleccionada = day.Fecha;
        HoraSeleccionada = string.Empty;
        await CargarDisponibilidadAsync();
    }

    [RelayCommand]
    private void SeleccionarSlot(TimeSlotItem slot)
    {
        if (slot == null || !slot.Disponible) return;

        foreach (var s in SlotsManana)
        {
            s.Seleccionado = (s.Hora == slot.Hora);
        }
        foreach (var s in SlotsTarde)
        {
            s.Seleccionado = (s.Hora == slot.Hora);
        }

        HoraSeleccionada = slot.Hora;
    }

    [RelayCommand]
    private async Task VolverAsync()
    {
        await Shell.Current.GoToAsync("..");
    }

    [RelayCommand]
    private async Task ContinuarAResumenAsync()
    {
        if (Servicio == null) return;

        if (string.IsNullOrWhiteSpace(HoraSeleccionada))
        {
            if (Application.Current?.MainPage != null)
            {
                await Application.Current.MainPage.DisplayAlert(
                    "Horario Requerido", 
                    "Por favor selecciona una franja horaria disponible para continuar.", 
                    "Aceptar"
                );
            }
            return;
        }

        var fechaStr = FechaSeleccionada.ToString("yyyy-MM-dd");

        // Navegar a la pantalla de resumen (US-4.01)
        await Shell.Current.GoToAsync(
            $"BookingSummaryPage?servicioId={Servicio.Id}&estilistaId={EstilistaId}&estilistaNombre={Uri.EscapeDataString(EstilistaNombre)}&fecha={fechaStr}&hora={Uri.EscapeDataString(HoraSeleccionada)}"
        );
    }
}
