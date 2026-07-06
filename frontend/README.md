# TourPlanner Frontend

Angular 19 frontend for the TourPlanner application. The frontend follows MVVM with
standalone components, Angular signals, computed state, RxJS observables and injectable
view-models.

## MVVM Structure

- `src/app/core/models` — model interfaces used by the frontend.
- `src/app/core/api` — injectable HttpClient services for backend access.
- `src/app/core/auth/auth.service.ts` — global JWT/session state and localStorage persistence.
- `src/app/features/**/*.viewmodel.ts` — view-models with signal state, computed UI state,
  commands, API subscriptions and error/loading handling.
- `src/app/features/**/*.component.ts` and `*.html` — views. Components bind forms and
  forward UI events to view-model commands.
- `src/app/shared` — reusable UI components such as action buttons, statistic cards and
  charts.

Important view-models:

- `AuthViewModel` — login/register UI state, API calls, session storage and navigation.
- `ToursViewModel` — tour list, selection, CRUD, import/export and derived UI state.
- `TourFormViewModel` — location autocomplete, image upload, preview URL and DTO mapping.
- `TourLogsViewModel` — tour-log loading and mutation commands.
- `StatsViewModel` — statistics dashboard loading and state.

## Development

Install dependencies and start the local dev server:

```bash
npm install
npm start
```

The app runs at `http://localhost:4200/` and expects the backend at `http://localhost:8080`.

## Testing

Run the Angular/Jasmine unit tests once in headless Chrome:

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

Run the interactive Karma watcher during development:

```bash
npm test
```

Run TypeScript compile checks without opening a browser:

```bash
npx tsc -p tsconfig.app.json --noEmit
npx tsc -p tsconfig.spec.json --noEmit
```

The frontend tests cover API services, auth session state, view-model command behavior,
autocomplete, image upload, DTO mapping and formatting helpers.

## Build

```bash
npm run build
```

The build output is written to `dist/frontend`.
