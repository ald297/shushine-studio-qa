using ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

namespace ShushineStudio.Mobile.Presentation.Views.Reviews;

public partial class LeaveReviewPage : ContentPage
{
    public LeaveReviewPage(LeaveReviewViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
