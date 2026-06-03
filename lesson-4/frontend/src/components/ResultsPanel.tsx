import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import {
  Alert,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { scrollbarSx } from '../theme';
import type { CandidatePoint, KuhnTuckerResult, Variant } from '../types';
import { KuhnTuckerPlot } from './KuhnTuckerPlot';

interface Props {
  variantId: number;
  variant: Variant | undefined;
  result: KuhnTuckerResult | null;
  plotReloadKey: number;
  solving: boolean;
}

function KktChip({ ok }: { ok: boolean }) {
  return ok ? (
    <Chip icon={<CheckCircleIcon />} label="ККТ" size="small" color="success" />
  ) : (
    <Chip icon={<CancelIcon />} label="ККТ" size="small" color="default" />
  );
}

export function ResultsPanel({ variantId, variant, result, plotReloadKey, solving }: Props) {
  const feasibleCandidates = result?.candidates.filter((c) => c.feasible) ?? [];
  const minimize = result?.minimize ?? true;
  const sorted = [...feasibleCandidates].sort((a, b) =>
    minimize ? a.objectiveValue - b.objectiveValue : b.objectiveValue - a.objectiveValue,
  );

  return (
    <Stack spacing={2}>
      <KuhnTuckerPlot
        variantId={variantId}
        variant={variant}
        result={result}
        reloadKey={plotReloadKey}
      />

      {result && (
        <>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Условия Джона–Куна–Таккера
            </Typography>
            <Stack spacing={1}>
              <Typography variant="body2">
                <strong>Функция Лагранжа:</strong> {result.kktSystem.lagrangian}
              </Typography>
              <Typography variant="body2">• Стационарность: {result.kktSystem.stationarity}</Typography>
              <Typography variant="body2">• Допустимость: {result.kktSystem.feasibility}</Typography>
              <Typography variant="body2">• Доп. нулевость: {result.kktSystem.complementarity}</Typography>
              {result.kktSystem.constraintForms.map((line) => (
                <Typography key={line} variant="caption" color="text.secondary" display="block">
                  g: {line}
                </Typography>
              ))}
            </Stack>
          </Paper>

          {result.optimalPoint && (
            <Alert severity="success" variant="outlined">
              <Typography variant="body2">
                <strong>Оптимальная точка:</strong> ({result.optimalPoint.x1.toFixed(4)};{' '}
                {result.optimalPoint.x2.toFixed(4)}),{' '}
                <strong>F*</strong> = {result.optimalValue?.toFixed(6)}
                {result.optimalKktSatisfied ? ' — условия ККТ выполнены' : ''}
              </Typography>
            </Alert>
          )}

          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Проверка экстремальных точек
            </Typography>
            <TableContainer sx={{ maxHeight: 280, ...scrollbarSx }}>
              <Table size="small" stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell>Точка (x₁; x₂)</TableCell>
                    <TableCell align="right">F(x)</TableCell>
                    <TableCell>Тип</TableCell>
                    <TableCell align="center">ККТ</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sorted.map((c, i) => (
                    <CandidateRow key={i} c={c} isOptimal={result.optimalPoint?.x1 === c.point.x1 && result.optimalPoint?.x2 === c.point.x2} />
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Выводы
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {result.conclusion}
            </Typography>
          </Paper>
        </>
      )}

      {!result && !solving && (
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">
            Выберите вариант и нажмите «Проверить ККТ» для аналитической и геометрической проверки
          </Typography>
        </Paper>
      )}
    </Stack>
  );
}

function CandidateRow({ c, isOptimal }: { c: CandidatePoint; isOptimal: boolean }) {
  return (
    <TableRow selected={isOptimal} sx={isOptimal ? { bgcolor: 'action.selected' } : undefined}>
      <TableCell>
        ({c.point.x1.toFixed(3)}; {c.point.x2.toFixed(3)})
      </TableCell>
      <TableCell align="right">{c.objectiveValue.toFixed(4)}</TableCell>
      <TableCell>
        <Typography variant="caption">{c.description}</Typography>
      </TableCell>
      <TableCell align="center">
        <KktChip ok={c.kktSatisfied} />
      </TableCell>
    </TableRow>
  );
}
