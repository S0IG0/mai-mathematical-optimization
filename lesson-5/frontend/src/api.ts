import type { ContourData, SolveRequest, SolveResult, Variant } from './types';

const BASE = '/api/penalty-barrier';

export async function fetchVariants(): Promise<Variant[]> {
  const res = await fetch(`${BASE}/variants`);
  if (!res.ok) throw new Error('Не удалось загрузить варианты');
  return res.json();
}

export async function solve(request: SolveRequest): Promise<SolveResult> {
  const res = await fetch(`${BASE}/solve`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Ошибка расчёта');
  }
  return res.json();
}

export async function fetchContour(
  variantId: number,
  params?: { gridSize?: number; xMin?: number; xMax?: number; yMin?: number; yMax?: number },
): Promise<ContourData> {
  const q = new URLSearchParams();
  if (params?.gridSize) q.set('gridSize', String(params.gridSize));
  if (params?.xMin != null) q.set('xMin', String(params.xMin));
  if (params?.xMax != null) q.set('xMax', String(params.xMax));
  if (params?.yMin != null) q.set('yMin', String(params.yMin));
  if (params?.yMax != null) q.set('yMax', String(params.yMax));
  const qs = q.toString();
  const res = await fetch(`${BASE}/contour/${variantId}${qs ? `?${qs}` : ''}`);
  if (!res.ok) throw new Error('Не удалось загрузить контур');
  return res.json();
}
