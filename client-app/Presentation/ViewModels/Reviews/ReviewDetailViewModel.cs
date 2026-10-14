using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

public partial class ReviewDetailViewModel : BaseViewModel, IQueryAttributable
{
    [ObservableProperty]
    private Resena? resena;

    [ObservableProperty]
    private int estrellas = 5;

    [ObservableProperty]
    private string comentario = string.Empty;

    public ReviewDetailViewModel()
    {
        Title = "Detalle de Reseña";
    }

    public void ApplyQueryAttributes(IDictionary<string, object> query)
    {
        if (query.TryGetValue("resena", out var obj) && obj is Resena r)
        {
            CargarResena(r);
        }
    }

    private void CargarResena(Resena r)
    {
        Resena = r;
        Estrellas = r.Estrellas;
        Comentario = r.Comentario;
    }

    [RelayCommand]
    private async Task RegresarAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}
