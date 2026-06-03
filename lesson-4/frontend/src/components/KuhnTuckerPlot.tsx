import { Box, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import type { Data, Layout } from 'plotly.js';
import { lazy, Suspense, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { fetchContour } from '../api';
import {
  normalizeContourData,
  pathToPlotlySegments,
  squarePlotRange,
  valuesToPlotlyZ,
} from '../plot/grid';
import type { ContourData, KuhnTuckerResult, Point, Variant } from '../types';

const Plot = lazy(() => import('react-plotly.js'));

const plotLayoutBase: Partial<Layout> = {
  paper_bgcolor: '#0d1117',
  plot_bgcolor: '#161b22',
  font: { color: '#e6edf3', family: 'Segoe UI, Roboto, sans-serif', size: 12 },
  margin: { l: 56, r: 56, t: 48, b: 48 },
};

type AxisRange = { xRange: [number, number]; yRange: [number, number] };

interface Props {
  variantId: number;
  variant: Variant | undefined;
  result: KuhnTuckerResult | null;
  reloadKey?: number;
  onLoadingChange?: (loading: boolean) => void;
}

function defaultAxisRange(v: Variant): AxisRange {
  const { xRange, yRange } = squarePlotRange(v.plotXMin, v.plotXMax, v.plotYMin, v.plotYMax);
  return { xRange, yRange };
}

function readAxisRange(ev: Record<string, unknown>, axis: 'x' | 'y'): [number, number] | null {
  const a0 = ev[`${axis}axis.range[0]`];
  const a1 = ev[`${axis}axis.range[1]`];
  if (typeof a0 === 'number' && typeof a1 === 'number' && Number.isFinite(a0) && Number.isFinite(a1)) {
    return [Math.min(a0, a1), Math.max(a0, a1)];
  }
  const arr = ev[`${axis}axis.range`];
  if (Array.isArray(arr) && arr.length >= 2) {
    const v0 = Number(arr[0]);
    const v1 = Number(arr[1]);
    if (Number.isFinite(v0) && Number.isFinite(v1)) {
      return [Math.min(v0, v1), Math.max(v0, v1)];
    }
  }
  return null;
}

function parseRelayout(ev: Record<string, unknown>): AxisRange | 'reset' | null {
  if (ev['xaxis.autorange'] === true || ev['yaxis.autorange'] === true) {
    return 'reset';
  }
  const x = readAxisRange(ev, 'x');
  const y = readAxisRange(ev, 'y');
  if (x && y) {
    return { xRange: x, yRange: y };
  }
  return null;
}

function boundsFromAxisRange(r: AxisRange) {
  const padX = (r.xRange[1] - r.xRange[0]) * 0.02;
  const padY = (r.yRange[1] - r.yRange[0]) * 0.02;
  return {
    xMin: r.xRange[0] - padX,
    xMax: r.xRange[1] + padX,
    yMin: r.yRange[0] - padY,
    yMax: r.yRange[1] + padY,
  };
}

function rangeChangedEnough(prev: AxisRange | null, next: AxisRange): boolean {
  if (!prev) return true;
  const px = prev.xRange[1] - prev.xRange[0];
  const py = prev.yRange[1] - prev.yRange[0];
  const nx = next.xRange[1] - next.xRange[0];
  const ny = next.yRange[1] - next.yRange[0];
  const eps = 0.06;
  return (
    Math.abs(prev.xRange[0] - next.xRange[0]) > px * eps ||
    Math.abs(prev.xRange[1] - next.xRange[1]) > px * eps ||
    Math.abs(prev.yRange[0] - next.yRange[0]) > py * eps ||
    Math.abs(prev.yRange[1] - next.yRange[1]) > py * eps ||
    Math.abs(px - nx) > px * eps ||
    Math.abs(py - ny) > py * eps
  );
}

export function KuhnTuckerPlot({ variantId, variant, result, reloadKey = 0, onLoadingChange }: Props) {
  const [contour, setContour] = useState<ContourData | null>(null);
  const [contourLoading, setContourLoading] = useState(false);
  const [plotEpoch, setPlotEpoch] = useState(0);

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const fetchGenRef = useRef(0);
  const lastFetchedRangeRef = useRef<AxisRange | null>(null);
  const initialRangeRef = useRef<AxisRange | null>(null);

  const plotUiRevision = `${variantId}-${reloadKey}-${plotEpoch}`;

  const loadContourForRange = useCallback(
    async (range: AxisRange, showSpinner: boolean) => {
      if (!rangeChangedEnough(lastFetchedRangeRef.current, range)) {
        return;
      }
      lastFetchedRangeRef.current = range;

      const gen = ++fetchGenRef.current;
      if (showSpinner) {
        setContourLoading(true);
        onLoadingChange?.(true);
      }
      try {
        const data = await fetchContour(variantId, 72, boundsFromAxisRange(range));
        if (gen !== fetchGenRef.current) return;
        setContour(data);
      } catch {
        if (gen === fetchGenRef.current) setContour(null);
      } finally {
        if (gen === fetchGenRef.current) {
          setContourLoading(false);
          onLoadingChange?.(false);
        }
      }
    },
    [variantId, onLoadingChange],
  );

  useEffect(() => {
    if (!variant) return;
    setPlotEpoch(0);
    const initial = defaultAxisRange(variant);
    initialRangeRef.current = initial;
    lastFetchedRangeRef.current = null;
    setContour(null);
    loadContourForRange(initial, true);
    return () => {
      fetchGenRef.current += 1;
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [variantId, variant, reloadKey, loadContourForRange]);

  const handleRelayout = useCallback(
    (ev: Readonly<Record<string, unknown>>) => {
      if (!variant) return;
      const parsed = parseRelayout(ev);
      if (!parsed) return;

      if (parsed === 'reset') {
        const initial = defaultAxisRange(variant);
        initialRangeRef.current = initial;
        lastFetchedRangeRef.current = null;
        setPlotEpoch((e) => e + 1);
        loadContourForRange(initial, true);
        return;
      }

      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        loadContourForRange(parsed, false);
      }, 450);
    },
    [variant, loadContourForRange],
  );

  const plotData = useMemo(() => {
    if (!contour) return null;
    const normalized = normalizeContourData(contour, {
      plotXMin: contour.xMin,
      plotXMax: contour.xMax,
      plotYMin: contour.yMin,
      plotYMax: contour.yMax,
    });
    const { xMin, xMax, yMin, yMax, gridSize } = normalized;
    const xCoords =
      normalized.xCoords?.length === gridSize
        ? normalized.xCoords
        : Array.from({ length: gridSize }, (_, i) => xMin + (i / (gridSize - 1)) * (xMax - xMin));
    const yCoords =
      normalized.yCoords?.length === gridSize
        ? normalized.yCoords
        : Array.from({ length: gridSize }, (_, j) => yMin + (j / (gridSize - 1)) * (yMax - yMin));
    const z = valuesToPlotlyZ(normalized.values, gridSize, 'xMajor');
    return { xCoords, yCoords, z, xMin, xMax, yMin, yMax, zMin: normalized.zMin, zMax: normalized.zMax };
  }, [contour]);

  const traces = useMemo(() => {
    if (!plotData || !contour) return [] as Data[];
    const { zMin, zMax } = plotData;
    const list: Data[] = [
      {
        type: 'contour',
        x: plotData.xCoords,
        y: plotData.yCoords,
        z: plotData.z,
        colorscale: 'Viridis',
        zmin: zMin,
        zmax: zMax,
        ncontours: 16,
        contours: { coloring: 'heatmap', showlines: true },
        line: { width: 0.8, color: '#484f58' },
        colorbar: { title: { text: 'F(x)', font: { color: '#8b949e' } }, tickfont: { color: '#8b949e' } },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<br>F=%{z:.4f}<extra></extra>',
      },
    ];

    const poly = contour.feasiblePolygon ?? result?.feasiblePolygon;
    if (poly && poly.length >= 3) {
      list.push({
        type: 'scatter',
        mode: 'lines',
        x: [...poly.map((p) => p[0]), poly[0][0]],
        y: [...poly.map((p) => p[1]), poly[0][1]],
        fill: 'toself',
        fillcolor: 'rgba(92, 158, 255, 0.12)',
        line: { color: '#5c9eff', width: 2, dash: 'dot' },
        name: 'Допустимая область',
        hoverinfo: 'skip',
      });
    }

    for (const b of contour.constraintBoundaries ?? []) {
      if (b.x.length > 1) {
        list.push({
          type: 'scatter',
          mode: 'lines',
          x: b.x,
          y: b.y,
          name: b.label,
          line: { color: '#8b949e', width: 1.5, dash: 'dash' },
          hovertemplate: '%{text}<extra></extra>',
          text: b.x.map(() => b.label),
        });
      }
    }

    const candidates = result?.candidates?.filter((c) => c.feasible) ?? [];
    if (candidates.length) {
      list.push({
        type: 'scatter',
        mode: 'text+markers',
        x: candidates.map((c) => c.point.x1),
        y: candidates.map((c) => c.point.x2),
        text: candidates.map((c) => (c.kktSatisfied ? 'ККТ✓' : '·')),
        textposition: 'top center',
        textfont: { size: 9, color: '#8b949e' },
        name: 'Экстремальные точки',
        marker: {
          size: candidates.map((c) => (c.kktSatisfied ? 11 : 8)),
          color: candidates.map((c) => (c.kktSatisfied ? '#7ee8a2' : '#484f58')),
          line: { color: '#0d1117', width: 1 },
        },
        hovertemplate: 'x₁=%{x:.4f}<br>x₂=%{y:.4f}<extra></extra>',
      });
    }

    const infeasible = result?.candidates?.filter((c) => !c.feasible) ?? [];
    if (infeasible.length) {
      list.push({
        type: 'scatter',
        mode: 'markers',
        x: infeasible.map((c) => c.point.x1),
        y: infeasible.map((c) => c.point.x2),
        name: 'Недопустимые',
        marker: { size: 7, color: '#f85149', symbol: 'x' },
        hovertemplate: 'недопустимо<extra></extra>',
      });
    }

    if (result?.optimalPoint) {
      list.push({
        type: 'scatter',
        mode: 'text+markers',
        x: [result.optimalPoint.x1],
        y: [result.optimalPoint.x2],
        text: ['X*'],
        textposition: 'top right',
        textfont: { color: '#f7b955', size: 12 },
        name: 'Оптимум',
        marker: { size: 14, color: '#f7b955', symbol: 'star', line: { color: '#fff', width: 1 } },
        hovertemplate: `x₁=%{x:.4f}<br>x₂=%{y:.4f}<br>F*=${result.optimalValue?.toFixed(4)}<extra></extra>`,
      });
    }

    const path = result?.descentPath;
    if (path && path.length > 1 && variant) {
      const pathSeg = pathToPlotlySegments(
        path.map((p: Point) => ({ x: [p.x1, p.x2], f: p.f ?? 0 })),
        variant.plotXMin,
        variant.plotXMax,
        variant.plotYMin,
        variant.plotYMax,
      );
      if (pathSeg.insideCount > 1) {
        list.push({
          type: 'scatter',
          mode: 'lines+markers',
          x: pathSeg.x,
          y: pathSeg.y,
          name: 'Спуск от (0;0) — вар.1',
          connectgaps: false,
          line: { color: '#7ee8a2', width: 2.5 },
          marker: {
            size: pathSeg.x.map((_, i) => (i === 0 ? 9 : 5)),
            color: pathSeg.x.map((v, i) =>
              v == null ? 'transparent' : i === 0 ? '#5c9eff' : '#7ee8a2',
            ),
            line: { color: '#0d1117', width: 1 },
          },
          hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<extra></extra>',
        });
      }
    }

    return list;
  }, [plotData, contour, result, variant]);

  const layout: Partial<Layout> = useMemo(() => {
    const initial = initialRangeRef.current;
    return {
      ...plotLayoutBase,
      title: { text: 'Линии уровня F(x₁,x₂), область допустимости и кандидаты', font: { size: 13 } },
      uirevision: plotUiRevision,
      xaxis: {
        title: { text: 'x₁' },
        ...(initial ? { range: initial.xRange } : {}),
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      yaxis: {
        title: { text: 'x₂' },
        ...(initial ? { range: initial.yRange } : {}),
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      showlegend: true,
      legend: { orientation: 'h', y: 1.08 },
    };
  }, [plotUiRevision]);

  if (!variant) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', minHeight: 360 }}>
        <Typography color="text.secondary">Выберите вариант</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, overflow: 'hidden', position: 'relative' }}>
      <Stack direction="row" alignItems="center" spacing={1} mb={1}>
        <Typography variant="subtitle2">Визуализация (Plotly)</Typography>
        {result?.optimalPoint && <Chip label="X* найден" size="small" color="secondary" />}
        {contourLoading && (
          <Chip
            icon={<CircularProgress size={12} color="inherit" />}
            label="обновление…"
            size="small"
            variant="outlined"
          />
        )}
      </Stack>
      <Box sx={{ height: { xs: 380, md: 480 }, width: '100%', opacity: contourLoading ? 0.72 : 1 }}>
        <Suspense
          fallback={
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <Typography color="text.secondary">Инициализация Plotly…</Typography>
            </Box>
          }
        >
          {plotData ? (
            <Plot
              key={plotUiRevision}
              data={traces}
              layout={layout}
              onRelayout={handleRelayout}
              config={{
                responsive: true,
                displayModeBar: true,
                displaylogo: false,
                scrollZoom: true,
                doubleClick: 'reset',
              }}
              style={{ width: '100%', height: '100%' }}
              useResizeHandler
            />
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <CircularProgress size={32} />
            </Box>
          )}
        </Suspense>
      </Box>
      <Typography variant="caption" color="text.secondary" display="block" mt={1}>
        Зум колёсиком или рамкой — после отпускания контур подстраивается под вид; двойной щелчок — сброс
      </Typography>
    </Paper>
  );
}
