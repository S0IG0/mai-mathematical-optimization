import { Box, Chip, Paper, Stack, Typography } from '@mui/material';
import { Plot3DProjections } from './Plot3DProjections';
import { lazy, Suspense, useMemo } from 'react';
import type { Data, Layout } from 'plotly.js';
import {
  normalizeContourData,
  normalizeSurfaceData,
  path2dRanges,
  pathToPlotlySegments,
  scene3dRanges,
  squarePlotRange,
  subsamplePath,
  valuesToPlotlyZ,
} from '../plot/grid';
import type { PlotBounds } from '../plot/grid';
import type { ContourData, PathPoint, SurfaceData } from '../types';

const Plot = lazy(() => import('react-plotly.js'));

const plotLayoutBase: Partial<Layout> = {
  paper_bgcolor: '#0d1117',
  plot_bgcolor: '#161b22',
  font: { color: '#e6edf3', family: 'Segoe UI, Roboto, sans-serif', size: 12 },
  margin: { l: 56, r: 24, t: 48, b: 48 },
};

interface Props {
  mode: '2d' | '3d';
  contour: ContourData | null;
  surface: SurfaceData | null;
  path: PathPoint[];
  plotBounds?: PlotBounds | null;
  functionId?: 'F1' | 'F2';
  diverged?: boolean;
  streaming?: boolean;
  loading?: boolean;
}

/** Траектория в координатах (x₁, x₂, F): как поверхность F(x₁,x₂,x₃=const). */
function pathOnSurfaceSlice(path: PathPoint[]): {
  x: number[];
  y: number[];
  z: number[];
  x3: number[];
} {
  const x: number[] = [];
  const y: number[] = [];
  const z: number[] = [];
  const x3: number[] = [];
  for (const p of path) {
    if (p.x.length >= 3 && p.x.every((c) => Number.isFinite(c)) && Number.isFinite(p.f)) {
      x.push(p.x[0]);
      y.push(p.x[1]);
      z.push(p.f);
      x3.push(p.x[2]);
    }
  }
  return { x, y, z, x3 };
}

function Plot2DSafe({
  contour,
  path,
  plotBounds,
  functionId = 'F1',
}: {
  contour: ContourData;
  path: PathPoint[];
  plotBounds?: PlotBounds | null;
  functionId?: 'F1' | 'F2';
}) {
  try {
    return (
      <Plot2D contour={contour} path={path} plotBounds={plotBounds} functionId={functionId} />
    );
  } catch (e) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
        <Typography color="error">
          {e instanceof Error ? e.message : 'Ошибка построения 2D-графика'}
        </Typography>
      </Box>
    );
  }
}

