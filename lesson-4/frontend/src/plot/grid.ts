import type { ContourData, FunctionDefinition, SurfaceData } from '../types';

export type PlotBounds = Pick<
  FunctionDefinition,
  'plotXMin' | 'plotXMax' | 'plotYMin' | 'plotYMax'
>;

function finiteNum(...candidates: unknown[]): number | undefined {
  for (const c of candidates) {
    if (typeof c === 'number' && Number.isFinite(c)) return c;
    if (typeof c === 'string' && c.trim() !== '') {
      const n = Number(c);
      if (Number.isFinite(n)) return n;
    }
  }
  return undefined;
}

function readNum(obj: Record<string, unknown>, ...keys: string[]): number | undefined {
  for (const key of keys) {
    const v = obj[key];
    const n = finiteNum(v);
    if (n != null) return n;
  }
  return undefined;
}

function readNumArray(obj: Record<string, unknown>, ...keys: string[]): number[] | undefined {
  for (const key of keys) {
    const v = obj[key];
    if (!Array.isArray(v)) continue;
    const nums = v
      .map((item) => (typeof item === 'number' ? item : Number(item)))
      .filter((n) => Number.isFinite(n));
    if (nums.length > 0) return nums;
  }
  return undefined;
}

function linspace(min: number, max: number, count: number): number[] {
  if (count <= 1) return [min];
  const step = (max - min) / (count - 1);
  return Array.from({ length: count }, (_, i) => min + i * step);
}

/** Распаковка ответа API / WebSocket (в т.ч. вложенный `{ type, data }`). */
export function coerceContourPayload(raw: unknown): Record<string, unknown> {
  if (raw == null || typeof raw !== 'object') return {};
  const o = raw as Record<string, unknown>;
  if (o.type === 'CONTOUR' && o.data != null) return coerceContourPayload(o.data);
  if (typeof o.data === 'string') {
    try {
      return coerceContourPayload(JSON.parse(o.data));
    } catch {
      return o;
    }
  }
  return o;
}

/** Границы контура: из полей ответа API, координат сетки или plotXMin из варианта. */
export function getContourBounds(contour: ContourData, plot?: PlotBounds): {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
} {
  let xMin = finiteNum(contour.xMin);
  let xMax = finiteNum(contour.xMax);
  let yMin = finiteNum(contour.yMin);
  let yMax = finiteNum(contour.yMax);

  if (contour.xCoords?.length) {
    const xs = contour.xCoords.filter((v) => Number.isFinite(v));
    if (xs.length) {
      if (xMin == null) xMin = Math.min(...xs);
      if (xMax == null) xMax = Math.max(...xs);
    }
  }
  if (contour.yCoords?.length) {
    const ys = contour.yCoords.filter((v) => Number.isFinite(v));
    if (ys.length) {
      if (yMin == null) yMin = Math.min(...ys);
      if (yMax == null) yMax = Math.max(...ys);
    }
  }

  if (plot) {
    if (xMin == null) xMin = plot.plotXMin;
    if (xMax == null) xMax = plot.plotXMax;
    if (yMin == null) yMin = plot.plotYMin;
    if (yMax == null) yMax = plot.plotYMax;
  }

  if (xMin == null || xMax == null || yMin == null || yMax == null) {
    throw new Error('Некорректные данные контура: не заданы границы области');
  }

  return { xMin, xMax, yMin, yMax };
}

