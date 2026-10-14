namespace ShushineStudio.Mobile.Domain.Entities;

public class FranjaHoraria
{
    public string HoraInicio { get; set; } = string.Empty;
    public string HoraFin { get; set; } = string.Empty;
    public bool Disponible { get; set; }
    public string? MotivoNoDisponible { get; set; }

    public string HorarioTexto => $"{HoraInicio} - {HoraFin}";
}

public class DisponibilidadEstilista
{
    public long EstilistaId { get; set; }
    public string EstilistaNombre { get; set; } = string.Empty;
    public string Fecha { get; set; } = string.Empty;
    public List<FranjaHoraria> Franjas { get; set; } = new();
}
