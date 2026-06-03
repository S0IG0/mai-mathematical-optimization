export interface ConstraintInfo {
  id: string;
  label: string;
  type: string;
}

export interface Variant {
  id: number;
  title: string;
  objectiveFormula: string;
  minimize: boolean;
  variant1Extended: boolean;
  taskDescription?: string;
  plotXMin: number;
  plotXMax: number;
  plotYMin: number;
  plotYMax: number;
  constraints: ConstraintInfo[];
}

export interface Point {
  x1: number;
  x2: number;
  f?: number;
}

export interface ConstraintStatus {
  id: string;
  label: string;
  value: number;
  active: boolean;
}

export interface CandidatePoint {
  point: Point;
  objectiveValue: number;
  feasible: boolean;
  kktSatisfied: boolean;
  gradient?: Point;
  multipliers?: number[];
  description: string;
  constraints: ConstraintStatus[];
}

export interface KuhnTuckerSystem {
  lagrangian: string;
  stationarity: string;
  feasibility: string;
  complementarity: string;
  constraintForms: string[];
}

export interface KuhnTuckerResult {
  variantId: number;
  objectiveFormula: string;
  minimize: boolean;
  kktSystem: KuhnTuckerSystem;
  candidates: CandidatePoint[];
  optimalPoint?: Point;
  optimalValue?: number;
  optimalKktSatisfied?: boolean;
  conclusion: string;
  descentPath?: Point[];
  feasiblePolygon?: number[][];
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
  feasiblePolygon?: number[][];
  constraintBoundaries?: ConstraintBoundary[];
}

export interface ConstraintBoundary {
  id: string;
  label: string;
  x: number[];
  y: number[];
}

export interface FunctionDefinition {
  plotXMin: number;
  plotXMax: number;
  plotYMin: number;
  plotYMax: number;
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
