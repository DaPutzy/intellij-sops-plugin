# Simple Sops Edit

## Prerequisites

You should have sops installed and configured on your system.
On Windows `powershell` is required (should be available in anything >= Win 7).

## Settings

The plugin allows you to specify the path to your sops executable, additional environment variables and the option to make files encrypted with sops read-only for the normal editor.

These Settings can be found under `Settings` --> `Tools` --> `Simple Sops Settings`. 

When you hover over the lightbulb a short description, an example and the default value is shown.

## Functionality

When a sops file is "detected" by the plugin, a banner is shown. It allows you to view, edit or replace the content. 

| Action  | Description                       | CLI equivalent (loosely)           | Requirements                                                                                                                                                                                                     |
|---------|-----------------------------------|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| view    | decrypts and displays content     | `sops --decrypt <file>`            | sops needs to be able to decrypt the file i.e. provide the corresponding environment variables or a `.sops.yaml`                                                                                                 |
| edit    | allows you to change the content  | `sops <file>`                      | sops needs to be able to edit the file  i.e. sops will use the available information from the encrypted file, but some environment variables or a `.sops.yaml` may still be required                             |
| replace | allows you to replace the content | `sops --encrypt --in-place <file>` | sops needs to be able to encrypt content i.e. sops will use the file ending of the existing file to figure our the file type, but some environment variables or a `.sops.yaml` are still required for encryption |

Each of these actions can also be triggered by a shortcut, however no default shortcuts are assigned. To assign a shortcut just search for `sops` in `Settings` --> `Keymap`.
