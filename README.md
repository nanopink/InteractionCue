# Interaction Cue

Shows selected spell and Use item cues near the cursor, plus pending markers on inventory slots clicked with selected spell or item-use actions.

# Marker

Never wonder if an action is waiting for a reaction again. Markers make pending actions instantly obvious at a glance.

<img width="494" height="91" alt="image" src="https://github.com/user-attachments/assets/f0691311-ff66-487c-b4f1-54a2e6709c8a" />
<img width="244" height="91" alt="image" src="https://github.com/user-attachments/assets/53304e3a-d388-4c20-b349-ff1ff76a6387" />

# Tooltip

Take full control of your tooltips. Customize everything from colors and layout to behavior, and build the exact experience you want.

<img width="456" height="215" alt="image" src="https://github.com/user-attachments/assets/687ac6b3-6c96-474c-8ee2-7069cf70fefa" />
<img width="391" height="91" alt="image" src="https://github.com/user-attachments/assets/aeee7c9b-877c-4a85-a0a7-6ef0f1f1bca9" />
<img width="256" height="91" alt="image" src="https://github.com/user-attachments/assets/fcea1adb-5580-4986-b3a0-570e1a14de35" />
<img width="639" height="118" alt="image" src="https://github.com/user-attachments/assets/d85e9b16-d930-49ad-89c8-8b9b3ab8d34d" />

# Tooltip Format

The tooltip format can be changed from the settings.

Supported tokens:

```text
{action}         Cast, Use, or another selected action
{source}         The selected spell or item name
{target}         The current menu target, when available
{source_icon}    The selected spell or item icon
{#FFF}           Text color, short RGB
{#FF9040}        Text color, full RGB
{value:if}       Show if when value exists
{value:if:else}  Show if when value exists, otherwise show else
{value:@1}       Print the value when it exists
```

Examples:

```text
{target:My target exists}            Only show text if target exists
{target:My target exists:No target}  Show one text if target exists, another if not
{target::No target}                  Show text only when target is missing by using two colons
{!target:No target}                  Same, using a negative condition
{target:@1}                          Print the matched target value
{target:Target\: @1}                 Print a colon as text with \:
```
