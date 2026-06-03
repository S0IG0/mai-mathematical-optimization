import type { OptimizationParams, OptimizationResult, PlotData, Variant } from './types';

async function parseError(response: Response): Promise<string> {
  const err = await response.json().catch(() => ({ message: response.statusText }));
  return err.message ?? 'Неизвестная ошибка';
}

export async function fetchVariants(): Promise<Variant[]> {
  const response = await fetch('/api/variants');
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function runOptimization(params: OptimizationParams): Promise<OptimizationResult> {
  const response = await fetch('/api/optimize', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function runAllMethods(params: OptimizationParams): Promise<OptimizationResult[]> {
  const response = await fetch('/api/optimize/all', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function fetchPlotData(
  variantId: number,
  functionId: string,
  from: number,
  to: number,
): Promise<PlotData> {
  const query = new URLSearchParams({
    variantId: String(variantId),
    functionId,
    from: String(from),
    to: String(to),
    points: '500',
  });
  const response = await fetch(`/api/plot?${query}`);
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}
