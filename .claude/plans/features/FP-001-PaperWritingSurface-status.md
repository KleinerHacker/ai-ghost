# Feature-Status: Paper Writing Surface

Status: IN_PROGRESS

## Implementierungspläne

| ID    | Implementierungsplan                      | Status      |
|-------|-------------------------------------------|-------------|
| IP-01 | Font Discovery And Text Measuring         | COMPLETED   |
| IP-22 | Font Identity And Substitution Reporting  | COMPLETED   |
| IP-02 | Design Page Format Model                  | COMPLETED   |
| IP-24 | Optional Parts In The Model               | COMPLETED   |
| IP-03 | Layout Core                               | COMPLETED   |
| IP-04 | Pagination And Page Break Policy          | COMPLETED   |
| IP-25 | Renderer Library Module                   | COMPLETED   |
| IP-26 | Font And Measuring Migration              | COMPLETED   |
| IP-05 | Incremental Layout And Caching            | COMPLETED   |
| IP-06 | Layout Regression Harness                 | COMPLETED   |
| IP-07 | Paper Page View                           | COMPLETED   |
| IP-08 | Paper Flow View                           | COMPLETED   |
| IP-27 | Library Styling And Theming API           | NOT_STARTED |
| IP-28 | Standalone Reuse And Documentation        | NOT_STARTED |
| IP-09 | Undo And Redo Infrastructure              | COMPLETED   |
| IP-10 | Book Part Writing Surface                 | COMPLETED   |
| IP-11 | Paragraph Structure Operations            | NOT_STARTED |
| IP-12 | Inspector Shell And Content Sections      | COMPLETED   |
| IP-13 | Design Style Sections                     | COMPLETED   |
| IP-14 | Project Settings Dialog                   | COMPLETED   |
| IP-15 | Editor Arrangement And Tree Routing       | NOT_STARTED |
| IP-16 | Writing And Preview Modes                 | NOT_STARTED |
| IP-17 | AI Action Port                            | COMPLETED   |
| IP-18 | AI Actions On Paragraph And Heading       | NOT_STARTED |
| IP-19 | AI Part Generation (button only)          | NOT_STARTED |
| IP-21 | In-Paragraph Sheet Split                  | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree           | NOT_STARTED |

## Gesamtfortschritt

65 %

## Anmerkungen

IP-01, IP-22, IP-02, IP-24, IP-03, IP-25, IP-14, IP-26, IP-17, IP-04, IP-07, IP-08, IP-13, IP-10,
IP-06 und IP-05 sind umgesetzt; IP-11, IP-18, IP-19 und IP-27 sind entsperrt, und IP-16 ist durch
IP-05 entsperrt (wartet noch auf IP-15).

IP-18 und IP-19 wurden zurückgeschnitten: Ihre KI-Schaltflächen sind per FXML `onAction` an eine
parameterlose `*View`-Methode gebunden, deren einziger Rumpf `TODO("AI action: …")` ist – keine
Verdrahtung, kein Ergebnis, kein vorläufiger Zustand. Der in IP-17 ausgelieferte `lib/ai`-Aktions-Port
(`AiAction`, `AiActionRequest`, Callbacks, `AiActionLimits`, `ParagraphSplitter`) bleibt unverändert
bestehen und wird für eine spätere Wiederverwendung nicht angetastet; dieses Feature verdrahtet ihn
nur nicht. Der Feature-Plan wurde im selben Durchgang angepasst: Architektur, Planübersicht,
Abhängigkeitsgraph, Abschnitt „außerhalb des Umfangs“, Risiken und Abschlusskriterien besagen jetzt,
dass die KI-Schaltflächen an eine leere `*View`-Methode gebunden sind und der Port ungenutzt für
später stehen bleibt; ein Provider kommt weiterhin erst über ein späteres, eigenes
Plugin-System-Feature.

IP-05 wurde wie geplant gebaut. `lib/layouting` erhielt `IncrementalLineBreaker`, einen
`LineBreaker`, der einem echten Umbrecher (`GreedyLineBreaker`) vorgeschaltet ist und das Ergebnis
des Umbruchs jedes Blocks *für sich* zwischenspeichert, gekennzeichnet durch Blocktext, `TextStyle`
und Spaltenbreite. Ein späterer Aufruf verwendet dieses Ergebnis für jeden Block mit unverändertem
Schlüssel wieder und reicht nur den Rest an den Delegaten weiter; die Blöcke werden anschließend zu
einem `LaidOutText` zusammengestapelt, was reine Arithmetik ist und keine Messung trägt. Das
zusammengesetzte Ergebnis ist gleich einem vollständigen Umbruch mit dem Delegaten – bei
deterministischen Metriken bitgenau, bei echten Schriftmetriken bis auf das letzte Bit einer
Koordinate. Der Cache wächst nur, solange die Spaltenbreite gehalten wird: Eine geänderte Breite
verwirft ihn; eine Designänderung ist im Schlüssel nicht sichtbar, daher ruft der Aufrufer selbst
`clear()`. Zusätzlich bietet `prewarm(blocks, columnWidth)` das Vorabmessen der benötigten Blöcke an.
Die Seitengrenzen werden nach jeder Verwerfung über `LayoutEngine.layout` voll neu berechnet – das
ist Arithmetik über die Zeilen und trägt keine Messung. `BookPartEditorViewModel.recompute` verwendet
jetzt einen dauerhaften `IncrementalLineBreaker` statt einer neuen Instanz je Tastendruck; er wird
bei Designwechsel, Projektwechsel und beim Freigeben geleert, ein Spaltenbreitenwechsel wird vom
Umbrecher selbst behandelt.

