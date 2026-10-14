using ShushineStudio.Mobile.Presentation.ViewModels.Catalog;

namespace ShushineStudio.Mobile.Presentation.Views.Catalog;

public partial class CatalogPage : ContentPage
{
	private readonly CatalogViewModel _viewModel;
	private bool _mostrandoServicios = true;

	public CatalogPage(CatalogViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		_mostrandoServicios = true;
		if (Application.Current?.Resources != null)
		{
			if (Application.Current.Resources.TryGetValue("Primary", out var primaryColor))
				ChipClienteServicios.BackgroundColor = (Color)primaryColor;
			if (Application.Current.Resources.TryGetValue("TextSecondary", out var textColor))
				LabelClienteProductos.TextColor = (Color)textColor;
			LabelClienteServicios.TextColor = Colors.White;
			ChipClienteProductos.BackgroundColor = Colors.Transparent;
		}

		if (_viewModel.Servicios.Count == 0)
		{
			await _viewModel.LoadServiciosCommand.ExecuteAsync(null);
		}
	}

	private void OnServiciosChipTapped(object sender, EventArgs e)
	{
		if (_mostrandoServicios) return;
		_mostrandoServicios = true;

		// Activar chip Servicios
		ChipClienteServicios.BackgroundColor = (Color)Application.Current!.Resources["Primary"];
		LabelClienteServicios.TextColor = Colors.White;

		// Desactivar chip Productos
		ChipClienteProductos.BackgroundColor = Colors.Transparent;
		LabelClienteProductos.TextColor = (Color)Application.Current!.Resources["TextSecondary"];
	}

	private async void OnProductosChipTapped(object sender, EventArgs e)
	{
		// Actualizar visual del chip
		if (!_mostrandoServicios)
		{
			// Ya está en productos, navegar directamente
			await Shell.Current.GoToAsync("ClientProductsPage");
			return;
		}

		_mostrandoServicios = false;

		// Activar chip Productos
		ChipClienteProductos.BackgroundColor = (Color)Application.Current!.Resources["Primary"];
		LabelClienteProductos.TextColor = Colors.White;

		// Desactivar chip Servicios
		ChipClienteServicios.BackgroundColor = Colors.Transparent;
		LabelClienteServicios.TextColor = (Color)Application.Current!.Resources["TextSecondary"];

		// Navegar a productos
		await Shell.Current.GoToAsync("ClientProductsPage");
	}
}
