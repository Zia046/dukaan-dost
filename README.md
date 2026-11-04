# Dukaan Dost (dukaan-dost)

Har Dukandar Ka Digital Hisaab Kitaab. Offline-first shop management app for Pakistan.
Developed by Burhan Nexus.

## Status
Phases 1-5 (part 1) complete: products, purchases, sales/POS, returns, customers, suppliers, ledger, PDF receipts and expenses. Reports/charts, barcode scanning, backup and Secret Code are next.

## Build
1. Install Android Studio (Koala or newer) with JDK 17.
2. File > Open > select this folder, wait for Gradle sync.
3. Run on an emulator or phone (Android 8.0+).
4. Unit tests: `./gradlew testDebugUnitTest`

## Architecture
MVVM + Clean Architecture: `domain` (models, repository interfaces),
`data` (repository implementations), `ui` (Compose screens + ViewModels), `di` (Hilt).

## Security
Never commit keystores, passwords or API secrets (see .gitignore).
