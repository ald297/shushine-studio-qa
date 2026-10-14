using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

/// <summary>
/// ViewModel para la pantalla de 'Mis Reseñas'.
/// Carga citas completadas pendientes de calificar y el historial real de reseñas publicadas en la Web API.
/// </summary>
public partial class MyReviewsViewModel : BaseViewModel
{
    private readonly IReservaRepository _reservaRepository;
    private readonly IResenaRepository _resenaRepository;

    [ObservableProperty]
    private ObservableCollection<Reserva> citasPendientesCalificar = new();

    [ObservableProperty]
    private ObservableCollection<Resena> resenasPublicadas = new();

    [ObservableProperty]
    private bool tieneCitasPendientes;

    [ObservableProperty]
    private bool tieneResenasPublicadas;

    [ObservableProperty]
    private bool noTieneResenasPublicadas = true;

    public bool BackendResenasDisponible => true;

    public MyReviewsViewModel(IReservaRepository reservaRepository, IResenaRepository resenaRepository)
    {
        _reservaRepository = reservaRepository;
        _resenaRepository = resenaRepository;
        Title = "Mis Reseñas";
    }

    [RelayCommand]
    public async Task CargarDatosAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            // 1. Obtener citas reales del cliente autenticado
            var citas = await _reservaRepository.GetMisCitasAsync();

            // 2. Obtener reseñas reales publicadas por el cliente
            var misResenas = await _resenaRepository.GetMisResenasAsync();
            ResenasPublicadas.Clear();
            var idsCitasCalificadas = new HashSet<long>();
            foreach (var r in misResenas)
            {
                ResenasPublicadas.Add(r);
                if (r.CitaId > 0)
                {
                    idsCitasCalificadas.Add(r.CitaId);
                }
            }
            TieneResenasPublicadas = ResenasPublicadas.Count > 0;
            NoTieneResenasPublicadas = !TieneResenasPublicadas;

            // 3. Filtrar citas completadas que NO hayan sido calificadas aún
            var completadas = citas.Where(c =>
                (c.Estado?.Equals("COMPLETADA", StringComparison.OrdinalIgnoreCase) == true ||
                 c.Estado?.Equals("COMPLETED", StringComparison.OrdinalIgnoreCase) == true) &&
                !idsCitasCalificadas.Contains(c.Id)
            ).ToList();

            CitasPendientesCalificar.Clear();
            foreach (var cita in completadas)
            {
                CitasPendientesCalificar.Add(cita);
            }

            TieneCitasPendientes = CitasPendientesCalificar.Count > 0;
        }
        catch (Exception ex)
        {
            ErrorMessage = "No se pudieron sincronizar las reseñas: " + ex.Message;
            System.Diagnostics.Debug.WriteLine($"[MyReviewsViewModel] Error: {ex.Message}");
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async Task IrACalificarAsync(Reserva cita)
    {
        if (cita == null) return;

        await Shell.Current.GoToAsync("LeaveReviewPage", new Dictionary<string, object>
        {
            { "reserva", cita }
        });
    }

    [RelayCommand]
    private async Task IrADetalleResenaAsync(Resena resena)
    {
        if (resena == null) return;

        await Shell.Current.GoToAsync("ReviewDetailPage", new Dictionary<string, object>
        {
            { "resenaId", resena.Id }
        });
    }

    [RelayCommand]
    private async Task RegresarAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