Messung des Verhaltens (Benchmark-Entwicklertest `IncrementalLayoutBenchmarkTest` in
`lib/layouting-fx`, kopfloses TestFX mit `JavaFxTextMetrics`, Kunstbuch aus 201 Blöcken über 34
Seiten): kaltes Gesamtlayout **164,5 ms**, Neulayout nach einer Einzelabsatz-Änderung **1,68 ms**
(genau ein Block neu umbrochen, 200 Cache-Treffer). Getestet werden Treffer und Verwerfung des
Zwischenspeichers, die Gleichheit von inkrementellem und vollständigem Layout,
`IncrementalLineBreakerTest` in `lib/layouting` (Entwicklertest, `FixedTextMetrics`, 10 Fälle) sowie
der Benchmark. Der volle `build` ist grün: `:lib:ai-ghost-layouting:test` 49,
`:lib:ai-ghost-layouting:regressionTest` 7 (Golden Files unverändert), `:lib:ai-ghost-layouting-fx:test`
54, alle ohne Fehler. Die README-Zeile `ai-ghost-layouting` nennt jetzt das inkrementelle Caching;
der CHANGELOG erhielt einen Eintrag, dass die Schreibfläche in einem buchlangen Teil
reaktionsschnell bleibt. Die Plandatei wurde bei Abschluss per `git rm` entfernt.

IP-06 wurde wie geplant gebaut, mit einer Kategorie, die auf ausdrücklichen Wunsch des Nutzers zum
`testing`-Skill hinzugefügt wurde: Der Golden-File-Test und der Oberflächenvergleich sind weder ein
einfacher Entwicklertest noch ein `IT`-Test (in `lib` verboten), daher wurde dort zuerst eine dritte
Kategorie ergänzt, der Regressionstest (Suffix `RT`, ausschließlich in `lib`, spiegelbildlich zur
Beschränkung von `IT` auf `app`). `lib/layouting` erhielt `LayoutGoldenFileRT` (sieben Beispiel-
projekte: kurzer Teil, langer Teil, Blocksatz-Block, zwei Designs, gespiegelte Ränder für gerade/
ungerade Seiten, Prolog aus- und eingeschaltet) sowie `GoldenFileSupport`, das die Seiten eines
`DocumentLayout` als reine Zahlen serialisiert (`position`, `number`, `active`, `lines`,
`leftMargin`, `rightMargin`) und gegen eine eingecheckte `.golden`-Datei vergleicht, wobei bei einer
Abweichung die erste abweichende Zeile benannt wird; eine Datei wird durch erneutes Ausführen des
Tests mit `-DlayoutGoldenFiles.update=true` neu erzeugt und vor dem Commit per `git diff` geprüft.
`lib/layouting-fx` erhielt `PaperFlowPageComparisonRT`, das ein `DocumentLayout` auf derselben Bühne
sowohl in `PaperFlowView` als auch in `PaperPageView` einspeist und zwei Zählungen gegen das Layout
selbst prüft: die Seitenzahl von `PaperPageView` sowie das Verhältnis der
`.paper-flow-view-gap`-Bereiche von `PaperFlowView` (einer je Blockgrenze, unabhängig davon, wo eine
Seite umbricht) zu den `.paper-flow-view-break-mark`-Marken (eine je Seitenumbruch innerhalb eines
einzelnen Blocks). Beide Module erhielten eine Gradle-Aufgabe `regressionTest` (`*RT`-Klassen, aus
der einfachen `test`-Aufgabe ausgeschlossen), von der `check` – und damit `build` – abhängt; die
`test`-Aufgabe in `ci.yml` führt jetzt `test regressionTest` ausdrücklich aus, da `regressionTest`
getrennt von `test` steht. Zwei Fehler traten bei der Prüfung zutage und wurden vor dem Commit der
Golden Files behoben: Die `-D`-Systemeigenschaft zur Neuerzeugung der Golden Files wurde nicht in die
geforkte Test-JVM der Aufgabe weitergereicht, und die erste Fassung des Oberflächenvergleichs nahm
fälschlich an, ein Abstand markiere immer nur eine Seitengrenze, während `PaperFlowViewSkin`
tatsächlich zwischen je zwei Blöcken einen zeichnet. Die veraltete README-Zeile für
`lib/ai-ghost-layouting-fx` („Planned“) wurde im selben Durchgang auf „Implemented“ korrigiert, da
IP-07/IP-08 sie bereits ausgeliefert hatten. Der CHANGELOG blieb unberührt, da ein
Regressionsprüfstand für einen Endnutzer unsichtbar ist. Die Plandatei wurde bei Abschluss entfernt;
diese Anmerkung ist der einzige Nachweis. Kein anderer Plan wird durch ihn entsperrt.

