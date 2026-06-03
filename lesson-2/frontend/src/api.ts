import { normalizeContourData, normalizeSurfaceData, type PlotBounds } from './plot/grid';
import type { ContourData, OptimizationParams, OptimizationResult, SurfaceData, Variant } from './types';

async function parseError(response: Response): Promise<string> {
  const err = await response.json().catch(() => ({ message: response.statusText }));
  return err.message ?? 'Неизвестная ошибка';
}

export async function fetchVariants(): Promise<Variant[]> {
  const response = await fetch('/api/variants');
  if (!response.ok) throw new Error(await parseError(response));
  return response.json();
}

export async function runOptimization(params: OptimizationParams): Promise<OptimizationResult> {
  const response = await fetch('/api/optimize', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  if (!response.ok) throw new Error(await parseError(response));
  return response.json();
}

export async function compareGaussModes(params: OptimizationParams): Promise<OptimizationResult[]> {
  const response = await fetch('/api/optimize/compare-gauss', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  if (!response.ok) throw new Error(await parseError(response));
  return response.json();
}

export async function fetchSurface(
  variantId: number,
  functionId: string,
  x3Slice: number,
  plot: PlotBounds,
  gridSize = 50,
): Promise<SurfaceData> {
  const query = new URLSearchParams({
    variantId: String(variantId),
    functionId,
    x3Slice: String(x3Slice),
    gridSize: String(gridSize),
  });
  const response = await fetch(`/api/surface?${query}`);
  if (!response.ok) throw new Error(await parseError(response));
  const data = await response.json();
  return normalizeSurfaceData(data, plot);
}

export async function fetchContour(
  variantId: number,
  functionId: string,
  plot: PlotBounds,
  gridSize = 60,
): Promise<ContourData> {
  const query = new URLSearchParams({
    variantId: String(variantId),
    functionId,
    gridSize: String(gridSize),
  });
  const response = await fetch(`/api/contour?${query}`);
  if (!response.ok) throw new Error(await parseError(response));
  const data = await response.json();
  return normalizeContourData(data, plot);
}