export function normalizeContourData(raw: unknown, plot?: PlotBounds): ContourData {
  const src = coerceContourPayload(raw);

  const gridSize =
    readNum(src, 'gridSize', 'grid_size') ??
    readNumArray(src, 'xCoords', 'x_coords', 'xcoords')?.length ??
    readNumArray(src, 'yCoords', 'y_coords', 'ycoords')?.length ??
    60;

  let xMin = readNum(src, 'xMin', 'x_min', 'xmin');
  let xMax = readNum(src, 'xMax', 'x_max', 'xmax');
  let yMin = readNum(src, 'yMin', 'y_min', 'ymin');
  let yMax = readNum(src, 'yMax', 'y_max', 'ymax');

  let xCoords = readNumArray(src, 'xCoords', 'x_coords', 'xcoords');
  let yCoords = readNumArray(src, 'yCoords', 'y_coords', 'ycoords');

  if (xCoords?.length) {
    if (xMin == null) xMin = Math.min(...xCoords);
    if (xMax == null) xMax = Math.max(...xCoords);
  }
  if (yCoords?.length) {
    if (yMin == null) yMin = Math.min(...yCoords);
    if (yMax == null) yMax = Math.max(...yCoords);
  }

  if (plot) {
    if (xMin == null) xMin = plot.plotXMin;
    if (xMax == null) xMax = plot.plotXMax;
    if (yMin == null) yMin = plot.plotYMin;
    if (yMax == null) yMax = plot.plotYMax;
  }

  if (xMin == null || xMax == null || yMin == null || yMax == null) {
    throw new Error('Некорректные данные контура: не заданы границы области');
  }

  if (!xCoords?.length) xCoords = linspace(xMin, xMax, gridSize);
  if (!yCoords?.length) yCoords = linspace(yMin, yMax, gridSize);

  const values = (src.values ?? src.Values) as (number | null)[] | undefined;
  const levels = (src.levels ?? src.Levels) as number[] | undefined;

  const draft: ContourData = {
    xMin,
    xMax,
    yMin,
    yMax,
    gridSize,
    values: Array.isArray(values) ? values : [],
    levels: Array.isArray(levels) ? levels : [],
    xCoords,
    yCoords,
    zMin: readNum(src, 'zMin', 'z_min', 'zmin') ?? 0,
    zMax: readNum(src, 'zMax', 'z_max', 'zmax') ?? 1,
  };

  return draft;
}

/**
 * Сетка z[yIndex][xIndex] для Plotly (строки — y, столбцы — x).
 * @param flatOrder — порядок плоского массива с бэкенда:
 *   `xMajor` — контур (цикл i по x, j по y): index = i * gridSize + j;
 *   `yMajor` — поверхность (цикл j по y, i по x): index = j * gridSize + i.
 */
export function valuesToPlotlyZ(
  values: (number | null)[],
  gridSize: number,
  flatOrder: 'xMajor' | 'yMajor' = 'yMajor',
): number[][] {
  const z: number[][] = [];
  for (let j = 0; j < gridSize; j++) {
    const row: number[] = [];
    for (let i = 0; i < gridSize; i++) {
      const idx = flatOrder === 'xMajor' ? i * gridSize + j : j * gridSize + i;
      const v = values[idx];
      row.push(v == null || !Number.isFinite(v) ? NaN : v);
    }
    z.push(row);
  }
  return z;
}

export function pathAxis(path: { x: number[] }[], index: number): number[] {
  return path.map((p) => p.x[index]).filter((v) => Number.isFinite(v));
}

export function extendRange(
  dataMin: number,
  dataMax: number,
  points: number[],
  marginRatio = 0.1,
): [number, number] {
  let lo = dataMin;
  let hi = dataMax;
  for (const p of points) {
    if (Number.isFinite(p)) {
      lo = Math.min(lo, p);
      hi = Math.max(hi, p);
    }
  }
  const pad = Math.max((hi - lo) * marginRatio, 0.5);
  return [lo - pad, hi + pad];
}

/** Фиксированная область построения линий уровня (не растягивать оси из-за расходящейся траектории). */
export function contourPlotRange(
  min: number,
  max: number,
  marginRatio = 0.08,
): [number, number] {
  const pad = Math.max((max - min) * marginRatio, 0.35);
  return [min - pad, max + pad];
}

