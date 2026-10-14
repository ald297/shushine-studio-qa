using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class GetEstilistasUseCase
{
    private readonly IEstilistaRepository _repository;

    public GetEstilistasUseCase(IEstilistaRepository repository)
    {
        _repository = repository;
    }

    public async Task<IEnumerable<Estilista>> ExecuteAsync()
    {
        return await _repository.GetEstilistasAsync();
    }
}
