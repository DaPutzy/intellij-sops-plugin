<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Simple Sops Edit Changelog

## [Unreleased]

### Changed
- Moved to scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Switched to IntelliJ Platform Gradle Plugin (2.x).

### Added
- Added support for decryption of "non-existing" files (e.g. old git revisions)
- Added warning if no or an incorrect sops version is installed 

### Removed
- Removed support for sops versions <3.10.0

## [2.1.2] - 2025-01-15
