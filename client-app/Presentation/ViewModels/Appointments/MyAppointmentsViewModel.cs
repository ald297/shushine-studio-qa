using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;
using ShushineStudio.Mobile.Domain.UseCases;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Appointments;

/// <summary>
/// ViewModel reactivo para la gestión e historial de citas del cliente.
/// Soporta pestañas para citas próximas e historial, navegación a detalle y cancelación confirmada.
/// </summary>
public partial class MyAppointmentsViewModel : BaseViewModel
{
	private readonly GetMyAppointmentsUseCase _getMyAppointmentsUseCase;
	private readonly CancelAppointmentUseCase _cancelAppointmentUseCase;
	private readonly IReservaRepository _reservaRepository;

	[ObservableProperty]
	private ObservableCollection<Reserva> citasProximas = new();

	[ObservableProperty]
	private ObservableCollection<Reserva> citasPasadas = new();

	[ObservableProperty]
	private bool mostrarFuturas = true;

	[ObservableProperty]
	private bool mostrarPasadas = false;

	[ObservableProperty]
	private int totalProximas;

	[ObservableProperty]
	private int totalPasadas;

	[ObservableProperty]
	private bool hasProximas;

	[ObservableProperty]
	private bool hasPasadas;

	public MyAppointmentsViewModel(
		GetMyAppointmentsUseCase getMyAppointmentsUseCase,
		CancelAppointmentUseCase cancelAppointmentUseCase,
		IReservaRepository reservaRepository)
	{
		_getMyAppointmentsUseCase = getMyAppointmentsUseCase;
		_cancelAppointmentUseCase = cancelAppointmentUseCase;
		_reservaRepository = reservaRepository;
		Title = "Mis Citas";
		_ = LoadCitasAsync();
	}

	[RelayCommand]
	private void VerFuturas()
	{
		MostrarFuturas = true;
		MostrarPasadas = false;
	}

	[RelayCommand]
	private void VerPasadas()
	{
		MostrarFuturas = false;
		MostrarPasadas = true;
	}

	[RelayCommand]
	public async Task LoadCitasAsync()
	{
		if (IsBusy) return;

		try
		{
			IsBusy = true;
			ErrorMessage = null;

			var citas = await _getMyAppointmentsUseCase.ExecuteAsync();
			CitasProximas.Clear();
			CitasPasadas.Clear();

			var now = DateTime.Now;
			foreach (var cita in citas)
			{
				var estado = (cita.Estado ?? string.Empty).Trim().ToUpperInvariant();
				// Próximas: estados activos y fechas no vencidas
				if ((estado == "PENDIENTE" || estado == "CONFIRMADA" || estado == "CONFIRMED" || estado == "PENDING" || estado == "EN PROCESO" || estado == "EN_PROCESO")
					&& cita.FechaHoraInicio >= now.Date)
				{
					CitasProximas.Add(cita);
				}
				else
				{
					CitasPasadas.Add(cita);
				}
			}

			TotalProximas = CitasProximas.Count;
			TotalPasadas = CitasPasadas.Count;
			HasProximas = CitasProximas.Count > 0;
			HasPasadas = CitasPasadas.Count > 0;
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
	private async Task VerDetalleCitaAsync(Reserva reserva)
	{
		if (reserva == null) return;

		try
		{
			await Shell.Current.GoToAsync("AppointmentDetailPage", new Dictionary<string, object>
			{
				{ "reserva", reserva },
				{ "reservaId", reserva.Id }
			});
		}
		catch (Exception ex)
		{
			await Application.Current.MainPage!.DisplayAlert("Error de Navegación", ex.Message, "Entendido");
		}
	}

	[RelayCommand]
	private async Task CancelarCitaAsync(Reserva reserva)
	{
		if (reserva == null || Application.Current?.MainPage == null || IsBusy) return;

		bool confirm = await Application.Current.MainPage.DisplayAlert(
			"Cancelar Reserva",
			$"¿Estás segura de cancelar tu cita para {reserva.ServicioNombre} ({reserva.CodigoReserva})?",
			"Sí, Cancelar",
			"Mantener Cita"
		);

		if (!confirm) return;

		try
		{
			IsBusy = true;
			var success = await _cancelAppointmentUseCase.ExecuteAsync(reserva.Id);
			if (success)
			{
				// Actualización visual reactiva inmediata
				CitasProximas.Remove(reserva);
				reserva.Estado = "Cancelled";
				CitasPasadas.Insert(0, reserva);
				TotalProximas = CitasProximas.Count;
				TotalPasadas = CitasPasadas.Count;
				HasProximas = CitasProximas.Count > 0;
				HasPasadas = CitasPasadas.Count > 0;

				await Application.Current.MainPage.DisplayAlert(
					"Cita Cancelada",
					"Tu reserva ha sido cancelada exitosamente y el horario del especialista ha sido liberado.",
					"Aceptar"
				);
				await LoadCitasAsync();
			}
			else
			{
				await Application.Current.MainPage.DisplayAlert(
					"Aviso",
					"No fue posible cancelar la cita en este momento. Por favor intenta más tarde o comunícate con el salón.",
					"Entendido"
				);
			}
		}
		catch (Exception ex)
		{
			await Application.Current.MainPage.DisplayAlert("Error", ex.Message, "Entendido");
		}
		finally
		{
			IsBusy = false;
		}
	}

	[RelayCommand]
	private async Task ReprogramarCitaAsync(Reserva reserva)
	{
		if (reserva == null) return;

		try
		{
			if (reserva.ServicioId > 0)
			{
				await Shell.Current.GoToAsync($"ServiceDetailPage?servicioId={reserva.ServicioId}");
			}
			else
			{
				await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
			}
		}
		catch
		{
			try
			{
				await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
			}
			catch
			{
				// Navegacion fallback
			}
		}
	}

	[RelayCommand]
	private async Task ExplorarServiciosAsync()
	{
		try
		{
			await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
		}
		catch
		{
			// Fallback
		}
	}
}