IP-10 wurde auf Wunsch des Nutzers breiter gebaut als der ursprüngliche Plan-Titel. Der Projektbaum
erhielt zwei echte Knoten, `TitlePageItem` und `CopyrightPageItem` (`ProjectListItem`,
`ProjectListView`, `ProjectListCell`), vor dem Prolog platziert und aus dem Bündel beschriftet; das
vollständige Baum-Routing jedes Knotens bleibt bei IP-15. Die Tipp-Pause, die das Zusammenfassen von
Undo-Schritten beendet, wurde zu einer neuen Präferenzgruppe `Editor`
(`Preferences.editor.paragraphMergePauseMillis`, Vorgabe 600, Bereich 100..5000), gespiegelt als
`EditorProperty` gemäß `fx-model` und einmal von der Schreibfläche gelesen, wenn der Undo-Stack
übergeben wird – es gibt dafür noch kein Steuerelement im Einstellungsdialog. Der Undo-Stack wird
`MainWindowView -> Editor.bindUndoStack -> BookPartEditor` weitergereicht.

Die Fläche selbst ist ein neues MVVM-FX-Trio `BookPartEditor` in `app/ui`, das `PaperFlowView` aus
`lib/layouting-fx` einbettet (`app/ui` `requires`/hängt jetzt auch von `lib/ai-ghost-layouting-model`
ab). Ein `BookPartEditorViewModel` leitet den gewählten Knoten auf einen `PartMode` (NONE,
TITLE_PAGE, COPYRIGHT_PAGE, BOOK_PART, BLURB); Prolog, Kapitel und Epilog laufen durch eine
`BookPartProperty` (Kapitel über `ChapterProperty.of`), die Titelseite und die Copyright-Seite sind
schreibgeschützt, der Klappentext ist ein Fluss ohne Überschrift. Bei jeder gemeldeten Änderung und
jeder Designänderung wird das Modell über `BookPartBuilder`/`TitlePageBuilder`/`CopyrightPageBuilder`/
`BlurbBuilder` neu in Blöcke aufgebaut, mit `GreedyLineBreaker(JavaFxTextMetrics)` umbrochen und mit
`LayoutEngine.layout` paginiert, dann an die View zurückgegeben. Da `PaperFlowView` seine
Spaltenbreite aus den eigenen Textsteuerelementen ableitet, verwendet das erste Layout die schlichte
Inhaltsbreite der Seite, und der Spaltenbreiten-Listener rechnet einmal neu, sobald die echte Breite
gemeldet wird; ein leerer beschreibbarer Teil erhält einen leeren Absatzblock, damit es einen Ort zum
Tippen gibt. Jedes Blockziel (Überschrift, Anhangzeile, Absatz) trägt eine `StringProperty`, sodass
ein Tastendruck über `UndoStack.record` mit einem `(partId, target)`-Merge-Schlüssel aufgezeichnet
wird und ein Undo ihn genauso abspielt. Das Platzhalter-`Label` von `EditorView.fxml` wird durch
`BookPartEditor` ersetzt; die tote Regel `.editor-placeholder` in `editor.css` blieb bestehen.
`styles/component/book-part-editor.css` ist in `AiGhostTheme` registriert. Der volle `build` (alle
Module, alle Tests) ist grün.

IP-13 wurde enger gebaut als geplant: Die Beschränkung des Familienkatalogs, der familienspezifische
Beispieltext und die Kennzeichnung nicht installierter Familien existierten bereits in
`StyleDataEditor` (gebaut für `BookPartPageDesignSettings` aus IP-14), sodass die eigentliche Arbeit
dieses Plans ein dritter, stets aktiver `TitledPane`-Abschnitt „Design“ in `Inspector` war, neben
„Book“ und „Chapter“, mit vier wiederverwendeten `StyleDataEditor`-Instanzen (Titel, Kapiteltitel,
Kapiteltitel-Anhang, Fließtext), direkt gebunden an
`project.designProperty.titlePageProperty.titleStyleProperty` und die drei entsprechenden
Eigenschaften von `chapterPageProperty`. Anders als die beiden anderen Abschnitte folgt er nicht der
Auswahl im Projektbaum, sondern nur der Frage, ob überhaupt ein Projekt gebunden ist
(`InspectorViewModel.designAvailable`); die vier Editoren werden von `InspectorView` gehalten und an
`InspectorViewModel` weitergereicht, genauso wie `BookPartPageDesignSettingsView` seine drei
weiterreicht. `StyleDataEditor` erhielt eine kleine Ergänzung, `release()`, das an das ohnehin
interne `StyleDataEditorViewModel.release()` delegiert, sodass der Abschnitt seine Bindungen sauber
löst, wenn das Projekt geschlossen wird, statt sie auf einem verwaisten Design zu belassen.
`:app:ai-ghost-ui:compileKotlin`, `compileTestKotlin` und `test --tests "*Inspector*"` sind grün. Die
Plandatei wurde bei Abschluss entfernt; diese Anmerkung ist der einzige Nachweis. Kein anderer Plan
wird durch ihn entsperrt.

