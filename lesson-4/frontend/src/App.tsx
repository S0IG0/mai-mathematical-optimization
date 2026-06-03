import BalanceIcon from '@mui/icons-material/Balance';
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
import { fetchVariants, solveVariant } from './api';
import { ResultsPanel } from './components/ResultsPanel';
import { SiteFooter } from './components/SiteFooter';
import { VariantForm } from './components/VariantForm';
import type { KuhnTuckerResult, Variant } from './types';

const LAB_TITLE =
  'Теория оптимального планирования и управления (условная оптимизация). Задание 4: Условия оптимальности Джона и Куна-Таккера';

export default function App() {
  const [variants, setVariants] = useState<Variant[]>([]);
  const [variantId, setVariantId] = useState(1);
  const [includeUnconstrained, setIncludeUnconstrained] = useState(true);
  const [result, setResult] = useState<KuhnTuckerResult | null>(null);
  const [plotReloadKey, setPlotReloadKey] = useState(0);
  const [solving, setSolving] = useState(false);
  const [bootLoading, setBootLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchVariants()
      .then((data) => {
        setVariants(data);
        if (data.length) setVariantId(data[0].id);
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки'))
      .finally(() => setBootLoading(false));
  }, []);

  const variant = variants.find((v) => v.id === variantId);

  useEffect(() => {
    if (variantId > 0) {
      setResult(null);
      setPlotReloadKey((k) => k + 1);
    }
  }, [variantId]);

  const handleSolve = async () => {
    setSolving(true);
    setError(null);
    try {
      const data = await solveVariant(variantId, includeUnconstrained);
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
          <BalanceIcon sx={{ mr: 1.5 }} />
          <Box>
            <Typography variant="h6" component="h1">
              Условия Куна–Таккера
            </Typography>
            <Typography variant="caption" color="text.secondary">
              18 вариантов · Plotly · аналитическая проверка ККТ
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
                includeUnconstrained={includeUnconstrained}
                loading={solving}
                onVariantChange={setVariantId}
                onUnconstrainedChange={setIncludeUnconstrained}
                onSolve={handleSolve}
              />
            </Paper>
          </Grid>

          <Grid item xs={12} md={9}>
            <ResultsPanel
              variantId={variantId}
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
