using ShushineStudio.Mobile.Presentation.ViewModels.Notifications;

namespace ShushineStudio.Mobile.Presentation.Views.Notifications;

public partial class NotificationCenterPage : ContentPage
{
    private readonly NotificationCenterViewModel _viewModel;

    public NotificationCenterPage(NotificationCenterViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        BindingContext = viewModel;
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await _viewModel.CargarNotificacionesCommand.ExecuteAsync(null);
    }
}
