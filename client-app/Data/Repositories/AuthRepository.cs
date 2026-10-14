using System.Net.Http.Json;
using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Data.Services;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

public class AuthRepository : IAuthRepository
{
    private readonly HttpClient _httpClient;
    private readonly ITokenStorageService _tokenStorage;

    public AuthRepository(HttpClient httpClient, ITokenStorageService tokenStorage)
    {
        _httpClient = httpClient;
        _tokenStorage = tokenStorage;
    }

    public async Task<bool> LoginAsync(string emailOrLogin, string password)
    {
        // 1. Limpiar cualquier token previo residual para garantizar un inicio limpio
        await _tokenStorage.ClearAsync();
        _httpClient.DefaultRequestHeaders.Authorization = null;

        var cleanInput = emailOrLogin.Trim();
        var request = new LoginRequestDto
        {
            Login = cleanInput,
            Clave = password
        };

        HttpResponseMessage response;
        try
        {
            response = await _httpClient.PostAsJsonAsync("auth/login", request);

            // Si falla con 401 y hay variantes comunes (ej. mayúscula por autocapitalización o correo con @)
            if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
            {
                var lowerLogin = cleanInput.ToLowerInvariant();
                if (lowerLogin != cleanInput)
                {
                    var lowerRequest = new LoginRequestDto { Login = lowerLogin, Clave = password };
                    var lowerResponse = await _httpClient.PostAsJsonAsync("auth/login", lowerRequest);
                    if (lowerResponse.IsSuccessStatusCode)
                    {
                        response = lowerResponse;
                    }
                }
                else if (cleanInput.Contains("@"))
                {
                    var fallbackUsername = cleanInput.Split('@')[0].ToLowerInvariant();
                    var fallbackRequest = new LoginRequestDto { Login = fallbackUsername, Clave = password };
                    var fallbackResponse = await _httpClient.PostAsJsonAsync("auth/login", fallbackRequest);
                    if (fallbackResponse.IsSuccessStatusCode)
                    {
                        response = fallbackResponse;
                    }
                }
            }
        }
        catch (Exception ex) when (ex is System.IO.IOException 
                                || ex.Message.Contains("Socket", StringComparison.OrdinalIgnoreCase) 
                                || ex.Message.Contains("closed", StringComparison.OrdinalIgnoreCase)
                                || ex.Message.Contains("reset", StringComparison.OrdinalIgnoreCase))
        {
            // Reintento único transparente ante inicio en frío (cold start) de Render
            try
            {
                await Task.Delay(2500);
                response = await _httpClient.PostAsJsonAsync("auth/login", request);
            }
            catch
            {
                await _tokenStorage.ClearAsync();
                throw new HttpRequestException("El servidor del salón está iniciando en la nube. Por favor presiona 'Iniciar Sesión' nuevamente.");
            }
        }
        catch (TaskCanceledException)
        {
            await _tokenStorage.ClearAsync();
            throw new TimeoutException("El servidor del salón está iniciando y tardó en responder. Por favor presiona 'Iniciar Sesión' nuevamente.");
        }
        catch (HttpRequestException)
        {
            await _tokenStorage.ClearAsync();
            throw new HttpRequestException("No se pudo conectar con el servidor del salón. Por favor verifica tu acceso a internet.");
        }

        // 2. Procesar respuesta según código HTTP exacto
        if (response.IsSuccessStatusCode)
        {
            var authResult = await response.Content.ReadFromJsonAsync<AuthResponseDto>();
            if (authResult != null && !string.IsNullOrWhiteSpace(authResult.Token))
            {
                await _tokenStorage.SaveTokenAsync(
                    authResult.Token, 
                    null, 
                    authResult.Rol, 
                    authResult.Id ?? authResult.Login ?? "user"
                );
                return true;
            }
            await _tokenStorage.ClearAsync();
            throw new InvalidOperationException("La respuesta del servidor no contiene un token válido.");
        }

        await _tokenStorage.ClearAsync();

        // 3. Diferenciar errores estrictamente
        switch (response.StatusCode)
        {
            case System.Net.HttpStatusCode.Unauthorized:
                throw new InvalidOperationException("El usuario o contraseña ingresados son incorrectos. Por favor verifica tus credenciales.");

            case System.Net.HttpStatusCode.Forbidden:
                throw new InvalidOperationException("Acceso restringido. Tu cuenta no cuenta con permisos suficientes o está suspendida.");

            case System.Net.HttpStatusCode.NotFound:
                throw new InvalidOperationException("El servicio de autenticación no fue encontrado en el servidor (404).");

            case System.Net.HttpStatusCode.Conflict:
                throw new InvalidOperationException("Existe un conflicto de sesión activo en el servidor (409).");

            case System.Net.HttpStatusCode.InternalServerError:
            case System.Net.HttpStatusCode.BadGateway:
            case System.Net.HttpStatusCode.ServiceUnavailable:
            case System.Net.HttpStatusCode.GatewayTimeout:
                throw new InvalidOperationException("El servidor del salón está iniciando o en mantenimiento momentáneo. Por favor espera unos momentos e intenta de nuevo.");

            default:
                throw new InvalidOperationException($"Error de autenticación ({response.StatusCode}). Por favor intenta de nuevo.");
        }
    }

