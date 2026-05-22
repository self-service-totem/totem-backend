# Create PR for a backend feature

Read first:
- CLAUDE.md
- PR-CONTEXT.md
- docs/09-PACKAGE-STRUCTURE.md
- docs/10-TESTING-STRATEGY.md
- docs/11-OPENAPI-STRATEGY.md

Rules:
- Do not introduce new frameworks.
- Do not modify unrelated files.
- Follow package-by-capability structure.
- Keep business logic in domain/application.
- Keep infrastructure thin.
- Update OpenAPI source files, not only generated OpenAPI.
- Add unit tests for relevant business rules: successful case, invalid case, boundary case.
- Add acceptance-style tests in JUnit under the correct acceptance package.

Task:
$ARGUMENTS

Before editing:
1. Inspect the existing project style.
2. Propose files to create or modify.
3. Explain assumptions.
4. Wait for confirmation unless the user explicitly says to implement directly.

After editing:
1. Run relevant tests.
2. Run mvn test or the closest available test command.
3. Summarize changed files.
4. Report failed commands clearly.