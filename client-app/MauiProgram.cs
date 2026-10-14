using CommunityToolkit.Maui;
using Microsoft.Extensions.Logging;
using ShushineStudio.Mobile.Core.Constants;
using ShushineStudio.Mobile.Core.Handlers;
using ShushineStudio.Mobile.Data.Repositories;
using ShushineStudio.Mobile.Data.Services;
using ShushineStudio.Mobile.Domain.Repositories;
using ShushineStudio.Mobile.Domain.UseCases;
using ShushineStudio.Mobile.Presentation.ViewModels.Appointments;
using ShushineStudio.Mobile.Presentation.ViewModels.Auth;
using ShushineStudio.Mobile.Presentation.ViewModels.Booking;
using ShushineStudio.Mobile.Presentation.ViewModels.Catalog;
using ShushineStudio.Mobile.Presentation.ViewModels.Chat;
using ShushineStudio.Mobile.Presentation.ViewModels.Home;
using ShushineStudio.Mobile.Presentation.ViewModels.Notifications;
using ShushineStudio.Mobile.Presentation.ViewModels.Profile;
using ShushineStudio.Mobile.Presentation.ViewModels.Reviews;
using ShushineStudio.Mobile.Presentation.Views.Appointments;
using ShushineStudio.Mobile.Presentation.Views.Auth;
using ShushineStudio.Mobile.Presentation.Views.Booking;
using ShushineStudio.Mobile.Presentation.Views.Catalog;
using ShushineStudio.Mobile.Presentation.Views.Chat;
using ShushineStudio.Mobile.Presentation.Views.Home;
using ShushineStudio.Mobile.Presentation.Views.Notifications;
using ShushineStudio.Mobile.Presentation.Views.Profile;
using ShushineStudio.Mobile.Presentation.Views.Reviews;

namespace ShushineStudio.Mobile;

