# Intelligent Scissors Project Analysis

## 1. Project Overview
This project, located in `untitled/src/project`, implements a semi-automatic image segmentation tool based on the Live-Wire (Intelligent Scissors) paradigm.

Primary workflow:
1. The user selects and loads an image.
2. The first seed point is placed on the image.
3. As the mouse moves, the optimal path from the latest seed to the cursor is rendered in real time.
4. Clicking confirms the current segment and continues contour construction.
5. Clicking near the initial seed closes the contour and extracts the selected region.

## 2. Module Responsibilities

### 2.1 `IntelligentScissors`
- Application entry point.
- Handles file selection, image loading, and main window initialization.
- Creates and hosts the `ImagePanel`.

### 2.2 `GradientCalculator`
- Provides the static method `computeGradient(BufferedImage img)`.
- Computes Sobel gradient magnitude used as the cost signal for path optimization.

### 2.3 `ImagePanel`
- Interaction core: mouse handling, seed management, and path rendering.
- Algorithmic core: runs Dijkstra from the latest seed to build shortest-path trees.
- Advanced behavior: cursor snapping and virtual-seed cooling for reduced manual effort.

## 3. Core Algorithms

### 3.1 Sobel Gradient Estimation
- Applies 3x3 Sobel kernels to estimate $G_x$ and $G_y$ at each pixel.
- Uses gradient magnitude $\sqrt{G_x^2 + G_y^2}$ as edge strength.
- Time complexity is approximately $O(W \times H)$.

### 3.2 Dijkstra Shortest Path
- Treats image pixels as graph vertices with 8-neighborhood connectivity.
- Uses a priority queue implementation with complexity approximately $O(E \log V)$ per run.
- Cost is inversely related to gradient strength so paths naturally align to strong boundaries.

### 3.3 Cost Function and Relaxation Rule (Report-Aligned)

The concrete edge cost between neighboring pixels follows:

$$
	ext{weight}(p, q)=\frac{1}{1+\frac{G(p)+G(q)}{2}}
$$

Diagonal transitions apply geometric correction:

$$
	ext{cost}_{diag}=\text{weight}\cdot\sqrt{2}
$$

Relaxation is performed with:

$$
	ext{newDist}=\text{dist}[x][y]+\text{cost}
$$

If `newDist < dist[nx][ny]`, the algorithm updates:

- `dist[nx][ny]`
- `parentX[nx][ny] = x`
- `parentY[nx][ny] = y`

This yields both shortest distances and a predecessor tree for path reconstruction.

### 3.4 Core Runtime Data Structures

- `dist[x][y]`: shortest known distance from active seed.
- `parentX[x][y]`, `parentY[x][y]`: predecessor coordinates for backtracking.
- `PriorityQueue<Node>`: frontier sorted by tentative distance.
- `dirs`: 8-neighbor offsets for graph expansion.

## 4. Interaction Features
- Real-time live-wire feedback while moving the cursor.
- Snap radius to bias cursor targets toward stronger local edges.
- Automatic virtual seeds based on overlap and distance thresholds to reduce repetitive clicks.

### 4.1 Cursor Snapping Details

In each mouse-move event, the algorithm searches within `snapRadius` around the cursor and chooses the highest-gradient candidate as the path target.

- Adjustable range: `0` to `10`.
- `0` disables snapping.
- Higher values improve edge capture for noisy boundaries.

### 4.2 Path Freezing via Virtual Seeds

To reduce manual seed placement frequency, the panel periodically samples virtual paths and compares overlap:

- `INTERVAL = 200 ms`
- `overlapThreshold = 40`
- `minDistance = 100`

When overlap criteria are satisfied, a stable endpoint is promoted to a real seed, effectively freezing a reliable contour segment.

## 5. Compatibility Work Completed
To ensure stable execution under JDK 1.8, the following updates were applied:

1. Replaced lambda parameter name `_` with `event` in `IntelligentScissors`.
2. Replaced `List` calls such as `getFirst/getLast/addFirst` with Java 8 compatible index-based operations in `ImagePanel`.
3. Compiled with explicit UTF-8 encoding to avoid character-set related build failures.

## 6. Local Build and Run
Execute from `untitled/src`:

```bash
javac -encoding UTF-8 -source 8 -target 8 project/*.java
java project.IntelligentScissors
```

## 7. Recommended Improvements
1. Performance: full-image Dijkstra recomputation can be expensive for large images.
2. Memory footprint: `dist`, `parentX`, and `parentY` arrays scale with image resolution.
3. Responsiveness: offload heavy path computation to a background worker thread.
4. Project structure: migrate to Maven or Gradle and add automated test coverage.

