# Steps for a release

- Bump `pluginVersion` in [gradle.properties](https://github.com/DaPutzy/intellij-sops-plugin/blob/c3513782f4d7a09489be4aebdb1ef861e83e3ae1/gradle.properties#L7)
- Leave changes under `## [Unreleased]` in [CHANGELOG.md](https://github.com/DaPutzy/intellij-sops-plugin/blob/master/CHANGELOG.md?plain=1#L5)
- Run [Build](https://github.com/DaPutzy/intellij-sops-plugin/actions/workflows/build.yml) workflow
- Check everything
- Release the Draft release
- [Release](https://github.com/DaPutzy/intellij-sops-plugin/actions/workflows/release.yml) workflow will run automatically
- Change `## [Unreleased]` in [CHANGELOG.md](https://github.com/DaPutzy/intellij-sops-plugin/blob/master/CHANGELOG.md?plain=1#L5) to whatever version was release (e.g. `## [3.0.1]`)
- Profit
- ???
