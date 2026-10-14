# 📱 Shushine Studio — Aplicación Móvil (.NET MAUI)

> **Módulo:** Construcción de APIs Web & Aplicaciones Móviles  
> **Institución:** Escuela Superior Franciscana Especializada – AGAPE (ESFE AGAPE / MEGATEC)  
> **Desarrolladora Responsable:** Camila Antonia Calderon Cortez (Frontend Lead / QA)  
> **Arquitectura:** Clean Architecture + MVVM (`CommunityToolkit.Mvvm`)  
> **Framework:** .NET 8 MAUI (C# 12 / XAML)  

---

## 🚀 1. Requisitos para Ejecutar el Proyecto

1. **Visual Studio 2022** (versión 17.8 o superior) en Windows o Mac.
2. Carga de trabajo instalada en el instalador de Visual Studio:
   * **Desarrollo de interfaz de usuario de aplicaciones .NET multiplataforma (.NET MAUI)**
   * Componentes de Android SDK y Emulador de Android (API 34 o 33).
3. **Dispositivo o Emulador:**
   * **Emulador Android:** Configurado por defecto. Mapea al backend en `http://10.0.2.2:8080/api`.
   * **Dispositivo Físico:** Conectar por cable USB, activar "Depuración por USB" y configurar la IP local de la computadora en `Core/Constants/ApiConstants.cs`.

---

## 🏛️ 2. Arquitectura del Proyecto (Clean Architecture + MVVM)

El proyecto sigue estrictamente la arquitectura desacoplada estipulada en [AGENTS.md](../../AGENTS.md):

```
src/mobile/
├── Core/                     <-- Utilidades transversales
│   ├── Constants/            <-- ApiConstants.cs (URLs de backend y Supabase)
│   ├── Handlers/             <-- ErrorDelegatingHandler.cs (Intercepta JWT y RFC 7807)
│   └── Models/               <-- ProblemDetailsDto.cs (Estándar RFC 7807)
│
├── Domain/                   <-- Núcleo Puro del Negocio (Independiente de UI y Frameworks)
│   ├── Entities/             <-- Servicio, Reserva, Estilista, Usuario
│   ├── Repositories/         <-- IServicioRepository, IReservaRepository, IAuthRepository
│   └── UseCases/             <-- GetServiciosCatalogUseCase, CreateAppointmentUseCase
│
├── Data/                     <-- Implementación de Datos y Red
│   ├── Dtos/                 <-- ServicioDto, ReservaDto, AuthDtos (JSON)
│   ├── Repositories/         <-- ServicioRepository, ReservaRepository, AuthRepository
│   └── Services/             <-- TokenStorageService (SecureStorage para tokens JWT)
│
└── Presentation/             <-- Capa Visual y de Estado Reactivo
    ├── ViewModels/           <-- ViewModels basados en CommunityToolkit.Mvvm
    │   ├── Auth/             <-- LoginViewModel, RegisterViewModel
    │   ├── Catalog/          <-- CatalogViewModel
    │   ├── Appointments/     <-- MyAppointmentsViewModel
    │   └── Profile/          <-- ProfileViewModel
    └── Views/                <-- Páginas declarativas XAML y Code-behinds
        ├── Auth/             <-- LoginPage.xaml, RegisterPage.xaml
        ├── Catalog/          <-- CatalogPage.xaml
        ├── Appointments/     <-- MyAppointmentsPage.xaml
        └── Profile/          <-- ProfilePage.xaml
```

---

## 🎨 3. Paleta Cromática y Estilos

Los colores y estilos oficiales del salón se encuentran centralizados en:
* [`Resources/Styles/Colors.xaml`](./Resources/Styles/Colors.xaml):
  * **Rosa Principal:** `#D48B96` (PrimaryColor)
  * **Rosa Oscuro:** `#B86F7A` (PrimaryDark)
  * **Fondo Soft Blush:** `#FDF6F6` (BackgroundColor)
  * **Lavanda Acento:** `#D8B4E2` (SecondaryColor)
  * **Texto Principal:** `#2D2926` (TextColorPrimary)
* [`Resources/Styles/Styles.xaml`](./Resources/Styles/Styles.xaml):
  * Estilos globales para `PrimaryButtonStyle`, `SecondaryButtonStyle`, `InputContainerStyle`, `CardBorderStyle`.

---

## 🗺️ 4. Trazabilidad con los Wireframes Oficiales

| Pantalla XAML | ViewModel | Wireframe (PDF) | Historia de Usuario |
| :--- | :--- | :---: | :---: |
| [`LoginPage.xaml`](./Presentation/Views/Auth/LoginPage.xaml) | `LoginViewModel` | Página 6 | **US-2.02** |
| [`RegisterPage.xaml`](./Presentation/Views/Auth/RegisterPage.xaml) | `RegisterViewModel` | Página 5 | **US-2.01** |
| [`CatalogPage.xaml`](./Presentation/Views/Catalog/CatalogPage.xaml) | `CatalogViewModel` | Página 7 | **US-3.01** |
| [`MyAppointmentsPage.xaml`](./Presentation/Views/Appointments/MyAppointmentsPage.xaml) | `MyAppointmentsViewModel` | Página 13 | **US-4.03** |
| [`ProfilePage.xaml`](./Presentation/Views/Profile/ProfilePage.xaml) | `ProfileViewModel` | Página 14 | **US-2.03** |

---

## 🔄 5. Flujo de Trabajo en Git

1. Crear rama individual para cada funcionalidad desde `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/movil-catalogo-servicios
   ```
2. Realizar commits en **español** bajo Conventional Commits:
   ```bash
   git commit -m "feat(movil): maquetar vista de catalogo con filtros por categoria en XAML"
   ```
3. Subir rama y crear Pull Request hacia `develop` en Azure DevOps para revisión de Alex Alfaro.
