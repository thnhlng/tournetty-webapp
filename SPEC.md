# SPEC.md — Tournetty

> **Zweck dieses Dokuments:** `SPEC.md` ist der **Input für den Entwurf** — Ziel, Scope,
> Qualitätsziele, Anforderungen, Guardrails.
>
> **Bewusst nicht enthalten:** Architektur-Entscheide, Design-Prinzipien, Guardrail-Matrix,
> fertiges Kontextdiagramm — ebenso wenig die Mittel (asynchron? Fallback? wo wird gefiltert?).
> Die SPEC sagt **was** gelten muss, nicht **wie** es erreicht wird. Das **Wie** kommt später
> (Architektur / ADRs).
>
> **Version:** 0.1 · **Status:** Entwurf

---

## 1. Ziel

<!-- 2–4 Sätze: Was tut das System? Nutze aktive Verben (nimmt entgegen, erzeugt, schlägt vor, eskaliert). -->
Tournetty plant Volleyball-Turniere für das Organisationsteam. Viele Organisationsteam organisieren ihre Turniere von Hand.
Dies ist in der Regel sehr zeitaufwändig und unflexibel.
Das Ziel dieses Projekts ist diese Schritte zu automatisieren und auf unvorhersehbare Ereignisse wie kurzfristige Abmeldungen, schneller zu reagieren zu können.
Es generiert Gruppenaufteilung, K.O. Runden nach eingestellten Regeln. Es verteilt die Matches auf die Spielfeldern, so dass die maximal ausgeschöpft sind.
Die Spielergebnisse werden aufgezeichnet und Ergebnisse werden getrackt.

- **Zielgruppe:** Organisationsteam, Spieler und Zuschauer
- **Wichtigster Nutzen:** Eine automatische statt manuelle Turnier-Planung. Der KI entlastet der Mensch bei unvorhersehbare Ereignisse z.B. ein Team ist nicht aufgetaucht.

Dieser MVP-Slice deckt ab: Teams melden sich an -> Teilnehmerliste aufgenommen -> Gruppeneinteilung sowie Spiele generieren

## 2. Scope

### Enthalten
<!-- 4–6 Punkte, je eine Zeile, jeweils mit der wichtigsten Eigenschaft dahinter -->
- Teams können sich über Forms anmelden
- Turnier Generierung nach Gruppenphase(Round-Robin) und K.O. Runden
- Teams, Gruppen und Spielfeldern können erstellt werden
- Gestützt auf Anzahl Teilnehmer, schlägt die KI auf Anfrage vor wie viel Gruppen erstellt werden soll.
- Bei spontane Teamausfälle kann die KI Platzzuteilungen anpassen, jedoch nur mit Bestätigung vom User
- Ergebnisse können erfasst, geändert, gelesen und gelöscht werden

### Nicht enthalten
<!-- Explizite Abgrenzung: was naheliegend wäre, aber bewusst draussen bleibt -->
- Turniermodel Swiss
- Spieler von Teams können erfasst werden
- Sponsoren können angezeigt werden
- Teams können sich selber mutieren
- Login für Teams
- Live-Ticker

## 3. Qualitätsziele

Priorisiert — **bei Konflikten gilt diese Reihenfolge**:

1. **Korrektheit** — Ein Team kann nur einen Match spielen pro Slot
2. **Regelmässigkeit** — Alle Feldern sind pro Slot ausgelastet
3. **Wartbarkeit** Turniermodi, Kategorien, Punktesysteme, Pausenzeiten und Platzregeln sind konfigurierbar statt fest im Code eingebaut.
4. **Testbarkeit** — Spielplanerstellung, Tabellenberechnung, Tie-Breaker und Regelprüfungen funktionieren deterministisch und können automatisiert getestet werden.
5. **Performance** — Anmeldungen und Änderungen werden sofort bestätigt; aufwendige Spielplanberechnungen können danach im Hintergrund erfolgen.

*Diese Qualitätsattribute treiben die Architekturwahl.*

## 4. Systemkontext

**Akteure:** Organisator(organisiert das Turnier) ·
Vorort-Mitarbeitende (trägt Ergebnisse ein) · Teilnehmer (verfolgt das Ergebnis)

**Nachbarsysteme:** externer LLM-Provider für Klassifikation und Turnierplanung · DB für Ergebnisse und Gruppen speichern

## 5. Funktionale Anforderungen

