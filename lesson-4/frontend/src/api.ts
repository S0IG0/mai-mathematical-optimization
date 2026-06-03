import { normalizeContourData } from './plot/grid';
import type { ContourData, KuhnTuckerResult, Variant } from './types';

export interface ContourBounds {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
}

async function parseError(response: Response): Promise<string> {
  const err = await response.json().catch(() => ({ message: response.statusText }));
  return err.message ?? 'Неизвестная ошибка';
}

export async function fetchVariants(): Promise<Variant[]> {
  const response = await fetch('/api/variants');
  if (!response.ok) throw new Error(await parseError(response));
  return response.json();
}

export async function solveVariant(
  variantId: number,
  includeUnconstrainedPart = false,
): Promise<KuhnTuckerResult> {
  const response = await fetch('/api/solve', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ variantId, includeUnconstrainedPart }),
  });
  if (!response.ok) throw new Error(await parseError(response));
  return response.json();
}

export async function fetchContour(
  variantId: number,
  gridSize = 60,
  bounds?: ContourBounds,
): Promise<ContourData> {
  const query = new URLSearchParams({
    variantId: String(variantId),
    gridSize: String(gridSize),
  });
  if (bounds) {
    query.set('xMin', String(bounds.xMin));
    query.set('xMax', String(bounds.xMax));
    query.set('yMin', String(bounds.yMin));
    query.set('yMax', String(bounds.yMax));
  }
  const response = await fetch(`/api/contour?${query}`);
  if (!response.ok) throw new Error(await parseError(response));
  const data = await response.json();
  const plot = {
    plotXMin: data.xMin,
    plotXMax: data.xMax,
    plotYMin: data.yMin,
    plotYMax: data.yMax,
  };
  return {
    ...normalizeContourData(data, plot),
    feasiblePolygon: data.feasiblePolygon,
    constraintBoundaries: data.constraintBoundaries,
  };
}
