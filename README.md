# Simple Sops Edit

## Prerequisites

* You should have [sops](https://github.com/getsops/sops) (>=v3.10.0) installed and configured on your system.
* On Windows `powershell` (>=7.0.0) is required. Please follow the [installation instructions](https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows).

## Settings

The plugin allows you to specify the path to your sops executable, additional environment variables and the option to make files encrypted with sops read-only for the normal editor.

These Settings can be found under `Settings` --> `Tools` --> `Simple Sops Settings`. 

When you hover over the lightbulb a short description, an example and the default value is shown.

Environment variables are configured as a list of name / value pairs, use the `+` / `-` buttons to add or remove entries. Values are passed to sops exactly as entered, so do **not** quote or escape them.

> [!NOTE]
> Older versions of the plugin configured the environment variables in a single text field. These settings are migrated to the list automatically, quotes surrounding a value are removed during the migration. Please check the migrated entries once.

## Using sops with age

The plugin does not handle any keys itself. It simply runs sops, so sops looks up your age keys exactly as it does on the command line, using its [environment variables](https://github.com/getsops/sops#encrypting-using-age). You can set them in two places:

* in the plugin settings (`Sops environment`), or
* in the environment the IDE inherits (e.g. your shell profile on macOS / Linux or the user environment variables on Windows; restart the IDE afterward).

### Encrypting (recipients)

Needed for [Replace](#replace) and for files that are not encrypted yet. [Edit](#edit) reuses the recipients stored in the encrypted file.

* a `.sops.yaml` in your project with matching `creation_rules`, or
* `SOPS_AGE_RECIPIENTS` e.g. `SOPS_AGE_RECIPIENTS=age1...`

### Decrypting (private key)

Needed for [View](#view) and [Edit](#edit). sops tries all of these sources:

| Source                                       | Description                                                                                                                                                                       |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| default key file                             | `$XDG_CONFIG_HOME/sops/age/keys.txt` or `~/.config/sops/age/keys.txt` on Linux, `~/Library/Application Support/sops/age/keys.txt` on macOS, `%AppData%\sops\age\keys.txt` on Windows |
| `SOPS_AGE_KEY_FILE`                          | path to a key file e.g. `SOPS_AGE_KEY_FILE=/Users/bockwurst/keys.txt`                                                                                                             |
| `SOPS_AGE_KEY_CMD`                           | command that prints the private key to stdout, see [below](#keeping-the-private-key-out-of-plain-files)                                                                            |
| `SOPS_AGE_KEY`                               | the private key itself, **not recommended** in the plugin settings since they are stored as plain text in the IDE configuration                                                   |
| `SOPS_AGE_SSH_PRIVATE_KEY_FILE`, `~/.ssh/id_ed25519`, `~/.ssh/id_rsa` | if your files are encrypted for an SSH key                                                                                                                       |

If you can decrypt a file with `sops decrypt <file>` in a terminal but not with the plugin, the IDE most likely does not have the same environment as your terminal. Set the variables in the plugin settings in that case.

### Keeping the private key out of plain files

The plugin runs sops without a terminal, so sops **cannot ask for a passphrase**. A passphrase-protected key file (`age -p`) therefore fails with an error like `could not read passphrase: standard input is not a terminal` (see [#34](https://github.com/DaPutzy/intellij-sops-plugin/issues/34)).

Use `SOPS_AGE_KEY_CMD` instead. sops runs this command whenever it needs the private key and reads the key from its output. This way the key can stay in a password manager or in the credential store of your OS, which may show its own unlock dialog if needed.

Add an entry with the name `SOPS_AGE_KEY_CMD` and the command as value, e.g. `security find-generic-password -s sops-age -a default -w`. sops splits the command into its arguments like a shell would, so put an argument that contains spaces in quotes, e.g. `"C:\Program Files\keyring\keyring.exe" get sops-age default`.

Examples for the command (the key is stored as a single line `AGE-SECRET-KEY-1...` under service `sops-age` and account `default`):

| OS / store                                        | Command                                                     | Store the key with                                         |
|---------------------------------------------------|-------------------------------------------------------------|------------------------------------------------------------|
| Windows Credential Manager (via Python `keyring`) | `keyring get sops-age default`                              | `keyring set sops-age default`                             |
| macOS Keychain                                    | `security find-generic-password -s sops-age -a default -w`  | `security add-generic-password -s sops-age -a default -w`  |
| Linux Secret Service (GNOME Keyring, KeePassXC)   | `secret-tool lookup service sops-age account default`       | `secret-tool store --label=sops-age service sops-age account default` |
| Linux KDE Wallet                                  | `kwallet-query -r age.keys -f sops kdewallet`               | `kwallet-query -w age.keys -f sops kdewallet`              |

sops runs the command directly, not through a shell. If you need pipes, variables or several commands, put them into a small script and use the path to the script as value.

Before configuring the plugin, check that the command works in a terminal, e.g. `SOPS_AGE_KEY_CMD="security find-generic-password -s sops-age -a default -w" sops decrypt <file>`.

## Functionality

When a sops file is "detected" by the plugin, a banner is shown. It allows you to view, edit or replace the content.

Each of these actions can also be triggered by a shortcut, however no default shortcuts are assigned. To assign a shortcut just search for `sops` in `Settings` --> `Keymap`.

### View

**Description:** decrypts and displays content

**CLI equivalent:** `sops decrypt <file>`

**Requirements:** sops needs to be able to decrypt the file i.e. provide the corresponding environment variables or a `.sops.yaml`

### Edit

**Description:** allows you to change the content

**CLI equivalent:** `sops edit <file>`

**Requirements:** sops needs to be able to edit the file i.e. sops will use the available information from the encrypted file, but some environment variables or a `.sops.yaml` may still be required

### Replace

**Description:** allows you to replace the content

**CLI equivalent:** `sops encrypt --in-place <file>`

**Requirements:** sops needs to be able to encrypt content i.e. sops will use the file ending of the existing file to figure out the file type, but some environment variables or a `.sops.yaml` are still required for encryption