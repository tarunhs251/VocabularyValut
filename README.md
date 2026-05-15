# VocabVault 📚

A modern Android vocabulary learning application built with Jetpack Compose, featuring real-time word definitions and persistent local storage.

## About

VocabVault is an Android app designed to help users build and expand their vocabulary. Users can search for word definitions from an external API, save their favorite words locally, and manage their personal vocabulary collection. The app provides a clean, modern interface with Material Design 3 principles.

## Features

- 🔍 **Word Search** - Look up word definitions from the Dictionary API
- 💾 **Save Words** - Store favorite words in your personal collection using Room database
- 📱 **Modern UI** - Built with Jetpack Compose and Material Design 3
- 🎯 **Dependency Injection** - Clean architecture with Hilt DI
- 🌐 **Network Requests** - Retrofit for seamless API integration
- 📱 **Responsive Design** - Optimized for various screen sizes
- 🧭 **Navigation** - Compose Navigation with smooth transitions

## Tech Stack

### Core Android
- **API Level**: Min 26, Target 34, Compile 34
- **Kotlin**: With coroutines support
- **Java**: Version 17

### UI Framework
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material 3**: Latest Material Design components
- **Navigation Compose**: Type-safe navigation

### Database & Storage
- **Room**: Local SQLite database with DAO pattern
- **SQLite**: Persistent data storage

### Networking
- **Retrofit**: HTTP client for REST API calls
- **OkHttp**: HTTP logging and interceptors
- **Gson**: JSON serialization/deserialization

### Architecture & Dependency Injection
- **Hilt**: Compile-time dependency injection
- **Lifecycle Components**: ViewModel, LiveData
- **AndroidX**: Modern Android libraries

### Build Tools
- **Gradle 8+**: Build automation
- **KSP (Kotlin Symbol Processing)**: Compiler plugins
- **ProGuard**: Code obfuscation and optimization

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/vocabvault/
│   │   │   ├── data/              # Data layer (Room, Retrofit)
│   │   │   │   ├── local/         # Room database and DAOs
│   │   │   │   ├── remote/        # Retrofit API services
│   │   │   │   └── repository/    # Data repository pattern
│   │   │   ├── di/                # Hilt dependency injection modules
│   │   │   ├── ui/                # UI layer (Compose)
│   │   │   │   ├── screens/       # Screen composables
│   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   ├── navigation/    # Navigation setup
│   │   │   │   └── theme/         # App theming (colors, typography)
│   │   │   ├── utils/             # Utility functions and extensions
│   │   │   ├── MainActivity.kt     # Entry point activity
│   │   │   └── VocabVaultApplication.kt # App class
│   │   └── res/                   # Resources (layouts, strings, colors)
│   ├── androidTest/               # Instrumented tests
│   └── test/                      # Unit tests
├── build.gradle.kts               # App-level build configuration
└── proguard-rules.pro             # ProGuard rules for release builds
```

## Getting Started

### Prerequisites
- Android Studio Flamingo or newer
- JDK 17+
- Android SDK 34+
- Gradle 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/VocabVault.git
   cd VocabVault
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Connect an Android device or emulator
   - Click "Run" in Android Studio or use:
     ```bash
     ./gradlew installDebug
     ```

## API Integration

The app uses the [Free Dictionary API](https://dictionaryapi.dev/) to fetch word definitions. No API key is required.

**Network Security Configuration**: The app includes a custom network security config to allow traffic to `dictionaryapi.dev`.

## Build Variants

- **Debug**: Unoptimized build with debugging enabled
- **Release**: Optimized build with ProGuard obfuscation

## Permissions

- `android.permission.INTERNET` - Required for fetching word definitions from the API

## Architecture

The app follows **Clean Architecture** principles:

- **Data Layer**: Handles all data operations (local database, remote API)
- **Domain Layer** (implicit): Business logic and repository interfaces
- **UI Layer**: Jetpack Compose screens and components with ViewModels

## Dependencies

Key dependencies are managed through `libs.versions.toml` in the `gradle/` directory for centralized version management.

## Testing

The project includes support for:
- **Unit Tests**: Tests for individual components
- **Instrumented Tests**: Android-specific tests running on device/emulator

Run tests with:
```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Building for Release

1. **Configure signing** in `local.properties` or in Android Studio
2. **Build signed APK**:
   ```bash
   ./gradlew bundleRelease
   ```
3. The optimized app will include ProGuard obfuscation and shrinking

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Free Dictionary API](https://dictionaryapi.dev/) - for providing free word definitions
- [Google Android Developers](https://developer.android.com/) - for excellent documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - for modern UI development

## Contact

For questions or support, please open an issue on the GitHub repository.
