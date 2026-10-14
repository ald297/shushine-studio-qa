using ShushineStudio.Mobile.Presentation.ViewModels.Booking;

namespace ShushineStudio.Mobile.Presentation.Views.Booking;

public partial class StylistSelectionPage : ContentPage
{
	public StylistSelectionPage(StylistSelectionViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
