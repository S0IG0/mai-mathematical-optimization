import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import {
  Alert,
  Box,
  Chip,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { PenaltyBarrierPlot } from './PenaltyBarrierPlot';
import { ResultsTable } from './ResultsTable';
import type { SolveResult, Variant } from '../types';

interface Props {
  variant: Variant | undefined;
  result: SolveResult | null;
  plotReloadKey: number;
  solving: boolean;
}

export function ResultsPanel({ variant, result, plotReloadKey, solving }: Props) {
  if (!variant) {
    return (
      <Paper sx={{ p: 4 }}>
        <Typography color="text.secondary">Выберите вариант</Typography>
      </Paper>
    );
  }

  return (
    <Stack spacing={2}>
      {solving && (
        <Alert severity="info">Выполняется оптимизация…</Alert>
      )}

      {!result && !solving && (
        <Paper sx={{ p: 3 }}>
          <Typography color="text.secondary">
            Нажмите «Запустить», чтобы построить таблицу итераций и график с траекторией.
          </Typography>
        </Paper>
      )}

      {result && (
        <>
          <Paper sx={{ p: 2 }}>
            <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
              <Typography variant="h6">Результат</Typography>
              <Chip
                icon={result.feasible ? <CheckCircleIcon /> : <CancelIcon />}
                label={result.feasible ? 'Допустимо' : 'Есть нарушения'}
                color={result.feasible ? 'success' : 'warning'}
                size="small"
              />
              <Chip label={`F* = ${result.optimalF.toFixed(4)}`} size="small" variant="outlined" />
              <Chip
                label={`X* = (${result.optimalX[0].toFixed(4)}; ${result.optimalX[1].toFixed(4)})`}
                size="small"
                variant="outlined"
              />
            </Stack>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1.5 }}>
              {result.conclusion}
            </Typography>
            {!result.feasible && result.constraintViolations?.length > 0 && (
              <Box component="ul" sx={{ mt: 1, mb: 0, pl: 2.5 }}>
                {result.constraintViolations.map((v) => (
                  <Typography key={v} component="li" variant="caption" color="warning.main">
                    {v}
                  </Typography>
                ))}
              </Box>
            )}
          </Paper>

          <ResultsTable result={result} />
        </>
      )}

      <PenaltyBarrierPlot variant={variant} result={result} reloadKey={plotReloadKey} />
    </Stack>
  );
}
