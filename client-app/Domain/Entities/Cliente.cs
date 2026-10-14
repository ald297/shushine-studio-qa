namespace ShushineStudio.Mobile.Domain.Entities;

public class Cliente
{
    public long Id { get; set; }
    public string NombreCompleto { get; set; } = string.Empty;
    public string? Telefono { get; set; }
    public string? Correo { get; set; }
    public string? NivelFidelidad { get; set; } = "Bronce";
    public int PuntosAcumulados { get; set; } = 0;
    public string? TipoCabello { get; set; }
    public string? NotasPreferencias { get; set; }
    public bool EsWalkin { get; set; } = false;
    public DateTime FechaCreacion { get; set; } = DateTime.Now;

    public int TotalCitas { get; set; } = 0;
    public List<Reserva> Citas { get; set; } = new();

    public string TipoClienteDisplay => EsWalkin ? "Walk-in (Presencial)" : "Cliente Registrado";
    public string TipoClienteColor => EsWalkin ? "#C5A059" : "#9B2D47";

    public string Iniciales
    {
        get
        {
            if (string.IsNullOrWhiteSpace(NombreCompleto)) return "CL";
            var partes = NombreCompleto.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            if (partes.Length == 1) return partes[0].Substring(0, Math.Min(2, partes[0].Length)).ToUpperInvariant();
            return string.Concat(partes.Take(2).Select(p => p[0])).ToUpperInvariant();
        }
    }
}
