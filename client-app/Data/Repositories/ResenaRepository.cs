using System.Net.Http.Json;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class ResenaRepository : IResenaRepository
{
    private readonly HttpClient _httpClient;

    public ResenaRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<Resena> CrearResenaAsync(long idCita, int estrellas, string comentario, bool visiblePublica = true)
    {
        var body = new
        {
            idCita = idCita,
            estrellas = estrellas,
            comentario = comentario,
            visiblePublica = visiblePublica
        };

        var response = await _httpClient.PostAsJsonAsync("resenas", body);
        response.EnsureSuccessStatusCode();

        var resena = await response.Content.ReadFromJsonAsync<Resena>();
        return resena ?? throw new InvalidOperationException("No se pudo deserializar la respuesta de la reseña.");
    }

    public async Task<List<Resena>> GetMisResenasAsync()
    {
        var resenas = await _httpClient.GetFromJsonAsync<List<Resena>>("resenas/mis-resenas");
        return resenas ?? new List<Resena>();
    }

    public async Task<List<Resena>> GetResenasAdminAsync()
    {
        var resenas = await _httpClient.GetFromJsonAsync<List<Resena>>("admin/resenas");
        return resenas ?? new List<Resena>();
    }

    public async Task<Resena?> GetResenaPorIdAsync(long id)
    {
        return await _httpClient.GetFromJsonAsync<Resena>($"resenas/{id}");
    }
}
