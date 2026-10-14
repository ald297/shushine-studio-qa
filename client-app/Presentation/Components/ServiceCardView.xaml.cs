using System.Windows.Input;

namespace ShushineStudio.Mobile.Presentation.Components;

public partial class ServiceCardView : ContentView
{
	public static readonly BindableProperty ServiceNameProperty =
		BindableProperty.Create(nameof(ServiceName), typeof(string), typeof(ServiceCardView), string.Empty);

	public static readonly BindableProperty CategoryProperty =
		BindableProperty.Create(nameof(Category), typeof(string), typeof(ServiceCardView), string.Empty);

	public static readonly BindableProperty DurationTextProperty =
		BindableProperty.Create(nameof(DurationText), typeof(string), typeof(ServiceCardView), string.Empty);

	public static readonly BindableProperty PriceTextProperty =
		BindableProperty.Create(nameof(PriceText), typeof(string), typeof(ServiceCardView), string.Empty);

	public static readonly BindableProperty ImageUrlProperty =
		BindableProperty.Create(nameof(ImageUrl), typeof(string), typeof(ServiceCardView), default(string));

	public static readonly BindableProperty ActionTextProperty =
		BindableProperty.Create(nameof(ActionText), typeof(string), typeof(ServiceCardView), "Agendar");

	public static readonly BindableProperty ActionCommandProperty =
		BindableProperty.Create(nameof(ActionCommand), typeof(ICommand), typeof(ServiceCardView), default(ICommand));

	public static readonly BindableProperty ActionCommandParameterProperty =
		BindableProperty.Create(nameof(ActionCommandParameter), typeof(object), typeof(ServiceCardView), default(object));

	public string ServiceName
	{
		get => (string)GetValue(ServiceNameProperty);
		set => SetValue(ServiceNameProperty, value);
	}

	public string Category
	{
		get => (string)GetValue(CategoryProperty);
		set => SetValue(CategoryProperty, value);
	}

	public string DurationText
	{
		get => (string)GetValue(DurationTextProperty);
		set => SetValue(DurationTextProperty, value);
	}

	public string PriceText
	{
		get => (string)GetValue(PriceTextProperty);
		set => SetValue(PriceTextProperty, value);
	}

	public string? ImageUrl
	{
		get => (string?)GetValue(ImageUrlProperty);
		set => SetValue(ImageUrlProperty, value);
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

	public ServiceCardView()
	{
		InitializeComponent();
	}
}
