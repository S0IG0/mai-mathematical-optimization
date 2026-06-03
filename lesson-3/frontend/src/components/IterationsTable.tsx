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
import { scrollbarSx } from '../theme';
import type { OptimizationResult } from '../types';

function fmt(v: number | null | undefined, digits = 4): string {
  if (v == null || !Number.isFinite(v)) return '—';
  return v.toFixed(digits);
}

function fmtVec(v: number[] | null | undefined): string {
  if (!v?.length) return '—';
  return `(${v.map((x) => x.toFixed(4)).join('; ')})`;
}

interface Props {
  result: OptimizationResult;
}

export function IterationsTable({ result }: Props) {
  const rows: {
    k: number | string;
    xk: string;
    fXk: string;
    j: number | string;
    dj: string;
    yj: string;
    fYj: string;
    step: string;
    yNext: string;
    fNext: string;
  }[] = [];

  for (const iter of result.iterations) {
    if (iter.subSteps.length === 0) {
      rows.push({
        k: iter.k,
        xk: fmtVec(iter.xk),
        fXk: fmt(iter.fXk),
        j: '—',
        dj: '—',
        yj: '—',
        fYj: '—',
        step: '—',
        yNext: '—',
        fNext: '—',
      });
      continue;
    }
    iter.subSteps.forEach((sub, idx) => {
      const stepVal =
        sub.lambdaJ != null ? `λ=${fmt(sub.lambdaJ)}` : sub.deltaJ != null ? `Δ=${fmt(sub.deltaJ)}` : '—';
      rows.push({
        k: idx === 0 ? iter.k : '',
        xk: idx === 0 ? fmtVec(iter.xk) : '',
        fXk: idx === 0 ? fmt(iter.fXk) : '',
        j: sub.j,
        dj: fmtVec(sub.dj),
        yj: fmtVec(sub.yj),
        fYj: fmt(sub.fYj),
        step: stepVal,
        yNext: fmtVec(sub.yjPlus),
        fNext: fmt(sub.fYjPlus),
      });
    });
  }

  return (
    <Paper sx={{ overflow: 'hidden' }}>
      <Typography variant="subtitle2" sx={{ p: 2, pb: 1 }}>
        Таблица итераций — {result.methodLabel}
      </Typography>
      <TableContainer sx={{ maxHeight: 420, ...scrollbarSx }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>K</TableCell>
              <TableCell>Xk</TableCell>
              <TableCell>F(Xk)</TableCell>
              <TableCell>j</TableCell>
              <TableCell>dj</TableCell>
              <TableCell>yj</TableCell>
              <TableCell>F(yj)</TableCell>
              <TableCell>шаг</TableCell>
              <TableCell>yj+1</TableCell>
              <TableCell>F(yj+1)</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row, i) => (
              <TableRow key={i} hover>
                <TableCell>{row.k}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>{row.xk}</TableCell>
                <TableCell>{row.fXk}</TableCell>
                <TableCell>{row.j}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 11 }}>{row.dj}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 11 }}>{row.yj}</TableCell>
                <TableCell>{row.fYj}</TableCell>
                <TableCell>{row.step}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 11 }}>{row.yNext}</TableCell>
                <TableCell>{row.fNext}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
