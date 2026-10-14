using System.Windows.Input;

namespace ShushineStudio.Mobile.Presentation.Components;

public partial class EmptyStateView : ContentView
{
	public static readonly BindableProperty IconSourceProperty =
		BindableProperty.Create(nameof(IconSource), typeof(ImageSource), typeof(EmptyStateView), default(ImageSource));

	public static readonly BindableProperty IconEmojiProperty =
		BindableProperty.Create(nameof(IconEmoji), typeof(string), typeof(EmptyStateView), "✨");

	public static readonly BindableProperty TitleProperty =
		BindableProperty.Create(nameof(Title), typeof(string), typeof(EmptyStateView), "Sin información");

	public static readonly BindableProperty DescriptionProperty =
		BindableProperty.Create(nameof(Description), typeof(string), typeof(EmptyStateView), string.Empty);

	public static readonly BindableProperty ActionTextProperty =
		BindableProperty.Create(nameof(ActionText), typeof(string), typeof(EmptyStateView), string.Empty);

	public static readonly BindableProperty ActionCommandProperty =
		BindableProperty.Create(nameof(ActionCommand), typeof(ICommand), typeof(EmptyStateView), default(ICommand));

	public static readonly BindableProperty IsActionVisibleProperty =
		BindableProperty.Create(nameof(IsActionVisible), typeof(bool), typeof(EmptyStateView), false);

	public ImageSource? IconSource
	{
		get => (ImageSource?)GetValue(IconSourceProperty);
		set => SetValue(IconSourceProperty, value);
	}

	public string IconEmoji
	{
		get => (string)GetValue(IconEmojiProperty);
		set => SetValue(IconEmojiProperty, value);
	}

	public string Title
	{
		get => (string)GetValue(TitleProperty);
		set => SetValue(TitleProperty, value);
	}

	public string Description
	{
		get => (string)GetValue(DescriptionProperty);
		set => SetValue(DescriptionProperty, value);
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

	public bool IsActionVisible
	{
		get => (bool)GetValue(IsActionVisibleProperty);
		set => SetValue(IsActionVisibleProperty, value);
	}

	public EmptyStateView()
	{
		InitializeComponent();
	}
}
