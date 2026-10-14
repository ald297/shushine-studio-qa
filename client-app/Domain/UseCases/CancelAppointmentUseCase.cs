using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class CancelAppointmentUseCase
{
    private readonly IReservaRepository _repository;

    public CancelAppointmentUseCase(IReservaRepository repository)
    {
        _repository = repository;
    }

    public async Task<bool> ExecuteAsync(long reservaId, string? motivo = null)
    {
        return await _repository.CancelarReservaAsync(reservaId, motivo);
    }
}
