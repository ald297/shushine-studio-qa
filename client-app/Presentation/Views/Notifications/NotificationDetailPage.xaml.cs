using ShushineStudio.Mobile.Presentation.ViewModels.Notifications;

namespace ShushineStudio.Mobile.Presentation.Views.Notifications;

public partial class NotificationDetailPage : ContentPage
{
    public NotificationDetailPage(NotificationDetailViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
