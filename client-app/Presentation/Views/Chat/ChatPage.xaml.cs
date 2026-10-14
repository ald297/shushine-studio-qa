using ShushineStudio.Mobile.Presentation.ViewModels.Chat;

namespace ShushineStudio.Mobile.Presentation.Views.Chat;

public partial class ChatPage : ContentPage
{
    private readonly ChatViewModel _viewModel;

    public ChatPage(ChatViewModel viewModel)
    {
        InitializeComponent();
        _viewModel = viewModel;
        BindingContext = viewModel;
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await _viewModel.IniciarChatAsync();
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        _viewModel.DetenerPolling();
    }

    protected override bool OnBackButtonPressed()
    {
        _viewModel.DetenerPolling();
        if (Navigation.NavigationStack.Count > 1)
        {
            return base.OnBackButtonPressed();
        }

        MainThread.BeginInvokeOnMainThread(async () =>
        {
            try
            {
                await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
            }
            catch { }
        });
        return true;
    }
}