IP-08 wurde wie geplant gebaut: `PaperFlowView` (ein schlichtes `javafx.scene.control.Control` mit
einem von `SkinBase` abgeleiteten `PaperFlowViewSkin`, kein FXML) landete in `lib/layouting-fx`,
Paket `org.pcsoft.app.aighost.layouting.fx.paper`, neben `PaperPageView`, dazu `PaperFlowListener`
(sechs standardmäßig leere Callbacks: `onTextChanged`, `onCaretMoved`, `onFocusChanged`,
`onSplitRequested`, `onMergeRequested`, `onRemoveRequested`) und ein neutrales Standard-Stylesheet
(`src/main/resources/.../paper/paper-flow-view.css`). Je Block wird eine native `TextArea` gebaut,
indem die umbrochenen Zeilen eines `DocumentLayout` nach `LaidOutLine.blockIndex` gruppiert und ihr
Text mit einem einzelnen Leerzeichen wieder zusammengefügt wird; die Risikoliste des Feature-Plans
benennt jetzt, dass ein harter Zeilenumbruch innerhalb eines Blocks diesen Umlauf nicht überstehen
würde, da es im Modell heute keinen gibt. Ein Seitenumbruch zwischen zwei Blöcken erzeugt einen
echten Abstand in Größe des Seitenabstands von `PaperPageViewSkin`; ein Umbruch innerhalb eines
Blocks zeichnet eine gestrichelte `paper-flow-view-break-mark`-Linie mit der Zielseitenzahl,
positioniert über den Zeichen-Offset-Anteil des Umbruchs, da das eigene Textlayout einer `TextArea`
privat ist und ein Zugriff darauf die Toolkit-Reflexion wäre, die dieser Code niemals verwendet. Die
gemeldete `columnWidthProperty` zieht die eigenen Insets des Textsteuerelements von der Inhaltsbreite
der `PageGeometry` ab (`width - innerMargin - outerMargin`). Jede Nutzeraktion wird nur gemeldet, nie
angewandt, über `PaperFlowListener` – geformt nach dem Request/Callback-Port `AiAction` aus IP-17
statt mehrerer getrennter Listener-Listen, da dies der stärkste vorhandene Präzedenzfall für „Absicht
melden, nie verändern“ in diesem Code ist. Enter wird in eine Split-Anfrage abgefangen statt in einen
Zeilenumbruch, da ein Block ein Absatz ist, kein mehrzeiliges Feld; Backspace am Blockanfang und
Delete am Blockende werden zu Merge-Anfragen; eingefügter Inhalt geht immer über `insertText` allein
als reine Zeichenkette der Zwischenablage hinein. Spaltenbreite und Umbruchmarken werden 100 ms
verzögert hinter einer `PauseTransition` bei einer Größenänderung neu berechnet; der Text selbst wird
nie verzögert. Beim Neuaufbau der Steuerelemente eines Blocks aus einem frischen `DocumentLayout`
wird zuerst gemerkt, welcher Block den Fokus trug und wo sein Cursor stand, und beides danach
wiederhergestellt, sodass die Übergabe eines neu berechneten Layouts – wie sie der Verbraucher aus
IP-10 nach dem Anwenden einer gemeldeten Änderung vornimmt – das Tippen nie unterbricht. Eine Lücke
wurde dabei geschlossen, die auch IP-07 betraf: Das `.paper`-Paket von `lib/layouting-fx` war seit
dem Erscheinen von IP-07 nie in `exports` von `module-info.java` aufgenommen worden, da noch nichts
außerhalb des Moduls es las; es wird jetzt exportiert, da ein Verbraucher (`app/ui`, ab IP-10) beide
Views benötigt. Die Komponente registriert keinen Listener außerhalb ihres eigenen Knoten-Teilbaums,
sodass das `showingBinding()`-Muster von `fx-component-lifecycle` nicht greift, genauso wie bei
`PaperPageView`. CHANGELOG und MkDocs blieben unberührt, da noch nichts hiervon in einen
`app/ui`-Bildschirm verdrahtet ist und somit für einen Endnutzer nichts sichtbar ist.
`:lib:ai-ghost-layouting-fx:build` ist grün, mit 8 neuen Entwicklertests für `PaperFlowViewTest`.
IP-10 ist jetzt zusammen mit dem bereits abgeschlossenen IP-09 entsperrt; IP-06 und IP-27 sind
ebenfalls entsperrt.

