using ShushineStudio.Mobile.Presentation.ViewModels.Appointments;

namespace ShushineStudio.Mobile.Presentation.Views.Appointments;

public partial class MyAppointmentsPage : ContentPage
{
	private readonly MyAppointmentsViewModel _viewModel;

	public MyAppointmentsPage(MyAppointmentsViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.LoadCitasCommand.ExecuteAsync(null);
	}
}