/** Квадратная область осей (1:1) вокруг прямоугольника контура — без чёрных полей от scaleanchor. */
export function squarePlotRange(
  xMin: number,
  xMax: number,
  yMin: number,
  yMax: number,
  marginRatio = 0.06,
): { xRange: [number, number]; yRange: [number, number] } {
  const xMid = (xMin + xMax) / 2;
  const yMid = (yMin + yMax) / 2;
  const half = (Math.max(xMax - xMin, yMax - yMin) / 2) * (1 + marginRatio);
  return {
    xRange: [xMid - half, xMid + half],
    yRange: [yMid - half, yMid + half],
  };
}

export function path2dRanges(path: { x: number[] }[]): {
  xRange: [number, number];
  yRange: [number, number];
} | null {
  const xs = pathAxis(path, 0);
  const ys = pathAxis(path, 1);
  if (xs.length === 0 || ys.length === 0) return null;
  return {
    xRange: extendRange(Math.min(...xs), Math.max(...xs), xs, 0.06),
    yRange: extendRange(Math.min(...ys), Math.max(...ys), ys, 0.06),
  };
}

export function coerceSurfacePayload(raw: unknown): Record<string, unknown> {
  if (raw == null || typeof raw !== 'object') return {};
  const o = raw as Record<string, unknown>;
  if (o.type === 'SURFACE' && o.data != null) return coerceSurfacePayload(o.data);
  return o;
}

export function normalizeSurfaceData(raw: unknown, plot?: PlotBounds): SurfaceData {
  const src = coerceSurfacePayload(raw);

  const gridSize =
    readNum(src, 'gridSize', 'grid_size') ??
    readNumArray(src, 'xCoords', 'x_coords', 'xcoords')?.length ??
    50;

  let xMin = readNum(src, 'xMin', 'x_min', 'xmin');
  let xMax = readNum(src, 'xMax', 'x_max', 'xmax');
  let yMin = readNum(src, 'yMin', 'y_min', 'ymin');
  let yMax = readNum(src, 'yMax', 'y_max', 'ymax');

  let xCoords = readNumArray(src, 'xCoords', 'x_coords', 'xcoords');
  let yCoords = readNumArray(src, 'yCoords', 'y_coords', 'ycoords');

  if (xCoords?.length) {
    if (xMin == null) xMin = Math.min(...xCoords);
    if (xMax == null) xMax = Math.max(...xCoords);
  }
  if (yCoords?.length) {
    if (yMin == null) yMin = Math.min(...yCoords);
    if (yMax == null) yMax = Math.max(...yCoords);
  }

  const plotOk =
    plot != null && plot.plotXMax > plot.plotXMin && plot.plotYMax > plot.plotYMin;
  if (plotOk) {
    if (xMin == null) xMin = plot.plotXMin;
    if (xMax == null) xMax = plot.plotXMax;
    if (yMin == null) yMin = plot.plotYMin;
    if (yMax == null) yMax = plot.plotYMax;
  }

  if (xMin == null || xMax == null || yMin == null || yMax == null) {
    throw new Error('Некорректные данные поверхности: не заданы границы');
  }

  if (!xCoords?.length) xCoords = linspace(xMin, xMax, gridSize);
  if (!yCoords?.length) yCoords = linspace(yMin, yMax, gridSize);

  const values = (src.values ?? src.Values) as (number | null)[] | undefined;
  const x3Slice = readNum(src, 'x3Slice', 'x3_slice', 'x3slice') ?? 0;

  return {
    xMin,
    xMax,
    yMin,
    yMax,
    x3Slice,
    gridSize,
    xCoords,
    yCoords,
    values: Array.isArray(values) ? values : [],
    zMin: readNum(src, 'zMin', 'z_min', 'zmin') ?? 0,
    zMax: readNum(src, 'zMax', 'z_max', 'zmax') ?? 1,
  };
}

