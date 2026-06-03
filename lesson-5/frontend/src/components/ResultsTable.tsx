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
import type { SolveResult } from '../types';

interface Props {
  result: SolveResult;
}

export function ResultsTable({ result }: Props) {
  const isPenalty = result.methodKind === 'PENALTY';
  const auxCol = isPenalty ? 'α(Xμ)' : 'B(Xμ)';
  const thetaCol = 'Θ(μ)';

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        Таблица итераций по μ
      </Typography>
      <TableContainer sx={{ maxHeight: 320 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>K</TableCell>
              <TableCell>μₖ</TableCell>
              <TableCell>Xₖ₊₁ = X(μₖ)</TableCell>
              <TableCell>F(Xₖ₊₁)</TableCell>
              <TableCell>{auxCol}</TableCell>
              <TableCell>{thetaCol}</TableCell>
              <TableCell>μₖ·{isPenalty ? 'α' : 'B'}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {result.steps.map((row) => (
              <TableRow key={row.k} hover>
                <TableCell>{row.k}</TableCell>
                <TableCell>{row.mu}</TableCell>
                <TableCell>
                  ({row.x[0].toFixed(4)}; {row.x[1].toFixed(4)})
                </TableCell>
                <TableCell>{row.f.toFixed(4)}</TableCell>
                <TableCell>{row.alphaOrB.toExponential(3)}</TableCell>
                <TableCell>{row.theta.toExponential(3)}</TableCell>
                <TableCell>{row.muTimesAux.toExponential(3)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
