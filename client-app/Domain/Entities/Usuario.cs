namespace ShushineStudio.Mobile.Domain.Entities;

public class Usuario
{
    public long Id { get; set; }
    public string Login { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string NombreCompleto { get; set; } = string.Empty;
    public string Nombre { get; set; } = string.Empty;
    public string? Apellido { get; set; }
    public string Rol { get; set; } = "CLIENTE";
    public string? Telefono { get; set; }
    public bool Activo { get; set; } = true;
    public string? NivelFidelidad { get; set; } = "Bronce";
    public int PuntosAcumulados { get; set; } = 0;

    public string Iniciales => string.Concat(
        NombreCompleto.Split(' ', StringSplitOptions.RemoveEmptyEntries)
            .Take(2)
            .Select(s => s[0])
    ).ToUpper();
}
