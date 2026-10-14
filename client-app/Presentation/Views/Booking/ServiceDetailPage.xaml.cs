using ShushineStudio.Mobile.Presentation.ViewModels.Booking;

namespace ShushineStudio.Mobile.Presentation.Views.Booking;

public partial class ServiceDetailPage : ContentPage
{
	public ServiceDetailPage(ServiceDetailViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
