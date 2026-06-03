import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import {
  Box,
  Button,
  Chip,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type { Variant } from '../types';

interface Props {
  variants: Variant[];
  variantId: number;
  experimentIndex: number;
  x0: number[];
  customMu: string;
  loading: boolean;
  onVariantChange: (id: number) => void;
  onExperimentChange: (idx: number) => void;
  onX0Change: (x0: number[]) => void;
  onCustomMuChange: (s: string) => void;
  onSolve: () => void;
}

export function VariantForm({
  variants,
  variantId,
  experimentIndex,
  x0,
  customMu,
  loading,
  onVariantChange,
  onExperimentChange,
  onX0Change,
  onCustomMuChange,
  onSolve,
}: Props) {
  const variant = variants.find((v) => v.id === variantId);

  return (
    <Stack spacing={2}>
      <Typography variant="subtitle1" fontWeight={600}>
        Параметры задачи
      </Typography>

      <FormControl fullWidth size="small">
        <InputLabel>Вариант</InputLabel>
        <Select
          label="Вариант"
          value={variantId}
          onChange={(e) => onVariantChange(Number(e.target.value))}
        >
          {variants.map((v) => (
            <MenuItem key={v.id} value={v.id}>
              {v.title} — {v.methodKind === 'PENALTY' ? 'штраф' : 'барьер'}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {variant && (
        <>
          <Chip
            label={variant.methodLabel}
            color={variant.methodKind === 'PENALTY' ? 'primary' : 'secondary'}
            size="small"
            sx={{ alignSelf: 'flex-start' }}
          />
          <Typography variant="body2" color="text.secondary">
            <strong>f(x):</strong> {variant.objectiveFormula}
          </Typography>
          {variant.inequalities.length > 0 && (
            <Typography variant="caption" color="text.secondary" component="div">
              <strong>Неравенства:</strong>
              <ul style={{ margin: '4px 0', paddingLeft: 18 }}>
                {variant.inequalities.map((g) => (
                  <li key={g}>{g}</li>
                ))}
              </ul>
            </Typography>
          )}
          {variant.equalities.length > 0 && (
            <Typography variant="caption" color="text.secondary" component="div">
              <strong>Равенства:</strong>
              <ul style={{ margin: '4px 0', paddingLeft: 18 }}>
                {variant.equalities.map((h) => (
                  <li key={h}>{h}</li>
                ))}
              </ul>
            </Typography>
          )}
          <Typography variant="caption" color="text.secondary">
            Оптимизатор: {variant.optimizerKind.replace(/_/g, ' ')}
          </Typography>
        </>
      )}

      <Divider />

      <FormControl fullWidth size="small">
        <InputLabel>Эксперимент (μ)</InputLabel>
        <Select
          label="Эксперимент (μ)"
          value={experimentIndex}
          onChange={(e) => onExperimentChange(Number(e.target.value))}
        >
          {variant?.experiments.map((ex, i) => (
            <MenuItem key={ex.label} value={i}>
              {ex.label}
            </MenuItem>
          ))}
          <MenuItem value={-1}>Свои значения μ</MenuItem>
        </Select>
      </FormControl>

      {experimentIndex === -1 && (
        <TextField
          label="μ через запятую"
          size="small"
          fullWidth
          value={customMu}
          onChange={(e) => onCustomMuChange(e.target.value)}
          placeholder="0.1, 1, 10, 100"
        />
      )}

      <FormControl fullWidth size="small">
        <InputLabel>Начальная точка</InputLabel>
        <Select
          label="Начальная точка"
          value={variant?.initialPoints.findIndex(
            (p) => p.coordinates[0] === x0[0] && p.coordinates[1] === x0[1],
          ) ?? 0}
          onChange={(e) => {
            const idx = Number(e.target.value);
            const pt = variant?.initialPoints[idx];
            if (pt) onX0Change([...pt.coordinates]);
          }}
        >
          {variant?.initialPoints.map((p, i) => (
            <MenuItem key={p.label} value={i}>
              {p.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <Box sx={{ display: 'flex', gap: 1 }}>
        <TextField
          label="x₁"
          type="number"
          size="small"
          value={x0[0]}
          onChange={(e) => onX0Change([Number(e.target.value), x0[1]])}
          sx={{ flex: 1 }}
        />
        <TextField
          label="x₂"
          type="number"
          size="small"
          value={x0[1]}
          onChange={(e) => onX0Change([x0[0], Number(e.target.value)])}
          sx={{ flex: 1 }}
        />
      </Box>

      <Button
        variant="contained"
        size="large"
        startIcon={<PlayArrowIcon />}
        onClick={onSolve}
        disabled={loading}
        fullWidth
      >
        {loading ? 'Расчёт…' : 'Запустить'}
      </Button>
    </Stack>
  );
}
