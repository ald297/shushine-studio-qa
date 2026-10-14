using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class GetDisponibilidadUseCase
{
    private readonly IEstilistaRepository _repository;

    public GetDisponibilidadUseCase(IEstilistaRepository repository)
    {
        _repository = repository;
    }

    public async Task<DisponibilidadEstilista?> ExecuteAsync(long estilistaId, DateTime fecha, long servicioId)
    {
        return await _repository.GetDisponibilidadAsync(estilistaId, fecha, servicioId);
    }
}
