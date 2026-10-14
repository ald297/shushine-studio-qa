using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IAuthRepository
{
    Task<bool> LoginAsync(string emailOrLogin, string password);
    Task<bool> RegisterAsync(string nombre, string email, string password, string telefono);
    Task<bool> RegisterAsync(string login, string clave, string nombre, string apellido, string email, string telefono);
    Task LogoutAsync();
    Task<bool> IsAuthenticatedAsync();
    Task<Usuario?> GetCurrentUserAsync();
    Task<bool> CambiarClaveAsync(string claveActual, string nuevaClave, string confirmarClave);
    Task<Usuario?> ActualizarPerfilAsync(string nombre, string apellido, string telefono, string? correo);
}