IP-07 wurde wie geplant gebaut: `PaperPageView` (ein schlichtes `javafx.scene.control.Control` mit
einem von `SkinBase` abgeleiteten Skin, Canvas-basiertes Zeichnen, kein FXML) landete in
`lib/layouting-fx`, Paket `org.pcsoft.app.aighost.layouting.fx.paper`, neben einer
`PagePainter`-Schnittstelle mit einer `DefaultPagePainter`-Implementierung, und einem neutralen
Standard-Stylesheet (`src/main/resources/.../paper/paper-page-view.css`). Zwei bei der Erkundung
gefundene Lücken wurden vor der Umsetzung mit dem Nutzer geschlossen: `Page`/`DocumentLayout` tragen
keine Seitenbreite/-höhe, daher nimmt `PaperPageView` eine eigene `pageGeometryProperty` (Typ
`PageGeometry` aus `lib/layouting`) neben `documentLayoutProperty`, statt die bereits abgeschlossene
`Page` zu erweitern; und die „harte Kante“ einer inaktiven Seite wird abgeleitet, indem das
`active`-Flag einer Seite mit dem ihres unmittelbaren Nachbarn in `DocumentLayout.pages` verglichen
wird, statt ein neues Feld zu `Page` hinzuzufügen. Beides lässt IP-04 unangetastet. Eine
Blocksatzzeile (`LaidOutLine.wordSpacing > 0`) wird Wort für Wort gezeichnet, um genau den
Wortzwischenraum zu wahren, den die Engine berechnet hat; jede andere Zeile wird mit einem einzigen
`fillText`-Aufruf gezeichnet. Das Zeichnen löst seine `javafx.scene.text.Font` genauso auf, wie
`JavaFxTextMetrics` gemessen hat – `FontDescription(style.family, style.size.toInt(), style.bold,
style.italic)` über `FontResolver.font(...)`. Die Virtualisierung nutzt eine `ScrollPane` über einem
`Pane` voller Höhe; ein `Canvas` pro Seite existiert nur, solange diese Seite den Viewport plus einen
400px-Puffer schneidet, neu berechnet bei Scrollen, Größenänderung und Zoom. Stilklassen
(`paper-page-view`, `paper-page-view-scroll-pane`, `paper-page-view-container`,
`paper-page-view-sheet`, `paper-page-view-page-inactive`) markieren das Chrome für spätere
Überschreibung gemäß IP-27; Seitenzahlen und die inaktive/aktive Grenze werden gezeichnet, nicht
CSS-wählbar, da ein Aufrufer den `PagePainter` für ein anderes Aussehen austauscht. Die Komponente
registriert keinen Listener außerhalb ihres eigenen Knoten-Teilbaums, sodass das
`showingBinding()`-Muster von `fx-component-lifecycle` nicht greift. CHANGELOG und MkDocs blieben
unberührt, da noch nichts hiervon in einen `app/ui`-Bildschirm verdrahtet ist und somit für einen
Endnutzer nichts sichtbar ist. `:lib:ai-ghost-layouting-fx:build` ist grün. IP-08 bleibt entsperrt
wie zuvor; IP-06 und IP-27 rücken einen Schritt näher, warten aber weiter auf IP-08.

IP-04 wurde wie geplant gebaut, um eine ausdrückliche Nutzeranfrage erweitert: Der Rändertausch für
gerade/ungerade Seiten greift nur, wenn eine neue Projekteinstellung, „Spiegelnde Ränder“
(`PageFormat.mirroredMargins`, Vorgabe `false`), eingeschaltet ist – ohne sie behält jede Seite
denselben inneren/äußeren Rand. Das Feld wurde zu `PageFormat` in `lib/model` hinzugefügt und gemäß
dem `fx-model`-Skill in `PageFormatProperty` in `lib/fx-model` gespiegelt, mit vollständigen
Property-Tests; es wurde noch kein UI-Kontrollkästchen verdrahtet, da dies zum Projekt-
einstellungsdialog gehört, nicht zu diesem Plan. Die Titelseite und die Copyright-Seite tragen auf
Wunsch ebenfalls keine Seitenzahl.

`lib/layouting` erhielt `PageGeometry` (ein modellunabhängiges Abbild von `PageFormat`, so wie
`TextStyle` einen gespeicherten Stil abbildet), `Page` und `DocumentLayout`, die
`PageBreakPolicy`-Schnittstelle mit ihrer `NonePageBreakPolicy`-Implementierung (greedy, keine
Vermeidung von Witwen/Waisen, keine Vorausschau) sowie `LayoutEngine` mit `layout(...)` für einen
einzelnen Teil und `layoutBook(...)` für ein ganzes Buch über Teile hinweg. `LayoutEngine` hält
`position` (physische Seitenposition, inklusive inaktiver Seiten) und `pageNumber` (`null`, wo eine
Seite ungezählt ist) streng auseinander, löst `leftMargin`/`rightMargin` aus `mirroredMargins` und
der physischen Position der Seite auf (Position 0 = recto) und beachtet die beiden Leerseiten-Schalter
(`Design.startWithEmptyPage`/`endWithEmptyPage`) am Anfang und Ende eines Buches. Ein Teil beginnt
strukturell garantiert auf einer eigenen Seite, da `layoutBook` die Zeilen eines Teils nie auf dem
Ende der letzten Seite des vorherigen Teils fortsetzt. `lib/layouting-model` erhielt
`PageGeometryTranslation`, das ein gespeichertes `PageFormat` in eine `PageGeometry` übersetzt,
analog zu `StyleTranslation`. Zu `lib/layouting` selbst wurde keine Abhängigkeit hinzugefügt, sodass
es wie zuvor frei vom Manuskriptmodell bleibt.

