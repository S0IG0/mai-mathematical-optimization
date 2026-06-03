import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import type { OptimizationResult } from '../types';
import { scrollbarSx } from '../theme';

interface Props {
  result: OptimizationResult;
}

function fmt(v: number | null | undefined, digits = 6) {
  if (v == null || Number.isNaN(v)) return '—';
  if (Math.abs(v) > 1e8) return '∞';
  return v.toFixed(digits);
}

function readF(row: Record<string, unknown>, key: 'fLambda' | 'fMu'): number | null | undefined {
  const v = row[key] ?? row[key === 'fLambda' ? 'FLambda' : 'FMu'];
  return typeof v === 'number' ? v : null;
}

export function IterationsTable({ result }: Props) {
  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        Итерации — {result.methodLabel}
      </Typography>
      <TableContainer
        sx={{
          maxHeight: 360,
          borderRadius: 1,
          border: 1,
          borderColor: 'divider',
          bgcolor: 'background.default',
          ...scrollbarSx,
        }}
      >
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>k</TableCell>
              <TableCell>aₖ</TableCell>
              <TableCell>bₖ</TableCell>
              <TableCell>λₖ</TableCell>
              <TableCell>μₖ</TableCell>
              <TableCell>F(λₖ)</TableCell>
              <TableCell>F(μₖ)</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {result.iterations.map((row) => (
              <TableRow key={row.k} hover>
                <TableCell>{row.k}</TableCell>
                <TableCell>{fmt(row.a)}</TableCell>
                <TableCell>{fmt(row.b)}</TableCell>
                <TableCell>{fmt(row.lambda)}</TableCell>
                <TableCell>{fmt(row.mu)}</TableCell>
                <TableCell>{fmt(readF(row as unknown as Record<string, unknown>, 'fLambda'))}</TableCell>
                <TableCell>{fmt(readF(row as unknown as Record<string, unknown>, 'fMu'))}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
