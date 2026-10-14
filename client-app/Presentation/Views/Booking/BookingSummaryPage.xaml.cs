using ShushineStudio.Mobile.Presentation.ViewModels.Booking;

namespace ShushineStudio.Mobile.Presentation.Views.Booking;

public partial class BookingSummaryPage : ContentPage
{
	public BookingSummaryPage(BookingSummaryViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