function Plot2D({
  contour: rawContour,
  path,
  plotBounds,
  functionId = 'F1',
}: {
  contour: ContourData;
  path: PathPoint[];
  plotBounds?: PlotBounds | null;
  functionId?: 'F1' | 'F2';
}) {
  const fnLabel = functionId === 'F2' ? 'F₂' : 'F₁';
  const contour = useMemo(
    () => normalizeContourData(rawContour, plotBounds ?? undefined),
    [rawContour, plotBounds],
  );
  const { xMin, xMax, yMin, yMax } = contour;

  const gridSize = contour.gridSize;
  const xCoords =
    contour.xCoords?.length === gridSize
      ? contour.xCoords
      : Array.from({ length: gridSize }, (_, i) => xMin + (i / (gridSize - 1)) * (xMax - xMin));
  const yCoords =
    contour.yCoords?.length === gridSize
      ? contour.yCoords
      : Array.from({ length: gridSize }, (_, j) => yMin + (j / (gridSize - 1)) * (yMax - yMin));

  const z = useMemo(
    () => valuesToPlotlyZ(contour.values, gridSize, 'xMajor'),
    [contour.values, gridSize],
  );

  const { xRange, yRange } = useMemo(
    () => squarePlotRange(xMin, xMax, yMin, yMax),
    [xMin, xMax, yMin, yMax],
  );

  const pathSeg = useMemo(
    () => pathToPlotlySegments(path, xMin, xMax, yMin, yMax),
    [path, xMin, xMax, yMin, yMax],
  );

  const showFullPathPanel = pathSeg.outsideCount > 0 && path.length > 1;
  const fullPathRanges = useMemo(
    () => (showFullPathPanel ? path2dRanges(path) : null),
    [showFullPathPanel, path],
  );

  const zMin = Number.isFinite(contour.zMin) ? contour.zMin : undefined;
  const zMax = Number.isFinite(contour.zMax) ? contour.zMax : undefined;

  const traces = useMemo(() => {
    const list: Data[] = [
      {
        type: 'contour',
        x: xCoords,
        y: yCoords,
        z,
        colorscale: 'Viridis',
        reversescale: false,
        ...(zMin != null && zMax != null ? { zmin: zMin, zmax: zMax } : {}),
        ncontours: 14,
        contours: {
          coloring: 'heatmap',
          showlines: true,
        },
        line: { width: 1, color: '#484f58' },
        colorbar: {
          title: { text: 'F(x)', font: { color: '#8b949e' } },
          tickfont: { color: '#8b949e' },
        },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<br>F=%{z:.4f}<extra></extra>',
      },
    ];

    const validIdx = pathSeg.x
      .map((v, i) => (v != null && pathSeg.y[i] != null ? i : -1))
      .filter((i) => i >= 0);

    if (validIdx.length > 0) {
      list.push({
        type: 'scatter',
        mode: 'lines+markers',
        x: pathSeg.x,
        y: pathSeg.y,
        name: `Траектория в области ${fnLabel}`,
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
        text: pathSeg.f.map((_, i) => {
          if (pathSeg.x[i] == null) return '';
          if (i === validIdx[0]) return 'X₀';
          if (i === validIdx[validIdx.length - 1]) return 'выход';
          return '';
        }),
        hovertemplate: 'x₁=%{x:.4f}<br>x₂=%{y:.4f}<extra></extra>',
      });
    }

    if (showFullPathPanel && fullPathRanges) {
      const sampled = subsamplePath(
        path.filter((p) => p.x.length >= 2 && Number.isFinite(p.x[0]) && Number.isFinite(p.x[1])),
        120,
      );
      const fx = sampled.map((p) => p.x[0]);
      const fy = sampled.map((p) => p.x[1]);
      list.push({
        type: 'scatter',
        mode: 'lines',
        x: fx,
        y: fy,
        xaxis: 'x2',
        yaxis: 'y2',
        name: 'Полная траектория',
        line: { color: '#f7b955', width: 2 },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<extra></extra>',
      });
      const last = path[path.length - 1];
      if (last?.x.length >= 2) {
        list.push({
          type: 'scatter',
          mode: 'text+markers',
          x: [last.x[0]],
          y: [last.x[1]],
          xaxis: 'x2',
          yaxis: 'y2',
          name: 'X* (конец)',
          marker: { size: 10, color: '#f85149', symbol: 'x', line: { width: 1, color: '#fff' } },
          text: ['X*'],
          textposition: 'top right',
          textfont: { color: '#f85149', size: 10 },
          showlegend: false,
          hovertemplate: `x₁=%{x:.2f}<br>x₂=%{y:.2f}<br>F=${last.f.toFixed(2)}<extra></extra>`,
        });
      }
    }

    return list;
  }, [xCoords, yCoords, z, zMin, zMax, pathSeg, path, showFullPathPanel, fullPathRanges, fnLabel]);

  const layout: Partial<Layout> = useMemo(() => {
    const base: Partial<Layout> = {
      ...plotLayoutBase,
      title: {
        text: showFullPathPanel
          ? pathSeg.insideCount <= 1
            ? `В области контура видна только стартовая точка — при 1D-оптимизации шаг сразу уходит за пределы графика · низ: вся траектория`
            : `Верх: линии уровня ${fnLabel} · низ: полный уход (расходимость)`
          : `Линии уровня ${fnLabel}(x₁, x₂) и траектория`,
        font: { size: 13 },
      },
      margin: { l: 56, r: 56, t: 52, b: showFullPathPanel ? 40 : 48 },
      showlegend: true,
      legend: { orientation: 'h', y: showFullPathPanel ? 1.02 : 1.1 },
    };

    if (showFullPathPanel && fullPathRanges) {
      return {
        ...base,
        grid: { rows: 2, columns: 1, pattern: 'independent', roworder: 'top to bottom' },
        xaxis: {
          title: { text: 'x₁' },
          range: xRange,
          gridcolor: '#30363d',
          zerolinecolor: '#484f58',
          domain: [0, 1],
        },
        yaxis: {
          title: { text: 'x₂' },
          range: yRange,
          gridcolor: '#30363d',
          zerolinecolor: '#484f58',
          scaleanchor: 'x',
          scaleratio: 1,
          domain: [0.38, 1],
        },
        xaxis2: {
          title: { text: 'x₁' },
          range: fullPathRanges.xRange,
          gridcolor: '#30363d',
          zerolinecolor: '#484f58',
          domain: [0, 1],
        },
        yaxis2: {
          title: { text: 'x₂' },
          range: fullPathRanges.yRange,
          gridcolor: '#30363d',
          zerolinecolor: '#484f58',
          domain: [0, 0.32],
        },
        annotations: [
          {
            text: `Область ${fnLabel}: [${xMin.toFixed(1)}; ${xMax.toFixed(1)}]×[${yMin.toFixed(1)}; ${yMax.toFixed(1)}]`,
            xref: 'paper',
            yref: 'paper',
            x: 0,
            y: 1.04,
            showarrow: false,
            font: { size: 10, color: '#8b949e' },
            xanchor: 'left',
          },
        ],
      };
    }

    return {
      ...base,
      xaxis: {
        title: { text: 'x₁' },
        range: xRange,
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      yaxis: {
        title: { text: 'x₂' },
        range: yRange,
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
        scaleanchor: 'x',
        scaleratio: 1,
      },
    };
  }, [showFullPathPanel, fullPathRanges, xRange, yRange, xMin, xMax, yMin, yMax, fnLabel, pathSeg.insideCount]);

  return (
    <Plot
      data={traces}
      layout={layout}
      config={{ responsive: true, displayModeBar: true, displaylogo: false }}
      style={{ width: '100%', height: '100%' }}
      useResizeHandler
    />
  );
}

function Plot3DSafe({
  surface,
  path,
  plotBounds,
}: {
  surface: SurfaceData;
  path: PathPoint[];
  plotBounds?: PlotBounds | null;
}) {
  try {
    const normalized = normalizeSurfaceData(surface, plotBounds ?? undefined);
    return (
      <Stack spacing={2} sx={{ width: '100%' }}>
        <Box sx={{ height: { xs: 360, md: 440 }, width: '100%' }}>
          <Plot3D surface={normalized} path={path} />
        </Box>
        <Plot3DProjections surface={normalized} path={path} />
      </Stack>
    );
  } catch (e) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
        <Typography color="error">
          {e instanceof Error ? e.message : 'Ошибка построения 3D-графика'}
        </Typography>
      </Box>
    );
  }
}

