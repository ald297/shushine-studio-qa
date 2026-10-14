using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.UseCases;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Catalog;

public partial class CategoriaItem : ObservableObject
{
    public string Nombre { get; set; } = string.Empty;
    public string Icono { get; set; } = string.Empty;

    [ObservableProperty]
    private bool isSelected;
}

/// <summary>
/// ViewModel reactivo para el catálogo de servicios (Wireframe Pág. 7).
/// Permite búsqueda rápida por nombre y filtrado por chips de categorías con feedback visual activo.
/// </summary>
public partial class CatalogViewModel : BaseViewModel
{
    private readonly GetServiciosCatalogUseCase _getCatalogUseCase;
    private List<Servicio> _todosLosServicios = new();

    [ObservableProperty]
    private ObservableCollection<Servicio> servicios = new();

    [ObservableProperty]
    private ObservableCollection<CategoriaItem> categorias = new();

    [ObservableProperty]
    private string categoriaSeleccionada = "Todos";

    [ObservableProperty]
    private string searchText = string.Empty;

    public CatalogViewModel(GetServiciosCatalogUseCase getCatalogUseCase)
    {
        _getCatalogUseCase = getCatalogUseCase;
        Title = "Catálogo de Belleza";
        InicializarCategorias();
        InicializarCatalogoInmediato();
        _ = LoadServiciosAsync();
    }

    public bool HasErrorMessage => !string.IsNullOrWhiteSpace(ErrorMessage);

    private void InicializarCategorias()
    {
        Categorias = new ObservableCollection<CategoriaItem>
        {
            new CategoriaItem { Nombre = "Todos", Icono = "✨", IsSelected = true },
            new CategoriaItem { Nombre = "Cabello", Icono = "✂️", IsSelected = false },
            new CategoriaItem { Nombre = "Color", Icono = "🎨", IsSelected = false },
            new CategoriaItem { Nombre = "Uñas", Icono = "💅", IsSelected = false },
            new CategoriaItem { Nombre = "Maquillaje", Icono = "💄", IsSelected = false },
            new CategoriaItem { Nombre = "Faciales", Icono = "🌸", IsSelected = false }
        };
    }

    private void InicializarCatalogoInmediato()
    {
        _todosLosServicios = new List<Servicio>
        {
            new Servicio { Id = 1, Nombre = "Balayage Iluminador & Gloss", Descripcion = "Técnica francesa de degradado a mano alzada con baño de brillo nutritivo.", Precio = 65.00m, DuracionMinutos = 120, CategoriaNombre = "Color", Activo = true, Protocolo = "Diagnóstico capilar • Decoloración controlada • Baño de gloss • Sellado de cutícula con mascarilla de caviar." },
            new Servicio { Id = 2, Nombre = "Corte de Autor & Cepillado", Descripcion = "Diseño de corte personalizado según morfología facial con lavado dermocalmante.", Precio = 25.00m, DuracionMinutos = 45, CategoriaNombre = "Cabello", Activo = true, Protocolo = "Asesoría visagista • Lavado aromático con masaje craneal • Corte de precisión • Brushing final." },
            new Servicio { Id = 3, Nombre = "Manicura Rusa & Esmaltado Semi", Descripcion = "Limpieza profunda de cutículas con torno y esmaltado de alta duración gelish.", Precio = 22.00m, DuracionMinutos = 60, CategoriaNombre = "Uñas", Activo = true, Protocolo = "Esterilización y preparación • Pulido de cutículas en seco • Nivelación de base • Esmaltado e hidratación." },
            new Servicio { Id = 4, Nombre = "Pedicura Spa Rejuvenecedora", Descripcion = "Exfoliación con sales minerales, mascarilla de parafina y esmaltado profesional.", Precio = 28.00m, DuracionMinutos = 60, CategoriaNombre = "Uñas", Activo = true, Protocolo = "Baño de burbujas emoliente • Exfoliación botánica • Tratamiento de talones • Esmaltado en gel y masaje." },
            new Servicio { Id = 5, Nombre = "Lifting de Pestañas & Keratina", Descripcion = "Curvatura natural con nutrición intensiva de keratina y tinte negro profundo.", Precio = 30.00m, DuracionMinutos = 50, CategoriaNombre = "Maquillaje", Activo = true, Protocolo = "Limpieza ocular desengrasante • Moldeado de silicona • Aplicación de keratina botánica • Tinte HD y sérum." },
            new Servicio { Id = 6, Nombre = "Diseño & Laminado de Cejas", Descripcion = "Depilación con hilo orgánico, laminado y perfilado de mirada.", Precio = 20.00m, DuracionMinutos = 40, CategoriaNombre = "Maquillaje", Activo = true, Protocolo = "Mapeo geométrico de cejas • Laminado alisador • Fijación de forma • Depilación hilo orgánico y tinte." },
            new Servicio { Id = 7, Nombre = "Tratamiento Facial Hidratante Glow", Descripcion = "Limpieza profunda con ácido hialurónico, exfoliación suave y masaje facial rejuvenecedor.", Precio = 45.00m, DuracionMinutos = 60, CategoriaNombre = "Faciales", Activo = true, Protocolo = "Doble limpieza coreana • Peeling enzimático suave • Infusión de ácido hialurónico con ultrasonido • Mascarilla calmante." }
        };
        AplicarFiltros();
    }

    [RelayCommand]
    public async Task LoadServiciosAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            ErrorMessage = null;
            OnPropertyChanged(nameof(HasErrorMessage));

            var items = await _getCatalogUseCase.ExecuteAsync();
            var list = items.ToList();
            if (list.Count > 0)
            {
                _todosLosServicios = list;
            }

            AplicarFiltros();
        }
        catch (Exception ex)
        {
            ErrorMessage = ex.Message;
            OnPropertyChanged(nameof(HasErrorMessage));
        }
        finally
        {
            IsBusy = false;
        }
    }

    [RelayCommand]
    public void SeleccionarCategoria(CategoriaItem item)
    {
        if (item == null) return;

        foreach (var c in Categorias)
        {
            c.IsSelected = (c.Nombre == item.Nombre);
        }

        CategoriaSeleccionada = item.Nombre;
        AplicarFiltros();
    }

    partial void OnSearchTextChanged(string value) => AplicarFiltros();

    private void AplicarFiltros()
    {
        var filtrados = _todosLosServicios.Where(s => s.Activo);

        if (!string.IsNullOrWhiteSpace(CategoriaSeleccionada) && CategoriaSeleccionada != "Todos")
        {
            filtrados = filtrados.Where(s => 
                string.Equals(s.CategoriaNombre, CategoriaSeleccionada, StringComparison.OrdinalIgnoreCase));
        }

        if (!string.IsNullOrWhiteSpace(SearchText))
        {
            var query = SearchText.Trim().ToLowerInvariant();
            filtrados = filtrados.Where(s => 
                s.Nombre.ToLowerInvariant().Contains(query) || 
                (s.Descripcion != null && s.Descripcion.ToLowerInvariant().Contains(query)));
        }

        Servicios.Clear();
        foreach (var item in filtrados)
        {
            Servicios.Add(item);
        }
    }

    [RelayCommand]
    private async Task SelectServicioAsync(Servicio servicio)
    {
        if (servicio == null) return;
        await Shell.Current.GoToAsync($"ServiceDetailPage?servicioId={servicio.Id}");
    }

    [RelayCommand]
    private async Task IrAProductosAsync()
    {
        await Shell.Current.GoToAsync("ClientProductsPage");
    }
}
