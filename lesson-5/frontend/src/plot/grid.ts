import type { ContourData } from '../types';

const PLOT_HEIGHT = 480;

export { PLOT_HEIGHT };

export function normalizeContourData(raw: unknown): ContourData {
  if (raw == null || typeof raw !== 'object') {
    throw new Error('Пустой ответ контура');
  }
  const o = raw as Record<string, unknown>;
  const gridSize = Number(o.gridSize) || 60;
  const values = (o.values ?? o.Values) as (number | null)[] | undefined;
  const xCoords = (o.xCoords ?? o.xcoords) as number[] | undefined;
  const yCoords = (o.yCoords ?? o.ycoords) as number[] | undefined;

  return {
    xMin: Number(o.xMin ?? o.xmin ?? 0),
    xMax: Number(o.xMax ?? o.xmax ?? 1),
    yMin: Number(o.yMin ?? o.ymin ?? 0),
    yMax: Number(o.yMax ?? o.ymax ?? 1),
    gridSize,
    zMin: Number(o.zMin ?? o.zmin ?? 0),
    zMax: Number(o.zMax ?? o.zmax ?? 1),
    xCoords: Array.isArray(xCoords) ? xCoords : [],
    yCoords: Array.isArray(yCoords) ? yCoords : [],
    values: Array.isArray(values) ? values : [],
    levels: Array.isArray(o.levels) ? (o.levels as number[]) : [],
    constraints: Array.isArray(o.constraints) ? (o.constraints as ContourData['constraints']) : [],
  };
}

/** z[row][col] = F(x[col], y[row]); backend: index = i * gridSize + j (xMajor). */
export function valuesToPlotlyZ(
  values: (number | null)[],
  gridSize: number,
  flatOrder: 'xMajor' | 'yMajor' = 'xMajor',
): number[][] {
  const z: number[][] = [];
  for (let j = 0; j < gridSize; j++) {
    const row: number[] = [];
    for (let i = 0; i < gridSize; i++) {
      const idx = flatOrder === 'xMajor' ? i * gridSize + j : j * gridSize + i;
      const v = values[idx];
      row.push(v == null || !Number.isFinite(Number(v)) ? NaN : Number(v));
    }
    z.push(row);
  }
  return z;
}

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

export function pathToPlotlySegments(
  path: { x: number[] }[],
  xMin: number,
  xMax: number,
  yMin: number,
  yMax: number,
): {
  x: (number | null)[];
  y: (number | null)[];
  insideCount: number;
} {
  const x: (number | null)[] = [];
  const y: (number | null)[] = [];
  let insideCount = 0;
  const mx = (xMax - xMin) * 0.15;
  const my = (yMax - yMin) * 0.15;

  for (const p of path) {
    if (p.x.length < 2) continue;
    const px = p.x[0];
    const py = p.x[1];
    if (!Number.isFinite(px) || !Number.isFinite(py)) continue;
    const inside =
      px >= xMin - mx && px <= xMax + mx && py >= yMin - my && py <= yMax + my;
    if (inside) {
      insideCount++;
      x.push(px);
      y.push(py);
    } else if (x.length > 0 && x[x.length - 1] !== null) {
      x.push(null);
      y.push(null);
    }
  }
  return { x, y, insideCount };
}
