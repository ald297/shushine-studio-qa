using ShushineStudio.Mobile.Presentation.ViewModels.Profile;

namespace ShushineStudio.Mobile.Presentation.Views.Profile;

public partial class EditProfilePage : ContentPage
{
    private readonly EditProfileViewModel _viewModel;

    public EditProfilePage(EditProfileViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        BindingContext = viewModel;
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        if (_viewModel.CargarDatosCommand.CanExecute(null))
        {
            await _viewModel.CargarDatosCommand.ExecuteAsync(null);
        }
    }
}
