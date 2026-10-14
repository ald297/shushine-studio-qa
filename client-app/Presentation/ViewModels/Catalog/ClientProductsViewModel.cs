using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Catalog;

/// <summary>
/// ViewModel reactivo para el catálogo de productos de belleza para clientes.
/// Permite explorar productos disponibles, búsqueda por nombre o marca y ver detalles.
/// </summary>
public partial class ClientProductsViewModel : BaseViewModel
{
    private readonly IProductoRepository _productoRepository;
    private List<Producto> _todosLosProductos = new();

    [ObservableProperty]
    private ObservableCollection<Producto> productos = new();

    [ObservableProperty]
    private string searchText = string.Empty;

    public bool HasProductos => Productos.Count > 0;
    public bool ShowEmptyState => !IsBusy && !HasError && !HasProductos;
    public bool HasError => !string.IsNullOrWhiteSpace(ErrorMessage);

    public ClientProductsViewModel(IProductoRepository productoRepository)
    {
        _productoRepository = productoRepository;
        Title = "Productos de Belleza";
        _ = CargarProductosAsync();
    }

    [RelayCommand]
    public async Task CargarProductosAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = null;
            OnPropertyChanged(nameof(HasError));

            var lista = await _productoRepository.GetProductosActivosAsync();
            _todosLosProductos = lista ?? new List<Producto>();
            AplicarFiltro();
        }
        catch (Exception ex)
        {
            ErrorMessage = "No fue posible cargar los productos: " + ex.Message;
            OnPropertyChanged(nameof(HasError));
        }
        finally
        {
            IsBusy = false;
            OnPropertyChanged(nameof(ShowEmptyState));
        }
    }

    [RelayCommand]
    private void Search(string text)
    {
        SearchText = text;
        AplicarFiltro();
    }

    private void AplicarFiltro()
    {
        Productos.Clear();
        var filtrados = _todosLosProductos.AsEnumerable();

        if (!string.IsNullOrWhiteSpace(SearchText))
        {
            var query = SearchText.Trim().ToLowerInvariant();
            filtrados = filtrados.Where(p => 
                (p.Nombre != null && p.Nombre.ToLowerInvariant().Contains(query)) ||
                (p.Descripcion != null && p.Descripcion.ToLowerInvariant().Contains(query)) ||
                (p.Marca != null && p.Marca.ToLowerInvariant().Contains(query)));
        }

        foreach (var p in filtrados)
        {
            Productos.Add(p);
        }

        OnPropertyChanged(nameof(HasProductos));
        OnPropertyChanged(nameof(ShowEmptyState));
    }

    [RelayCommand]
    private async Task VerDetalleAsync(Producto? producto)
    {
        if (producto == null) return;
        await Shell.Current.DisplayAlert(
            producto.Nombre,
            $"{producto.Descripcion}\n\nMarca: {producto.Marca ?? "Shunshine Studio"}\nPrecio: ${producto.Precio:F2}\nEstado: {(producto.Activo ? "Disponible en salón" : "Agotado")}",
            "Cerrar"
        );
    }

    [RelayCommand]
    private async Task GoBackAsync()
    {
        if (Shell.Current.Navigation.NavigationStack.Count > 1)
        {
            await Shell.Current.GoToAsync("..");
        }
        else
        {
            await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
        }
    }
}
