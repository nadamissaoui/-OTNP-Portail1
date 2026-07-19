This repository uses **npm** as its primary dependency management system, bootstrapped via **Create React App (CRA)**. Dependencies are declared in `package.json` and locked using `package-lock.json` (lockfileVersion 3), ensuring deterministic builds across environments.

### Key Components
- **Package Manager**: npm (standard for CRA projects).
- **Lockfile**: `package-lock.json` is present and actively maintained, pinning exact versions of all direct and transitive dependencies.
- **Core Dependencies**:
  - `react` and `react-dom` (v19.2.6): UI framework.
  - `react-router-dom` (v7.15.0): Client-side routing.
  - `axios` (v1.18.1): HTTP client for API communication.
  - `recharts` (v3.8.1): Data visualization library.
  - `i18next` / `react-i18next`: Internationalization support.
  - `jspdf` / `xlsx`: Document generation and export capabilities.
- **Build Tooling**: `react-scripts` (v5.0.1) manages the underlying webpack, Babel, and ESLint configurations, abstracting complex build dependencies.

### Conventions & Rules
1. **Dependency Installation**: Use `npm install <package>` to add new libraries. This automatically updates `package.json` and regenerates `package-lock.json`.
2. **Version Pinning**: The lockfile ensures that `npm ci` or `npm install` reproduces the exact same dependency tree. Do not manually edit `package-lock.json`.
3. **No Vendoring**: The `node_modules` directory is excluded from version control (via `.gitignore`) and is reconstructed during installation.
4. **Script Usage**: Standard npm scripts (`start`, `build`, `test`) are defined in `package.json` to interact with the managed dependencies.
5. **Private Registry**: No custom registries or private scopes are configured in the visible files; dependencies are resolved from the public npm registry (`registry.npmjs.org`).