import GavelIcon from '@mui/icons-material/Gavel';
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
import { useEffect, useState } from 'react';
import { fetchVariants, solve } from './api';
import { ResultsPanel } from './components/ResultsPanel';
import { SiteFooter } from './components/SiteFooter';
import { VariantForm } from './components/VariantForm';
import type { SolveResult, Variant } from './types';

const LAB_TITLE =
  'Теория оптимального планирования и управления (безусловная оптимизация). Задание 5: Методы штрафных и барьерных функций';

export default function App() {
  const [variants, setVariants] = useState<Variant[]>([]);
  const [variantId, setVariantId] = useState(1);
  const [experimentIndex, setExperimentIndex] = useState(0);
  const [x0, setX0] = useState<number[]>([0, 0]);
  const [customMu, setCustomMu] = useState('0.1, 1, 100');
  const [result, setResult] = useState<SolveResult | null>(null);
  const [plotReloadKey, setPlotReloadKey] = useState(0);
  const [solving, setSolving] = useState(false);
  const [bootLoading, setBootLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchVariants()
      .then((data) => {
        setVariants(data);
        if (data.length) {
          setVariantId(data[0].id);
          const pt = data[0].initialPoints[0];
          if (pt) setX0([...pt.coordinates]);
        }
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки'))
      .finally(() => setBootLoading(false));
  }, []);

  const variant = variants.find((v) => v.id === variantId);

  useEffect(() => {
    if (variant) {
      const pt = variant.initialPoints[0];
      if (pt) setX0([...pt.coordinates]);
      setExperimentIndex(0);
      setResult(null);
      setPlotReloadKey((k) => k + 1);
    }
  }, [variantId]);

  const handleSolve = async () => {
    if (!variant) return;
    setSolving(true);
    setError(null);
    try {
      let muValues: number[] | undefined;
      let schedule = 'INCREASING';
      let domainMode = 'ALL_CONSTRAINTS';
      let expIdx = experimentIndex;

      if (experimentIndex === -1) {
        muValues = customMu
          .split(/[,;\s]+/)
          .map((s) => parseFloat(s.trim()))
          .filter((n) => Number.isFinite(n));
        if (!muValues.length) throw new Error('Укажите корректные значения μ');
      } else {
        const exp = variant.experiments[experimentIndex];
        if (exp) {
          muValues = [...exp.muValues];
          schedule = exp.schedule;
          domainMode = exp.domainMode;
        }
      }

      const data = await solve({
        variantId,
        x0,
        muValues,
        schedule,
        domainMode,
        experimentIndex: expIdx,
      });
      setResult(data);
      setPlotReloadKey((k) => k + 1);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка расчёта');
    } finally {
      setSolving(false);
    }
  };

  if (bootLoading) {
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
          <GavelIcon sx={{ mr: 1.5 }} />
          <Box>
            <Typography variant="h6" component="h1">
              Штрафные и барьерные функции
            </Typography>
            <Typography variant="caption" color="text.secondary">
              18 вариантов · Plotly · Spring Boot + React
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
              <VariantForm
                variants={variants}
                variantId={variantId}
                experimentIndex={experimentIndex}
                x0={x0}
                customMu={customMu}
                loading={solving}
                onVariantChange={setVariantId}
                onExperimentChange={setExperimentIndex}
                onX0Change={setX0}
                onCustomMuChange={setCustomMu}
                onSolve={handleSolve}
              />
            </Paper>
          </Grid>

          <Grid item xs={12} md={9}>
            <ResultsPanel
              variant={variant}
              result={result}
              plotReloadKey={plotReloadKey}
              solving={solving}
            />
          </Grid>
        </Grid>
      </Container>

      <SiteFooter labTitle={LAB_TITLE} />
    </Box>
  );
}
