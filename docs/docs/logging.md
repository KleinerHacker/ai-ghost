# Logging

AI Ghost writes down what it does while it runs. The messages go to two places at once: to the
console the application was started from, and to a log file. When something goes wrong, that log
file is what a bug report is best written from.

## Where the log file is

The log is written into the folder `.ai-ghost/logs` of your home directory, next to your
`preferences.yml`:

| System  | Path                                          |
|---------|-----------------------------------------------|
| Windows | `C:\Users\<you>\.ai-ghost\logs\ai-ghost.log`   |
| Linux   | `/home/<you>/.ai-ghost/logs/ai-ghost.log`      |
| macOS   | `/Users/<you>/.ai-ghost/logs/ai-ghost.log`     |

## What is written where

| Destination | Detail                                                          |
|-------------|-----------------------------------------------------------------|
| Console     | The course of a session: what was loaded, saved, opened, closed  |
| Log file    | The same, plus the detailed steps in between                     |

The console keeps to the messages that mean something while you watch the application. The file also
carries the detailed steps, so it is longer than what you see in the console - that is intended.

!!! note "The log file is written in blocks"
    Lines are collected and written to disk in blocks, so the running application is not slowed down
    by the log. The last block is written when AI Ghost closes, so read the file after the
    application has ended.

## How much is kept

The current session writes into `ai-ghost.log`. The file is rolled over once a day, and also as soon
as it reaches 10 MB. Older logs sit next to it as compressed files named by their date, for example
`ai-ghost-2026-08-23-1.log.gz`. AI Ghost keeps the last ten of them and removes what is older, so the
folder cannot grow without end.

## Sending a log with a bug report

1. Close AI Ghost, so the last block is written.
2. Open `.ai-ghost/logs` in your home directory.
3. Attach `ai-ghost.log` - and, if the problem happened on an earlier day, the compressed file of
   that day.
