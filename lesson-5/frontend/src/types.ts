export interface InitialPointPreset {
  label: string;
  coordinates: number[];
}

export interface ExperimentPreset {
  label: string;
  muValues: number[];
  schedule: string;
  domainMode: string;
}

export interface Variant {
  id: number;
  title: string;
  methodKind: 'PENALTY' | 'BARRIER';
  methodLabel: string;
  optimizerKind: string;
  objectiveFormula: string;
  plotXMin: number;
  plotXMax: number;
  plotYMin: number;
  plotYMax: number;
  boundedBox: boolean;
  inequalities: string[];
  equalities: string[];
  initialPoints: InitialPointPreset[];
  experiments: ExperimentPreset[];
}

export interface PathPoint {
  x: number[];
  f: number;
}

export interface MuStep {
  k: number;
  mu: number;
  x: number[];
  f: number;
  alphaOrB: number;
  theta: number;
  muTimesAux: number;
  innerPath: PathPoint[];
}

export interface SolveResult {
  variantId: number;
  methodKind: string;
  schedule: string;
  domainMode: string;
  optimalX: number[];
  optimalF: number;
  penaltyOrBarrierAtOpt: number;
  feasible: boolean;
  maxViolation: number;
  constraintViolations: string[];
  conclusion: string;
  steps: MuStep[];
  path: PathPoint[];
}

export interface ConstraintBoundary {
  label: string;
  x: number[];
  y: number[];
}

export interface ContourData {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
  gridSize: number;
  zMin: number;
  zMax: number;
  xCoords: number[];
  yCoords: number[];
  values: (number | null)[];
  levels: number[];
  constraints: ConstraintBoundary[];
}

export interface SolveRequest {
  variantId: number;
  x0: number[];
  muValues?: number[];
  schedule?: string;
  domainMode?: string;
  experimentIndex?: number;
}
