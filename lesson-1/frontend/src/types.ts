export type OptimizationMethod = 'DICHOTOMY' | 'GOLDEN_SECTION' | 'FIBONACCI';

export interface IntervalPreset {
  label: string;
  a: number;
  b: number;
  minimize: boolean;
}

export interface FunctionDefinition {
  id: string;
  label: string;
  formula: string;
  defaultMinimize: boolean;
  domainFrom: number;
  domainTo: number;
  presets: IntervalPreset[];
}

export interface Variant {
  id: number;
  title: string;
  f1: FunctionDefinition;
  f2: FunctionDefinition;
}

export interface OptimizationParams {
  variantId: number;
  functionId: 'F1' | 'F2';
  method: OptimizationMethod;
  a: number;
  b: number;
  epsilon: number;
  l: number;
  minimize: boolean;
}

export interface IterationStep {
  k: number;
  a: number;
  b: number;
  lambda: number | null;
  mu: number | null;
  fLambda: number | null;
  fMu: number | null;
}

export interface OptimizationResult {
  method: OptimizationMethod;
  methodLabel: string;
  minimize: boolean;
  initialA: number;
  initialB: number;
  finalA: number;
  finalB: number;
  optimalX: number;
  optimalF: number;
  functionEvaluations: number;
  iterationsCount: number;
  iterations: IterationStep[];
}

export interface PlotPoint {
  x: number;
  y: number | null;
}

export interface PlotData {
  points: PlotPoint[];
  plotFrom: number;
  plotTo: number;
}