    public Task<bool> RegisterAsync(string nombre, string email, string password, string telefono)
    {
        var partes = nombre.Split(' ', 2, StringSplitOptions.RemoveEmptyEntries);
        var nom = partes.Length > 0 ? partes[0] : nombre;
        var ape = partes.Length > 1 ? partes[1] : "Cliente";
        var login = email.Contains("@") ? email.Split('@')[0] : email;

        return RegisterAsync(login, password, nom, ape, email, telefono);
    }

    public async Task<bool> RegisterAsync(string login, string clave, string nombre, string apellido, string email, string telefono)
    {
        try
        {
            var request = new RegisterRequestDto
            {
                Login = !string.IsNullOrWhiteSpace(login) ? login.Trim() : email.Trim(),
                Clave = clave,
                Nombre = nombre.Trim(),
                Apellido = apellido.Trim(),
                Telefono = telefono?.Trim(),
                RolId = null // Backend asigna automáticamente CLIENTE
            };

            var response = await _httpClient.PostAsJsonAsync("auth/registro", request);
            if (response.IsSuccessStatusCode)
            {
                var authResult = await response.Content.ReadFromJsonAsync<AuthResponseDto>();
                if (authResult != null && !string.IsNullOrWhiteSpace(authResult.Token))
                {
                    await _tokenStorage.SaveTokenAsync(
                        authResult.Token, 
                        null, 
                        authResult.Rol, 
                        authResult.Id ?? authResult.Login ?? "user"
                    );
                    return true;
                }
            }
            else if (!string.IsNullOrWhiteSpace(email) && request.Login != email.Trim())
            {
                // Si el alias extraído del correo ya existía, intentar registrar con el correo completo como login único
                request.Login = email.Trim();
                var retryResponse = await _httpClient.PostAsJsonAsync("auth/registro", request);
                if (retryResponse.IsSuccessStatusCode)
                {
                    var authResult = await retryResponse.Content.ReadFromJsonAsync<AuthResponseDto>();
                    if (authResult != null && !string.IsNullOrWhiteSpace(authResult.Token))
                    {
                        await _tokenStorage.SaveTokenAsync(
                            authResult.Token, 
                            null, 
                            authResult.Rol, 
                            authResult.Id ?? authResult.Login ?? "user"
                        );
                        return true;
                    }
                }
            }
        }
        catch (Exception)
        {
            return false;
        }

        return false;
    }

    public async Task LogoutAsync()
    {
        await _tokenStorage.ClearAsync();
        _httpClient.DefaultRequestHeaders.Authorization = null;
    }

    public async Task<bool> IsAuthenticatedAsync()
    {
        return await _tokenStorage.HasValidTokenAsync();
    }

    public async Task<Usuario?> GetCurrentUserAsync()
    {
        if (!await IsAuthenticatedAsync()) return null;

        try
        {
            // Intentar obtener perfil actualizado desde el endpoint oficial GET /api/auth/me
            var response = await _httpClient.GetAsync("auth/me");
            if (response.IsSuccessStatusCode)
            {
                var perfilDto = await response.Content.ReadFromJsonAsync<UsuarioPerfilDto>();
                if (perfilDto != null)
                {
                    return perfilDto.ToEntity();
                }
            }
        }
        catch (Exception)
        {
            // Fallback a almacenamiento local si se está fuera de línea
        }

        var role = await _tokenStorage.GetRoleAsync() ?? "CLIENTE";
        var userId = await _tokenStorage.GetUserIdAsync() ?? "cliente";
        return new Usuario
        {
            Login = userId,
            NombreCompleto = userId,
            Rol = role
        };
    }

    public async Task<bool> CambiarClaveAsync(string claveActual, string nuevaClave, string confirmarClave)
    {
        var body = new
        {
            claveActual = claveActual,
            nuevaClave = nuevaClave,
            confirmarClave = confirmarClave
        };

        var response = await _httpClient.PutAsJsonAsync("auth/cambiar-clave", body);
        if (!response.IsSuccessStatusCode)
        {
            var errorContent = await response.Content.ReadAsStringAsync();
            throw new InvalidOperationException(!string.IsNullOrWhiteSpace(errorContent) ? errorContent : "Error al cambiar la contraseña en el servidor.");
        }
        return true;
    }

    public async Task<Usuario?> ActualizarPerfilAsync(string nombre, string apellido, string telefono, string? correo)
    {
        var body = new
        {
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            correo = correo
        };

        var response = await _httpClient.PutAsJsonAsync("auth/perfil", body);
        response.EnsureSuccessStatusCode();

        var perfilDto = await response.Content.ReadFromJsonAsync<UsuarioPerfilDto>();
        return perfilDto?.ToEntity();
    }
}

