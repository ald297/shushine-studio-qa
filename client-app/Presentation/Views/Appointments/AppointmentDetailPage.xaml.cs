using ShushineStudio.Mobile.Presentation.ViewModels.Appointments;

namespace ShushineStudio.Mobile.Presentation.Views.Appointments;

public partial class AppointmentDetailPage : ContentPage
{
	public AppointmentDetailPage(AppointmentDetailViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
