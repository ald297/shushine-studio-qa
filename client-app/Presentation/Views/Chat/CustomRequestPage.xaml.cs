using ShushineStudio.Mobile.Presentation.ViewModels.Chat;

namespace ShushineStudio.Mobile.Presentation.Views.Chat;

public partial class CustomRequestPage : ContentPage
{
    private readonly CustomRequestViewModel _viewModel;

    public CustomRequestPage(CustomRequestViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        BindingContext = viewModel;
    }

    protected override bool OnBackButtonPressed()
    {
        MainThread.BeginInvokeOnMainThread(async () =>
        {
            try
            {
                if (Shell.Current?.Navigation?.NavigationStack?.Count > 1)
                {
                    await Shell.Current.GoToAsync("..");
                }
                else
                {
                    await Shell.Current.GoToAsync("//MainTabs/ChatPage");
                }
            }
            catch
            {
                try
                {
                    await Shell.Current.GoToAsync("//MainTabs/ChatPage");
                }
                catch { }
            }
        });
        return true;
    }
}
