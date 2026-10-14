using ShushineStudio.Mobile.Presentation.ViewModels.Auth;

namespace ShushineStudio.Mobile.Presentation.Views.Auth;

public partial class LoginPage : ContentPage
{
	public LoginPage(LoginViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
		Shell.SetTabBarIsVisible(this, false);
		Shell.SetNavBarIsVisible(this, false);
	}

	protected override bool OnBackButtonPressed()
	{
		// En la pantalla de login, no permitir que el botón Back de Android retroceda a pantallas protegidas
		return true;
	}
}
