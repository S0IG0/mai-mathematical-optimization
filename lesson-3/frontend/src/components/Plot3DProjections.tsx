import { Box, Grid, Typography } from '@mui/material';
import { lazy, Suspense, useMemo } from 'react';
import type { Data, Layout } from 'plotly.js';
import { extendRange, squarePlotRange, valuesToPlotlyZ } from '../plot/grid';
import type { PathPoint, SurfaceData } from '../types';

const Plot = lazy(() => import('react-plotly.js'));

const layoutBase: Partial<Layout> = {
  paper_bgcolor: '#0d1117',
  plot_bgcolor: '#161b22',
  font: { color: '#e6edf3', family: 'Segoe UI, Roboto, sans-serif', size: 11 },
  margin: { l: 48, r: 16, t: 36, b: 40 },
  showlegend: false,
};

function pathCoords3d(path: PathPoint[]): { x1: number[]; x2: number[]; x3: number[] } {
  const x1: number[] = [];
  const x2: number[] = [];
  const x3: number[] = [];
  for (const p of path) {
    if (p.x.length >= 3 && p.x.every((c) => Number.isFinite(c))) {
      x1.push(p.x[0]);
      x2.push(p.x[1]);
      x3.push(p.x[2]);
    }
  }
  return { x1, x2, x3 };
}

function trajectoryTrace(
  x: number[],
  y: number[],
  xTitle: string,
  yTitle: string,
): { data: Data[]; layout: Partial<Layout> } {
  const xRange = extendRange(Math.min(...x), Math.max(...x), x, 0.1);
  const yRange = extendRange(Math.min(...y), Math.max(...y), y, 0.1);

  const data: Data[] = [];
  if (x.length > 0) {
    data.push({
      type: 'scatter',
      mode: 'lines+markers',
      x,
      y,
      line: { color: '#7ee8a2', width: 2.5 },
      marker: {
        size: x.map((_, i) => (i === 0 || i === x.length - 1 ? 9 : 5)),
        color: x.map((_, i) =>
          i === 0 ? '#5c9eff' : i === x.length - 1 ? '#f7b955' : '#7ee8a2',
        ),
        line: { color: '#0d1117', width: 1 },
      },
      hovertemplate: `${xTitle}=%{x:.4f}<br>${yTitle}=%{y:.4f}<extra></extra>`,
    });
  }

  const layout: Partial<Layout> = {
    ...layoutBase,
    xaxis: { title: { text: xTitle }, range: xRange, gridcolor: '#30363d', zerolinecolor: '#484f58' },
    yaxis: { title: { text: yTitle }, range: yRange, gridcolor: '#30363d', zerolinecolor: '#484f58' },
  };

  return { data, layout };
}

function ProjectionChart({
  title,
  data,
  layout,
}: {
  title: string;
  data: Data[];
  layout: Partial<Layout>;
}) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" display="block" mb={0.5} px={0.5}>
        {title}
      </Typography>
      <Box sx={{ height: 220, width: '100%' }}>
        <Suspense
          fallback={
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <Typography variant="caption" color="text.secondary">
                …
              </Typography>
            </Box>
          }
        >
          <Plot
            data={data}
            layout={{ ...layout, title: undefined }}
            config={{ responsive: true, displayModeBar: false, displaylogo: false }}
            style={{ width: '100%', height: '100%' }}
            useResizeHandler
          />
        </Suspense>
      </Box>
    </Box>
  );
}

interface Props {
  surface: SurfaceData;
  path: PathPoint[];
}

export function Plot3DProjections({ surface, path }: Props) {
  const coords = useMemo(() => pathCoords3d(path), [path]);
  const gridSize = surface.gridSize;

  const x1x2Panel = useMemo(() => {
    const xCoords = surface.xCoords;
    const yCoords = surface.yCoords;
    const z = valuesToPlotlyZ(surface.values, gridSize, 'yMajor');
    const { xRange, yRange } = squarePlotRange(surface.xMin, surface.xMax, surface.yMin, surface.yMax);
    const zMin = Number.isFinite(surface.zMin) ? surface.zMin : undefined;
    const zMax = Number.isFinite(surface.zMax) ? surface.zMax : undefined;

    const data: Data[] = [
      {
        type: 'contour',
        x: xCoords,
        y: yCoords,
        z,
        colorscale: 'Viridis',
        ...(zMin != null && zMax != null ? { zmin: zMin, zmax: zMax } : {}),
        ncontours: 12,
        contours: { coloring: 'heatmap', showlines: true },
        line: { width: 0.8, color: '#484f58' },
        colorbar: { len: 0.9, y: 0.5, title: { text: 'F' } },
        hovertemplate: 'x₁=%{x:.3f}<br>x₂=%{y:.3f}<br>F=%{z:.4f}<extra></extra>',
      },
    ];

    if (coords.x1.length > 0) {
      data.push({
        type: 'scatter',
        mode: 'lines+markers',
        x: coords.x1,
        y: coords.x2,
        line: { color: '#7ee8a2', width: 2.5 },
        marker: {
          size: coords.x1.map((_, i) => (i === 0 || i === coords.x1.length - 1 ? 9 : 5)),
          color: coords.x1.map((_, i) =>
            i === 0 ? '#5c9eff' : i === coords.x1.length - 1 ? '#f7b955' : '#7ee8a2',
          ),
          line: { color: '#0d1117', width: 1 },
        },
        hovertemplate: 'x₁=%{x:.4f}<br>x₂=%{y:.4f}<extra></extra>',
      });
    }

    const layout: Partial<Layout> = {
      ...layoutBase,
      xaxis: { title: { text: 'x₁' }, range: xRange, gridcolor: '#30363d', zerolinecolor: '#484f58' },
      yaxis: { title: { text: 'x₂' }, range: yRange, gridcolor: '#30363d', zerolinecolor: '#484f58' },
      margin: { l: 48, r: 48, t: 24, b: 40 },
    };

    return { data, layout };
  }, [surface, coords, gridSize]);

  const x1x3Panel = useMemo(() => {
    if (coords.x1.length === 0) return { data: [] as Data[], layout: layoutBase };
    return trajectoryTrace(coords.x1, coords.x3, 'x₁', 'x₃');
  }, [coords]);

  const x2x3Panel = useMemo(() => {
    if (coords.x2.length === 0) return { data: [] as Data[], layout: layoutBase };
    return trajectoryTrace(coords.x2, coords.x3, 'x₂', 'x₃');
  }, [coords]);

  if (coords.x1.length === 0) return null;

  return (
    <Box>
      <Typography variant="caption" color="text.secondary" display="block" mb={1}>
        Проекции траектории на координатные плоскости (x₃ срез поверхности ={' '}
        {surface.x3Slice.toFixed(2)})
      </Typography>
      <Grid container spacing={1.5}>
        <Grid item xs={12} md={4}>
          <ProjectionChart
            title={`Плоскость (x₁, x₂) · F(x₁,x₂, x₃=${surface.x3Slice.toFixed(2)})`}
            data={x1x2Panel.data}
            layout={x1x2Panel.layout}
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <ProjectionChart title="Проекция (x₁, x₃)" data={x1x3Panel.data} layout={x1x3Panel.layout} />
        </Grid>
        <Grid item xs={12} md={4}>
          <ProjectionChart title="Проекция (x₂, x₃)" data={x2x3Panel.data} layout={x2x3Panel.layout} />
        </Grid>
      </Grid>
    </Box>
  );
}
