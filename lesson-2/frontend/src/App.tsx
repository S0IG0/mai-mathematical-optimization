import GridOnIcon from '@mui/icons-material/GridOn';
import {
  Alert,
  AppBar,
  Box,
  CircularProgress,
  Container,
  Grid,
  Paper,
  Toolbar,
  Typography,
} from '@mui/material';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { compareGaussModes, fetchContour, fetchSurface, fetchVariants } from './api';
import { normalizeContourData, normalizeSurfaceData } from './plot/grid';
import { buildDefaultParams, OptimizationForm } from './components/OptimizationForm';
import { ResultsPanel } from './components/ResultsPanel';
import { SiteFooter } from './components/SiteFooter';
import { useOptimizationStream } from './hooks/useOptimizationStream';
import type { ContourData, OptimizationParams, OptimizationResult, PathPoint, SurfaceData, Variant } from './types';

const LAB_TITLE =
  'Теория оптимального планирования и управления (безусловная оптимизация). Задание 2: Методы многомерной оптимизации';

export default function App() {
  const [variants, setVariants] = useState<Variant[]>([]);
  const [params, setParams] = useState<OptimizationParams | null>(null);
  const [results, setResults] = useState<OptimizationResult[]>([]);
  const [contour, setContour] = useState<ContourData | null>(null);
  const [surface, setSurface] = useState<SurfaceData | null>(null);
  const [plotLoading, setPlotLoading] = useState(false);
  const [livePath, setLivePath] = useState<PathPoint[]>([]);
  const [streaming, setStreaming] = useState(false);
  const [running, setRunning] = useState(false);
  const [bootLoading, setBootLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { run: runStream, cancel: cancelStream } = useOptimizationStream();

  useEffect(() => {
    fetchVariants()
      .then((data) => {
        setVariants(data);
        setParams(buildDefaultParams(data));
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки вариантов'))
      .finally(() => setBootLoading(false));
  }, []);

  useEffect(() => () => cancelStream(), [cancelStream]);

  const variant = variants.find((v) => v.id === params?.variantId);
  const fnDef = params && variant ? (params.functionId === 'F2' ? variant.f2 : variant.f1) : null;

  const plotMode = useMemo((): '2d' | '3d' | 'none' => {
    if (!fnDef) return 'none';
    if (fnDef.plottable2d) return '2d';
    if (fnDef.dimension === 3) return '3d';
    return 'none';
  }, [fnDef]);

  const loadPlotData = useCallback(
    async (p: OptimizationParams) => {
      const v = variants.find((x) => x.id === p.variantId);
      if (!v) return;
      const f = p.functionId === 'F2' ? v.f2 : v.f1;

      setPlotLoading(true);
      try {
        if (f.plottable2d) {
          setSurface(null);
          const data = await fetchContour(p.variantId, p.functionId, {
            plotXMin: f.plotXMin,
            plotXMax: f.plotXMax,
            plotYMin: f.plotYMin,
            plotYMax: f.plotYMax,
          });
          setContour(data);
        } else if (f.dimension === 3) {
          setContour(null);
          const x3 = p.x0[2] ?? 0;
          const data = await fetchSurface(p.variantId, p.functionId, x3, {
            plotXMin: f.plotXMin,
            plotXMax: f.plotXMax,
            plotYMin: f.plotYMin,
            plotYMax: f.plotYMax,
          });
          setSurface(data);
        } else {
          setContour(null);
          setSurface(null);
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Ошибка построения графика');
      } finally {
        setPlotLoading(false);
      }
    },
    [variants],
  );

  useEffect(() => {
    if (!params) return;
    let cancelled = false;
    loadPlotData(params).catch(() => {
      if (!cancelled) {
        setContour(null);
        setSurface(null);
      }
    });
    return () => {
      cancelled = true;
    };
  }, [params?.variantId, params?.functionId, params?.x0?.[2], loadPlotData]);

  const handleParamsChange = useCallback((next: OptimizationParams) => {
    setParams((prev) => {
      if (prev) {
        const changed =
          prev.variantId !== next.variantId ||
          prev.functionId !== next.functionId ||
          prev.x0.join() !== next.x0.join();
        if (changed) {
          setResults([]);
          setLivePath([]);
        }
      }
      return next;
    });
  }, []);

  const handleRun = async () => {
    if (!params) return;
    cancelStream();
    setRunning(true);
    setStreaming(true);
    setError(null);
    setResults([]);
    setLivePath([]);

    try {
      await runStream(params, {
        onContour: (data) => {
          if (!fnDef?.plottable2d) return;
          try {
            setContour(
              normalizeContourData(data, {
                plotXMin: fnDef.plotXMin,
                plotXMax: fnDef.plotXMax,
                plotYMin: fnDef.plotYMin,
                plotYMax: fnDef.plotYMax,
              }),
            );
          } catch {
            /* оставляем контур с REST, если WS-пакет неполный */
          }
        },
        onSurface: (data) => {
          if (fnDef?.dimension !== 3) return;
          try {
            setSurface(
              normalizeSurfaceData(data, {
                plotXMin: fnDef.plotXMin,
                plotXMax: fnDef.plotXMax,
                plotYMin: fnDef.plotYMin,
                plotYMax: fnDef.plotYMax,
              }),
            );
          } catch {
            /* оставляем поверхность с REST */
          }
        },
        onPathPoint: (_point, path) => setLivePath(path),
        onDone: (data) => {
          setResults([data]);
          setLivePath(data.path);
          if (data.diverged && data.statusMessage) {
            setError(data.statusMessage);
          }
        },
        onError: (msg) => setError(msg),
      });
      if (plotMode === '2d' && !contour) {
        await loadPlotData(params);
      }
      if (plotMode === '3d' && !surface) {
        await loadPlotData(params);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Неизвестная ошибка');
    } finally {
      setRunning(false);
      setStreaming(false);
    }
  };

  const handleCompareGauss = async () => {
    if (!params) return;
    cancelStream();
    setRunning(true);
    setStreaming(false);
    setError(null);
    setResults([]);
    setLivePath([]);
    try {
      const data = await compareGaussModes(params);
      setResults(data);
      setLivePath(data[0]?.path ?? []);
      await loadPlotData(params);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Неизвестная ошибка');
    } finally {
      setRunning(false);
    }
  };

  if (bootLoading || !params) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar>
          <GridOnIcon sx={{ mr: 1.5 }} />
          <Box>
            <Typography variant="h6" component="h1">
              Методы многомерной оптимизации
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Plotly · 2D контуры · 3D поверхность · WebSocket
            </Typography>
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ flex: 1, py: 3 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid item xs={12} md={3}>
            <Paper sx={{ p: 2, position: 'sticky', top: 16 }}>
              <OptimizationForm
                variants={variants}
                params={params}
                loading={running}
                onChange={handleParamsChange}
                onRun={handleRun}
                onCompareGauss={variant?.supportsOneDimensional ? handleCompareGauss : undefined}
              />
            </Paper>
          </Grid>

          <Grid item xs={12} md={9}>
            <ResultsPanel
              results={results}
              contour={contour}
              surface={surface}
              plotLoading={plotLoading}
              plotMode={plotMode}
              plotBounds={
                fnDef && (fnDef.plottable2d || fnDef.dimension === 3)
                  ? {
                      plotXMin: fnDef.plotXMin,
                      plotXMax: fnDef.plotXMax,
                      plotYMin: fnDef.plotYMin,
                      plotYMax: fnDef.plotYMax,
                    }
                  : null
              }
              functionId={params.functionId}
              livePath={livePath}
              streaming={streaming}
              running={running}
            />
          </Grid>
        </Grid>
      </Container>

      <SiteFooter labTitle={LAB_TITLE} />
    </Box>
  );
}
