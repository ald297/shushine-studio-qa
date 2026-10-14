using ShushineStudio.Mobile.Presentation.ViewModels.Auth;

namespace ShushineStudio.Mobile.Presentation.Views.Auth;

public partial class RegisterPage : ContentPage
{
	public RegisterPage(RegisterViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
