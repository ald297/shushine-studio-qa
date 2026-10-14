using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;
using ShushineStudio.Mobile.Data.Services;

namespace ShushineStudio.Mobile.Data.Repositories;

public class ClienteRepository : IClienteRepository
{
    private readonly HttpClient _httpClient;
    private readonly IReservaRepository _reservaRepository;
    private readonly ITokenStorageService _tokenStorageService;

    public ClienteRepository(
        HttpClient httpClient,
        IReservaRepository reservaRepository,
        ITokenStorageService tokenStorageService)
    {
        _httpClient = httpClient;
        _reservaRepository = reservaRepository;
        _tokenStorageService = tokenStorageService;
    }

    public async Task<List<Cliente>> ObtenerClientesAsync(int page = 0, int size = 50)
    {

        try
        {
            var response = await _httpClient.GetAsync($"admin/clientes?page={page}&size={size}");
            if (!response.IsSuccessStatusCode)
            {
                response = await _httpClient.GetAsync($"clientes?page={page}&size={size}");
            }

            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);

                JsonElement arrayElement;
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = doc.RootElement;
                }
                else if (doc.RootElement.TryGetProperty("content", out var contentElem) && contentElem.ValueKind == JsonValueKind.Array)
                {
                    arrayElement = contentElem;
                }
                else
                {
                    arrayElement = default;
                }

                if (arrayElement.ValueKind == JsonValueKind.Array)
                {
                    var lista = new List<Cliente>();
                    foreach (var elem in arrayElement.EnumerateArray())
                    {
                        var cliente = new Cliente
                        {
                            Id = elem.TryGetProperty("id", out var idProp) ? idProp.GetInt64() : 0,
                            NombreCompleto = elem.TryGetProperty("nombreCompleto", out var ncProp) ? ncProp.GetString() ?? "" :
                                             elem.TryGetProperty("nombre", out var nProp) ? nProp.GetString() ?? "" : "Cliente",
                            Telefono = elem.TryGetProperty("telefono", out var tProp) ? tProp.GetString() : null,
                            Correo = elem.TryGetProperty("correo", out var cProp) ? cProp.GetString() :
                                     elem.TryGetProperty("email", out var eProp) ? eProp.GetString() : null,
                            NivelFidelidad = elem.TryGetProperty("nivelFidelidad", out var nfProp) ? nfProp.GetString() : "Bronce",
                            PuntosAcumulados = elem.TryGetProperty("puntosAcumulados", out var paProp) ? paProp.GetInt32() : 0,
                            TotalCitas = elem.TryGetProperty("totalCitas", out var tcProp) ? tcProp.GetInt32() : 0,
                            EsWalkin = elem.TryGetProperty("esWalkin", out var ewProp) && ewProp.GetBoolean()
                        };
                        lista.Add(cliente);
                    }
                    return lista;
                }
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ClienteRepository] Error al consultar /api/admin/clientes: {ex.Message}");
        }

        return new List<Cliente>();
    }

    public async Task<Cliente?> ObtenerClientePorIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"admin/clientes/{id}");
            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);
                var elem = doc.RootElement;
                return new Cliente
                {
                    Id = elem.TryGetProperty("id", out var idProp) ? idProp.GetInt64() : id,
                    NombreCompleto = elem.TryGetProperty("nombreCompleto", out var ncProp) ? ncProp.GetString() ?? "" : "Cliente",
                    Telefono = elem.TryGetProperty("telefono", out var tProp) ? tProp.GetString() : null,
                    Correo = elem.TryGetProperty("correo", out var cProp) ? cProp.GetString() : null,
                    NivelFidelidad = elem.TryGetProperty("nivelFidelidad", out var nfProp) ? nfProp.GetString() : "Bronce",
                    PuntosAcumulados = elem.TryGetProperty("puntosAcumulados", out var paProp) ? paProp.GetInt32() : 0,
                    TotalCitas = elem.TryGetProperty("totalCitas", out var tcProp) ? tcProp.GetInt32() : 0,
                    EsWalkin = elem.TryGetProperty("esWalkin", out var ewProp) && ewProp.GetBoolean()
                };
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"[ClienteRepository] Error al consultar cliente por id: {ex.Message}");
        }

        var todos = await ObtenerClientesAsync(0, 100);
        return todos.FirstOrDefault(c => c.Id == id);
    }

    public async Task<List<Reserva>> ObtenerHistorialCitasClienteAsync(long clienteId)
    {
        try
        {
            var citas = await _reservaRepository.ObtenerTodasCitasAdminPaginadasAsync(0, 100);
            return citas?.Where(c => c.ClienteId == clienteId).ToList() ?? new List<Reserva>();
        }
        catch
        {
            return new List<Reserva>();
        }
    }
}
