import { Box, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import type { Data, Layout } from 'plotly.js';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Plot from 'react-plotly.js';
import { fetchContour } from '../api';
import {
  normalizeContourData,
  pathToPlotlySegments,
  PLOT_HEIGHT,
  squarePlotRange,
  valuesToPlotlyZ,
} from '../plot/grid';
import type { ContourData, SolveResult, Variant } from '../types';

const plotLayoutBase: Partial<Layout> = {
  paper_bgcolor: '#0d1117',
  plot_bgcolor: '#161b22',
  font: { color: '#e6edf3', family: 'Segoe UI, Roboto, sans-serif', size: 12 },
  margin: { l: 56, r: 56, t: 52, b: 48 },
  height: PLOT_HEIGHT,
  autosize: true,
};

const CONSTRAINT_COLORS = ['#f85149', '#f7b955', '#a371f7', '#79c0ff'];

interface Bounds {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
}

function sortConstraintXY(x: number[], y: number[]): { x: number[]; y: number[] } {
  const pairs = x
    .map((xi, i) => ({ x: xi, y: y[i] }))
    .filter((p) => Number.isFinite(p.x) && Number.isFinite(p.y));
  pairs.sort((a, b) => a.x - b.x || a.y - b.y);
  return { x: pairs.map((p) => p.x), y: pairs.map((p) => p.y) };
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

function boundsChangedEnough(prev: Bounds | null, next: Bounds): boolean {
  if (!prev) return true;
  const px = prev.xMax - prev.xMin;
  const py = prev.yMax - prev.yMin;
  const eps = 0.05;
  return (
    Math.abs(prev.xMin - next.xMin) > px * eps ||
    Math.abs(prev.xMax - next.xMax) > px * eps ||
    Math.abs(prev.yMin - next.yMin) > py * eps ||
    Math.abs(prev.yMax - next.yMax) > py * eps
  );
}

interface Props {
  variant: Variant;
  result: SolveResult | null;
  reloadKey: number;
}

export function PenaltyBarrierPlot({ variant, result, reloadKey }: Props) {
  const [contour, setContour] = useState<ContourData | null>(null);
  const [loading, setLoading] = useState(true);
  const [refetching, setRefetching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const plotWrapRef = useRef<HTMLDivElement>(null);
  const fetchGenRef = useRef(0);
  const lastBoundsRef = useRef<Bounds | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const defaultBounds = useMemo<Bounds>(() => {
    const { xRange, yRange } = squarePlotRange(
      variant.plotXMin,
      variant.plotXMax,
      variant.plotYMin,
      variant.plotYMax,
    );
    return { xMin: xRange[0], xMax: xRange[1], yMin: yRange[0], yMax: yRange[1] };
  }, [variant.plotXMin, variant.plotXMax, variant.plotYMin, variant.plotYMax]);

  const loadContour = useCallback(
    async (bounds: Bounds, showSpinner: boolean) => {
      if (!boundsChangedEnough(lastBoundsRef.current, bounds)) {
        return;
      }
      lastBoundsRef.current = bounds;
      const gen = ++fetchGenRef.current;
      if (showSpinner) {
        setLoading(true);
      } else {
        setRefetching(true);
      }
      setError(null);
      try {
        const raw = await fetchContour(variant.id, {
          gridSize: 80,
          xMin: bounds.xMin,
          xMax: bounds.xMax,
          yMin: bounds.yMin,
          yMax: bounds.yMax,
        });
        if (gen !== fetchGenRef.current) return;
        setContour(normalizeContourData(raw));
      } catch (e) {
        if (gen === fetchGenRef.current) {
          setError(e instanceof Error ? e.message : 'Ошибка графика');
        }
      } finally {
        if (gen === fetchGenRef.current) {
          setLoading(false);
          setRefetching(false);
        }
      }
    },
    [variant.id],
  );

  useEffect(() => {
    lastBoundsRef.current = null;
    setContour(null);
    loadContour(defaultBounds, true);
    return () => {
      fetchGenRef.current += 1;
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [variant.id, reloadKey, defaultBounds, loadContour]);

  const handleRelayout = useCallback(
    (ev: Readonly<Record<string, unknown>>) => {
      const e = ev as Record<string, unknown>;
      if (e['xaxis.autorange'] === true || e['yaxis.autorange'] === true || e.autosize === true) {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => loadContour(defaultBounds, false), 300);
        return;
      }
      const x = readAxisRange(e, 'x');
      const y = readAxisRange(e, 'y');
      if (!x || !y) return;
      const bounds: Bounds = { xMin: x[0], xMax: x[1], yMin: y[0], yMax: y[1] };
      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => loadContour(bounds, false), 400);
    },
    [defaultBounds, loadContour],
  );

  const plotData = useMemo(() => {
    if (!contour) return null;

    const { xMin, xMax, yMin, yMax, gridSize } = contour;
    const xCoords =
      contour.xCoords.length === gridSize
        ? contour.xCoords
        : Array.from({ length: gridSize }, (_, i) => xMin + (i / (gridSize - 1)) * (xMax - xMin));
    const yCoords =
      contour.yCoords.length === gridSize
        ? contour.yCoords
        : Array.from({ length: gridSize }, (_, j) => yMin + (j / (gridSize - 1)) * (yMax - yMin));

    const z = valuesToPlotlyZ(contour.values, gridSize, 'xMajor');
    let zMin = contour.zMin;
    let zMax = contour.zMax;
    if (!Number.isFinite(zMin) || !Number.isFinite(zMax) || zMax <= zMin) {
      const flat = z.flat().filter((v) => Number.isFinite(v));
      if (flat.length) {
        zMin = Math.min(...flat);
        zMax = Math.max(...flat);
      } else {
        zMin = -10;
        zMax = 10;
      }
    }

    const list: Data[] = [
      {
        type: 'heatmap',
        x: xCoords,
        y: yCoords,
        z,
        colorscale: 'Viridis',
        zmin: zMin,
        zmax: zMax,
        colorbar: {
          title: { text: 'F(x)', font: { color: '#8b949e' } },
          tickfont: { color: '#8b949e' },
        },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<br>F=%{z:.4f}<extra></extra>',
      },
      {
        type: 'contour',
        x: xCoords,
        y: yCoords,
        z,
        colorscale: 'Viridis',
        zmin: zMin,
        zmax: zMax,
        ncontours: 14,
        contours: { coloring: 'lines', showlines: true },
        line: { width: 1, color: 'rgba(230,237,243,0.35)' },
        showscale: false,
        hoverinfo: 'skip',
      },
    ];

    contour.constraints?.forEach((c, idx) => {
      if (c.x?.length > 2 && c.y?.length === c.x.length) {
        const sorted = sortConstraintXY(c.x, c.y);
        list.push({
          type: 'scatter',
          mode: 'lines',
          x: sorted.x,
          y: sorted.y,
          name: c.label,
          line: { color: CONSTRAINT_COLORS[idx % CONSTRAINT_COLORS.length], width: 2.5, dash: 'dot' },
          hovertemplate: `${c.label}<extra></extra>`,
        });
      }
    });

    if (result?.path?.length) {
      const pathSeg = pathToPlotlySegments(result.path, xMin, xMax, yMin, yMax);
      const validIdx = pathSeg.x
        .map((v, i) => (v != null && pathSeg.y[i] != null ? i : -1))
        .filter((i) => i >= 0);

      if (validIdx.length > 0) {
        list.push({
          type: 'scatter',
          mode: 'lines+markers',
          x: pathSeg.x,
          y: pathSeg.y,
          name: 'Траектория',
          connectgaps: false,
          line: { color: '#7ee8a2', width: 3 },
          marker: {
            size: pathSeg.x.map((v, i) => {
              if (v == null) return 0;
              if (i === validIdx[0]) return 11;
              if (i === validIdx[validIdx.length - 1]) return 10;
              return 5;
            }),
            color: pathSeg.x.map((v, i) => {
              if (v == null) return 'transparent';
              if (i === validIdx[0]) return '#5c9eff';
              if (i === validIdx[validIdx.length - 1]) return '#f7b955';
              return '#7ee8a2';
            }),
            line: { color: '#0d1117', width: 1 },
          },
          hovertemplate: 'x₁=%{x:.4f}<br>x₂=%{y:.4f}<extra></extra>',
        });
      }

      const opt = result.optimalX;
      if (opt?.length >= 2 && Number.isFinite(opt[0]) && Number.isFinite(opt[1])) {
        list.push({
          type: 'scatter',
          mode: 'text+markers',
          x: [opt[0]],
          y: [opt[1]],
          marker: {
            size: 14,
            color: '#f7b955',
            symbol: 'star',
            line: { color: '#fff', width: 1 },
          },
          text: ['X*'],
          textposition: 'top right',
          textfont: { color: '#f7b955', size: 12 },
          showlegend: false,
          hovertemplate: `X*<br>x₁=%{x:.4f}<br>x₂=%{y:.4f}<br>F=${result.optimalF.toFixed(4)}<extra></extra>`,
        });
      }
    }

    return { traces: list, xMin, xMax, yMin, yMax };
  }, [contour, result]);

  const layout = useMemo((): Partial<Layout> => {
    return {
      ...plotLayoutBase,
      uirevision: `${variant.id}-${reloadKey}`,
      title: {
        text: `Линии уровня F(x₁,x₂), ограничения и траектория — вариант ${variant.id}`,
        font: { size: 13 },
      },
      showlegend: true,
      legend: { orientation: 'h', y: 1.08 },
      xaxis: {
        title: { text: 'x₁' },
        ...(plotData ? { range: [plotData.xMin, plotData.xMax] } : {}),
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      yaxis: {
        title: { text: 'x₂' },
        ...(plotData ? { range: [plotData.yMin, plotData.yMax] } : {}),
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
    };
  }, [plotData, variant.id, reloadKey]);

  const handleAfterPlot = useCallback(() => {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('resize'));
    }
  }, []);

  if (loading) {
    return (
      <Paper
        sx={{
          p: 4,
          textAlign: 'center',
          minHeight: PLOT_HEIGHT,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <CircularProgress />
      </Paper>
    );
  }

  if (error || !plotData) {
    return (
      <Paper sx={{ p: 4, minHeight: 360 }}>
        <Typography color="error">{error ?? 'Нет данных для графика'}</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, overflow: 'hidden' }}>
      <Stack direction="row" alignItems="center" spacing={1} mb={1}>
        <Typography variant="subtitle2">Линии уровня и траектория (Plotly)</Typography>
        {refetching && (
          <Chip
            icon={<CircularProgress size={12} color="inherit" />}
            label="обновление области…"
            size="small"
            variant="outlined"
          />
        )}
      </Stack>
      <Box ref={plotWrapRef} sx={{ width: '100%', height: PLOT_HEIGHT, opacity: refetching ? 0.75 : 1 }}>
        <Plot
          data={plotData.traces}
          layout={layout}
          onInitialized={handleAfterPlot}
          onRelayout={handleRelayout}
          config={{
            responsive: true,
            displayModeBar: true,
            displaylogo: false,
            scrollZoom: true,
            doubleClick: 'reset',
          }}
          style={{ width: '100%', height: `${PLOT_HEIGHT}px` }}
          useResizeHandler
        />
      </Box>
      <Typography variant="caption" color="text.secondary" display="block" mt={1}>
        Колёсико или рамка — зум; при отдалении/приближении контур и ограничения дорисовываются под новую область. Двойной щелчок — сброс
      </Typography>
    </Paper>
  );
}
