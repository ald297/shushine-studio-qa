using ShushineStudio.Mobile.Presentation.ViewModels.Profile;

namespace ShushineStudio.Mobile.Presentation.Views.Profile;

public partial class AppInfoPage : ContentPage
{
    public AppInfoPage(AppInfoViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
