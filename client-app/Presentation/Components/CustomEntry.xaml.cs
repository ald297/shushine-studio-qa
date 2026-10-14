namespace ShushineStudio.Mobile.Presentation.Components;

public partial class CustomEntry : ContentView
{
	public static readonly BindableProperty LabelTextProperty =
		BindableProperty.Create(nameof(LabelText), typeof(string), typeof(CustomEntry), string.Empty);

	public static readonly BindableProperty TextProperty =
		BindableProperty.Create(nameof(Text), typeof(string), typeof(CustomEntry), string.Empty, BindingMode.TwoWay);

	public static readonly BindableProperty PlaceholderProperty =
		BindableProperty.Create(nameof(Placeholder), typeof(string), typeof(CustomEntry), string.Empty);

	public static readonly BindableProperty LeadingIconProperty =
		BindableProperty.Create(nameof(LeadingIcon), typeof(ImageSource), typeof(CustomEntry), default(ImageSource));

	public static readonly BindableProperty IsPasswordProperty =
		BindableProperty.Create(nameof(IsPassword), typeof(bool), typeof(CustomEntry), false);

	public static readonly BindableProperty IsPasswordToggleEnabledProperty =
		BindableProperty.Create(nameof(IsPasswordToggleEnabled), typeof(bool), typeof(CustomEntry), false);

	public static readonly BindableProperty KeyboardProperty =
		BindableProperty.Create(nameof(Keyboard), typeof(Keyboard), typeof(CustomEntry), Keyboard.Default);

	public static readonly BindableProperty ReturnTypeProperty =
		BindableProperty.Create(nameof(ReturnType), typeof(ReturnType), typeof(CustomEntry), ReturnType.Default);

	public static readonly BindableProperty ErrorTextProperty =
		BindableProperty.Create(nameof(ErrorText), typeof(string), typeof(CustomEntry), string.Empty);

	public static readonly BindableProperty HasErrorProperty =
		BindableProperty.Create(nameof(HasError), typeof(bool), typeof(CustomEntry), false, propertyChanged: OnHasErrorChanged);

	public string LabelText
	{
		get => (string)GetValue(LabelTextProperty);
		set => SetValue(LabelTextProperty, value);
	}

	public string Text
	{
		get => (string)GetValue(TextProperty);
		set => SetValue(TextProperty, value);
	}

	public string Placeholder
	{
		get => (string)GetValue(PlaceholderProperty);
		set => SetValue(PlaceholderProperty, value);
	}

	public ImageSource? LeadingIcon
	{
		get => (ImageSource?)GetValue(LeadingIconProperty);
		set => SetValue(LeadingIconProperty, value);
	}

	public bool IsPassword
	{
		get => (bool)GetValue(IsPasswordProperty);
		set => SetValue(IsPasswordProperty, value);
	}

	public bool IsPasswordToggleEnabled
	{
		get => (bool)GetValue(IsPasswordToggleEnabledProperty);
		set => SetValue(IsPasswordToggleEnabledProperty, value);
	}

	public Keyboard Keyboard
	{
		get => (Keyboard)GetValue(KeyboardProperty);
		set => SetValue(KeyboardProperty, value);
	}

	public ReturnType ReturnType
	{
		get => (ReturnType)GetValue(ReturnTypeProperty);
		set => SetValue(ReturnTypeProperty, value);
	}

	public string ErrorText
	{
		get => (string)GetValue(ErrorTextProperty);
		set => SetValue(ErrorTextProperty, value);
	}

	public bool HasError
	{
		get => (bool)GetValue(HasErrorProperty);
		set => SetValue(HasErrorProperty, value);
	}

	public CustomEntry()
	{
		InitializeComponent();
	}

	private void OnEntryFocused(object? sender, FocusEventArgs e)
	{
		if (!HasError)
		{
			// Acento dorado champaña en foco
			EntryBorder.Stroke = (Color)Application.Current!.Resources["Secondary"];
			EntryBorder.StrokeThickness = 1.5;
			EntryBorder.BackgroundColor = (Color)Application.Current!.Resources["Surface"];
		}
	}

	private void OnEntryUnfocused(object? sender, FocusEventArgs e)
	{
		if (!HasError)
		{
			EntryBorder.Stroke = (Color)Application.Current!.Resources["Border"];
			EntryBorder.StrokeThickness = 1;
			EntryBorder.BackgroundColor = (Color)Application.Current!.Resources["InputBackground"];
		}
	}

	private static void OnHasErrorChanged(BindableObject bindable, object oldValue, object newValue)
	{
		if (bindable is CustomEntry customEntry)
		{
			if (customEntry.HasError)
			{
				customEntry.EntryBorder.Stroke = (Color)Application.Current!.Resources["Error"];
				customEntry.EntryBorder.StrokeThickness = 1.5;
			}
			else
			{
				customEntry.EntryBorder.Stroke = (Color)Application.Current!.Resources["Border"];
				customEntry.EntryBorder.StrokeThickness = 1;
			}
		}
	}

	private void OnPasswordToggleTapped(object? sender, EventArgs e)
	{
		IsPassword = !IsPassword;
		PasswordToggleLabel.Text = IsPassword ? "👁️" : "🙈";
	}
}
