# Development

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