Die `lib/ai-ghost-layouting`-Zeile in der README wurde erweitert, um die Paginierung neben
Zeilenumbruch und Ausrichtung zu benennen; der CHANGELOG blieb unberührt, da nichts von diesem Plan
Erzeugtes in der laufenden Anwendung sichtbar ist – die neue Präferenz hat kein UI-Steuerelement, bis
ein späterer Plan eines verdrahtet.

IP-14 wurde auf Wunsch des Nutzers breiter gebaut als zunächst geplant: ein Master-Detail-Dialog mit
einem `ProjectSettingsTree` (Wurzel verborgen) links und dem Abschnitts-Editor rechts. Nur der
`General`-Abschnitt ist echt (`GeneralSettings`: Seitengrößen-Vorgaben, vier Ränder in Millimetern,
die beiden Leerseiten-Flags), gebunden an eine Arbeitskopie-`DesignProperty`. Der `Design`-Knoten und
seine vier Kinder sind `PlaceholderSettings`; ihre Stil-Editoren bleiben bei IP-13. Es wurde kein
neues Modell hinzugefügt – die Design-POJOs für Prolog/Epilog/Klappentext und einen getrennten
Titelanhang sind weiterhin offen und aufgeschoben. Der Dialog hält ein tief kopiertes Arbeitsprojekt;
OK und APPLY schreiben die Seitengeometrie und die beiden Flags zurück, CANCEL / ESCAPE verwerfen.
Die Schaltflächen sind `OK`, `CANCEL`, `APPLY` (`DialogButtons.OK_CANCEL_APPLY`), APPLY konsumiert,
sodass es speichert ohne zu schließen, OK und APPLY deaktiviert, solange die Eingabe nicht gespeichert
werden kann. Der Menüeintrag und die Werkzeugleisten-Schaltfläche in `MainWindowView` sind
verdrahtet. Das Styling landete in einem neuen `styles/component/project-settings.css` (in
`AiGhostTheme` registriert) statt in `dialog.css`, da die Combobox, das Kontrollkästchen und der
schlichte Trenner hier zuerst verwendet werden. Die Plandateien wurden bei Abschluss entfernt; diese
Tabelle ist der einzige Nachweis.

Nachträglich auf Wunsch des Nutzers angepasst: Der Seitenformat-Editor wanderte vom `General`-Knoten
auf den `Design`-Knoten, `General` ist jetzt ein leerer Platzhalter und der Dialog öffnet auf
`Design`; der `Design`-Zweig erhielt zwei weitere Platzhalter-Kinder, `Title page` und `Copyright
page`, vor `Epilog`. `ProjectSettingsSection` wurde von einem `enum` in ein `sealed interface`
umgeformt, analog zu `ProjectListItem`, und `ProjectSettingsTreeView` baut seinen Baum jetzt
ausdrücklich mit einer benannten `ProjectSettingsTreeCell` wie `ProjectListView`. Der Fortschritt ist
unverändert.

IP-22 wurde entlang der Modulgrenze geschnitten, statt als Ganzes in `app/ui` zu bleiben. Die Messung
ist reines JavaFX und kennt keinen Typ dieser Anwendung, daher leben `FontFingerprint` und
`FontFingerprints` in `lib/layouting-fx`, das sein erstes Paket mit ihnen exportiert; `lib/layouting`
bleibt unangetastet, da die Engine nie einen Fingerabdruck sieht. `app/ui` behält, was
anwendungsgebunden ist: die Übersetzung auf `FontMetricsData`, den Vergleich in `FontIdentity`, den
Durchlauf über das Design in `FontIdentityCheck` und den Bericht. `app/ui` hängt daher von nun an von
`lib/layouting-fx` ab. Die Referenzmenge wuchs über die im Feature-Plan benannten ASCII plus Umlaute
hinaus: Sie trägt zusätzlich Latin-1, Latin Extended-A und Kyrillisch, da sich eine ersetzte Familie
meist genau in diesen Buchstaben unterscheidet, während reines ASCII noch übereinstimmt. Menge und
Größe sind ab jetzt fest. Ein Fingerabdruck wird beim Speichern eines Projekts geschrieben und nur
dort, wo noch keiner steht, da der Design-Editor aus IP-13 noch nicht existiert und ein Überschreiben
bei jedem Speichern den Vergleich sinnlos machen würde.

Die JavaFX-Entscheidung von IP-25 wurde getroffen: `.claude/rules/architecture.md` erlaubt jetzt
JavaFX in genau einer Komponentenbibliothek unter `lib`, und `lib/layouting-fx` ist diese eine. Das
Modul baut und sein kopfloser TestFX-Rauchtest läuft. Es exportiert noch nichts; das Renderer-Paket
kommt mit IP-26. Kein Plan wird durch irgendetwas blockiert.

