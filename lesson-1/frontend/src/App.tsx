import ShowChartIcon from '@mui/icons-material/ShowChart';
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
import { useCallback, useEffect, useState } from 'react';
import { fetchPlotData, fetchVariants, runAllMethods, runOptimization } from './api';
import { buildDefaultParams, OptimizationForm } from './components/OptimizationForm';
import { ResultsPanel } from './components/ResultsPanel';
import { SiteFooter } from './components/SiteFooter';
import type { OptimizationParams, OptimizationResult, PlotData, Variant } from './types';

const LAB_TITLE =
  'Теория оптимального планирования и управления (безусловная оптимизация). Задание 1: Методы одномерной оптимизации';

export default function App() {
  const [variants, setVariants] = useState<Variant[]>([]);
  const [params, setParams] = useState<OptimizationParams | null>(null);
  const [results, setResults] = useState<OptimizationResult[]>([]);
  const [plotData, setPlotData] = useState<PlotData | null>(null);
  const [plotLoading, setPlotLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [bootLoading, setBootLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchVariants()
      .then((data) => {
        setVariants(data);
        setParams(buildDefaultParams(data));
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки вариантов'))
      .finally(() => setBootLoading(false));
  }, []);

  const loadPlot = useCallback(async (p: OptimizationParams) => {
    const margin = Math.max(0.25, (p.b - p.a) * 0.08);
    const from = p.a - margin;
    const to = p.b + margin;
    const data = await fetchPlotData(p.variantId, p.functionId, from, to);
    setPlotData(data);
  }, []);

  const handleParamsChange = useCallback((next: OptimizationParams) => {
    setParams((prev) => {
      if (prev) {
        const problemChanged =
          prev.variantId !== next.variantId ||
          prev.functionId !== next.functionId ||
          prev.a !== next.a ||
          prev.b !== next.b ||
          prev.minimize !== next.minimize;
        if (problemChanged) {
          setResults([]);
        }
      }
      return next;
    });
  }, []);

  useEffect(() => {
    if (!params) return;

    let cancelled = false;
    setPlotLoading(true);

    loadPlot(params)
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : 'Ошибка построения графика');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setPlotLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [params?.variantId, params?.functionId, params?.a, params?.b, params?.minimize, loadPlot]);

  const handleRun = async () => {
    if (!params) return;
    setLoading(true);
    setError(null);
    try {
      const data = await runOptimization(params);
      setResults([data]);
      await loadPlot(params);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Неизвестная ошибка');
    } finally {
      setLoading(false);
    }
  };

  const handleCompareAll = async () => {
    if (!params) return;
    setLoading(true);
    setError(null);
    try {
      const data = await runAllMethods(params);
      setResults(data);
      await loadPlot(params);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Неизвестная ошибка');
    } finally {
      setLoading(false);
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
          <ShowChartIcon sx={{ mr: 1.5 }} />
          <Box>
            <Typography variant="h6" component="h1">
              Методы одномерной оптимизации
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Дихотомия · Золотое сечение · Фибоначчи · 18 вариантов задания
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
                loading={loading}
                onChange={handleParamsChange}
                onRun={handleRun}
                onCompareAll={handleCompareAll}
              />
            </Paper>
          </Grid>

          <Grid item xs={12} md={9}>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 12 }}>
                <CircularProgress />
              </Box>
            ) : (
              <ResultsPanel
                results={results}
                plotData={plotData}
                minimize={params.minimize}
                intervalA={params.a}
                intervalB={params.b}
                plotLoading={plotLoading}
              />
            )}
          </Grid>
        </Grid>
      </Container>

      <SiteFooter labTitle={LAB_TITLE} />
    </Box>
  );
}
