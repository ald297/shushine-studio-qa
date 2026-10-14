using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

/// <summary>
/// ViewModel para la pantalla de confirmación exitosa de reseña.
/// Alineado a la pantalla de Stitch 'Reseña Registrada con Éxito'.
/// </summary>
public partial class ReviewSuccessViewModel : BaseViewModel, IQueryAttributable
{
    [ObservableProperty]
    private string servicioNombre = "Servicio del Atelier";

    [ObservableProperty]
    private string estilistaNombre = "Especialista";

    [ObservableProperty]
    private int estrellas = 5;

    public string EstrellasTexto => new string('★', Math.Clamp(Estrellas, 1, 5));

    public ReviewSuccessViewModel()
    {
        Title = "Reseña Registrada";
    }

    public void ApplyQueryAttributes(IDictionary<string, object> query)
    {
        if (query.TryGetValue("servicioNombre", out var serv) && serv is string s)
        {
            ServicioNombre = s;
        }

        if (query.TryGetValue("estilistaNombre", out var estilista) && estilista is string e)
        {
            EstilistaNombre = e;
        }

        if (query.TryGetValue("estrellas", out var stars) && stars is int st)
        {
            Estrellas = st;
            OnPropertyChanged(nameof(EstrellasTexto));
        }
    }

    [RelayCommand]
    private async Task IrAMisResenasAsync()
    {
        await Shell.Current.GoToAsync("MyReviewsPage");
    }

    [RelayCommand]
    private async Task IrAInicioAsync()
    {
        await Shell.Current.GoToAsync("//CatalogPage");
    }
}