public static class MauiProgram
{
	public static MauiApp CreateMauiApp()
	{
		var builder = MauiApp.CreateBuilder();
		builder
			.UseMauiApp<App>()
			.UseMauiCommunityToolkit()
			.ConfigureFonts(fonts =>
			{
				fonts.AddFont("PlayfairDisplay-Regular.ttf", "PlayfairDisplay");
				fonts.AddFont("PlayfairDisplay-SemiBold.ttf", "PlayfairDisplaySemiBold");
				fonts.AddFont("PlayfairDisplay-Bold.ttf", "PlayfairDisplayBold");
				fonts.AddFont("PlusJakartaSans-Regular.ttf", "PlusJakartaSans");
				fonts.AddFont("PlusJakartaSans-Medium.ttf", "PlusJakartaSansMedium");
				fonts.AddFont("PlusJakartaSans-SemiBold.ttf", "PlusJakartaSansSemiBold");
				fonts.AddFont("PlusJakartaSans-Bold.ttf", "PlusJakartaSansBold");
			});

#if DEBUG
		builder.Logging.AddDebug();
#endif

		// ==========================================
		// 1. Core & Red (Handlers y HttpClient)
		// ==========================================
		builder.Services.AddTransient<ErrorDelegatingHandler>();

		builder.Services.AddHttpClient("ShushineApi", client =>
		{
			client.BaseAddress = new Uri(ApiConstants.BaseUrl.TrimEnd('/') + "/");
			client.Timeout = TimeSpan.FromSeconds(120);
		})
		.AddHttpMessageHandler<ErrorDelegatingHandler>();

		builder.Services.AddScoped(sp => sp.GetRequiredService<IHttpClientFactory>().CreateClient("ShushineApi"));

		// ==========================================
		// 2. Servicios de Datos (Storage, Supabase)
		// ==========================================
		builder.Services.AddSingleton<ITokenStorageService, TokenStorageService>();

		// ==========================================
		// 3. Repositorios (Data Layer -> Domain Interfaces)
		// ==========================================
		builder.Services.AddScoped<IServicioRepository, ServicioRepository>();
		builder.Services.AddScoped<IEstilistaRepository, EstilistaRepository>();
		builder.Services.AddScoped<IReservaRepository, ReservaRepository>();
		builder.Services.AddScoped<IAuthRepository, AuthRepository>();
		builder.Services.AddScoped<IClienteRepository, ClienteRepository>();
		builder.Services.AddScoped<IChatRepository, ChatRepository>();
		builder.Services.AddScoped<IResenaRepository, ResenaRepository>();
		builder.Services.AddScoped<IProductoRepository, ProductoRepository>();
		builder.Services.AddScoped<ISolicitudRepository, SolicitudRepository>();

		// ==========================================
		// 4. Casos de Uso (Domain Layer)
		// ==========================================
		builder.Services.AddTransient<GetServiciosCatalogUseCase>();
		builder.Services.AddTransient<GetEstilistasUseCase>();
		builder.Services.AddTransient<GetDisponibilidadUseCase>();
		builder.Services.AddTransient<GetMyAppointmentsUseCase>();
		builder.Services.AddTransient<CreateAppointmentUseCase>();
		builder.Services.AddTransient<CancelAppointmentUseCase>();

		// ==========================================
		// 5. ViewModels (Exclusivamente Clientes)
		// ==========================================
		builder.Services.AddTransient<LoginViewModel>();
		builder.Services.AddTransient<RegisterViewModel>();
		builder.Services.AddTransient<ClientHomeViewModel>();
		builder.Services.AddTransient<CatalogViewModel>();
		builder.Services.AddTransient<ClientProductsViewModel>();
		builder.Services.AddTransient<ServiceDetailViewModel>();
		builder.Services.AddTransient<StylistSelectionViewModel>();
		builder.Services.AddTransient<SlotSelectionViewModel>();
		builder.Services.AddTransient<BookingSummaryViewModel>();
		builder.Services.AddTransient<BookingConfirmationViewModel>();
		builder.Services.AddTransient<MyAppointmentsViewModel>();
		builder.Services.AddTransient<AppointmentDetailViewModel>();
		builder.Services.AddTransient<ProfileViewModel>();
		builder.Services.AddTransient<EditProfileViewModel>();
		builder.Services.AddTransient<SecurityViewModel>();
		builder.Services.AddTransient<PreferencesViewModel>();
		builder.Services.AddTransient<AppInfoViewModel>();
		builder.Services.AddTransient<ChatViewModel>();
		builder.Services.AddTransient<CustomRequestViewModel>();
		builder.Services.AddTransient<LeaveReviewViewModel>();
		builder.Services.AddTransient<MyReviewsViewModel>();
		builder.Services.AddTransient<ReviewDetailViewModel>();
		builder.Services.AddTransient<ReviewSuccessViewModel>();
		builder.Services.AddTransient<NotificationCenterViewModel>();
		builder.Services.AddTransient<NotificationDetailViewModel>();

		// ==========================================
		// 6. Páginas / Vistas (Exclusivamente Clientes)
		// ==========================================
		builder.Services.AddTransient<LoginPage>();
		builder.Services.AddTransient<RegisterPage>();
		builder.Services.AddTransient<ClientHomePage>();
		builder.Services.AddTransient<CatalogPage>();
		builder.Services.AddTransient<ClientProductsPage>();
		builder.Services.AddTransient<ServiceDetailPage>();
		builder.Services.AddTransient<StylistSelectionPage>();
		builder.Services.AddTransient<SlotSelectionPage>();
		builder.Services.AddTransient<BookingSummaryPage>();
		builder.Services.AddTransient<BookingConfirmationPage>();
		builder.Services.AddTransient<MyAppointmentsPage>();
		builder.Services.AddTransient<AppointmentDetailPage>();
		builder.Services.AddTransient<ProfilePage>();
		builder.Services.AddTransient<EditProfilePage>();
		builder.Services.AddTransient<SecurityPage>();
		builder.Services.AddTransient<PreferencesPage>();
		builder.Services.AddTransient<AppInfoPage>();
		builder.Services.AddTransient<LeaveReviewPage>();
		builder.Services.AddTransient<MyReviewsPage>();
		builder.Services.AddTransient<ReviewDetailPage>();
		builder.Services.AddTransient<ReviewSuccessPage>();
		builder.Services.AddTransient<NotificationCenterPage>();
		builder.Services.AddTransient<NotificationDetailPage>();
		builder.Services.AddTransient<ChatPage>();
		builder.Services.AddTransient<CustomRequestPage>();

		return builder.Build();
	}
}
