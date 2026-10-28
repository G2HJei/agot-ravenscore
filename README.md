# agot-ravenscore
A tournament score tracker and organizer for the "A Game of Thrones" board game.

Currently accessible for free at [ravenscore.zlatanov.xyz](https://ravenscore.zlatanov.xyz).

## Acceptance tests

End-to-end tests in `src/test/java/xyz/zlatanov/ravenscore/acceptance` drive Ravenscore through its web pages in a
real browser ([Playwright](https://playwright.dev/java/)). Ravenscore runs on an in-memory H2 database and talks to a
[WireMock](https://wiremock.org/) stand-in for Swords & Ravens. The tests are written in three layers:

- **Test cases** (`*AcceptanceTest`) describe tournaments in the language of the game, e.g.
  `ravenscore.createGame("name: Table 1", "type: 2nd edition")`.
- **DSL** (`dsl`) turns these steps into actions, filling in defaults and keeping tests isolated from each other.
- **Protocol drivers** (`drivers`) click through the web pages, stub Swords & Ravens and run the scheduled jobs, which
  are switched off in tests, on demand.

Run them with `mvn verify` (`mvn package` skips them). The first time, install the browser:

```shell
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test "-Dexec.args=install chromium"
```

Useful options:

- `-Dit.test=SwordsAndRavensAcceptanceTest` runs a single test class.
- `-Dplaywright.headless=false -Dplaywright.slowMo=200` shows the browser while the tests run.
- When a test fails, its screenshot and Playwright trace are saved in `target/playwright-traces`. Open a trace with
  `mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test "-Dexec.args=show-trace <trace.zip>"`.
