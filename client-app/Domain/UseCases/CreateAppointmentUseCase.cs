using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class CreateAppointmentRequest
{
    public long ServicioId { get; set; }
    public long EstilistaId { get; set; }
    public DateTime FechaHoraInicio { get; set; }
    public string? Notas { get; set; }
    public string MetodoPago { get; set; } = "Efectivo";
}

public class CreateAppointmentUseCase
{
    private readonly IReservaRepository _repository;

    public CreateAppointmentUseCase(IReservaRepository repository)
    {
        _repository = repository;
    }

    public async Task<Reserva?> ExecuteAsync(CreateAppointmentRequest request)
    {
        if (request == null) return null;
        var horaInicio = request.FechaHoraInicio.ToString("HH:mm");
        var servicioIds = new List<int> { (int)request.ServicioId };
        return await _repository.CrearReservaAsync(
            request.EstilistaId,
            request.FechaHoraInicio.Date,
            horaInicio,
            servicioIds,
            request.Notas,
            request.MetodoPago
        );
    }

    public async Task<Reserva?> ExecuteAsync(
        long estilistaId,
        DateTime fechaCita,
        string horaInicio,
        List<int> servicioIds,
        string? notas = null,
        string metodoPago = "Efectivo")
    {
        return await _repository.CrearReservaAsync(estilistaId, fechaCita, horaInicio, servicioIds, notas, metodoPago);
    }

    // Sobrecarga de conveniencia para un solo servicio
    public async Task<Reserva?> ExecuteAsync(
        long servicioId,
        long? estilistaId,
        DateTime fechaHora,
        string? notas)
    {
        return await _repository.CrearReservaAsync(servicioId, estilistaId, fechaHora, notas);
    }
}
