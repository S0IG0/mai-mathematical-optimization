import { Box, Chip, Grid, Paper, Typography } from '@mui/material';
import type { OptimizationMethod, OptimizationResult, PlotData } from '../types';
import { ConclusionsPanel } from './ConclusionsPanel';
import { FunctionChart } from './FunctionChart';
import { IterationsTable } from './IterationsTable';

const METHOD_COLORS: Record<OptimizationMethod, string> = {
  GOLDEN_SECTION: '#5c9eff',
  FIBONACCI: '#7ee8a2',
  DICHOTOMY: '#f7b955',
};

interface Props {
  results: OptimizationResult[];
  plotData: PlotData | null;
  minimize: boolean;
  intervalA: number;
  intervalB: number;
  plotLoading?: boolean;
}

export function ResultsPanel({ results, plotData, minimize, intervalA, intervalB, plotLoading }: Props) {
  const hasResults = results.length > 0;
  const primary = hasResults ? results[0] : null;

  return (
    <Box>
      {hasResults && primary && (
        <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
          {results.map((r) => (
            <Chip
              key={r.method}
              label={r.methodLabel}
              variant="outlined"
              sx={{
                borderColor: METHOD_COLORS[r.method],
                color: METHOD_COLORS[r.method],
              }}
            />
          ))}
          <Chip label={`x* = ${primary.optimalX.toFixed(6)}`} size="small" color="primary" />
          <Chip label={`F(x*) = ${primary.optimalF.toFixed(6)}`} size="small" color="secondary" />
          <Chip label={`N(F) = ${primary.functionEvaluations}`} size="small" />
        </Box>
      )}

      {!hasResults && (
        <Paper sx={{ p: 2, mb: 2, bgcolor: 'action.hover' }}>
          <Typography variant="body2" color="text.secondary">
            {plotLoading
              ? 'Обновление графика…'
              : 'График функции на выбранном интервале. Нажмите «Рассчитать» или «Сравнить методы» для поиска экстремума.'}
          </Typography>
        </Paper>
      )}

      <Grid container spacing={2}>
        <Grid item xs={12}>
          <FunctionChart
            plotData={plotData}
            results={results}
            minimize={minimize}
            intervalA={intervalA}
            intervalB={intervalB}
          />
        </Grid>

        {hasResults && (
          <>
            <Grid item xs={12}>
              <ConclusionsPanel results={results} />
            </Grid>

            {results.map((result) => (
              <Grid item xs={12} lg={results.length === 1 ? 12 : 6} key={result.method}>
                <IterationsTable result={result} />
              </Grid>
            ))}
          </>
        )}
      </Grid>
    </Box>
  );
}
