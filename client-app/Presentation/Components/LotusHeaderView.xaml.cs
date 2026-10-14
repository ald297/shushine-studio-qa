using System.Windows.Input;

namespace ShushineStudio.Mobile.Presentation.Components;

public partial class LotusHeaderView : ContentView
{
	public static readonly BindableProperty TitleProperty =
		BindableProperty.Create(nameof(Title), typeof(string), typeof(LotusHeaderView), string.Empty);

	public static readonly BindableProperty GreetingProperty =
		BindableProperty.Create(nameof(Greeting), typeof(string), typeof(LotusHeaderView), string.Empty);

	public static readonly BindableProperty SubtitleProperty =
		BindableProperty.Create(nameof(Subtitle), typeof(string), typeof(LotusHeaderView), string.Empty, propertyChanged: (b, o, n) =>
		{
			if (b is LotusHeaderView header && string.IsNullOrEmpty(header.Greeting))
			{
				header.Greeting = (string)n;
			}
		});

	public static readonly BindableProperty ShowAvatarProperty =
		BindableProperty.Create(nameof(ShowAvatar), typeof(bool), typeof(LotusHeaderView), false);

	public static readonly BindableProperty AvatarTextProperty =
		BindableProperty.Create(nameof(AvatarText), typeof(string), typeof(LotusHeaderView), "SS");

	public static readonly BindableProperty ShowBackButtonProperty =
		BindableProperty.Create(nameof(ShowBackButton), typeof(bool), typeof(LotusHeaderView), false);

	public static readonly BindableProperty BackCommandProperty =
		BindableProperty.Create(nameof(BackCommand), typeof(ICommand), typeof(LotusHeaderView), default(ICommand));

	public static readonly BindableProperty ShowNotificationButtonProperty =
		BindableProperty.Create(nameof(ShowNotificationButton), typeof(bool), typeof(LotusHeaderView), false);

	public static readonly BindableProperty NotificationCommandProperty =
		BindableProperty.Create(nameof(NotificationCommand), typeof(ICommand), typeof(LotusHeaderView), default(ICommand));

	public static readonly BindableProperty HasUnreadNotificationsProperty =
		BindableProperty.Create(nameof(HasUnreadNotifications), typeof(bool), typeof(LotusHeaderView), false);

	public string Title
	{
		get => (string)GetValue(TitleProperty);
		set => SetValue(TitleProperty, value);
	}

	public string Greeting
	{
		get => (string)GetValue(GreetingProperty);
		set => SetValue(GreetingProperty, value);
	}

	public string Subtitle
	{
		get => (string)GetValue(SubtitleProperty);
		set => SetValue(SubtitleProperty, value);
	}

	public bool ShowAvatar
	{
		get => (bool)GetValue(ShowAvatarProperty);
		set => SetValue(ShowAvatarProperty, value);
	}

	public string AvatarText
	{
		get => (string)GetValue(AvatarTextProperty);
		set => SetValue(AvatarTextProperty, value);
	}

	public bool ShowBackButton
	{
		get => (bool)GetValue(ShowBackButtonProperty);
		set => SetValue(ShowBackButtonProperty, value);
	}

	public ICommand? BackCommand
	{
		get => (ICommand?)GetValue(BackCommandProperty);
		set => SetValue(BackCommandProperty, value);
	}

	public bool ShowNotificationButton
	{
		get => (bool)GetValue(ShowNotificationButtonProperty);
		set => SetValue(ShowNotificationButtonProperty, value);
	}

	public ICommand? NotificationCommand
	{
		get => (ICommand?)GetValue(NotificationCommandProperty);
		set => SetValue(NotificationCommandProperty, value);
	}

	public bool HasUnreadNotifications
	{
		get => (bool)GetValue(HasUnreadNotificationsProperty);
		set => SetValue(HasUnreadNotificationsProperty, value);
	}

	public LotusHeaderView()
	{
		InitializeComponent();
	}
}
