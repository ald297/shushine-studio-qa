using System.Text.Json.Serialization;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Data.Dtos;

public class LoginRequestDto
{
    [JsonPropertyName("login")]
    public string Login { get; set; } = string.Empty;

    [JsonPropertyName("clave")]
    public string Clave { get; set; } = string.Empty;
}

public class RegisterRequestDto
{
    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("apellido")]
    public string Apellido { get; set; } = string.Empty;

    [JsonPropertyName("telefono")]
    public string? Telefono { get; set; }

    [JsonPropertyName("login")]
    public string Login { get; set; } = string.Empty;

    [JsonPropertyName("clave")]
    public string Clave { get; set; } = string.Empty;

    [JsonPropertyName("rolId")]
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public int? RolId { get; set; } = null;
}

public class AuthResponseDto
{
    [JsonPropertyName("token")]
    public string Token { get; set; } = string.Empty;

    [JsonPropertyName("id")]
    public string? Id { get; set; }

    [JsonPropertyName("login")]
    public string? Login { get; set; }

    [JsonPropertyName("nombre")]
    public string? Nombre { get; set; }

    [JsonPropertyName("rol")]
    public string Rol { get; set; } = "CLIENTE";
}

public class UsuarioPerfilDto
{
    [JsonPropertyName("id")]
    public string? Id { get; set; }

    [JsonPropertyName("login")]
    public string Login { get; set; } = string.Empty;

    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [JsonPropertyName("apellido")]
    public string? Apellido { get; set; }

    [JsonPropertyName("correo")]
    public string? Correo { get; set; }

    [JsonPropertyName("telefono")]
    public string? Telefono { get; set; }

    [JsonPropertyName("rol")]
    public string Rol { get; set; } = "CLIENTE";

    [JsonPropertyName("activo")]
    public bool Activo { get; set; } = true;

    [JsonPropertyName("puntosAcumulados")]
    public int PuntosAcumulados { get; set; } = 0;

    [JsonPropertyName("nivelFidelidad")]
    public string NivelFidelidad { get; set; } = "Bronce";

    [JsonPropertyName("tipoCabello")]
    public string? TipoCabello { get; set; }

    [JsonPropertyName("notasPreferencias")]
    public string? NotasPreferencias { get; set; }

    public Usuario ToEntity() => new()
    {
        Login = Login,
        Email = !string.IsNullOrWhiteSpace(Correo) ? Correo : (Login.Contains("@") ? Login : $"{Login}@shushinestudio.com"),
        NombreCompleto = !string.IsNullOrEmpty(Apellido) ? $"{Nombre} {Apellido}" : Nombre,
        Nombre = Nombre,
        Apellido = Apellido,
        Rol = Rol,
        Telefono = Telefono,
        Activo = Activo,
        NivelFidelidad = NivelFidelidad,
        PuntosAcumulados = PuntosAcumulados
    };
}
