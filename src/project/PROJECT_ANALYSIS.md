# Intelligent Scissors 项目分析

## 1. 项目概览
该项目位于 `untitled/src/project`，实现了一个基于 Live-Wire 思想的半自动图像分割工具（智能剪刀）。

核心流程：
1. 用户选择图片并加载到界面。
2. 在图像上点击第一个种子点。
3. 鼠标移动时实时显示从最后种子点到光标位置的最优路径（红线）。
4. 点击固定路径段（青色），继续下一个点。
5. 点击起始点附近闭合轮廓，提取区域到新窗口。

## 2. 模块职责

### 2.1 `IntelligentScissors`
- 应用入口。
- 负责文件选择、图像加载、主窗口与控件（滑块）初始化。
- 创建并托管 `ImagePanel`。

### 2.2 `GradientCalculator`
- 提供静态方法 `computeGradient(BufferedImage img)`。
- 使用 Sobel 核计算图像梯度幅值矩阵，作为路径代价的基础。

### 2.3 `ImagePanel`
- 交互核心：监听鼠标、维护种子点、实时绘制路径。
- 算法核心：以最后种子点为源点执行 Dijkstra，构建最短路径树。
- 功能扩展：光标捕捉（snap radius）和虚拟路径冷却（自动加种子）。

## 3. 关键算法

### 3.1 Sobel 梯度
- 在每个像素邻域做 3x3 卷积，计算 $G_x, G_y$。
- 梯度幅值为：$\sqrt{G_x^2 + G_y^2}$。
- 时间复杂度近似为 $O(W \times H)$。

### 3.2 Dijkstra 最短路径
- 图像像素视作图节点，8 邻域连边。
- 采用优先队列实现，单次复杂度约为 $O(E \log V)$。
- 成本函数与梯度相关：边缘越明显，代价越低，路径更容易“贴边”。

## 4. 交互设计亮点
- 实时活线反馈：鼠标移动时动态显示路径。
- 光标捕捉：在半径内寻找更强边缘点，提升贴边效果。
- 自动种子：通过路径重叠和距离阈值自动固化路径，减少用户点击次数。

## 5. 已处理的兼容性问题
为了保证在当前 JDK 1.8 环境可运行，已完成以下修复：

1. `IntelligentScissors` 中将 lambda 参数名 `_` 修改为 `event`（避免保留标识符冲突）。
2. `ImagePanel` 中将 `List` 上的 `getFirst/getLast/addFirst` 替换为 Java 8 兼容写法（索引访问与 `add(0, ...)`）。
3. 编译时显式使用 UTF-8 编码，避免中文注释触发 GBK 编码错误。

## 6. 本地运行方式
在 `untitled/src` 目录执行：

```bash
javac -encoding UTF-8 -source 8 -target 8 project/*.java
java project.IntelligentScissors
```

## 7. 可改进方向
1. 性能优化：当前每次设种子都会全图 Dijkstra，大图下可能卡顿。
2. 内存优化：`dist/parentX/parentY` 对高分辨率图像占用较大。
3. 线程优化：可考虑将耗时路径计算放入后台线程并分离 UI 更新。
4. 工程化：建议引入 Maven/Gradle、统一入口和测试用例。
