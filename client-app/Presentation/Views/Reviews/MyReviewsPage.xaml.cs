using ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

namespace ShushineStudio.Mobile.Presentation.Views.Reviews;

public partial class MyReviewsPage : ContentPage
{
    private readonly MyReviewsViewModel _viewModel;

    public MyReviewsPage(MyReviewsViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        BindingContext = viewModel;
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await _viewModel.CargarDatosCommand.ExecuteAsync(null);
    }
}