IP-26 verschob die Schriftgrundlage von IP-01 nach `lib/layouting-fx`: `FontCatalog`,
`FontResolver`, `FontResolution` und `JavaFxTextMetrics` leben jetzt im Paket
`org.pcsoft.app.aighost.layouting.fx.font` neben dem Fingerabdruck aus IP-22, und ihre drei Tests
zogen mit ihnen auf das kopflose TestFX-Setup des Moduls um (`:lib:ai-ghost-layouting-fx:test` führt
jetzt 34 aus). Der Bibliothekstyp, der `FontData` in jeder verschobenen Signatur ersetzt, ist
`FontDescription` (Familie, `size: Int`, `bold`, `italic`); die Größe bleibt ein ganzer Punkt, sodass
Auflösung und Messungs-Cache unverändert sind. Die anwendungsseitige Übersetzung ist eine Erweiterung
`FontData.toFontDescription()` in `app/ui` (`FontTranslation.kt`), nach dem Vorbild von
`FontFingerprintTranslation.kt`, auf Wunsch des Nutzers über ein Übersetzer-Objekt entschieden.
`FontIdentity` ist der einzige Produktionsaufrufer und löst darüber auf. Kein `module-info` brauchte
eine strukturelle Änderung – das Paket war bereits exportiert, `javafx.graphics` bereits `required`,
`app/ui` las die Bibliothek bereits seit IP-25 – nur Kommentare wurden geschärft. `app/ui` behält
`FontIdentity`, `FontIdentityCheck` und die beiden Übersetzungsdateien. `SplashStageTest` wurde auf
Wunsch des Nutzers gelöscht: Er scheiterte auf dem unveränderten HEAD in dieser kopflosen Umgebung
(Splash-Deckkraft `1.0` statt `0.0`), unabhängig von diesem Plan. Der volle `build` und ein
erzwungenes `clean :app:ai-ghost-ui:jlink` sind grün; das Laufzeit-Image baut ohne Split-Package-
oder Auflösungsfehler. IP-01 bleibt COMPLETED; IP-07, IP-08 und IP-13 sind jetzt entsperrt.

Bei der Prüfung von IP-25 gefundener Fehler, außerhalb seines Umfangs und nicht von ihm verursacht –
er scheiterte bereits auf dem unveränderten Projekt – und auf Wunsch des Nutzers sofort behoben:
`:app:ai-ghost-ui:createMergedModule` konnte den generierten Deskriptor des gemischten Moduls nicht
kompilieren, da diese Kompilierung nur die Staging-Verzeichnisse des jlink-Plugins sieht und weder
JavaFX noch die Kotlin-Standardbibliothek dort gestaget waren. `addExtraDependencies("javafx",
"kotlin")` im `jlink`-Block von `app/ui` staget sie; `jlink` erzeugt das Image jetzt wieder mit
seinem Launcher.

Der Renderer wurde nach IP-03 zu einer eigenen Bibliothek. Beide Oberflächen und die
JavaFX-Messung leben im neuen Modul `lib/layouting-fx` (`ai-ghost-layouting-fx`), das von
`ai-ghost-layouting` und JavaFX abhängt und von nichts sonst und keinen Typ dieser Anwendung in
irgendeiner Signatur trägt. Es ist eine wiederverwendbare JavaFX-Komponentenbibliothek, genau so, wie
`lib/layouting` eine wiederverwendbare Satzbibliothek ist. Vier Pläne tragen es: IP-25 erstellt das
Modul und die Regel, IP-26 verschiebt die Schrift- und Messklassen des abgeschlossenen IP-01 aus
`app/ui` heraus, IP-27 macht das Erscheinungsbild überschreibbar, IP-28 belegt und dokumentiert die
Unabhängigkeit. IP-07 und IP-08 wurden auf die Bibliothek umgeschnitten, nicht neu geschrieben; IP-01
bleibt COMPLETED, da IP-26 die Verschiebung übernimmt.

Zwei Dinge wanderten mit ihm zwischen Plänen: die Seitenvirtualisierung von IP-16 in IP-07, da eine
Bibliothek, die kein langes Dokument zeigen kann, nicht wiederverwendbar ist, und der
Oberflächenvergleich von IP-06 in das Test-Quellset der Bibliothek. `app/ui` behält die
`FontData`-Übersetzung, den Metrik-Fingerabdruck aus IP-22, die Bindung von Buch und Design, Undo,
Inspector und die KI-Schaltflächen.

Der Layout-Kern ist wie geplant umgesetzt. `LaidOutLine` trägt zwei Felder, die der Plan nicht
benannte: `width` und `wordSpacing` als den Abstand, um den eine Blocksatzzeile gedehnt wird – ohne
ihn ließe sich eine Blocksatzzeile nicht allein aus dem Ergebnis zeichnen. Eine Umbruchgelegenheit
ist nicht immer eine Lücke: Nach einem Bindestrich folgt das nächste Wort unmittelbar, daher trägt
ein Wort, ob Leerraum es vom nächsten trennte. Die Abstände über und unter einem Block werden nicht
im Dokument gespeichert und stehen fest in `BlockSpacing` von `lib/layouting-model`.

Der Umfang deckt die Titelseite, die Copyright-Seite und alle geschriebenen Teile ab. Prolog, Epilog
und Klappentext stehen immer auf eigenen Seiten und tragen immer ihren Text; das Kontrollkästchen im
Projektbaum entscheidet nur, ob sie zum Buch gehören. Ein ausgeschalteter Teil ist ausgegraut, aus
der Seitennummerierung genommen und weiterhin beschreibbar, weshalb IP-24 `Book` so überarbeitet,
dass nichts gelöscht wird. IP-23 ist der einzige Plan dieses Features, der `ProjectList` berührt.

