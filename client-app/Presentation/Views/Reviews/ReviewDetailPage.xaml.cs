using ShushineStudio.Mobile.Presentation.ViewModels.Reviews;

namespace ShushineStudio.Mobile.Presentation.Views.Reviews;

public partial class ReviewDetailPage : ContentPage
{
    public ReviewDetailPage(ReviewDetailViewModel viewModel)
    {
        InitializeComponent();
        BindingContext = viewModel;
    }
}
