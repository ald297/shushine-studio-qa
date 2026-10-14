using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ShushineStudio.Mobile.Domain.Entities;

public class Estilista : INotifyPropertyChanged
{
    private bool _activo = true;
    private string _nombreCompleto = string.Empty;
    private string _especialidadPrincipal = string.Empty;

    public long Id { get; set; }

    public string NombreCompleto
    {
        get => _nombreCompleto;
        set
        {
            if (_nombreCompleto != value)
            {
                _nombreCompleto = value;
                OnPropertyChanged();
                OnPropertyChanged(nameof(Iniciales));
            }
        }
    }

    public string EspecialidadPrincipal
    {
        get => _especialidadPrincipal;
        set
        {
            if (_especialidadPrincipal != value)
            {
                _especialidadPrincipal = value;
                OnPropertyChanged();
                OnPropertyChanged(nameof(Especialidad));
            }
        }
    }

    public string? Biografia { get; set; }
    public string? AvatarUrl { get; set; }
    public string ColorAgenda { get; set; } = "#E91E63";

    public bool Activo
    {
        get => _activo;
        set
        {
            if (_activo != value)
            {
                _activo = value;
                OnPropertyChanged();
                OnPropertyChanged(nameof(Inactivo));
                OnPropertyChanged(nameof(EstadoTexto));
                OnPropertyChanged(nameof(Disponible));
                OnPropertyChanged(nameof(EstadoBadgeBgColor));
                OnPropertyChanged(nameof(EstadoBadgeTextColor));
                OnPropertyChanged(nameof(BotonActivoBgColor));
                OnPropertyChanged(nameof(BotonActivoTextColor));
                OnPropertyChanged(nameof(BotonActivoBorderColor));
                OnPropertyChanged(nameof(BotonInactivoBgColor));
                OnPropertyChanged(nameof(BotonInactivoTextColor));
                OnPropertyChanged(nameof(BotonInactivoBorderColor));
            }
        }
    }

    // Ayudantes de reactividad visual (Wireframe Pág. 19)
    public bool Inactivo => !Activo;
    public string EstadoTexto => Activo ? "ACTIVO" : "INACTIVO";
    public string EstadoBadgeBgColor => Activo ? "#E8F5E9" : "#FFF3E0";
    public string EstadoBadgeTextColor => Activo ? "#2E7D32" : "#D97706";

    public string BotonActivoBgColor => Activo ? "#E88B9A" : "#FFFFFF";
    public string BotonActivoTextColor => Activo ? "#FFFFFF" : "#78716C";
    public string BotonActivoBorderColor => Activo ? "#E88B9A" : "#E7E5E4";

    public string BotonInactivoBgColor => !Activo ? "#78716C" : "#FFFFFF";
    public string BotonInactivoTextColor => !Activo ? "#FFFFFF" : "#78716C";
    public string BotonInactivoBorderColor => !Activo ? "#78716C" : "#E7E5E4";

    // Aliases para compatibilidad con vistas y ViewModels de reservas
    public string Especialidad
    {
        get => EspecialidadPrincipal;
        set => EspecialidadPrincipal = value;
    }

    public bool Disponible
    {
        get => Activo;
        set => Activo = value;
    }

    // Ayudante para interfaz gráfica (avatares con iniciales si no hay foto)
    public string Iniciales => string.Concat(
        (NombreCompleto ?? string.Empty).Split(' ', StringSplitOptions.RemoveEmptyEntries)
            .Take(2)
            .Select(s => s[0])
    ).ToUpper();

    public event PropertyChangedEventHandler? PropertyChanged;
    protected void OnPropertyChanged([CallerMemberName] string? propertyName = null)
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }
}
