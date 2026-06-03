import {
  Alert,
  Chip,
  Grid,
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import type { PlotBounds } from '../plot/grid';
import type { ContourData, OptimizationResult, PathPoint, SurfaceData } from '../types';
import { OptimizationPlot } from './OptimizationPlot';
import { IterationsTable } from './IterationsTable';

interface Props {
  results: OptimizationResult[];
  contour: ContourData | null;
  surface: SurfaceData | null;
  plotLoading: boolean;
  plotMode: '2d' | '3d' | 'none';
  plotBounds?: PlotBounds | null;
  functionId?: 'F1' | 'F2';
  livePath: PathPoint[];
  streaming: boolean;
  running: boolean;
}

function SummaryCard({ result }: { result: OptimizationResult }) {
  return (
    <Paper sx={{ p: 2, borderColor: result.diverged ? 'warning.main' : undefined, border: result.diverged ? 1 : 0 }}>
      <Typography variant="subtitle2" gutterBottom color={result.diverged ? 'warning.main' : 'primary'}>
        {result.methodLabel}
      </Typography>
      {result.diverged && result.statusMessage && (
        <Alert severity="warning" sx={{ mb: 1.5, py: 0 }}>
          {result.statusMessage}
        </Alert>
      )}
      <Stack spacing={0.75}>
        <Typography variant="body2">
          <strong>X*</strong> = ({result.optimalX.map((x) => x.toFixed(6)).join('; ')})
        </Typography>
        <Typography variant="body2">
          <strong>F(X*)</strong> = {result.optimalF.toFixed(6)}
        </Typography>
        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
          <Chip size="small" label={`Итераций: ${result.iterationsCount}`} />
          <Chip size="small" label={`Вычислений F: ${result.functionEvaluations}`} variant="outlined" />
        </Stack>
      </Stack>
    </Paper>
  );
}

function LiveSummary({ path, streaming }: { path: PathPoint[]; streaming: boolean }) {
  if (path.length === 0) return null;
  const last = path[path.length - 1];
  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle2" gutterBottom color="secondary">
        {streaming ? 'Текущая точка (расчёт…)' : 'Промежуточный результат'}
      </Typography>
      <Typography variant="body2">
        X = ({last.x.map((x) => x.toFixed(6)).join('; ')})
      </Typography>
      <Typography variant="body2">
        F(X) = {last.f.toFixed(6)}
      </Typography>
      <Chip size="small" label={`Точек траектории: ${path.length}`} sx={{ mt: 1 }} />
    </Paper>
  );
}

export function ResultsPanel({
  results,
  contour,
  surface,
  plotLoading,
  plotMode,
  plotBounds,
  functionId,
  livePath,
  streaming,
  running,
}: Props) {
  const rawPath = livePath.length > 0 ? livePath : results[0]?.path ?? [];
  const showPlaceholder = results.length === 0 && !running && livePath.length === 0;

  if (showPlaceholder) {
    return (
      <Paper sx={{ p: 6, textAlign: 'center' }}>
        <Typography color="text.secondary">
          Задайте параметры слева и нажмите «Запустить метод» — траектория появится на графике в реальном времени
        </Typography>
      </Paper>
    );
  }

  const primary = results[0];

  return (
    <Stack spacing={3}>
      {running && <LinearProgress color="secondary" sx={{ borderRadius: 1 }} />}

      <Grid container spacing={2}>
        {primary ? (
          results.map((r) => (
            <Grid item xs={12} md={results.length > 1 ? 6 : 12} key={r.methodLabel}>
              <SummaryCard result={r} />
            </Grid>
          ))
        ) : (
          <Grid item xs={12}>
            <LiveSummary path={livePath} streaming={streaming} />
          </Grid>
        )}
      </Grid>

      {plotMode !== 'none' && (
        <OptimizationPlot
          mode={plotMode}
          contour={contour}
          surface={surface}
          path={rawPath}
          plotBounds={plotBounds}
          functionId={functionId}
          diverged={primary?.diverged}
          loading={plotLoading}
          streaming={streaming}
        />
      )}

      {primary &&
        results.map((r) => (
          <IterationsTable key={r.methodLabel} result={r} />
        ))}

      {primary && (
        <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
          <Typography variant="subtitle2" gutterBottom>
            Выводы
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {primary.diverged ? (
              <>
                Метод <strong>{primary.methodLabel}</strong> не даёт минимум (расходимость / седловая функция): за{' '}
                {primary.iterationsCount} итераций получена точка с F = {primary.optimalF.toFixed(6)} — это не
                оптимум, а остановка по лимиту шагов.
              </>
            ) : (
              <>
                Метод <strong>{primary.methodLabel}</strong> сошёлся за {primary.iterationsCount} итераций к точке с
                F = {primary.optimalF.toFixed(6)}.
              </>
            )}{' '}
            Сравните результаты при разных X₀ и ε = 0.1, 0.01, 0.001.
            {' '}
            Сравните траектории при разных начальных точках и методах из задания (F₁ / F₂).
          </Typography>
        </Paper>
      )}
    </Stack>
  );
}
