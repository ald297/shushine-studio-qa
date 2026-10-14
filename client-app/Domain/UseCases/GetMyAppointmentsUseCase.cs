using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class GetMyAppointmentsUseCase
{
    private readonly IReservaRepository _repository;

    public GetMyAppointmentsUseCase(IReservaRepository repository)
    {
        _repository = repository;
    }

    public async Task<IEnumerable<Reserva>> ExecuteAsync()
    {
        return await _repository.GetMisCitasAsync();
    }
}
