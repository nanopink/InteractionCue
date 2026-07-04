# Interaction Cue

Shows selected spell and Use item cues near the cursor, plus pending markers on inventory slots clicked with selected spell or item-use actions.

# Tooltip

Take full control of your tooltips. Customize everything from colors and layout to behavior, and build the exact experience you want.

<img width="456" height="215" alt="image" src="https://github.com/user-attachments/assets/687ac6b3-6c96-474c-8ee2-7069cf70fefa" />
<img width="391" height="91" alt="image" src="https://github.com/user-attachments/assets/aeee7c9b-877c-4a85-a0a7-6ef0f1f1bca9" />
<img width="256" height="91" alt="image" src="https://github.com/user-attachments/assets/fcea1adb-5580-4986-b3a0-570e1a14de35" />
<img width="639" height="118" alt="image" src="https://github.com/user-attachments/assets/d85e9b16-d930-49ad-89c8-8b9b3ab8d34d" />

# Marker

Never wonder if an action is waiting for a reaction again. Markers make pending actions instantly obvious at a glance.

<img width="494" height="91" alt="image" src="https://github.com/user-attachments/assets/f0691311-ff66-487c-b4f1-54a2e6709c8a" />
<img width="244" height="91" alt="image" src="https://github.com/user-attachments/assets/53304e3a-d388-4c20-b349-ff1ff76a6387" />


## Run

Use the local launcher so Gradle runs with the installed JDK 21:

```powershell
.\run-plugin.ps1
```

That runs the Gradle `run` task, which starts RuneLite in developer/debug mode through `src/test/java/com/interactioncue/InteractionCuePluginTest.java`.

## Jagex Accounts

Jagex accounts cannot log in directly from a plain development client until RuneLite has saved launcher credentials.

1. Open **RuneLite (configure)** from the Windows Start menu.
2. Add this to **Client arguments**:

```text
--insecure-write-credentials
```

3. Save.
4. Launch RuneLite once from the Jagex Launcher and log in.
5. Close that RuneLite client.
6. Start the development client again:

```powershell
.\run-plugin.ps1
```

RuneLite writes the launcher credentials to `%USERPROFILE%\.runelite\credentials.properties`. Treat that file like a password and do not share it. Delete it when you are done with development if you want RuneLite to go back to normal launcher-only login behavior.

## Debug

To start the development client suspended for a debugger:

```powershell
.\run-plugin.ps1 run --debug-jvm
```

Then attach IntelliJ to `localhost:5005` with a remote JVM debug configuration. You can also debug the Gradle `run` task directly from IntelliJ.

## Verify

Run the test task to compile the plugin and check that the development launcher entry point still loads:

```powershell
.\run-plugin.ps1 test
```

## License

BSD 2-Clause. See [LICENSE](LICENSE).
