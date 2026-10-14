namespace ShushineStudio.Mobile.Presentation.Components;

public partial class StatusBadgeView : ContentView
{
	public static readonly BindableProperty StatusProperty =
		BindableProperty.Create(
			nameof(Status),
			typeof(string),
			typeof(StatusBadgeView),
			default(string),
			propertyChanged: OnStatusChanged);

	public string? Status
	{
		get => (string?)GetValue(StatusProperty);
		set => SetValue(StatusProperty, value);
	}

	public StatusBadgeView()
	{
		InitializeComponent();
		ApplyStatusColors(Status);
	}

	private static void OnStatusChanged(BindableObject bindable, object oldValue, object newValue)
	{
		if (bindable is StatusBadgeView badge)
		{
			badge.ApplyStatusColors(newValue as string);
		}
	}

	private void ApplyStatusColors(string? status)
	{
		var normalized = (status ?? string.Empty).Trim().ToUpperInvariant();

		Color bgColor;
		Color textColor;
		Color borderColor;

		switch (normalized)
		{
			case "CONFIRMADA":
				bgColor = Color.FromArgb("#EBF6EE");
				textColor = Color.FromArgb("#1B6A38");
				borderColor = Color.FromArgb("#C5E7CD");
				break;
			case "EN PROCESO":
			case "EN_PROCESO":
			case "ENPROCESO":
				bgColor = Color.FromArgb("#FCECEF");
				textColor = Color.FromArgb("#9B2D47");
				borderColor = Color.FromArgb("#F7CAD3");
				break;
			case "TERMINADA":
			case "COMPLETADA":
				bgColor = Color.FromArgb("#F5F3EF");
				textColor = Color.FromArgb("#594E46");
				borderColor = Color.FromArgb("#E2DCD5");
				break;
			case "CANCELADA":
				bgColor = Color.FromArgb("#FBEBEB");
				textColor = Color.FromArgb("#8E2323");
				borderColor = Color.FromArgb("#F2B8B8");
				break;
			case "PENDIENTE":
			default:
				bgColor = Color.FromArgb("#FDF8EB");
				textColor = Color.FromArgb("#8C6514");
				borderColor = Color.FromArgb("#F5E2B3");
				break;
		}

		BadgeContainer.BackgroundColor = bgColor;
		BadgeContainer.Stroke = borderColor;
		StatusLabel.TextColor = textColor;
		StatusDot.Color = textColor;
	}
}
