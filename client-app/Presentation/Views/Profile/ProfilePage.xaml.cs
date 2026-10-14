using ShushineStudio.Mobile.Presentation.ViewModels.Profile;

namespace ShushineStudio.Mobile.Presentation.Views.Profile;

public partial class ProfilePage : ContentPage
{
	private readonly ProfileViewModel _viewModel;

	public ProfilePage(ProfileViewModel viewModel)
	{
		InitializeComponent();
		_viewModel = viewModel;
		BindingContext = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		if (_viewModel.CargarPerfilCommand.CanExecute(null))
		{
			await _viewModel.CargarPerfilCommand.ExecuteAsync(null);
		}
	}
}