Je Anforderung mindestens ein Akzeptanzkriterium (Given/When/Then, verkürzt).

### FR-1 · CRUD für Domain-Objekte
- **When** Turnier, Gruppe, Phase, Spiel oder Spielfeld erstellt wird, **then** erhält das es eine eindeutige ID. Es enthält mindestens die Attribute ID und Name.

### FR-2 · Spielplan Generierung
- **When** der User auf Planung klickt, **then** wird eine Phase-Planung-Seite angezeigt, wo der User die Phasen erstellen kann (Gruppen, K.O Runden)
- **When** die Phase-Planung fertig ist, **then** können die Spiele generiert (Gruppenphase immer nach Round-Robin und K.O. Runden immer nur ein Spiel).
- **When** während dem Turnier ein Team sich disqualifiziert oder vor dem Turnier nicht ankommen, **then** wird KI den Spielplan mit möglichst wenig Änderungen neu generieren.

### FR-3 · Phase Erstellung für Turnierablauf
- **When** der User auf Phase erstellen klickt, **then** er die Phasen des Turniers erstellen z.B. dass zuerst Gruppenphase dann K.O. Runden.
- **When** der User auf eine Phase klickt **then** kann die Phasen per drag and drop verschoben werden.

### FR-4 · Zählen
- **When** das Ergebnis eines Spieles eingetragen wurde **then** werden diese angezeigt. Bei Gruppenspiele wird korrekt gezählt, bei K.O. Runden werden die Teams korrekt weitergeleitet.

### FR-5 · Feld Zuteilung 
- **When** der User auf Spielplan generieren klickt, **then** werden die Spiele auf die verfügbaren Felder gleichmässig verteilt. Die Felder sind ausgelastet.
- **When** Die Spiele sind verschiebbar by drag and drop.

### FR-6 · Dashboard
- **When** der User das Dashboard öffnet, **then** wird eine Übersicht vom Turnier mit den Spielen und Tabellen angezeigt.
- **When** das Administration-Team das Dashboard öffnet, **then** wird eine Übersicht von den Entitäten eines Turniers angezeigt. Diese können vom User bearbeitet, neu erstellt und gelöscht werden.


## 6. Nichtfunktionale Anforderungen (SMART)

| ID    | Anforderung                        | Messgrösse / Prüfung                                                                                          |
|-------|------------------------------------|---------------------------------------------------------------------------------------------------------------|
| NfA-1 | Die App ist zuverlässig und stabil | Die App unterstützt 50 gleichzeitige Usern                                                                    |
| NfA-2 | Sicherheit                         | Nach 5 fehlgeschlagene Login-Versuche wird das Konto gesperrt                                                 |
| NfA-3 | User-Freundlich                    | App ist auf Mobile sowie Monitor abgestimmt.                                                                  |
| NfA-4 | Nachvollziehbarkeit                | Die KI begründet seine Erstellung, die angezeigten Änderung wird nur angewendet, wenn der User es akzeptiert. |

## 7. Rahmenbedingungen
- Kleines Team, ein Deployment-Ziel, kein eigener Betrieb rund um die Uhr. Hosting Backend auf Heroku und Frontend auf Cloudflare.
- MVP-Volumen < 50 gleichzeitige Teilnehmern auf Frontend.
- Kein eigenes Modell-Hosting; ein externer Provider wird eingekauft.
   

## 7. Evaluation

- **Testkorpus (versioniert):** <!-- welche Fälle, mit welchem erwarteten Ergebnis -->
- **Vor/nach Änderungen gemessen:** <!-- Metriken mit Verweis auf NfA-IDs -->
- **KI-Anteile müssen prüfbar sein** — auch ohne Aufruf des echten Modells.

## 8. Annahmen & offene Fragen

- **Annahme:** <!-- z. B. Volumen, Nutzerzahl — und was daraus folgt -->
- **Offen:** <!-- Schwellwert / Parameter mit Startwert, noch zu kalibrieren -->
- **Offen:** <!-- Entscheidung, die noch mit jemandem abzustimmen ist -->

## 9. Glossar

- **<!-- Begriff -->** — <!-- Definition in einem Satz -->
- **HITL** — Human-in-the-Loop: ein Mensch bestätigt vor jeder irreversiblen Aktion.
- **ADR** — Architecture Decision Record (dokumentierter Architektur-Entscheid).
- **NfA** — nichtfunktionale Anforderung / Qualitätsattribut.
