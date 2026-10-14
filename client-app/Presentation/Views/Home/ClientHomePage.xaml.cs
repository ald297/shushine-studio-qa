using ShushineStudio.Mobile.Presentation.ViewModels.Home;

namespace ShushineStudio.Mobile.Presentation.Views.Home;

public partial class ClientHomePage : ContentPage
{
	private readonly ClientHomeViewModel _viewModel;

	public ClientHomePage(ClientHomeViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		_viewModel.Title = "Inicio";
		await _viewModel.CargarDatosCommand.ExecuteAsync(null);
	}
}
