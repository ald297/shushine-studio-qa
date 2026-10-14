using System.Net.Http.Json;
using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class EstilistaRepository : IEstilistaRepository
{
    private readonly HttpClient _httpClient;

    public EstilistaRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<Estilista>> GetEstilistasAsync()
    {
        try
        {
            var dtos = await _httpClient.GetFromJsonAsync<List<EstilistaDto>>("estilistas");
            return dtos?.Where(e => e.Activo).Select(e => e.ToEntity()) ?? Enumerable.Empty<Estilista>();
        }
        catch (Exception)
        {
            return Enumerable.Empty<Estilista>();
        }
    }

    public async Task<Estilista?> GetEstilistaByIdAsync(long id)
    {
        try
        {
            var dto = await _httpClient.GetFromJsonAsync<EstilistaDto>($"estilistas/{id}");
            return dto?.ToEntity();
        }
        catch (Exception)
        {
            return null;
        }
    }

    public async Task<DisponibilidadEstilista?> GetDisponibilidadAsync(long estilistaId, DateTime fecha, long servicioId)
    {
        try
        {
            var fechaStr = fecha.ToString("yyyy-MM-dd");
            var url = $"estilistas/{estilistaId}/disponibilidad?fecha={fechaStr}&servicioId={servicioId}";
            var dto = await _httpClient.GetFromJsonAsync<DisponibilidadSalidaDto>(url);
            return dto?.ToEntity();
        }
        catch (Exception)
        {
            return null;
        }
    }

    public async Task<DisponibilidadEstilista?> GetDisponibilidadHorariaAsync(long estilistaId, DateTime fecha, int duracionMinutos = 30)
    {
        try
        {
            var fechaStr = fecha.ToString("yyyy-MM-dd");
            var url = $"estilistas/{estilistaId}/disponibilidad?fecha={fechaStr}&duracionMinutos={duracionMinutos}";
            var dto = await _httpClient.GetFromJsonAsync<DisponibilidadSalidaDto>(url);
            return dto?.ToEntity();
        }
        catch (Exception)
        {
            return null;
        }
    }

    public async Task<IEnumerable<Estilista>> GetTodosEstilistasAsync()
    {

        try
        {
            var dtos = await _httpClient.GetFromJsonAsync<List<EstilistaDto>>("estilistas");
            return dtos?.Select(e => e.ToEntity()) ?? Enumerable.Empty<Estilista>();
        }
        catch (Exception)
        {
            return Enumerable.Empty<Estilista>();
        }
    }

    public async Task<bool> ActualizarEstadoEstilistaAsync(long id, bool activo)
    {
        try
        {
            var response = await _httpClient.PutAsJsonAsync($"estilistas/{id}/estado?activo={activo}", new { });
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }

    public async Task<bool> CrearEstilistaAsync(Estilista estilista)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("estilistas", new
            {
                nombreCompleto = estilista.NombreCompleto,
                especialidadPrincipal = estilista.EspecialidadPrincipal,
                colorAgenda = estilista.ColorAgenda,
                activo = estilista.Activo
            });
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }

    public async Task<bool> EliminarEstilistaAsync(long id)
    {
        try
        {
            var response = await _httpClient.DeleteAsync($"estilistas/{id}");
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }
}

