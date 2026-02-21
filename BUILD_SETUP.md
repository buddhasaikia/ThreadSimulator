# ThreadSimulator Build Environment Setup Guide

## Overview
This document provides a comprehensive guide for setting up and verifying the ThreadSimulator Android project build environment.

## ✅ Verified Environment Status

### System Information
- **OS**: macOS (aarch64)
- **Java**: OpenJDK 19.0.2 (Corretto-19.0.2.7.1)
- **Gradle**: 8.13 (via Gradle wrapper)

### Android SDK
- **SDK Location**: `/Users/buddhasaikia/Library/Android/sdk`
- **API Levels Installed**: 
  - android-34 ✅
  - android-35 ✅ (compileSdk & targetSdk)
  - android-36 ✅
- **Build-tools Installed**: 
  - 34.0.0 ✅
  - 35.0.0 ✅ (used by compileSdk=35)
  - 36.1.0 ✅

### Project Configuration
- **compileSdk**: 35 ✅
- **targetSdk**: 35 ✅
- **minSdk**: 24 (no explicit platform required for Android 7.0+)
- **Kotlin**: 2.0.21 ✅
- **Jetpack Compose**: Enabled ✅
- **Hilt DI**: Integrated ✅

### Gradle Configuration
- **JVM Args**: `-Xmx2048m -Dfile.encoding=UTF-8` ✅
- **Parallelization**: Disabled (safe for coupled projects) ✅
- **AndroidX**: Enabled ✅
- **Kotlin Code Style**: official ✅

## Environment Variables

### Currently Set
```
ANDROID_HOME=/Users/buddhasaikia/Library/Android/sdk
```

### Optional (Not Required - Working via System PATH)
```
JAVA_HOME=<not set, but java executable found in /usr/bin/java>
```

**Note**: JAVA_HOME is not explicitly required since Java is available in the system PATH. Gradle automatically detects the JDK via the Gradle wrapper configuration.

## Build Commands

### Standard Build (Recommended for Development)
```bash
# Clean build with all checks
./gradlew clean build

# Build debug APK only
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Run Android Lint checks
./gradlew lint

# Run all checks (lint, unit tests, build)
./gradlew check
```

### Installation & Running
```bash
# Build and install debug APK to device/emulator
./gradlew installDebug

# Install and launch in one command
./gradlew installDebug && adb shell am start -n com.bs.threadsimulator/.MainActivity
```

## Verification Checklist

Run this to verify your environment is properly set up:

```bash
#!/bin/bash
echo "Checking ThreadSimulator build environment..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    echo "✅ Java: $(java -version 2>&1 | head -1)"
else
    echo "❌ Java not found"
fi

# Check Android SDK
if [ -d "$ANDROID_HOME" ]; then
    echo "✅ Android SDK: $ANDROID_HOME"
else
    echo "❌ Android SDK not found at $ANDROID_HOME"
fi

# Check API 35
if [ -d "$ANDROID_HOME/platforms/android-35" ]; then
    echo "✅ Android API 35: Installed"
else
    echo "❌ Android API 35: Not found"
fi

# Check Gradle wrapper
if [ -f "./gradlew" ]; then
    echo "✅ Gradle wrapper: $(./gradlew --version 2>&1 | head -1)"
else
    echo "❌ Gradle wrapper not found"
fi

# Test build
echo ""
echo "Running test build..."
./gradlew clean build -x test 2>&1 | tail -3
```

## Build Output

### Latest Successful Build (2026-02-21)
```
BUILD SUCCESSFUL in 39s
105 actionable tasks: 104 executed, 1 up-to-date
APK Location: app/build/outputs/apk/debug/app-debug.apk (10M)
```

### Verification Results
- ✅ Clean build completes successfully
- ✅ All 105 tasks execute without errors
- ✅ APK generates correctly (10MB debug build)
- ✅ No dependency conflicts
- ✅ Compose compiler properly configured
- ✅ Hilt annotation processing successful

## Troubleshooting

### Build Fails: "Android SDK not found"
**Solution**: Set `ANDROID_HOME` environment variable or ensure `local.properties` has correct `sdk.dir`
```bash
export ANDROID_HOME=/Users/buddhasaikia/Library/Android/sdk
# Or check local.properties
cat local.properties
```

### Build Fails: "compileSdk 35 not installed"
**Solution**: Install API 35 via Android Studio SDK Manager or:
```bash
sdkmanager "platforms;android-35" "build-tools;35.0.0"
```

### Build Fails: "Gradle daemon issue"
**Solution**: Stop the daemon and rebuild
```bash
./gradlew --stop
./gradlew clean build
```

### Memory Issues During Build
**Solution**: Increase JVM memory in `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Compose Compiler Issues
**Enabled Features**:
- Compose compiler plugin: ✅ Integrated via `alias(libs.plugins.compose.compiler)`
- Strong skipping mode: ⚠️ Currently disabled (commented out in build.gradle.kts for compatibility)

To enable strong skipping mode for performance:
```kotlin
// In app/build.gradle.kts
composeCompiler {
    enableStrongSkippingMode = true
}
```

## CI/CD Integration

For GitHub Actions or other CI systems, ensure:
1. Java 8+ is installed (project targets Java 1.8)
2. Android SDK with API 35 and build-tools 35.0.0 available
3. `ANDROID_HOME` environment variable is set
4. Gradle wrapper is executable: `chmod +x gradlew`

**GitHub Actions Example**:
```yaml
- name: Setup Java
  uses: actions/setup-java@v3
  with:
    java-version: '19'
    distribution: 'corretto'

- name: Setup Android SDK
  uses: android-actions/setup-android@v2

- name: Build
  run: ./gradlew clean build
```

## Performance Optimization Opportunities

### Quick Wins
1. **Enable Strong Skipping Mode** (Compose optimization)
   - Reduces recomposition overhead
   - Uncomment in `app/build.gradle.kts`

2. **Enable Gradle Parallel Builds** (if architecture allows)
   - Currently disabled for compatibility
   - Add to `gradle.properties`: `org.gradle.parallel=true`

3. **Use Gradle Build Caching**
   - Enable: `org.gradle.caching=true` in `gradle.properties`

### Monitoring
- Use `./gradlew build --info` for detailed task information
- Check lint reports: `app/build/reports/lint-results-debug.html`
- Monitor build time: `./gradlew build --profile` generates build report

## Next Steps

1. **Run a test build to verify setup**:
   ```bash
   ./gradlew clean build
   ```

2. **Install and test the app**:
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.bs.threadsimulator/.MainActivity
   ```

3. **Enable logging** (BuildConfig.DEBUG is already configured):
   ```bash
   adb logcat | grep ThreadSimulator
   ```

4. **For development**, consider enabling these for faster iteration:
   - Gradle Build Cache: `org.gradle.caching=true`
   - Parallel Builds (if architecture supports): `org.gradle.parallel=true`
   - Compose Strong Skipping Mode (performance)

## References
- [Android Gradle Plugin Documentation](https://developer.android.com/studio/releases/gradle-plugin)
- [Jetpack Compose Compiler Configuration](https://developer.android.com/develop/ui/compose/compiler)
- [Gradle Build Environment Guide](https://gradle.org/releases/)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

---
**Last Updated**: 2026-02-21
**Verified By**: ThreadSimulator Build Environment Verification
**Status**: ✅ All Systems Operational
