## Overview
The project uses **Create React App (CRA)** as its sole build system, managed via `react-scripts` (v5.0.1). There are no custom build scripts, Dockerfiles, CI/CD configurations, or Makefiles present in the repository.

## Key Files and Scripts
- **`package.json`**: Defines the standard CRA lifecycle scripts:
  - `npm start`: Starts the development server.
  - `npm run build`: Compiles the application for production into the `build/` directory.
  - `npm test`: Runs tests using Jest via `react-scripts`.
  - `npm run eject`: Ejects from CRA to expose underlying Webpack/Babel configurations (one-way operation).
- **`public/index.html`**: The HTML template used during the build process.
- **`build/`**: The output directory for production artifacts (currently empty in the source tree, populated after running `npm run build`).

## Architecture and Conventions
- **Zero-Configuration Build**: The project relies entirely on CRA's default Webpack and Babel configurations. No custom `webpack.config.js` or `.babelrc` files are present.
- **Dependency Management**: Uses `npm` with a `package-lock.json` for deterministic installs.
- **Testing**: Integrated with Jest and React Testing Library via `react-scripts test`.
- **Deployment**: The README references standard CRA deployment strategies (e.g., static hosting), but no specific deployment pipelines (GitHub Actions, Jenkins, etc.) are configured in the codebase.

## Developer Rules
1. **Build Commands**: Use only the predefined npm scripts (`start`, `build`, `test`) for development and production builds.
2. **Ejection Avoidance**: Do not run `npm run eject` unless absolutely necessary, as it removes the abstraction layer and requires manual maintenance of build tools.
3. **Environment Variables**: Use `.env` files for configuration, following CRA's convention (`REACT_APP_` prefix required for client-side exposure).
4. **No Custom Webpack**: Avoid attempting to modify Webpack settings directly; use CRACO or similar tools if advanced configuration is needed (though none are currently installed).