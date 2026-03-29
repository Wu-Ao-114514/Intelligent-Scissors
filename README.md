# Intelligent Scissors

A Java Swing implementation of **Live-Wire (Intelligent Scissors)** for semi-automatic image segmentation.

This project lets users extract object regions by placing sparse seed points while the system computes edge-aligned shortest paths in real time.

## Highlights

- Real-time live-wire path preview while moving the cursor.
- Edge-aware segmentation using Sobel gradient + Dijkstra shortest path.
- Cursor snapping to stronger local edges for improved contour adherence.
- Automatic virtual seed insertion to reduce repetitive clicks.
- Region extraction to transparent PNG with save dialog support.
- Right-click immediate stop flow with optional save-before-exit.
- Java 8 compatibility.

## Demo Workflow

1. Launch the app and choose an image.
2. Click once to place the first seed.
3. Move the mouse to preview the optimal boundary path.
4. Click to confirm additional contour segments.
5. Click near the first seed to close the contour.
6. Preview opens in a new window and prompts to save as PNG.

## Core Algorithms

### 1) Sobel Gradient Estimation

For each pixel, the project computes:

$$
G = \sqrt{G_x^2 + G_y^2}
$$

where $G_x$ and $G_y$ are obtained from 3x3 Sobel kernels. The resulting gradient map represents edge strength and drives path costs.

### 2) Edge-Weighted Dijkstra

Pixels are modeled as graph vertices with 8-neighborhood connectivity. Path search runs from the latest seed using a priority queue.

Cost is inversely correlated with local gradient, so lower-cost paths naturally follow strong boundaries.

Approximate complexity per run:

$$
O(E \log V)
$$

with $V = W \times H$ and $E \approx 8V$.

### 3) Practical Cost Model Used in This Project

The implementation follows the report's edge-driven weighting strategy:

$$
	ext{weight}(p, q) = \frac{1}{1 + \frac{G(p)+G(q)}{2}}
$$

where $G(\cdot)$ is Sobel gradient magnitude. For diagonal moves, an additional Euclidean correction is applied:

$$
	ext{cost} = \text{weight} \cdot \sqrt{2}
$$

for diagonal neighbors, and `cost = weight` for axial neighbors.

This design encourages paths to pass through high-gradient pixels, which are more likely to be perceptual boundaries.

### 4) Shortest-Path State Representation

As documented in the report and implemented in code:

- `dist[x][y]`: current best distance from active seed.
- `parentX[x][y]`, `parentY[x][y]`: predecessor pointers for path reconstruction.
- `PriorityQueue<Node>`: min-priority frontier for Dijkstra expansion.
- `dirs`: 8-neighborhood offset table.

## Architecture

```text
src/project
|- IntelligentScissors.java    # app entry, UI bootstrap, image selection
|- ImagePanel.java             # interaction engine, Dijkstra, rendering, extraction
|- GradientCalculator.java     # Sobel gradient computation
|- PROJECT_ANALYSIS.md         # detailed technical analysis
```

## Build and Run

From repository root:

```bash
cd src
javac -encoding UTF-8 -source 8 -target 8 project/*.java
java project.IntelligentScissors
```

## Controls

- Left click: place a seed point / confirm current segment.
- Move mouse: update live-wire segment.
- Click near first seed: close contour and extract region.
- Slider: adjust cursor snap radius (0 disables snapping).
- Right click: immediately open stop dialog.

## Advanced Interaction Features

### Cursor Snapping

During `mouseMoved`, the panel scans a local neighborhood centered at the cursor and selects the pixel with the highest gradient as the effective target point.

- User-adjustable range: `snapRadius` in `[0, 10]`.
- `0` means no snapping.
- Larger values increase edge attraction robustness but may slightly reduce fine-grained manual precision.

### Boundary Freezing (Virtual Seeds)

To reduce repetitive clicking, the project includes a virtual path stabilization mechanism:

- Update interval: `INTERVAL = 200 ms`.
- Overlap trigger: `overlapThreshold = 40` pixels.
- Minimum path spacing: `minDistance = 100`.

If a newly sampled virtual path overlaps sufficiently with a prior one, the system promotes a stable overlap endpoint to a real seed, effectively freezing part of the contour.

This behavior aligns with the report's objective of improving segmentation continuity and reducing user effort.

## Right-Click Stop Behavior

When right-clicking in the image panel, the application opens a stop confirmation dialog:

1. `Yes`: save extracted result (if available), then exit.
2. `No`: exit immediately without saving.
3. `Cancel`: keep the application running.

If no extracted region is available yet, selecting `Yes` shows a secondary confirmation before exiting.

## Output Behavior

After contour closure:

1. The extracted region is displayed in a separate window.
2. A save prompt appears.
3. If confirmed, the result is saved as PNG (extension auto-appended when missing).

## Compatibility

- JDK: 1.8+
- Encoding: UTF-8 recommended during compilation.
- OS: tested in Windows development environment.

## Known Limitations

- Full-image Dijkstra recomputation can be expensive on large images.
- Memory usage grows with image resolution due to distance and parent grids.
- Path computation currently runs on the UI thread.
- Current extraction is designed for interactive foreground selection and does not include post-processing (e.g., alpha matting refinement).

## Roadmap

- Move heavy shortest-path computation to a background worker thread.
- Add undo/reset contour operations.
- Add export presets (PNG with alpha, mask-only output).
- Introduce Maven or Gradle structure and automated tests.

## Contributing

Contributions are welcome. Suggested workflow:

1. Fork the repository.
2. Create a feature branch.
3. Keep commits focused and descriptive.
4. Submit a pull request with validation details.

## License

No license file is currently provided. Add a `LICENSE` file before public reuse or distribution.