function Plot3D({ surface, path }: { surface: SurfaceData; path: PathPoint[] }) {
  const gridSize = surface.gridSize;
  const xCoords = surface.xCoords;
  const yCoords = surface.yCoords;
  const zMatrix = useMemo(() => valuesToPlotlyZ(surface.values, gridSize), [surface.values, gridSize]);
  const traj = useMemo(() => pathOnSurfaceSlice(path), [path]);
  const scene = useMemo(() => scene3dRanges(surface, path), [surface, path]);

  const traces = useMemo(() => {
    const list: Data[] = [
      {
        type: 'surface',
        x: xCoords,
        y: yCoords,
        z: zMatrix,
        colorscale: 'Viridis',
        opacity: 0.92,
        showscale: true,
        colorbar: { title: { text: 'F(x)' }, len: 0.6, y: 0.5 },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<br>F=%{z:.4f}<extra></extra>',
      },
    ];

    if (traj.x.length > 0) {
      const text = traj.x.map((_, i) => (i === 0 ? 'X₀' : i === traj.x.length - 1 ? 'X*' : ''));
      list.push({
        type: 'scatter3d',
        mode: 'lines+markers',
        x: traj.x,
        y: traj.y,
        z: traj.z,
        name: 'Траектория',
        line: { color: '#7ee8a2', width: 6 },
        marker: {
          size: traj.x.map((_, i) => (i === 0 || i === traj.x.length - 1 ? 6 : 3.5)),
          color: traj.x.map((_, i) =>
            i === 0 ? '#5c9eff' : i === traj.x.length - 1 ? '#f7b955' : '#7ee8a2',
          ),
          line: { color: '#0d1117', width: 1 },
        },
        text,
        customdata: traj.x3,
        hovertemplate:
          'x₁=%{x:.4f}<br>x₂=%{y:.4f}<br>x₃=%{customdata:.4f}<br>F=%{z:.4f}<extra></extra>',
      });
    }
    return list;
  }, [xCoords, yCoords, zMatrix, traj]);

  const layout: Partial<Layout> = {
    ...plotLayoutBase,
    title: {
      text: `F₂(x₁, x₂) при x₃=${surface.x3Slice.toFixed(2)} и траектория (ось z — значение F)`,
      font: { size: 13 },
    },
    scene: {
      xaxis: {
        title: { text: 'x₁' },
        range: scene.xRange,
        backgroundcolor: '#161b22',
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      yaxis: {
        title: { text: 'x₂' },
        range: scene.yRange,
        backgroundcolor: '#161b22',
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      zaxis: {
        title: { text: 'F(x)' },
        range: scene.zRange,
        backgroundcolor: '#161b22',
        gridcolor: '#30363d',
        zerolinecolor: '#484f58',
      },
      bgcolor: '#0d1117',
      aspectmode: 'manual',
      aspectratio: scene.aspectratio,
      camera: { eye: { x: 1.35, y: 1.35, z: 0.9 } },
    },
    margin: { l: 0, r: 0, t: 56, b: 0 },
  };

  return (
    <Plot
      data={traces}
      layout={layout}
      config={{ responsive: true, displayModeBar: true, displaylogo: false }}
      style={{ width: '100%', height: '100%' }}
      useResizeHandler
    />
  );
}

export function OptimizationPlot({
  mode,
  contour,
  surface,
  path,
  plotBounds,
  functionId,
  diverged,
  streaming,
  loading,
}: Props) {
  const tallPlot = mode === '2d' && Boolean(diverged);

  if (loading) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', minHeight: 420 }}>
        <Typography color="text.secondary">Загрузка данных для графика…</Typography>
      </Paper>
    );
  }

  if (mode === '2d' && !contour) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', minHeight: 360 }}>
        <Typography color="text.secondary">Нет данных для 2D-графика</Typography>
      </Paper>
    );
  }

  if (mode === '3d' && !surface) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', minHeight: 360 }}>
        <Typography color="text.secondary">Нет данных для 3D-графика</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, overflow: 'hidden' }}>
      <Stack direction="row" alignItems="center" spacing={1} mb={1}>
        <Typography variant="subtitle2">
          {mode === '2d' ? 'Линии уровня и траектория (Plotly)' : '3D-поверхность и траектория (Plotly)'}
        </Typography>
        {streaming && <Chip label="в реальном времени" size="small" color="secondary" />}
      </Stack>
      <Box
        sx={{
          height: mode === '3d' ? 'auto' : { xs: tallPlot ? 520 : 380, md: tallPlot ? 620 : 480 },
          width: '100%',
        }}
      >
        <Suspense
          fallback={
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 360 }}>
              <Typography color="text.secondary">Инициализация Plotly…</Typography>
            </Box>
          }
        >
          {mode === '2d' && contour ? (
            <Box sx={{ height: { xs: tallPlot ? 520 : 380, md: tallPlot ? 620 : 480 } }}>
              <Plot2DSafe
                contour={contour}
                path={path}
                plotBounds={plotBounds}
                functionId={functionId}
              />
            </Box>
          ) : null}
          {mode === '3d' && surface ? (
            <Plot3DSafe surface={surface} path={path} plotBounds={plotBounds} />
          ) : null}
        </Suspense>
      </Box>
      <Typography variant="caption" color="text.secondary" display="block" mt={1}>
        {mode === '2d'
          ? diverged
            ? 'Седловая F₁: метод уходит в бесконечность — сверху видно начало на линиях уровня, снизу весь уход координат'
            : 'Линии уровня и траектория в области построения функции'
          : '3D: поверхность F(x₁,x₂) при x₃=const · ниже — проекции траектории на плоскости (x₁,x₂), (x₁,x₃), (x₂,x₃)'}
      </Typography>
    </Paper>
  );
}