IP-24 trägt den Schalter als Feld `included` direkt auf `Prolog`, `Epilog` und `Blurb`. Die
gemeinsame Schnittstelle, die der Plan von IP-24 einst beschrieb, wurde vor der Umsetzung
fallengelassen: Kein Aufrufer erreicht den Schalter polymorph, daher hätte die Schnittstelle kein
Gewicht getragen. `Prolog.title` und `Epilog.title` erhielten die Vorgabe `""`, da `Book` die drei
Teile jetzt selbst baut.

Text wird mit `javafx.scene.text.Font` über einen wiederverwendeten verborgenen `Text`-Knoten
gemessen. Die Implementierung lebt heute in `app/ui` und zieht mit IP-26 nach `lib/layouting-fx` um;
`lib/layouting` besitzt die `TextMetrics`-Schnittstelle und bleibt frei von jedem Toolkit.

IP-03 wurde vor der Umsetzung neu geschnitten. `lib/layouting` ist eine Allzweck-Satzbibliothek: Sie
hängt von nichts ab, nicht einmal von `ai-ghost-model`, trägt eigene Stil- und Ausrichtungstypen und
kennt einen einzelnen Textblock plus Stil – keine Rolle, keine Titelseite, keinen Überschriftstyp.
Diese Blöcke unterscheiden sich nur darin, welcher Text und welcher Stil hineingeht, daher hätten
getrennte Typen kein Gewicht getragen. Die Builder, die `Book`, `Design` und `Meta` lesen, wanderten
in das neue Modul `lib/layouting-model`, benannt und paketiert nach `lib/fx-model`, das daher das
einzige Modul ist, das von beiden Seiten abhängt. IP-20 (PDF-Export) wurde aus diesem Feature
entfernt: Der Export wird ein eigenes Feature, als Plugin auf Apache PDFBox umgesetzt, und braucht
zuerst die Plugin-Infrastruktur. IP-22 wurde hinzugefügt, als entschieden wurde, dass keine
Manuskriptschrift ausgeliefert wird; es steht bei IP-01, da es zur Schriftgrundlage gehört. IP-25 bis
IP-28 wurden hinzugefügt, als der Renderer als eigenständige Bibliothek beschlossen wurde. Die
Nummerierung wurde stabil gehalten, statt die Pläne neu zu nummerieren.

Unabhängige Ausgangspunkte: IP-09, IP-12 (abgeschlossen), IP-17 (abgeschlossen; Port bleibt für
spätere Wiederverwendung, von IP-18/IP-19 nicht verdrahtet).

Neben der JavaFX-Entscheidung von IP-25 sind zwei Fragen der Renderer-Bibliothek offen und blockieren
sie nicht: die Benennung von Modul und Paket, die den Anwendungsnamen in eine wiederverwendbare
Bibliothek trägt, und ob die Bibliothek als echtes Artefakt veröffentlicht oder nur hier gebaut wird.

Seitenformat und Ränder, das Vorwerk, der Ort des Klappentexts, das Verhalten eines ausgeschalteten
Teils und die Referenzmenge des Metrik-Fingerabdrucks sind in Abschnitt 9 des Plans unter
„Getroffene Entscheidungen“ festgehalten.

Kein Plan hebt eine Modellversion an oder migriert eine bestehende Nutzerdatei; ein Dokument, das
keinen der neuen Werte trägt, wird mit deren Vorgaben gelesen.

Die verbleibenden Implementierungspläne sind unter `.claude/plans/implementation` ausgeschrieben,
jeder mit seiner eigenen Statusdatei und mit seinem Ursprung und seinen Abhängigkeiten benannt;
`FP-001-Overview.md` listet sie der Reihe nach. Die Dateien eines abgeschlossenen Plans werden
entfernt, sodass die obige Tabelle der einzige Nachweis ist, dass er erledigt ist.

Der Feature-Plan dient nur der Orientierung: Er trägt das Ziel, die Architektur, die Planübersicht,
den Abhängigkeitsgraphen, die Entscheidungen und die Abschlusskriterien. Aufgaben, Einschränkungen
und Tests eines Plans leben in seiner Datei unter `.claude/plans/implementation`; Abschnitt 7 des
Feature-Plans benennt diese Datei je Plan und behält nur die Begründung, die der detaillierte Plan
nicht trägt.

Das Feature fügt drei Gradle-Module hinzu (`lib/layouting`, `lib/layouting-model`, `lib/layouting-fx`)
und braucht eine neue Drittanbieter-Abhängigkeit: JavaFX in einem Bibliotheksmodul.

Das zentrale technische Risiko ist, dass die Messung dem FX-Thread gehört. Sie muss in IP-05 gemessen
werden, nicht erst in IP-16 bemerkt.

IP-21 ist optional und wird nur begonnen, wenn sich die Umbruchmarke von IP-08 im Gebrauch als
unzureichend erweist.
