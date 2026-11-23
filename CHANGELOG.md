<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Simple Sops Edit Changelog

## [Unreleased]

### Added
- Added VCS change markers (diff to decrypted HEAD) in the gutter of decrypted view/edit editors ([#90](https://github.com/DaPutzy/intellij-sops-plugin/issues/90))

### Changed
- Environment variables are now configured as a list of name / value pairs instead of a single text field, existing settings are migrated automatically ([#100](https://github.com/DaPutzy/intellij-sops-plugin/issues/100))
- Documented how to use sops with age in the README, including keeping the private key in a credential store via `SOPS_AGE_KEY_CMD` ([#100](https://github.com/DaPutzy/intellij-sops-plugin/issues/100))
- Raised minimal supported IDE version to 2024.2 (242)
- Build with Java 21

### Fixed
- Removed usage of deprecated `ReadAction.compute`
- Environment variable values containing spaces or quotes (e.g. `SOPS_AGE_KEY_CMD` with arguments) are passed to sops correctly

## [3.0.1]

### Fixed
- Removed usage of deprecated classes
- Updated gradle plugins and dependencies

## [3.0.0]

### Changed
- Moved to scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Switched to IntelliJ Platform Gradle Plugin (2.x).

### Added
- Added support for decryption of "non-existing" files (e.g. old git revisions)
- Added warning if no or an incorrect sops version is installed 

### Removed
- Removed support for sops versions <3.10.0

## [2.1.2] - 2025-01-15
