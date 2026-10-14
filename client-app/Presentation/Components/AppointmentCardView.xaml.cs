using System.Windows.Input;

namespace ShushineStudio.Mobile.Presentation.Components;

public partial class AppointmentCardView : ContentView
{
	public static readonly BindableProperty DateTextProperty =
		BindableProperty.Create(nameof(DateText), typeof(string), typeof(AppointmentCardView), string.Empty);

	public static readonly BindableProperty TimeTextProperty =
		BindableProperty.Create(nameof(TimeText), typeof(string), typeof(AppointmentCardView), string.Empty);

	public static readonly BindableProperty ServiceNameProperty =
		BindableProperty.Create(nameof(ServiceName), typeof(string), typeof(AppointmentCardView), string.Empty);

	public static readonly BindableProperty StylistNameProperty =
		BindableProperty.Create(nameof(StylistName), typeof(string), typeof(AppointmentCardView), string.Empty);

	public static readonly BindableProperty StatusProperty =
		BindableProperty.Create(nameof(Status), typeof(string), typeof(AppointmentCardView), "Pendiente");

	public static readonly BindableProperty PriceTextProperty =
		BindableProperty.Create(nameof(PriceText), typeof(string), typeof(AppointmentCardView), string.Empty);

	public static readonly BindableProperty ActionTextProperty =
		BindableProperty.Create(nameof(ActionText), typeof(string), typeof(AppointmentCardView), "Ver Detalle");

	public static readonly BindableProperty ActionCommandProperty =
		BindableProperty.Create(nameof(ActionCommand), typeof(ICommand), typeof(AppointmentCardView), default(ICommand));

	public static readonly BindableProperty ActionCommandParameterProperty =
		BindableProperty.Create(nameof(ActionCommandParameter), typeof(object), typeof(AppointmentCardView), default(object));

	public string DateText
	{
		get => (string)GetValue(DateTextProperty);
		set => SetValue(DateTextProperty, value);
	}

	public string TimeText
	{
		get => (string)GetValue(TimeTextProperty);
		set => SetValue(TimeTextProperty, value);
	}

	public string ServiceName
	{
		get => (string)GetValue(ServiceNameProperty);
		set => SetValue(ServiceNameProperty, value);
	}

	public string StylistName
	{
		get => (string)GetValue(StylistNameProperty);
		set => SetValue(StylistNameProperty, value);
	}

	public string Status
	{
		get => (string)GetValue(StatusProperty);
		set => SetValue(StatusProperty, value);
	}

	public string PriceText
	{
		get => (string)GetValue(PriceTextProperty);
		set => SetValue(PriceTextProperty, value);
	}

	public string ActionText
	{
		get => (string)GetValue(ActionTextProperty);
		set => SetValue(ActionTextProperty, value);
	}

	public ICommand? ActionCommand
	{
		get => (ICommand?)GetValue(ActionCommandProperty);
		set => SetValue(ActionCommandProperty, value);
	}

	public object? ActionCommandParameter
	{
		get => GetValue(ActionCommandParameterProperty);
		set => SetValue(ActionCommandParameterProperty, value);
	}

	public AppointmentCardView()
	{
		InitializeComponent();
	}
}
