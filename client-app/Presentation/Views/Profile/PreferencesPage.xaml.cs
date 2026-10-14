using ShushineStudio.Mobile.Presentation.ViewModels.Profile;

namespace ShushineStudio.Mobile.Presentation.Views.Profile;

public partial class PreferencesPage : ContentPage
{
    public PreferencesPage(PreferencesViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
