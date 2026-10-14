namespace ShushineStudio.Mobile.Domain.Entities;

public class Reserva
{
    public long Id { get; set; }
    public string CodigoCita { get; set; } = string.Empty;

    public string CodigoReserva
    {
        get => !string.IsNullOrEmpty(CodigoCita) ? CodigoCita : "#SHU-0000";
        set => CodigoCita = value;
    }

    public long ClienteId { get; set; }
    public string ClienteNombre { get; set; } = string.Empty;
    public string? ClienteTelefono { get; set; }

    public long EstilistaId { get; set; }
    public string EstilistaNombre { get; set; } = string.Empty;

    public long ServicioId { get; set; }

    public string FechaCita { get; set; } = string.Empty; // "yyyy-MM-dd"
    public string HoraInicio { get; set; } = string.Empty; // "HH:mm"
    public string HoraFin { get; set; } = string.Empty; // "HH:mm"

    private DateTime? _fechaHoraInicio;
    public DateTime FechaHoraInicio
    {
        get
        {
            if (_fechaHoraInicio.HasValue) return _fechaHoraInicio.Value;
            if (DateTime.TryParse($"{FechaCita} {HoraInicio}", out var dt))
                return dt;
            return DateTime.MinValue;
        }
        set
        {
            _fechaHoraInicio = value;
            FechaCita = value.ToString("yyyy-MM-dd");
            HoraInicio = value.ToString("HH:mm");
        }
    }

    private DateTime? _fechaHoraFin;
    public DateTime FechaHoraFin
    {
        get
        {
            if (_fechaHoraFin.HasValue) return _fechaHoraFin.Value;
            if (DateTime.TryParse($"{FechaCita} {HoraFin}", out var dt))
                return dt;
            return DateTime.MinValue;
        }
        set
        {
            _fechaHoraFin = value;
            HoraFin = value.ToString("HH:mm");
        }
    }

    public string Estado { get; set; } = "PENDIENTE";
    public decimal Subtotal { get; set; }
    public decimal Iva { get; set; }
    public decimal Total { get; set; }

    public string MetodoPagoPreferente { get; set; } = "Efectivo";
    public string EstadoPago { get; set; } = "PENDIENTE";
    public string? Notas { get; set; }

    public List<string> ServiciosNombres { get; set; } = new();

    private string? _servicioNombre;
    public string ServicioNombre
    {
        get => !string.IsNullOrEmpty(_servicioNombre) ? _servicioNombre : ServiciosResumen;
        set => _servicioNombre = value;
    }

    public string ServiciosResumen => ServiciosNombres != null && ServiciosNombres.Count > 0
        ? string.Join(", ", ServiciosNombres)
        : "Servicio de Belleza";

    public string TotalFormateado => $"${Total:N2}";
    public string SubtotalFormateado => $"${Subtotal:N2}";
    public string IvaFormateado => $"${Iva:N2}";

    public string HorarioTexto => $"{HoraInicio} - {HoraFin}";

    public string EstadoDisplay => Estado?.ToUpperInvariant() switch
    {
        "CONFIRMED" or "CONFIRMADA" => "Confirmada",
        "INPROGRESS" or "IN_PROGRESS" or "EN_PROCESO" => "En proceso",
        "COMPLETED" or "COMPLETADA" => "Completada",
        "CANCELLED" or "CANCELADA" => "Cancelada",
        _ => !string.IsNullOrEmpty(Estado) ? Estado : "Pendiente"
    };

    public string EstadoColor => Estado?.ToUpperInvariant() switch
    {
        "CONFIRMED" or "CONFIRMADA" => "#2E7D32", // Verde bosque
        "COMPLETED" or "COMPLETADA" => "#1565C0", // Azul primario
        "CANCELLED" or "CANCELADA" => "#C62828",  // Rojo alerta
        "INPROGRESS" or "IN_PROGRESS" or "EN_PROCESO" => "#E65100", // Naranja
        _ => "#C5A059"            // Warm Gold
    };

    public bool PuedeCancelar => Estado?.ToUpperInvariant() is "PENDIENTE" or "CONFIRMED" or "CONFIRMADA";
}
