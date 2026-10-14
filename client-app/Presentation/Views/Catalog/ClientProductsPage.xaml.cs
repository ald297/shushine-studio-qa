using ShushineStudio.Mobile.Presentation.ViewModels.Catalog;

namespace ShushineStudio.Mobile.Presentation.Views.Catalog;

public partial class ClientProductsPage : ContentPage
{
	private readonly ClientProductsViewModel _viewModel;

	public ClientProductsPage(ClientProductsViewModel viewModel)
	{
		InitializeComponent();
		_viewModel = viewModel;
		BindingContext = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.CargarProductosAsync();
	}

	// Botón de flecha: regresar a CatalogPage (tab Catálogo)
	private async void OnRegresarTapped(object sender, EventArgs e)
	{
		await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
	}

	// Chip Servicios: regresar a CatalogPage
	private async void OnRegresarServiciosTapped(object sender, EventArgs e)
	{
		await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
	}
}
