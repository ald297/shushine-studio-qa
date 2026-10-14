using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Domain.UseCases;

public class GetServiciosCatalogUseCase
{
    private readonly IServicioRepository _repository;

    public GetServiciosCatalogUseCase(IServicioRepository repository)
    {
        _repository = repository;
    }

    public async Task<IEnumerable<Servicio>> ExecuteAsync(long? categoriaId = null)
    {
        return await _repository.GetServiciosAsync(categoriaId);
    }
}
