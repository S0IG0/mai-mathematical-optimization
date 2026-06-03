export interface InitialPointPreset {
  label: string;
  coordinates: number[];
}

export interface FunctionDefinition {
  id: string;
  label: string;
  formula: string;
  dimension: number;
  plottable2d: boolean;
  plotXMin: number;
  plotXMax: number;
  plotYMin: number;
  plotYMax: number;
  initialPoints: InitialPointPreset[];
}

export interface Variant {
  id: number;
  title: string;
  method: string;
  methodLabel: string;
  supportsOneDimensional: boolean;
  f1: FunctionDefinition;
  f2: FunctionDefinition;
}

export interface OptimizationParams {
  variantId: number;
  functionId: 'F1' | 'F2';
  x0: number[];
  epsilon: number;
  delta: number;
  useOneDimensional: boolean;
  minimize: boolean;
}

export interface SubStep {
  j: number;
  dj: number[] | null;
  yj: number[] | null;
  fYj: number | null;
  deltaJ: number | null;
  lambdaJ: number | null;
  yjPlus: number[] | null;
  fYjPlus: number | null;
  yjMinus: number[] | null;
  fYjMinus: number | null;
}

export interface Iteration {
  k: number;
  xk: number[];
  fXk: number;
  subSteps: SubStep[];
}

export interface PathPoint {
  x: number[];
  f: number;
  k: number;
}

export interface OptimizationResult {
  method: string;
  methodLabel: string;
  minimize: boolean;
  optimalX: number[];
  optimalF: number;
  iterationsCount: number;
  functionEvaluations: number;
  iterations: Iteration[];
  path: PathPoint[];
  diverged?: boolean;
  statusMessage?: string;
}

export interface ContourData {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
  gridSize: number;
  values: (number | null)[];
  levels: number[];
  xCoords?: number[];
  yCoords?: number[];
  zMin: number;
  zMax: number;
}

export interface SurfaceData {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
  x3Slice: number;
  gridSize: number;
  xCoords: number[];
  yCoords: number[];
  values: (number | null)[];
  zMin: number;
  zMax: number;
}
