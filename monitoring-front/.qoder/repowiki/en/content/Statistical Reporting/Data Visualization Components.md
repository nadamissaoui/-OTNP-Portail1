# Data Visualization Components

<cite>
**Referenced Files in This Document**
- [StatistiquesPortabilite.jsx](file://src/components/StatistiquesPortabilite.jsx)
- [Statistique.css](file://src/components/Statistique.css)
- [package.json](file://package.json)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)

## Introduction
This document provides comprehensive documentation for the data visualization components in the SmartPorta statistical reporting system. It focuses on the Recharts integration within the portability statistics page, covering PieChart and BarChart implementations, responsive container usage, chart configuration patterns, color schemes, tooltip behaviors, legend positioning, data transformation functions, and styling approaches. It also includes examples of customization, interactivity patterns, and responsive design considerations.

## Project Structure
The data visualization features are implemented in a dedicated statistics component that integrates with external monitoring APIs to render performance metrics and drill-down capabilities.

```mermaid
graph TB
subgraph "SmartPorta Frontend"
SP["StatistiquesPortabilite.jsx<br/>Main Statistics Component"]
SCSS["Statistique.css<br/>Chart Styles & Layout"]
PKG["package.json<br/>Dependencies"]
end
subgraph "External Services"
PERF["Monitoring API<br/>statistics/performance"]
MONTHLY["Monitoring API<br/>statistics/monthly"]
BYSTATUS["Monitoring API<br/>instances/by-status"]
end
SP --> PERF
SP --> MONTHLY
SP --> BYSTATUS
SP --> SCSS
PKG --> SP
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:12-63](file://src/components/StatistiquesPortabilite.jsx#L12-L63)
- [Statistique.css:1-698](file://src/components/Statistique.css#L1-L698)
- [package.json:20](file://package.json#L20)

**Section sources**
- [StatistiquesPortabilite.jsx:12-63](file://src/components/StatistiquesPortabilite.jsx#L12-L63)
- [Statistique.css:1-698](file://src/components/Statistique.css#L1-L698)
- [package.json:20](file://package.json#L20)

## Core Components
The statistics page implements:
- Two PieCharts (IN and OUT) for status distribution
- One BarChart with monthly trend data
- Responsive container for adaptive sizing
- Status mapping constants and data transformation function
- Drill-down capability for filtered instance lists
- Pagination and CSV export for large datasets

Key implementation highlights:
- Recharts imports: PieChart, Pie, Cell, Tooltip, Legend, BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer
- Color scheme: ['#00C49F', '#FF8042', '#0088FE']
- Status mapping: active → 1, completed → 2, aborted → 3
- Data transformation: prepareData() converts status counts to Recharts-compatible format

**Section sources**
- [StatistiquesPortabilite.jsx:2-6](file://src/components/StatistiquesPortabilite.jsx#L2-L6)
- [StatistiquesPortabilite.jsx:8-10](file://src/components/StatistiquesPortabilite.jsx#L8-L10)
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)

## Architecture Overview
The visualization architecture follows a data-driven pattern with clear separation between data fetching, transformation, and rendering.

```mermaid
sequenceDiagram
participant C as "Component"
participant API as "Monitoring API"
participant RC as "Recharts"
participant DOM as "DOM"
C->>API : Fetch performance stats
API-->>C : Stats payload
C->>C : Transform with prepareData()
C->>RC : Render PieCharts (IN/OUT)
RC-->>DOM : Rendered charts
C->>API : Fetch monthly stats
API-->>C : Monthly data
C->>RC : Render BarChart (ResponsiveContainer)
RC-->>DOM : Rendered chart
C->>API : Fetch instances by status
API-->>C : Instances list
C->>DOM : Render drill-down table with pagination
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:28-63](file://src/components/StatistiquesPortabilite.jsx#L28-L63)
- [StatistiquesPortabilite.jsx:182-230](file://src/components/StatistiquesPortabilite.jsx#L182-L230)

## Detailed Component Analysis

### PieChart Implementation (IN/OUT)
The component renders two PieCharts side-by-side, each representing status distribution for IN and OUT processes.

```mermaid
classDiagram
class StatistiquesPortabilite {
+useState stats
+useState monthlyStats
+useState drilldown
+useState drillInstances
+prepareData(obj) Array
+handleCellClick(statusKey, type) void
+render() JSX.Element
}
class PieChart_IN {
+Pie data IN
+Cell colors COLORS_PIE
+Tooltip enabled
+Legend enabled
}
class PieChart_OUT {
+Pie data OUT
+Cell colors COLORS_PIE
+Tooltip enabled
+Legend enabled
}
StatistiquesPortabilite --> PieChart_IN : "renders"
StatistiquesPortabilite --> PieChart_OUT : "renders"
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:182-213](file://src/components/StatistiquesPortabilite.jsx#L182-L213)
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)

Implementation details:
- Chart dimensions: width 300px, height 250px
- Outer radius: 80px
- Color mapping: ['#00C49F', '#FF8042', '#0088FE']
- Data prepared via prepareData() function
- Interactive elements: Tooltip and Legend enabled
- Click handling: handleCellClick triggers drill-down filtering

**Section sources**
- [StatistiquesPortabilite.jsx:182-213](file://src/components/StatistiquesPortabilite.jsx#L182-L213)
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)
- [StatistiquesPortabilite.jsx:65-77](file://src/components/StatistiquesPortabilite.jsx#L65-L77)

### BarChart Implementation (Monthly Trends)
The monthly BarChart displays trends across months for both IN and OUT processes.

```mermaid
flowchart TD
Start([Render BarChart]) --> RC["ResponsiveContainer<br/>width: 100%, height: 350px"]
RC --> BC["BarChart"]
BC --> CG["CartesianGrid"]
BC --> XAXIS["XAxis dataKey: 'month'"]
BC --> YAXIS["YAxis"]
BC --> TOOLTIP["Tooltip"]
BC --> LEGEND["Legend"]
BC --> BAR_IN["Bar dataKey: 'PortabilityIN'"]
BC --> BAR_OUT["Bar dataKey: 'PortabilityOUT'"]
BAR_IN --> End([Rendered])
BAR_OUT --> End
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:215-228](file://src/components/StatistiquesPortabilite.jsx#L215-L228)

Key characteristics:
- Responsive container ensures adaptivity across screen sizes
- Dual-series bars for IN and OUT comparisons
- Grid lines for readability
- Axis labels and legends for clarity

**Section sources**
- [StatistiquesPortabilite.jsx:215-228](file://src/components/StatistiquesPortabilite.jsx#L215-L228)

### Data Transformation and Status Mapping
The prepareData() function transforms raw statistics into Recharts-compatible arrays.

```mermaid
flowchart TD
Input["Raw Stats Object"] --> Prepare["prepareData(obj)"]
Prepare --> CheckCompleted["Check obj.completed"]
Prepare --> CheckAborted["Check obj.aborted"]
Prepare --> CheckActive["Check obj.active"]
CheckCompleted --> Output1["{ name: 'Completed', value: Number }"]
CheckAborted --> Output2["{ name: 'Aborted', value: Number }"]
CheckActive --> Output3["{ name: 'Active', value: Number }"]
Output1 --> Array["Return Array"]
Output2 --> Array
Output3 --> Array
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)

Status mapping constants:
- active → 1
- completed → 2
- aborted → 3

These mappings enable drill-down filtering and consistent status representation across the UI.

**Section sources**
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)
- [StatistiquesPortabilite.jsx:8-10](file://src/components/StatistiquesPortabilite.jsx#L8-L10)

### Drill-Down Interactivity Pattern
The component implements a drill-down pattern that filters instances by status and type.

```mermaid
sequenceDiagram
participant User as "User"
participant Comp as "StatistiquesPortabilite"
participant API as "Monitoring API"
User->>Comp : Click PieChart segment
Comp->>Comp : handleCellClick(statusKey, type)
Comp->>Comp : Build status code from STATUS_CODE
Comp->>API : Fetch instances by status/type
API-->>Comp : Instances array
Comp->>Comp : Set drillInstances state
Comp->>User : Render drill-down table
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:65-77](file://src/components/StatistiquesPortabilite.jsx#L65-L77)

Behavior:
- Translates status keys to numeric codes
- Constructs API endpoint with status and type parameters
- Loads up to 50 instances for immediate display
- Supports subsequent pagination for larger datasets

**Section sources**
- [StatistiquesPortabilite.jsx:65-77](file://src/components/StatistiquesPortabilite.jsx#L65-L77)

### Responsive Design and Layout
The component employs a responsive layout that adapts to different screen sizes.

```mermaid
graph TB
SL["stats-layout<br/>display: flex, gap: 20px"]
PS["pie-section<br/>display: flex, row, gap: 20px"]
CB1["chart-box IN<br/>min-width: 320px"]
CB2["chart-box OUT<br/>min-width: 320px"]
MCB["monthly-chart-box<br/>flex: 1, min-width: 550px"]
SL --> PS
SL --> MCB
PS --> CB1
PS --> CB2
subgraph "Mobile Breakpoint"
MQ["@media (max-width: 1300px)"]
FL["flex-direction: column"]
MW["width: 100%"]
end
SL -.-> MQ
MQ --> FL
MQ --> MW
```

**Diagram sources**
- [StatistiquesPortabilite.jsx:182-230](file://src/components/StatistiquesPortabilite.jsx#L182-L230)
- [Statistique.css:608-622](file://src/components/Statistique.css#L608-L622)

Layout features:
- Flexbox-based responsive grid
- Minimum width constraints for optimal readability
- Mobile-first breakpoint at 1300px
- Equal-height alignment for pie charts

**Section sources**
- [StatistiquesPortabilite.jsx:182-230](file://src/components/StatistiquesPortabilite.jsx#L182-L230)
- [Statistique.css:608-622](file://src/components/Statistique.css#L608-L622)

### Styling Approach and Customization
The component uses a layered CSS approach combining base styles with chart-specific overrides.

```mermaid
classDiagram
class BaseStyles {
+stats-panel
+stats-table
+performance-cards
}
class ChartLayout {
+stats-layout
+pie-section
+charts-container
+chart-box
+monthly-chart-box
}
class InteractiveElements {
+cell-clickable
+status-ok
+status-notok
+pagination-btn
}
BaseStyles --> ChartLayout : "extends"
ChartLayout --> InteractiveElements : "adds"
```

**Diagram sources**
- [Statistique.css:1-698](file://src/components/Statistique.css#L1-L698)

Styling patterns:
- Consistent card-based design with shadows and rounded corners
- Color-coded status indicators
- Hover effects for interactive elements
- Mobile-responsive breakpoints
- Typography hierarchy for chart titles and labels

**Section sources**
- [Statistique.css:1-698](file://src/components/Statistique.css#L1-L698)

## Dependency Analysis
The visualization component relies on Recharts for rendering and depends on external monitoring APIs for data.

```mermaid
graph LR
subgraph "Visualization Layer"
RC["Recharts v3.8.1"]
SP["StatistiquesPortabilite.jsx"]
end
subgraph "Data Layer"
PERF["statistics/performance"]
MONTHLY["statistics/monthly"]
BYSTATUS["instances/by-status"]
end
subgraph "External Dependencies"
AXIOS["axios"]
REACT["react"]
end
SP --> RC
SP --> PERF
SP --> MONTHLY
SP --> BYSTATUS
SP --> AXIOS
RC --> REACT
```

**Diagram sources**
- [package.json:20](file://package.json#L20)
- [StatistiquesPortabilite.jsx:28-63](file://src/components/StatistiquesPortabilite.jsx#L28-L63)

Key dependencies:
- Recharts: ^3.8.1 for chart rendering
- React: for component lifecycle and state management
- Axios: for API communication
- CSS modules: for styling and responsive behavior

**Section sources**
- [package.json:20](file://package.json#L20)
- [StatistiquesPortabilite.jsx:28-63](file://src/components/StatistiquesPortabilite.jsx#L28-L63)

## Performance Considerations
- Data fetching: Uses Promise.all for concurrent API requests to minimize load time
- Memory management: Limits drill-down instance count to 50 per request
- Rendering optimization: Recharts handles efficient SVG updates automatically
- Responsive performance: CSS flexbox provides lightweight layout calculations
- Network efficiency: Single request per chart type with minimal payload processing

## Troubleshooting Guide
Common issues and solutions:

### Chart Rendering Problems
- Verify Recharts installation: Ensure recharts ^3.8.1 is installed
- Check data shape: prepareData() expects objects with completed, aborted, active properties
- Validate API responses: Confirm monitoring endpoints return expected JSON structures

### Responsive Issues
- Media query conflicts: Review @media (max-width: 1300px) rules
- Container sizing: Ensure parent containers have defined widths
- Flexbox fallbacks: Verify CSS fallbacks for older browsers

### Interactivity Problems
- Click handlers: Verify handleCellClick receives valid statusKey and type parameters
- State updates: Confirm drilldown state transitions trigger re-renders
- API connectivity: Test monitoring service availability and CORS configuration

**Section sources**
- [StatistiquesPortabilite.jsx:81-85](file://src/components/StatistiquesPortabilite.jsx#L81-L85)
- [StatistiquesPortabilite.jsx:65-77](file://src/components/StatistiquesPortabilite.jsx#L65-L77)
- [Statistique.css:608-622](file://src/components/Statistique.css#L608-L622)

## Conclusion
The SmartPorta statistical reporting system demonstrates robust data visualization implementation using Recharts. The component architecture effectively separates concerns between data fetching, transformation, and rendering while maintaining responsive design and interactive capabilities. The modular approach enables easy customization of colors, layouts, and data sources, making it adaptable to evolving monitoring requirements.