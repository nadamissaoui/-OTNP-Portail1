## Overview

This React-based monitoring UI does **not** implement a dedicated logging framework or structured logging system. All logging is performed exclusively through native browser `console` methods (`console.log`, `console.error`, `console.warn`).

## What System/Approach Is Used

- **No logging framework**: There are no dependencies such as `winston`, `pino`, `bunyan`, `log4js`, or any similar library listed in `package.json`.
- **Native console only**: All log output uses built-in browser `console.*` methods scattered directly throughout component and service files.
- **No centralized logger**: There is no shared logger module, no logger initialization file, and no abstraction layer over console methods.
- **No log level management**: Log levels are not configurable; usage of `console.log`, `console.error`, and `console.warn` is ad-hoc and inconsistent.
- **No structured fields**: Log messages are free-form strings with interpolated variables (e.g., `console.error("Erreur lors de la récupération des demandes", error)`). There is no standard structure for timestamps, request IDs, user context, or other metadata.
- **No log routing/sinks**: Logs go only to the browser developer console. There is no remote logging, no file output, and no integration with external monitoring services.

## Key Files Where Logging Occurs

Logging statements appear inline across multiple files:

- `src/services/monitoringService.js` — `console.error` in catch blocks for API failures
- `src/services/processService.js` — `console.log` for debugging API URL construction
- `src/components/ConsultationPage.jsx` — `console.error` and `console.warn` for data fetching and parsing errors
- `src/components/ErrorReportPage.jsx` — `console.error` for PDF/Excel export and data retrieval failures
- `src/components/RecyclageMassePage.jsx` — `console.error` for error handling
- `src/components/StartProcessPage.jsx` — `console.log` for SOAP response debugging, `console.error` for SOAP errors
- `src/components/StatistiquesPortabilite.jsx` — `console.log` for instance/process debugging, `console.error` for failures
- `src/components/AiAnalysisPage.jsx` — `console.error` in catch blocks
- `src/PortabilityOutForm.js` — `console.log` for form data debugging
- `src/reportWebVitals.js` — Performance metrics callback (optional, passed from `index.js`)

## Architecture and Conventions

The logging approach is entirely informal:

1. **Error logging**: Most `console.error` calls appear in `catch` blocks to surface exceptions during async operations (API calls, file exports, data parsing).
2. **Debug logging**: `console.log` statements are used for development-time debugging (e.g., printing API URLs, form data, process instances). These are not gated by environment checks and will appear in production builds.
3. **Warning logging**: Rare use of `console.warn` for non-critical issues (e.g., JSON parsing fallbacks).
4. **No cleanup strategy**: Debug `console.log` statements are left in the codebase without conditional compilation or environment-based filtering.
5. **French-language messages**: Most log messages are in French, reflecting the application's localization but creating inconsistency for international teams.

## Rules Developers Should Follow

Since no formal logging system exists, developers currently follow these implicit patterns:

- Use `console.error()` in catch blocks to surface exceptions
- Use `console.log()` for temporary debugging during development
- Use `console.warn()` for recoverable issues
- Do **not** introduce a logging framework unless there is a team-wide decision to adopt one
- Be aware that all console output is visible in production browser consoles — avoid logging sensitive data (PII, tokens, etc.)
- Consider removing or gating debug `console.log` statements before committing code

## Recommendation

For a production monitoring application, consider adopting a lightweight structured logging library (e.g., `pino` or `loglevel`) with:
- Environment-based log level configuration
- Structured JSON output for easier parsing
- Optional remote log shipping for production error tracking
- A centralized logger module to replace scattered console calls
