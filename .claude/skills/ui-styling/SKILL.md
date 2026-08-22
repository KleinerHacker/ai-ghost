---
name: ui-styling
description: Rules for the Java FX UI module app/ui - MVVM FX architecture pattern, I18N resource bundles and the global stylesheet/palette. Load before creating or changing anything under app/ui, any FXML, view model, message bundle or CSS.
---

# Architecture Pattern

* Always use MVVM based on MVVM FX framework

# I18N

* ALWAYS use I18N for UI

## Languages

* The base bundle `app/ui/src/main/resources/messages/bundle.properties` is ENGLISH and is the single
  source of truth for all keys
* The following translations MUST be present, one file per language, next to the base bundle:
  * `bundle_de.properties` - German
* Adding, renaming or removing a key in the base bundle MUST be applied to EVERY translation in the
  same change
  * FORBIDDEN: a translation carrying a key the base bundle does not have
  * FORBIDDEN: a translation missing a key of the base bundle
* Every file MUST be UTF-8; `\uXXXX` escapes are FORBIDDEN
* MessageFormat placeholders (`{0}`, `{1}`, ...) MUST be kept identical to the base bundle, including
  the doubled apostrophes of `''{0}''`
* Adding another language MUST be confirmed with the user first

# Styling

* The global stylesheet `app/ui/src/main/resources/styles/ai-ghost.css` is the ONLY source of the
  application appearance
  * It is accessed exclusively through the object class `AiGhostTheme` in the root package
    `org.pcsoft.app.aighost.app` of `app/ui`; the file name MUST NOT appear anywhere else
  * Every scene MUST be decorated with `AiGhostTheme.apply(scene)`
* Whenever a standard JavaFX control is used that the stylesheet does not style yet, the stylesheet
  MUST be extended for that control in the SAME change
  * FORBIDDEN: leaving a newly used control on the default Modena look
  * The new rules MUST cover the normal, hover, pressed, focused and disabled state where the
    control offers them
* Colours MUST come from the palette variables defined at the top of the stylesheet
  * FORBIDDEN: literal colour values inside a control rule, inline `style` attributes in FXML, or
    styling from Kotlin code
  * Radii, spacing and border widths MUST be written literally and MUST follow the documented values
    (8px for controls, 10px for popups); JavaFX resolves looked-up variables to plain numbers, which
    size based properties such as `-fx-background-radius` cannot consume
* A state change MUST NOT change the geometry of a control
  * Borders used as focus rings MUST exist in EVERY state with the same width and MUST only change
    their colour, `transparent` when not focused
  * FORBIDDEN: adding `-fx-border-width` or padding in a state selector such as `:focused`, because
    the control would jump
* The palette MUST stay consistent with `docs/docs/stylesheets/extra.css` and the shapes and colours
  of `docs/docs/assets/images/logo.png`
