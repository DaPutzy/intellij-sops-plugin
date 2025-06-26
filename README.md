# Simple Sops Edit

## Prerequisites

* You should have [sops](https://github.com/getsops/sops) (>=v3.10.0) installed and configured on your system.
* On Windows `powershell` (>=7.0.0) is required. Please follow the [installation instructions](https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows).

## Settings

The plugin allows you to specify the path to your sops executable, additional environment variables and the option to make files encrypted with sops read-only for the normal editor.

These Settings can be found under `Settings` --> `Tools` --> `Simple Sops Settings`. 

When you hover over the lightbulb a short description, an example and the default value is shown.

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
