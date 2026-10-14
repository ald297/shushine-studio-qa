using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;
using ShushineStudio.Mobile.Domain.UseCases;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Appointments;

/// <summary>
/// ViewModel reactivo para la pantalla de detalle de una cita específica.
/// Muestra datos reales de la reserva y permite ejecutar la cancelación oficial o re-agendamiento.
/// </summary>
public partial class AppointmentDetailViewModel : BaseViewModel, IQueryAttributable
{
	private readonly CancelAppointmentUseCase _cancelAppointmentUseCase;
	private readonly IReservaRepository _reservaRepository;

	[ObservableProperty]
	private Reserva? reserva;

	[ObservableProperty]
	private bool puedeCancelar;

	[ObservableProperty]
	private bool tieneNotas;

	[ObservableProperty]
	private bool puedeCalificar;

	public AppointmentDetailViewModel(
		CancelAppointmentUseCase cancelAppointmentUseCase,
		IReservaRepository reservaRepository)
	{
		_cancelAppointmentUseCase = cancelAppointmentUseCase;
		_reservaRepository = reservaRepository;
		Title = "Detalle de Cita";
	}

	public void ApplyQueryAttributes(IDictionary<string, object> query)
	{
		if (query.TryGetValue("reserva", out var reservaObj) && reservaObj is Reserva res)
		{
			CargarReserva(res);
			return;
		}

		if (query.TryGetValue("reservaId", out var idObj))
		{
			long id = 0;
			if (idObj is long lId) id = lId;
			else if (idObj is int iId) id = iId;
			else if (idObj is string sId && long.TryParse(sId, out var parsedId)) id = parsedId;

			if (id > 0)
			{
				_ = CargarPorIdAsync(id);
			}
		}
	}

	private void CargarReserva(Reserva res)
	{
		Reserva = res;
		PuedeCancelar = res.PuedeCancelar;
		PuedeCalificar = res.Estado?.ToUpper() is "COMPLETADA" or "COMPLETED";
		TieneNotas = !string.IsNullOrWhiteSpace(res.Notas);
	}

	public async Task CargarPorIdAsync(long id)
	{
		try
		{
			IsBusy = true;
			var citas = await _reservaRepository.GetMisCitasAsync();
			var match = citas.FirstOrDefault(c => c.Id == id);
			if (match != null)
			{
				CargarReserva(match);
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
	private async Task VolverAsync()
	{
		await Shell.Current.GoToAsync("..");
	}

	[RelayCommand]
	private async Task CancelarCitaAsync()
	{
		if (Reserva == null || IsBusy || Application.Current?.MainPage == null) return;

		bool confirm = await Application.Current.MainPage.DisplayAlert(
			"Cancelar Cita",
			$"¿Estás segura de cancelar tu cita para {Reserva.ServicioNombre} ({Reserva.CodigoReserva})?",
			"Sí, Cancelar",
			"Mantener Cita"
		);

		if (!confirm) return;

		try
		{
			IsBusy = true;
			var success = await _cancelAppointmentUseCase.ExecuteAsync(Reserva.Id);
			if (success)
			{
				Reserva.Estado = "CANCELADA";
				CargarReserva(Reserva);

				if (Application.Current?.MainPage != null)
				{
					await Application.Current.MainPage.DisplayAlert(
						"Cita Cancelada",
						"Tu reserva ha sido cancelada exitosamente y el horario ha sido liberado.",
						"Aceptar"
					);
				}
				await Shell.Current.GoToAsync("..");
			}
			else
			{
				if (Application.Current?.MainPage != null)
				{
					await Application.Current.MainPage.DisplayAlert(
						"Error",
						"No fue posible cancelar la cita. Por favor intenta más tarde.",
						"Aceptar"
					);
				}
			}
		}
		catch (Exception ex)
		{
			if (Application.Current?.MainPage != null)
			{
				await Application.Current.MainPage.DisplayAlert("Error", ex.Message, "Aceptar");
			}
		}
		finally
		{
			IsBusy = false;
		}
	}

	[RelayCommand]
	private async Task ReprogramarCitaAsync()
	{
		if (Reserva == null) return;

		try
		{
			if (Reserva.ServicioId > 0)
			{
				await Shell.Current.GoToAsync($"ServiceDetailPage?servicioId={Reserva.ServicioId}");
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
				// Fallback navigation
			}
		}
	}

	[RelayCommand]
	private async Task CalificarServicioAsync()
	{
		if (Reserva == null) return;

		await Shell.Current.GoToAsync("LeaveReviewPage", new Dictionary<string, object>
		{
			{ "reserva", Reserva }
		});
	}
}
