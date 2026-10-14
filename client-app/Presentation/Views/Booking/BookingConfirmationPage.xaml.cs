using ShushineStudio.Mobile.Presentation.ViewModels.Booking;

namespace ShushineStudio.Mobile.Presentation.Views.Booking;

public partial class BookingConfirmationPage : ContentPage
{
	public BookingConfirmationPage(BookingConfirmationViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
