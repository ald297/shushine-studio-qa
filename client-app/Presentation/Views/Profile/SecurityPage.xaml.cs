using ShushineStudio.Mobile.Presentation.ViewModels.Profile;

namespace ShushineStudio.Mobile.Presentation.Views.Profile;

public partial class SecurityPage : ContentPage
{
    public SecurityPage(SecurityViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
