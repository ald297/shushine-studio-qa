using ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

namespace ShushineStudio.Mobile.Presentation.Views.Reviews;

public partial class ReviewSuccessPage : ContentPage
{
    public ReviewSuccessPage(ReviewSuccessViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