/** Равномерная прореживание длинной траектории для второго графика. */
export function surfaceAxisBounds(surface: SurfaceData): {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
  zMin: number;
  zMax: number;
} {
  return {
    xMin: surface.xMin,
    xMax: surface.xMax,
    yMin: surface.yMin,
    yMax: surface.yMax,
    zMin: finiteNum(surface.zMin) ?? 0,
    zMax: finiteNum(surface.zMax) ?? 1,
  };
}

/** Диапазоны осей 3D: x₁, x₂ и F(x) — поверхность + траектория в одной системе координат. */
export function scene3dRanges(
  surface: SurfaceData,
  path: { x: number[]; f: number }[],
): {
  xRange: [number, number];
  yRange: [number, number];
  zRange: [number, number];
  aspectratio: { x: number; y: number; z: number };
} {
  const b = surfaceAxisBounds(surface);
  let xLo = b.xMin;
  let xHi = b.xMax;
  let yLo = b.yMin;
  let yHi = b.yMax;
  let zLo = b.zMin;
  let zHi = b.zMax;

  for (const p of path) {
    if (p.x.length < 2) continue;
    const px = p.x[0];
    const py = p.x[1];
    if (Number.isFinite(px)) {
      xLo = Math.min(xLo, px);
      xHi = Math.max(xHi, px);
    }
    if (Number.isFinite(py)) {
      yLo = Math.min(yLo, py);
      yHi = Math.max(yHi, py);
    }
    if (Number.isFinite(p.f)) {
      zLo = Math.min(zLo, p.f);
      zHi = Math.max(zHi, p.f);
    }
  }

  const xRange = extendRange(xLo, xHi, [], 0.12);
  const yRange = extendRange(yLo, yHi, [], 0.12);
  const zRange = extendRange(zLo, zHi, [], 0.15);

  const sx = xRange[1] - xRange[0];
  const sy = yRange[1] - yRange[0];
  const sz = zRange[1] - zRange[0];
  const m = Math.max(sx, sy, sz, 1e-6);

  return {
    xRange,
    yRange,
    zRange,
    aspectratio: { x: sx / m, y: sy / m, z: sz / m },
  };
}

export function subsamplePath<T>(items: T[], maxPoints: number): T[] {
  if (items.length <= maxPoints) return items;
  const step = (items.length - 1) / (maxPoints - 1);
  const out: T[] = [];
  for (let i = 0; i < maxPoints; i++) {
    out.push(items[Math.round(i * step)]);
  }
  return out;
}

export function isInPlotDomain(
  x: number,
  y: number,
  xMin: number,
  xMax: number,
  yMin: number,
  yMax: number,
  marginRatio = 0.02,
): boolean {
  const mx = (xMax - xMin) * marginRatio;
  const my = (yMax - yMin) * marginRatio;
  return x >= xMin - mx && x <= xMax + mx && y >= yMin - my && y <= yMax + my;
}

/** Траектория с разрывами (null), если точка вне области — линия не тянется через весь график. */
export function pathToPlotlySegments(
  path: { x: number[]; f: number }[],
  xMin: number,
  xMax: number,
  yMin: number,
  yMax: number,
): {
  x: (number | null)[];
  y: (number | null)[];
  f: (number | null)[];
  insideCount: number;
  outsideCount: number;
} {
  const x: (number | null)[] = [];
  const y: (number | null)[] = [];
  const f: (number | null)[] = [];
  let insideCount = 0;
  let outsideCount = 0;

  for (const p of path) {
    if (p.x.length < 2) continue;
    const px = p.x[0];
    const py = p.x[1];
    if (!Number.isFinite(px) || !Number.isFinite(py)) continue;

    const inside = isInPlotDomain(px, py, xMin, xMax, yMin, yMax);
    if (inside) {
      insideCount++;
      x.push(px);
      y.push(py);
      f.push(p.f);
    } else {
      outsideCount++;
      if (x.length > 0 && x[x.length - 1] !== null) {
        x.push(null);
        y.push(null);
        f.push(null);
      }
    }
  }

  return { x, y, f, insideCount, outsideCount };
}
