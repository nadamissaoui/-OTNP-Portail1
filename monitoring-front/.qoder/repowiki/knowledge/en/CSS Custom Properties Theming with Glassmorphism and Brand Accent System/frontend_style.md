## Styling Approach

This React application uses **plain CSS with CSS Custom Properties (CSS Variables)** for theming, organized in per-component `.css` files colocated with their JSX components. There is no CSS preprocessor (Sass/SCSS), no utility-first framework actively used (TailwindCSS is listed in `package-lock.json` but has no config file and zero usage via `@tailwind` or `@apply` directives), and no CSS-in-JS library.

### Core Architecture

**1. Global Theme System via CSS Custom Properties**

The application implements a dual-theme system (light/dark) using CSS variables defined at `:root` and toggled via class names on `<body>`:

- **Light theme**: Activated by `body.light-theme` or `body[data-theme='light']`
- **Dark theme**: Activated by `body.dark-theme` or `body[data-theme='dark']`

Key design tokens are centralized in `src/index.css` and repeated/refined in component-specific files:

| Variable | Light Value | Dark Value | Purpose |
|---|---|---|---|
| `--bg-primary` | `#ffffff` | `#000000` | Main page background |
| `--bg-secondary` | `#f8f9fa` | `#121212` | Cards, navbar backgrounds |
| `--bg-tertiary` | `#edf2f7` | `#1a1a1a` | Inputs, hover states |
| `--text-main` / `--text-primary` | `#0a0f2c` | `#ffffff` | Primary text |
| `--text-muted` / `--text-secondary` | `#64748b` | `#94a3b8` | Secondary/muted text |
| `--border-color` | `#dee2e6` | `rgba(255,255,255,0.1)` | Borders |
| `--accent-color` | `#ff6600` | `#ff6600` | Brand orange (Billcom) |
| `--shadow` | `rgba(0,0,0,0.05)` | `rgba(0,0,0,0.5)` | Box shadows |

**2. Brand Color Palette**

A consistent accent color (`#ff6600`, referred to as "Billcom orange") is used throughout for:
- Active navigation items (sidebar border-left indicator)
- Primary buttons (`.btn-main`, `.submit-btn`, `.analyze-button`)
- Focus ring highlights on inputs
- Logo text and brand elements
- Notification badges

Secondary semantic colors include:
- Error: `#c0392b` / `#E24B4A`
- Success: `#1D9E75` / `#1a6b3c`
- Warning: `#BA7517`

**3. Component-Level CSS Organization**

Each component has its own `.css` file following a BEM-like naming convention with prefixes:
- `.navbar-*` for Navbar styles
- `.sidebar-item`, `.submenu-item` for Sidebar
- `.sp-*` prefix in `StartProcess.css` (e.g., `.sp-container`, `.sp-form-card`)
- `.ai-analysis-*` prefix in `AiAnalysis.css`
- `.dashboard-*` in Dashboard styles

**4. Layout Structure**

The app uses a fixed-layout dashboard pattern:
- **Navbar**: Fixed at top, `height: 70px`, `z-index: 1000`
- **Sidebar**: Fixed at left, `width: 280px`, starts below navbar (`top: 70px`), dark gradient background (`linear-gradient(180deg, #141124 0%, #0b0813 100%)`)
- **Main content**: Uses `margin-left: 280px` and `margin-top: 70px` to offset fixed elements

Responsive behavior collapses sidebar on mobile (`max-width: 768px`) via `transform: translateX(-100%)`.

**5. Visual Design Patterns**

- **Glassmorphism**: Login card uses `backdrop-filter: blur(16px)` with semi-transparent backgrounds (`rgba(255,255,255,0.05)`) and subtle borders
- **Animated backgrounds**: Login page features rotating gradient blobs (`@keyframes rotateWave`) and floating elements (`@keyframes floatAnimation`)
- **Page transitions**: Full-screen overlay with progress bar animation during navigation
- **Custom fonts**: `StartProcess.css` imports Google Fonts (`DM Sans`, `Space Grotesk`) for a distinct form aesthetic

**6. Heavy Use of `!important`**

Many CSS rules across `App.css`, `Navbar.css`, `Sidebar.css`, and `Dashboard.css` use `!important` declarations extensively. This suggests either override conflicts from a base framework (possibly Bootstrap remnants from Create React App defaults) or a lack of CSS specificity management discipline.

## Key Files

- `src/index.css` — Global theme variable definitions and body-level theme switching
- `src/App.css` — Login page styles (glassmorphism card, animated backgrounds, split layout), page transitions, legacy layout rules
- `src/components/dashboard/Dashboard.css` — Dashboard-scoped theme variables, table/input styling, loading spinner
- `src/components/layout/Navbar.css` — Fixed navbar styling, dropdown menus, avatar/profile menu, notification badge
- `src/components/layout/Sidebar.css` — Fixed sidebar with dark gradient, navigation items, submenu nesting, responsive collapse
- `src/components/StartProcess.css` — Standalone form styling with custom font imports, grid layout, branded inputs
- `src/components/AiAnalysis.css` — AI analysis page with NETWATCH-aligned color scheme, severity badges, confidence bars, tab controls
- `src/pages/DashboardPage.css` — Page-level layout wrapper with ambient background glow effect

## Developer Conventions

1. **Theme variable naming**: Use `--bg-*`, `--text-*`, `--border-color`, `--accent-color`, `--shadow` consistently. Reference via `var(--variable-name)` rather than hardcoded colors.

2. **Component CSS scoping**: Prefix class names with a component-specific abbreviation (e.g., `.sp-` for StartProcess, `.ai-analysis-` for AiAnalysis) to avoid global collisions.

3. **Accent color**: Always use `#ff6600` (or `var(--accent-color)`) for primary actions, active states, and brand highlights. Do not introduce new primary brand colors.

4. **Layout offsets**: Remember that main content areas need `margin-left: 280px` (sidebar width) and `margin-top: 70px` (navbar height) unless inside a flex container that handles spacing.

5. **Avoid `!important`**: The codebase overuses `!important`. New styles should rely on proper CSS specificity and cascade order instead.

6. **No TailwindCSS**: Despite being in `package-lock.json`, Tailwind is not configured or used. Do not add Tailwind utility classes; stick to plain CSS with custom properties.

7. **Responsive breakpoints**: Use `@media (max-width: 768px)` for tablet/mobile adjustments and `@media (max-width: 520px)` for small mobile screens.

8. **Font stack**: Default is `-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif`. Custom fonts should be imported at the top of the specific component CSS file.