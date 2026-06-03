import { Alert, Grid, Paper, Typography } from '@mui/material';
import type { OptimizationResult } from '../types';

interface Props {
  results: OptimizationResult[];
}

function fmt(v: number) {
  return Number.isFinite(v) ? v.toFixed(6) : '—';
}

export function ConclusionsPanel({ results }: Props) {
  if (results.length === 0) return null;

  const bestEval = Math.min(...results.map((r) => r.functionEvaluations));
  const bestIter = Math.min(...results.map((r) => r.iterationsCount));
  const fastestByEval = results.filter((r) => r.functionEvaluations === bestEval);
  const fastestByIter = results.filter((r) => r.iterationsCount === bestIter);

  const xSpread =
    results.length > 1
      ? Math.max(...results.map((r) => r.optimalX)) - Math.min(...results.map((r) => r.optimalX))
      : 0;

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        Выводы по результатам исследований
      </Typography>
      <Grid container spacing={2}>
        {results.map((r) => (
          <Grid item xs={12} md={results.length === 1 ? 12 : 4} key={r.method}>
            <Alert severity="info" variant="outlined" sx={{ height: '100%' }}>
              <Typography variant="body2" fontWeight={600} gutterBottom>
                {r.methodLabel}
              </Typography>
              <Typography variant="body2">x* = {fmt(r.optimalX)}</Typography>
              <Typography variant="body2">F(x*) = {fmt(r.optimalF)}</Typography>
              <Typography variant="body2">Вычислений F: {r.functionEvaluations}</Typography>
              <Typography variant="body2">Итераций: {r.iterationsCount}</Typography>
              <Typography variant="body2">
                Конечный интервал: [{fmt(r.finalA)}; {fmt(r.finalB)}]
              </Typography>
            </Alert>
          </Grid>
        ))}
      </Grid>

      {results.length > 1 && (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          Метод{fastestByEval.length > 1 ? 'ы' : ''} с минимальным числом вычислений F(x):{' '}
          <strong>{fastestByEval.map((r) => r.methodLabel).join(', ')}</strong> ({bestEval} вычислений).
          Наименьшее число итераций у{' '}
          <strong>{fastestByIter.map((r) => r.methodLabel).join(', ')}</strong> ({bestIter} ит.).
          {xSpread < 0.01
            ? ' Все методы сошлись к одной точке с высокой точностью.'
            : ` Разброс x* между методами: ${xSpread.toFixed(6)}.`}{' '}
          Метод золотого сечения и Фибоначчи обычно экономнее дихотомии за счёт переиспользования одного вычисления
          на итерации; дихотомия проще, но требует двух вычислений F(x) на каждом шаге.
        </Typography>
      )}
    </Paper>
  );
}
