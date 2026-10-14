using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

/// <summary>
/// ViewModel para la pantalla de Calificar / Dejar Reseña.
/// Recibe la cita completada, valida la calificación y persiste realmente en la Web API.
/// </summary>
public partial class LeaveReviewViewModel : BaseViewModel, IQueryAttributable
{
    private readonly IResenaRepository _resenaRepository;

    [ObservableProperty]
    private Reserva? cita;

    [ObservableProperty]
    private int estrellas = 5;

    [ObservableProperty]
    private string comentario = string.Empty;

    [ObservableProperty]
    private bool visiblePublica = true;

    [ObservableProperty]
    private bool mostrarModalPosponer;

    public bool BackendDisponible => true;

    public string ServicioNombre => Cita?.ServicioNombre ?? "Servicio del Atelier";
    public string EstilistaNombre => Cita?.EstilistaNombre ?? "Especialista asignada";
    public string CodigoCita => Cita?.CodigoReserva ?? "#SHU-0000";
    public string FechaTexto => Cita != null && Cita.FechaHoraInicio != DateTime.MinValue
        ? Cita.FechaHoraInicio.ToString("ddd dd MMM yyyy · h:mm tt")
        : "Cita terminada";

    public string NivelFeedback => Estrellas switch
    {
        1 => "Queremos Mejorar",
        2 => "Oportunidad de Mejora",
        3 => "Buena Experiencia",
        4 => "Gran Experiencia",
        5 => "Experiencia Excepcional",
        _ => "Calificación"
    };

    public string FraseFeedback => Estrellas switch
    {
        1 => "Lamentamos que tu visita no fuera perfecta. Revisaremos cada detalle para mejorar.",
        2 => "Tu opinión nos ayuda a perfeccionar la atención y técnicas de nuestras especialistas.",
        3 => "¡Gracias por tu valoración! Seguiremos elevando nuestros estándares de servicio.",
        4 => "¡Muchas gracias! Nos alegra saber que disfrutaste tu momento en el atelier.",
        5 => "¡Nos encanta saber que disfrutaste tu visita! Gracias por ser parte de Shunshine Studio.",
        _ => string.Empty
    };

    public string ContadorCaracteres => $"{Comentario?.Length ?? 0} / 500 caracteres";

    public LeaveReviewViewModel(IResenaRepository resenaRepository)
    {
        _resenaRepository = resenaRepository;
        Title = "Calificar Servicio";
    }

    public void ApplyQueryAttributes(IDictionary<string, object> query)
    {
        if (query.TryGetValue("reserva", out var obj) && obj is Reserva res)
        {
            Cita = res;
            OnPropertyChanged(nameof(ServicioNombre));
            OnPropertyChanged(nameof(EstilistaNombre));
            OnPropertyChanged(nameof(CodigoCita));
            OnPropertyChanged(nameof(FechaTexto));
        }
    }

    [RelayCommand]
    private void SeleccionarEstrellas(string valor)
    {
        if (int.TryParse(valor, out var stars))
        {
            Estrellas = Math.Clamp(stars, 1, 5);
            OnPropertyChanged(nameof(NivelFeedback));
            OnPropertyChanged(nameof(FraseFeedback));
        }
    }

    partial void OnComentarioChanged(string value)
    {
        OnPropertyChanged(nameof(ContadorCaracteres));
    }

    [RelayCommand]
    private async Task EnviarResenaAsync()
    {
        if (IsBusy) return;

        if (Cita == null || Cita.Id <= 0)
        {
            ErrorMessage = "No se ha seleccionado una cita válida para calificar.";
            return;
        }

        if (Estrellas < 1 || Estrellas > 5)
        {
            ErrorMessage = "Por favor selecciona una valoración entre 1 y 5 estrellas.";
            return;
        }

        try
        {
            IsBusy = true;
            ErrorMessage = string.Empty;

            var resenaCreada = await _resenaRepository.CrearResenaAsync(Cita.Id, Estrellas, Comentario, VisiblePublica);

            if (Application.Current?.MainPage != null)
            {
                await Application.Current.MainPage.DisplayAlert(
                    "¡Gracias por tu Reseña!",
                    $"Tu valoración de {Estrellas} estrellas ha sido enviada con éxito.",
                    "Aceptar"
                );
            }

            await Shell.Current.GoToAsync("..");
        }
        catch (Exception ex)
        {
            ErrorMessage = "No se pudo registrar la reseña: " + ex.Message;
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    private void AbrirModalPosponer()
    {
        MostrarModalPosponer = true;
    }

    [RelayCommand]
    private void CerrarModalPosponer()
    {
        MostrarModalPosponer = false;
    }

    [RelayCommand]
    private async Task ConfirmarPosponerAsync()
    {
        MostrarModalPosponer = false;
        await Shell.Current.GoToAsync("..");
    }

    [RelayCommand]
    private async Task RegresarAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
