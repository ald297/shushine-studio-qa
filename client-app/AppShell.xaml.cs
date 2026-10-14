using ShushineStudio.Mobile.Presentation.Views.Appointments;
using ShushineStudio.Mobile.Presentation.Views.Auth;
using ShushineStudio.Mobile.Presentation.Views.Booking;
using ShushineStudio.Mobile.Presentation.Views.Catalog;
using ShushineStudio.Mobile.Presentation.Views.Chat;
using ShushineStudio.Mobile.Presentation.Views.Home;
using ShushineStudio.Mobile.Presentation.Views.Notifications;
using ShushineStudio.Mobile.Presentation.Views.Profile;
using ShushineStudio.Mobile.Presentation.Views.Reviews;

namespace ShushineStudio.Mobile;

public partial class AppShell : Shell
{
	public AppShell()
	{
		InitializeComponent();

		// Registro de Rutas secundarias para navegación de clientes
		Routing.RegisterRoute("RegisterPage", typeof(RegisterPage));
		Routing.RegisterRoute("ClientProductsPage", typeof(ClientProductsPage));
		Routing.RegisterRoute("ServiceDetailPage", typeof(ServiceDetailPage));
		Routing.RegisterRoute("StylistSelectionPage", typeof(StylistSelectionPage));
		Routing.RegisterRoute("SlotSelectionPage", typeof(SlotSelectionPage));
		Routing.RegisterRoute("BookingSummaryPage", typeof(BookingSummaryPage));
		Routing.RegisterRoute("BookingConfirmationPage", typeof(BookingConfirmationPage));
		Routing.RegisterRoute("AppointmentDetailPage", typeof(AppointmentDetailPage));
		Routing.RegisterRoute("CustomRequestPage", typeof(CustomRequestPage));
		Routing.RegisterRoute("EditProfilePage", typeof(EditProfilePage));
		Routing.RegisterRoute("SecurityPage", typeof(SecurityPage));
		Routing.RegisterRoute("PreferencesPage", typeof(PreferencesPage));
		Routing.RegisterRoute("AppInfoPage", typeof(AppInfoPage));
		Routing.RegisterRoute("LeaveReviewPage", typeof(LeaveReviewPage));
		Routing.RegisterRoute("MyReviewsPage", typeof(MyReviewsPage));
		Routing.RegisterRoute("ReviewDetailPage", typeof(ReviewDetailPage));
		Routing.RegisterRoute("ReviewSuccessPage", typeof(ReviewSuccessPage));
		Routing.RegisterRoute("NotificationCenterPage", typeof(NotificationCenterPage));
		Routing.RegisterRoute("NotificationDetailPage", typeof(NotificationDetailPage));

		// Inicializar la aplicación en la pantalla de Login sin pestañas
		CurrentItem = LoginShellContent;
	}

	public void SwitchToRole(string role)
	{
		bool isAdmin = !string.IsNullOrWhiteSpace(role) && role.ToUpperInvariant().Contains("ADMIN");
		if (isAdmin)
		{
			// Los administradores son redirigidos al panel web
			Application.Current?.MainPage?.DisplayAlert(
				"Acceso Administrativo",
				"Esta aplicación móvil es exclusiva para clientes. Por favor ingrese al Panel Web de Administración.",
				"Aceptar");
			SwitchToLogin();
		}
		else
		{
			SwitchToClient();
		}
	}

	public void SwitchToClient()
	{
		ClearNavigationStack();
		if (ClientTabBar != null)
		{
			if (ClientTabBar.Items.Count > 0)
			{
				ClientTabBar.CurrentItem = ClientTabBar.Items[0];
				if (ClientTabBar.CurrentItem?.Items?.Count > 0)
				{
					ClientTabBar.CurrentItem.CurrentItem = ClientTabBar.CurrentItem.Items[0];
				}
			}
			CurrentItem = ClientTabBar;
		}
	}

	public void SwitchToLogin()
	{
		ClearNavigationStack();
		if (ClientTabBar?.Items?.Count > 0)
		{
			ClientTabBar.CurrentItem = ClientTabBar.Items[0];
		}
		CurrentItem = LoginShellContent;
	}

	private void ClearNavigationStack()
	{
		try
		{
			if (Navigation != null)
			{
				while (Navigation.ModalStack.Count > 0)
				{
					Navigation.PopModalAsync(false);
				}
				if (Navigation.NavigationStack.Count > 1)
				{
					Navigation.PopToRootAsync(false);
				}
			}

			// Limpiar pilas de navegación internas de cada pestaña de Cliente
			if (ClientTabBar?.Items != null)
			{
				foreach (var section in ClientTabBar.Items)
				{
					try
					{
						if (section.Navigation != null && section.Navigation.NavigationStack.Count > 1)
						{
							section.Navigation.PopToRootAsync(false);
						}
					}
					catch { }
				}
			}
		}
		catch
		{
			// Si la pila ya está en la raíz, ignorar
		}
	}
}
