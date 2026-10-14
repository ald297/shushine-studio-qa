using ShushineStudio.Mobile.Presentation.ViewModels.Booking;

namespace ShushineStudio.Mobile.Presentation.Views.Booking;

public partial class SlotSelectionPage : ContentPage
{
	public SlotSelectionPage(SlotSelectionViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
