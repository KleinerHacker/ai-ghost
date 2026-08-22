# Appearance

AI Ghost ships in two appearances: a light one with indigo accents on paper toned surfaces, and a
dark one with the same indigo family on deep navy surfaces. Both follow the logo and the colours of
this documentation site, so application and documentation stay in step.

## Choosing the appearance

The appearance is part of your preferences, stored in `preferences.json` inside the folder
`.ai-ghost` of your home directory:

```json
{
  "themeMode": "SYSTEM"
}
```

| Value    | Appearance                                              |
|----------|---------------------------------------------------------|
| `LIGHT`  | Always the light appearance                             |
| `DARK`   | Always the dark appearance                              |
| `SYSTEM` | Follows the setting of your operating system (default)  |

!!! note "The appearance is chosen at start up"
    AI Ghost reads the setting once while starting. Changing it - in the preferences file or in your
    operating system - takes effect the next time you start the application.

If your operating system does not report an appearance at all, AI Ghost uses the light one.

## What changes

Only colours change: surfaces, text, outlines, selection and the accents of the controls. Sizes,
spacing and the rounded shapes stay the same, and so does the shipped `Ghost Writer` type face, so
nothing in the window moves when you switch the appearance.
