import RestartAltIcon from '@mui/icons-material/RestartAlt';
import ZoomInIcon from '@mui/icons-material/ZoomIn';
import ZoomOutIcon from '@mui/icons-material/ZoomOut';
import { Box, FormControlLabel, IconButton, Paper, Stack, Switch, Tooltip, Typography } from '@mui/material';
import { useCallback, useEffect, useId, useMemo, useRef, useState } from 'react';
import {
  Brush,
  CartesianGrid,
  Customized,
  Line,
  ReferenceLine,
  ResponsiveContainer,
  ComposedChart,
  Tooltip as RechartsTooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { OptimizationMethod, OptimizationResult, PlotData } from '../types';

interface Props {
  plotData: PlotData | null;
  results: OptimizationResult[];
  minimize: boolean;
  intervalA: number;
  intervalB: number;
}

const COLORS = ['#5c9eff', '#7ee8a2', '#f7b955'];
const CHART_MARGIN = { top: 16, right: 28, bottom: 34, left: 52 };
const BRUSH_HEIGHT = 26;

type PointKind = 'lambda' | 'mu' | 'optimal';

interface MarkPoint {
  id: string;
  x: number;
  y: number;
  color: string;
  kind: PointKind;
  k?: number;
  method: string;
  methodKey: OptimizationMethod;
  shortLabel: string;
  detail: string;
}

interface ViewDomain {
  xMin: number;
  xMax: number;
  yMin: number;
  yMax: number;
}

interface DragState {
  startX: number;
  startY: number;
  startView: ViewDomain;
}

function readF(step: Record<string, unknown>, key: 'fLambda' | 'fMu'): number | null {
  const v = step[key] ?? step[key === 'fLambda' ? 'FLambda' : 'FMu'];
  return typeof v === 'number' && Number.isFinite(v) ? v : null;
}

function collectMarkPoints(results: OptimizationResult[]): MarkPoint[] {
  const marks: MarkPoint[] = [];

  results.forEach((res, i) => {
    const color = COLORS[i % COLORS.length];
    const method = res.methodLabel;

    for (const step of res.iterations) {
      const row = step as unknown as Record<string, unknown>;
      if (step.lambda != null) {
        const y = readF(row, 'fLambda');
        if (y != null) {
          marks.push({
            id: `${res.method}-lambda-${step.k}`,
            x: step.lambda,
            y,
            color,
            kind: 'lambda',
            k: step.k,
            method,
            methodKey: res.method,
            shortLabel: `λ${step.k}`,
            detail: `${method} · k=${step.k} · λ=${step.lambda.toFixed(4)} · F=${y.toFixed(4)}`,
          });
        }
      }
      if (step.mu != null) {
        const y = readF(row, 'fMu');
        if (y != null) {
          marks.push({
            id: `${res.method}-mu-${step.k}`,
            x: step.mu,
            y,
            color,
            kind: 'mu',
            k: step.k,
            method,
            methodKey: res.method,
            shortLabel: `μ${step.k}`,
            detail: `${method} · k=${step.k} · μ=${step.mu.toFixed(4)} · F=${y.toFixed(4)}`,
          });
        }
      }
    }

    if (Number.isFinite(res.optimalX) && Number.isFinite(res.optimalF)) {
      marks.push({
        id: `${res.method}-optimal`,
        x: res.optimalX,
        y: res.optimalF,
        color,
        kind: 'optimal',
        method,
        methodKey: res.method,
        shortLabel: `x*=${res.optimalX.toFixed(3)}`,
        detail: `${method} · x*=${res.optimalX.toFixed(6)} · F=${res.optimalF.toFixed(6)}`,
      });
    }
  });

  return marks;
}

function computeYDomain(lineValues: number[], results: OptimizationResult[]): [number, number] {
  const extra: number[] = [];
  for (const res of results) {
    if (Number.isFinite(res.optimalF)) extra.push(res.optimalF);
    for (const step of res.iterations) {
      const row = step as unknown as Record<string, unknown>;
      const fl = readF(row, 'fLambda');
      const fm = readF(row, 'fMu');
      if (fl != null) extra.push(fl);
      if (fm != null) extra.push(fm);
    }
  }

  const finite = [...lineValues, ...extra].filter((v) => Number.isFinite(v) && Math.abs(v) < 1e7);
  if (finite.length === 0) return [-1, 1];

  const sorted = [...finite].sort((a, b) => a - b);
  const loIdx = Math.floor(sorted.length * 0.02);
  const hiIdx = Math.min(sorted.length - 1, Math.ceil(sorted.length * 0.98));
  const yMin = sorted[loIdx];
  const yMax = sorted[hiIdx];
  const pad = Math.max((yMax - yMin) * 0.12, 0.5);
  return [yMin - pad, yMax + pad];
}

function clampView(view: ViewDomain, full: ViewDomain): ViewDomain {
  const xSpan = view.xMax - view.xMin;
  const ySpan = view.yMax - view.yMin;
  const fullXSpan = full.xMax - full.xMin;
  const fullYSpan = full.yMax - full.yMin;

  if (xSpan >= fullXSpan * 0.999 && ySpan >= fullYSpan * 0.999) {
    return { ...full };
  }

  const xMin = Math.max(full.xMin, Math.min(view.xMin, full.xMax - xSpan));
  const yMin = Math.max(full.yMin, Math.min(view.yMin, full.yMax - ySpan));

  return {
    xMin,
    xMax: xMin + xSpan,
    yMin,
    yMax: yMin + ySpan,
  };
}

function zoomView(view: ViewDomain, factor: number, full: ViewDomain): ViewDomain {
  const cx = (view.xMin + view.xMax) / 2;
  const cy = (view.yMin + view.yMax) / 2;
  const halfX = ((view.xMax - view.xMin) / 2) * factor;
  const halfY = ((view.yMax - view.yMin) / 2) * factor;

  return clampView(
    { xMin: cx - halfX, xMax: cx + halfX, yMin: cy - halfY, yMax: cy + halfY },
    full,
  );
}

function panView(view: ViewDomain, dx: number, dy: number, full: ViewDomain): ViewDomain {
  return clampView(
    {
      xMin: view.xMin + dx,
      xMax: view.xMax + dx,
      yMin: view.yMin + dy,
      yMax: view.yMax + dy,
    },
    full,
  );
}

interface ChartMaps {
  xAxisMap?: Record<number, { scale: (v: number) => number }>;
  yAxisMap?: Record<number, { scale: (v: number) => number }>;
}

interface MarksLayerProps extends ChartMaps {
  marks: MarkPoint[];
  offset?: { top: number; left: number; width: number; height: number };
  view: ViewDomain;
  hoveredId: string | null;
  onHover: (id: string | null, clientPos?: { x: number; y: number }) => void;
  showAllLabels: boolean;
  onMarkPointerDown: (e: React.MouseEvent) => void;
}

function isMarkInView(p: MarkPoint, view: ViewDomain): boolean {
  return p.x >= view.xMin && p.x <= view.xMax && p.y >= view.yMin && p.y <= view.yMax;
}

function IterationMarks({
  xAxisMap,
  yAxisMap,
  marks,
  offset,
  view,
  hoveredId,
  onHover,
  showAllLabels,
  onMarkPointerDown,
}: MarksLayerProps) {
  const clipId = useId();
  const xScale = xAxisMap?.[0]?.scale;
  const yScale = yAxisMap?.[0]?.scale;
  if (!xScale || !yScale || !offset || marks.length === 0) return null;

  const plotTop = offset.top;
  const plotLeft = offset.left;
  const plotBottom = offset.top + offset.height;
  const plotRight = offset.left + offset.width;

  const stopPan = (e: React.MouseEvent) => {
    e.stopPropagation();
    onMarkPointerDown(e);
  };

  const visibleMarks = marks.filter((p) => isMarkInView(p, view));

  return (
    <g className="iteration-marks">
      <defs>
        <clipPath id={clipId}>
          <rect x={plotLeft} y={plotTop} width={offset.width} height={offset.height} />
        </clipPath>
      </defs>
      <g clipPath={`url(#${clipId})`}>
        {visibleMarks.map((p) => {
          const cx = xScale(p.x);
          const cy = yScale(p.y);
          if (!Number.isFinite(cx) || !Number.isFinite(cy)) return null;
          if (cx < plotLeft || cx > plotRight || cy < plotTop || cy > plotBottom) return null;

          const isHovered = hoveredId === p.id;
          const isDimmed = hoveredId != null && !isHovered;
          const showLabel = !isHovered && (p.kind === 'optimal' || showAllLabels);
          const opacity = isDimmed ? 0.25 : 1;
          const labelY = cy - 12 < plotTop + 4 ? cy + 16 : cy - 10;

          const handleEnter = (e: React.MouseEvent) => {
            onHover(p.id, { x: e.clientX, y: e.clientY });
          };

          const label = (
            <text
              x={cx + 10}
              y={labelY}
              fill={p.color}
              fontSize={10}
              fontWeight={500}
              opacity={showLabel ? 1 : 0}
              pointerEvents="none"
            >
              {p.shortLabel}
            </text>
          );

          const hitArea = (
            <circle
              cx={cx}
              cy={cy}
              r={14}
              fill="transparent"
              style={{ cursor: 'pointer' }}
              onMouseEnter={handleEnter}
              onMouseLeave={() => onHover(null)}
              onMouseDown={stopPan}
            />
          );

          if (p.kind === 'lambda') {
            const r = isHovered ? 8 : 5;
            return (
              <g key={p.id} opacity={opacity}>
                {hitArea}
                <circle
                  cx={cx}
                  cy={cy}
                  r={r}
                  fill={p.color}
                  stroke={isHovered ? '#fff' : '#0d1117'}
                  strokeWidth={isHovered ? 2.5 : 1.5}
                  pointerEvents="none"
                />
                {isHovered && (
                  <circle cx={cx} cy={cy} r={14} fill="none" stroke={p.color} strokeWidth={2} opacity={0.6} />
                )}
                {label}
              </g>
            );
          }

          if (p.kind === 'mu') {
            const s = isHovered ? 8 : 6;
            return (
              <g key={p.id} opacity={opacity}>
                {hitArea}
                <line x1={cx - s} y1={cy - s} x2={cx + s} y2={cy + s} stroke={p.color} strokeWidth={isHovered ? 3.5 : 2.5} pointerEvents="none" />
                <line x1={cx - s} y1={cy + s} x2={cx + s} y2={cy - s} stroke={p.color} strokeWidth={isHovered ? 3.5 : 2.5} pointerEvents="none" />
                {isHovered && (
                  <circle cx={cx} cy={cy} r={14} fill="none" stroke={p.color} strokeWidth={2} opacity={0.6} />
                )}
                {label}
              </g>
            );
          }

          const r = isHovered ? 11 : 9;
          const starPoints = Array.from({ length: 10 }, (_, i) => {
            const radius = i % 2 === 0 ? r : r / 2.4;
            const angle = (i * Math.PI) / 5 - Math.PI / 2;
            return `${cx + radius * Math.cos(angle)},${cy + radius * Math.sin(angle)}`;
          }).join(' ');

          return (
            <g key={p.id} opacity={opacity}>
              {hitArea}
              <polygon
                points={starPoints}
                fill={p.color}
                stroke={isHovered ? '#fff' : '#0d1117'}
                strokeWidth={isHovered ? 2.5 : 1.5}
                pointerEvents="none"
              />
              {isHovered && (
                <circle cx={cx} cy={cy} r={16} fill="none" stroke={p.color} strokeWidth={2} opacity={0.7} />
              )}
              {label}
            </g>
          );
        })}
      </g>
    </g>
  );
}

interface IntervalLayerProps extends ChartMaps {
  offset?: { top: number; left: number; width: number; height: number };
  intervalA: number;
  intervalB: number;
  view: ViewDomain;
  results: OptimizationResult[];
  visibleMethods: Record<string, boolean>;
}

function IntervalBackgroundLayer({ xAxisMap, offset, intervalA, intervalB, view, results, visibleMethods }: IntervalLayerProps) {
  const xScale = xAxisMap?.[0]?.scale;
  if (!xScale || !offset) return null;

  const drawArea = (xStart: number, xEnd: number, fill: string, opacity: number, key: string) => {
    const x1 = Math.max(xStart, view.xMin);
    const x2 = Math.min(xEnd, view.xMax);
    if (x1 >= x2 - 1e-12) return null;

    const left = xScale(x1);
    const right = xScale(x2);
    if (!Number.isFinite(left) || !Number.isFinite(right)) return null;

    const width = right - left;
    if (width <= 0) return null;

    return (
      <rect
        key={key}
        x={left}
        y={offset.top}
        width={width}
        height={offset.height}
        fill={fill}
        fillOpacity={opacity}
      />
    );
  };

  return (
    <g className="interval-backgrounds">
      {drawArea(intervalA, intervalB, '#5c9eff', 0.12, 'initial')}
      {results
        .map((res, i) => ({ res, color: COLORS[i % COLORS.length] }))
        .filter(({ res }) => visibleMethods[res.method] !== false)
        .map(({ res, color }) => drawArea(res.finalA, res.finalB, color, 0.18, `final-${res.method}`))}
    </g>
  );
}

function LegendSwatch({
  type,
  color,
  label,
}: {
  type: 'line' | 'area' | 'circle' | 'cross' | 'star' | 'dashed';
  color: string;
  label: string;
}) {
  return (
    <Stack direction="row" spacing={1} alignItems="center" sx={{ py: 0.35, minWidth: 0 }}>
      <Box
        sx={{
          width: 24,
          height: 16,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        {type === 'line' && <Box sx={{ width: 24, height: 2, bgcolor: color, borderRadius: 1 }} />}
        {type === 'area' && <Box sx={{ width: 24, height: 12, bgcolor: color, opacity: 0.35, borderRadius: 0.5 }} />}
        {type === 'dashed' && <Box sx={{ width: 24, borderTop: `2px dashed ${color}` }} />}
        {type === 'circle' && (
          <Box sx={{ width: 9, height: 9, borderRadius: '50%', bgcolor: color, border: '1.5px solid #fff' }} />
        )}
        {type === 'cross' && (
          <Box sx={{ position: 'relative', width: 10, height: 10 }}>
            <Box
              sx={{
                position: 'absolute',
                top: '50%',
                left: 0,
                right: 0,
                height: 2,
                bgcolor: color,
                transform: 'translateY(-50%) rotate(45deg)',
              }}
            />
            <Box
              sx={{
                position: 'absolute',
                top: '50%',
                left: 0,
                right: 0,
                height: 2,
                bgcolor: color,
                transform: 'translateY(-50%) rotate(-45deg)',
              }}
            />
          </Box>
        )}
        {type === 'star' && (
          <Typography component="span" sx={{ color, fontSize: 14, lineHeight: 1 }}>
            ★
          </Typography>
        )}
      </Box>
      <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.4 }}>
        {label}
      </Typography>
    </Stack>
  );
}

function MarkHoverTooltip({ mark, anchor, containerWidth }: { mark: MarkPoint; anchor: { x: number; y: number }; containerWidth: number }) {
  const kindLabel =
    mark.kind === 'lambda' ? 'λ — пробная (левая)' : mark.kind === 'mu' ? 'μ — пробная (правая)' : 'x* — экстремум';
  const tooltipWidth = 200;
  const left = anchor.x + 14 + tooltipWidth > containerWidth ? anchor.x - tooltipWidth - 8 : anchor.x + 14;
  const top = Math.max(8, anchor.y - 72);

  return (
    <Paper
      elevation={4}
      sx={{
        position: 'absolute',
        left,
        top,
        p: 1.25,
        pointerEvents: 'none',
        zIndex: 10,
        width: tooltipWidth,
        bgcolor: '#161b22',
        border: '1px solid',
        borderColor: mark.color,
      }}
    >
      <Typography variant="caption" sx={{ color: mark.color, fontWeight: 700, display: 'block', mb: 0.5 }}>
        {mark.method}
      </Typography>
      <Typography variant="caption" color="text.secondary" display="block">
        {mark.k != null ? `${kindLabel} · k = ${mark.k}` : kindLabel}
      </Typography>
      <Typography variant="caption" color="text.secondary" display="block" mt={0.75}>
        x = {mark.x.toFixed(6)}
      </Typography>
      <Typography variant="caption" color="text.secondary" display="block">
        F(x) = {mark.y.toFixed(6)}
      </Typography>
    </Paper>
  );
}

function ChartLegend({ results, hasResults }: { results: OptimizationResult[]; hasResults: boolean }) {
  const items: { key: string; type: 'line' | 'area' | 'circle' | 'cross' | 'star' | 'dashed'; color: string; label: string }[] = [
    { key: 'fx', type: 'line', color: '#e6edf3', label: 'F(x) — график функции' },
    { key: 'init', type: 'area', color: '#5c9eff', label: 'Начальный интервал [a; b]' },
    { key: 'bounds', type: 'dashed', color: '#5c9eff', label: 'Границы a и b (пунктир)' },
  ];

  if (hasResults) {
    items.push(
      { key: 'lambda', type: 'circle', color: '#8b949e', label: '○ λ — пробная точка (левая)' },
      { key: 'mu', type: 'cross', color: '#8b949e', label: '✕ μ — пробная точка (правая)' },
      { key: 'opt', type: 'star', color: '#8b949e', label: '★ x* — найденный экстремум' },
    );
    results.forEach((res, i) => {
      items.push({
        key: `final-${res.method}`,
        type: 'area',
        color: COLORS[i],
        label: `${res.methodLabel} — конечный интервал и цвет точек`,
      });
    });
  }

  return (
    <Box
      sx={{
        mt: 1.5,
        p: 1.5,
        borderRadius: 1,
        bgcolor: 'background.default',
        border: 1,
        borderColor: 'divider',
        overflow: 'hidden',
      }}
    >
      <Typography variant="caption" color="text.secondary" display="block" mb={1} fontWeight={600}>
        Обозначения
      </Typography>
      <Stack component="ul" spacing={0.25} sx={{ m: 0, p: 0, listStyle: 'none' }}>
        {items.map((item) => (
          <Box component="li" key={item.key}>
            <LegendSwatch type={item.type} color={item.color} label={item.label} />
          </Box>
        ))}
      </Stack>
    </Box>
  );
}

export function FunctionChart({ plotData, results, minimize, intervalA, intervalB }: Props) {
  const chartWrapRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef<DragState | null>(null);
  const viewRef = useRef<ViewDomain | null>(null);
  const fullDomainRef = useRef<ViewDomain | null>(null);
  const isChartHoveredRef = useRef(false);

  const [hoveredId, setHoveredId] = useState<string | null>(null);
  const [hoverAnchor, setHoverAnchor] = useState<{ x: number; y: number } | null>(null);
  const [chartWidth, setChartWidth] = useState(0);
  const [view, setView] = useState<ViewDomain | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [showLabels, setShowLabels] = useState(false);
  const [visibleMethods, setVisibleMethods] = useState<Record<string, boolean>>({});

  useEffect(() => {
    setVisibleMethods((prev) => {
      const next = { ...prev };
      let changed = false;
      for (const r of results) {
        if (next[r.method] === undefined) {
          next[r.method] = true;
          changed = true;
        }
      }
      return changed ? next : prev;
    });
  }, [results]);

  const clearMarkHover = useCallback(() => {
    setHoveredId(null);
    setHoverAnchor(null);
  }, []);

  const lineData = useMemo(
    () =>
      plotData
        ? plotData.points.filter((p) => p.y != null).map((p) => ({ x: p.x, y: p.y as number }))
        : [],
    [plotData],
  );

  const fullDomain = useMemo((): ViewDomain | null => {
    if (!plotData) return null;
    const yValues = lineData.map((p) => p.y);
    const [yMin, yMax] = computeYDomain(yValues, results);
    return {
      xMin: Math.min(plotData.plotFrom, plotData.plotTo),
      xMax: Math.max(plotData.plotFrom, plotData.plotTo),
      yMin,
      yMax,
    };
  }, [plotData, lineData, results]);

  viewRef.current = view;
  fullDomainRef.current = fullDomain;

  useEffect(() => {
    if (fullDomain) setView(fullDomain);
  }, [fullDomain]);

  const getPlotSize = useCallback(() => {
    const rect = chartWrapRef.current?.getBoundingClientRect();
    if (!rect) return null;
    return {
      width: rect.width - CHART_MARGIN.left - CHART_MARGIN.right,
      height: rect.height - CHART_MARGIN.top - CHART_MARGIN.bottom - BRUSH_HEIGHT,
    };
  }, []);

  const handleZoom = useCallback(
    (factor: number) => {
      if (!view || !fullDomain) return;
      clearMarkHover();
      setView(zoomView(view, factor, fullDomain));
    },
    [view, fullDomain, clearMarkHover],
  );

  const handleReset = useCallback(() => {
    if (fullDomain) {
      clearMarkHover();
      setView(fullDomain);
    }
  }, [fullDomain, clearMarkHover]);

  useEffect(() => {
    const onWheel = (e: WheelEvent) => {
      if (!isChartHoveredRef.current) return;

      e.preventDefault();
      e.stopPropagation();

      const v = viewRef.current;
      const full = fullDomainRef.current;
      if (!v || !full) return;

      const factor = e.deltaY > 0 ? 1.12 : 0.88;
      setHoveredId(null);
      setHoverAnchor(null);
      setView(zoomView(v, factor, full));
    };

    window.addEventListener('wheel', onWheel, { passive: false });
    return () => window.removeEventListener('wheel', onWheel);
  }, []);

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    if (e.button !== 0 || !viewRef.current) return;
    clearMarkHover();
    dragRef.current = {
      startX: e.clientX,
      startY: e.clientY,
      startView: { ...viewRef.current },
    };
    setIsDragging(true);
  }, [clearMarkHover]);

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      const drag = dragRef.current;
      const full = fullDomainRef.current;
      const plot = getPlotSize();
      if (!drag || !full || !plot || plot.width <= 0 || plot.height <= 0) return;

      const dxPx = e.clientX - drag.startX;
      const dyPx = e.clientY - drag.startY;
      const xSpan = drag.startView.xMax - drag.startView.xMin;
      const ySpan = drag.startView.yMax - drag.startView.yMin;

      const dxData = (-dxPx / plot.width) * xSpan;
      const dyData = (dyPx / plot.height) * ySpan;

      setView(panView(drag.startView, dxData, dyData, full));
    };

    const handleMouseUp = () => {
      dragRef.current = null;
      setIsDragging(false);
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, [getPlotSize]);

  const marks = useMemo(
    () => collectMarkPoints(results).filter((m) => visibleMethods[m.methodKey] !== false),
    [results, visibleMethods],
  );

  const hoveredMark = useMemo(
    () => (hoveredId ? marks.find((m) => m.id === hoveredId) ?? null : null),
    [marks, hoveredId],
  );

  const handleMarkHover = useCallback((id: string | null, clientPos?: { x: number; y: number }) => {
    setHoveredId(id);
    if (id && clientPos && chartWrapRef.current) {
      const rect = chartWrapRef.current.getBoundingClientRect();
      setHoverAnchor({ x: clientPos.x - rect.left, y: clientPos.y - rect.top });
    } else {
      setHoverAnchor(null);
    }
  }, []);

  useEffect(() => {
    const el = chartWrapRef.current;
    if (!el) return;
    const update = () => setChartWidth(el.clientWidth);
    update();
    const ro = new ResizeObserver(update);
    ro.observe(el);
    return () => ro.disconnect();
  }, [plotData]);

  if (!plotData || !fullDomain || !view) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', minHeight: 360 }}>
        <Typography color="text.secondary">График функции появится после расчёта</Typography>
      </Paper>
    );
  }

  const hasResults = results.length > 0;
  const hasVisibleMarks = marks.length > 0;
  const crossesZero = intervalA < 0 && intervalB > 0;
  const xSpan = view.xMax - view.xMin;
  const xTickCount = xSpan > 8 ? 6 : xSpan > 3 ? 5 : 4;

  return (
    <Paper sx={{ p: 2, overflow: 'hidden' }}>
      <Stack direction="row" alignItems="flex-start" justifyContent="space-between" mb={1} gap={1} flexWrap="wrap">
        <Typography variant="subtitle2" sx={{ pt: 0.5 }}>
          График функции и интервалы неопределённости
        </Typography>
        <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap" useFlexGap>
          {results.map((res, i) => (
            <FormControlLabel
              key={res.method}
              control={
                <Switch
                  size="small"
                  checked={visibleMethods[res.method] !== false}
                  onChange={(e) =>
                    setVisibleMethods((prev) => ({ ...prev, [res.method]: e.target.checked }))
                  }
                />
              }
              label={
                <Stack direction="row" spacing={0.75} alignItems="center">
                  <Box
                    sx={{
                      width: 8,
                      height: 8,
                      borderRadius: '50%',
                      bgcolor: COLORS[i % COLORS.length],
                      flexShrink: 0,
                    }}
                  />
                  <Typography variant="caption" color="text.secondary" noWrap>
                    {res.methodLabel}
                  </Typography>
                </Stack>
              }
              sx={{ mr: 0, ml: 0 }}
            />
          ))}
          {hasResults && (
            <FormControlLabel
              control={
                <Switch
                  size="small"
                  checked={showLabels}
                  onChange={(e) => setShowLabels(e.target.checked)}
                />
              }
              label={
                <Typography variant="caption" color="text.secondary">
                  Метки точек
                </Typography>
              }
              sx={{ mr: 0, ml: 0.5 }}
            />
          )}
          <Tooltip title="Приблизить">
            <IconButton size="small" onClick={() => handleZoom(0.75)} aria-label="Приблизить">
              <ZoomInIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Отдалить">
            <IconButton size="small" onClick={() => handleZoom(1.33)} aria-label="Отдалить">
              <ZoomOutIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Сбросить масштаб">
            <IconButton size="small" onClick={handleReset} aria-label="Сбросить">
              <RestartAltIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      </Stack>

      <Box
        ref={chartWrapRef}
        onMouseDown={handleMouseDown}
        onMouseEnter={() => {
          isChartHoveredRef.current = true;
        }}
        onMouseLeave={() => {
          isChartHoveredRef.current = false;
        }}
        sx={{
          position: 'relative',
          height: 400,
          cursor: isDragging ? 'grabbing' : 'grab',
          touchAction: 'none',
          userSelect: 'none',
          overflow: 'hidden',
          overscrollBehavior: 'contain',
          borderRadius: 1,
          border: 1,
          borderColor: 'divider',
          bgcolor: 'background.default',
        }}
      >
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart margin={CHART_MARGIN}>
            <CartesianGrid strokeDasharray="3 3" stroke="#30363d" />
            <XAxis
              type="number"
              dataKey="x"
              domain={[view.xMin, view.xMax]}
              scale="linear"
              allowDataOverflow
              stroke="#8b949e"
              tickCount={xTickCount}
              tickFormatter={(v) => Number(v).toFixed(2)}
            />
            <YAxis
              type="number"
              domain={[view.yMin, view.yMax]}
              scale="linear"
              allowDataOverflow
              stroke="#8b949e"
              tickCount={6}
              tickFormatter={(v) => Number(v).toFixed(1)}
            />
            <RechartsTooltip
              active={hoveredId ? false : undefined}
              contentStyle={{ background: '#161b22', border: '1px solid #30363d' }}
              formatter={(v: number) => (v == null ? '—' : Number(v).toFixed(4))}
              labelFormatter={(x) => `x = ${Number(x).toFixed(4)}`}
            />

            <Customized
              component={(props: ChartMaps & { offset?: { top: number; left: number; width: number; height: number } }) => (
                <IntervalBackgroundLayer
                  {...props}
                  intervalA={intervalA}
                  intervalB={intervalB}
                  view={view}
                  results={results}
                  visibleMethods={visibleMethods}
                />
              )}
            />

            <Line
              data={lineData}
              type="monotone"
              dataKey="y"
              name="F(x)"
              stroke="#e6edf3"
              dot={false}
              strokeWidth={2}
              isAnimationActive={false}
            />

            {hasVisibleMarks && (
              <Customized
                component={(props: ChartMaps & { offset?: { top: number; left: number; width: number; height: number } }) => (
                  <IterationMarks
                    {...props}
                    marks={marks}
                    view={view}
                    hoveredId={hoveredId}
                    onHover={handleMarkHover}
                    showAllLabels={showLabels}
                    onMarkPointerDown={(e) => e.stopPropagation()}
                  />
                )}
              />
            )}

            <ReferenceLine x={intervalA} stroke="#5c9eff" strokeDasharray="4 4" />
            <ReferenceLine x={intervalB} stroke="#5c9eff" strokeDasharray="4 4" />
            {crossesZero && <ReferenceLine x={0} stroke="#e86a8a" strokeDasharray="6 3" />}

            <Brush
              data={lineData}
              dataKey="x"
              height={BRUSH_HEIGHT}
              travellerWidth={8}
              stroke="#5c9eff"
              fill="#161b22"
              tickFormatter={(v) => Number(v).toFixed(1)}
              onChange={(range) => {
                if (!range || range.startIndex == null || range.endIndex == null) return;
                const start = lineData[range.startIndex]?.x;
                const end = lineData[range.endIndex]?.x;
                if (start == null || end == null) return;
                clearMarkHover();
                setView((prev) =>
                  prev
                    ? clampView(
                        { ...prev, xMin: Math.min(start, end), xMax: Math.max(start, end) },
                        fullDomain,
                      )
                    : prev,
                );
              }}
            />
          </ComposedChart>
        </ResponsiveContainer>
        {hoveredMark && hoverAnchor && (
          <MarkHoverTooltip mark={hoveredMark} anchor={hoverAnchor} containerWidth={chartWidth} />
        )}
      </Box>

      <ChartLegend results={results} hasResults={hasResults} />

      <Typography variant="caption" color="text.secondary" display="block" mt={0.75}>
        {minimize ? 'MIN' : 'MAX'} · ЛКМ + перетаскивание — перемещение · колёсико над графиком — масштаб · ползунок — диапазон X
      </Typography>
    </Paper>
  );
}
