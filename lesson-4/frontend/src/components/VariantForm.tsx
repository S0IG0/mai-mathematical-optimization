import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FunctionsIcon from '@mui/icons-material/Functions';
import {
  Box,
  Button,
  Chip,
  Divider,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  Typography,
} from '@mui/material';
import type { Variant } from '../types';

interface Props {
  variants: Variant[];
  variantId: number;
  includeUnconstrained: boolean;
  loading: boolean;
  onVariantChange: (id: number) => void;
  onUnconstrainedChange: (v: boolean) => void;
  onSolve: () => void;
}

export function VariantForm({
  variants,
  variantId,
  includeUnconstrained,
  loading,
  onVariantChange,
  onUnconstrainedChange,
  onSolve,
}: Props) {
  const variant = variants.find((v) => v.id === variantId);

  return (
    <Stack spacing={2}>
      <Stack direction="row" alignItems="center" spacing={1}>
        <FunctionsIcon color="primary" fontSize="small" />
        <Typography variant="subtitle1">Параметры задачи</Typography>
      </Stack>

      <FormControl fullWidth size="small">
        <InputLabel>Вариант</InputLabel>
        <Select
          label="Вариант"
          value={variantId}
          onChange={(e) => onVariantChange(Number(e.target.value))}
        >
          {variants.map((v) => (
            <MenuItem key={v.id} value={v.id}>
              {v.title}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {variant && (
        <>
          <Box>
            <Typography variant="caption" color="text.secondary">
              Целевая функция
            </Typography>
            <Typography variant="body2" sx={{ mt: 0.5, fontFamily: 'monospace', fontSize: '0.8rem' }}>
              {variant.objectiveFormula}
            </Typography>
            <Chip
              label={variant.minimize ? 'минимизация' : 'максимизация'}
              size="small"
              color={variant.minimize ? 'primary' : 'secondary'}
              sx={{ mt: 1 }}
            />
          </Box>

          <Divider />

          <Typography variant="caption" color="text.secondary">
            Ограничения (стандартная форма gᵢ(x) ≤ 0)
          </Typography>
          <Stack spacing={0.5}>
            {variant.constraints.map((c) => (
              <Typography key={c.id} variant="body2" sx={{ fontSize: '0.78rem' }}>
                • {c.label}
              </Typography>
            ))}
          </Stack>

          {variant.variant1Extended && (
            <FormControlLabel
              control={
                <Switch
                  checked={includeUnconstrained}
                  onChange={(e) => onUnconstrainedChange(e.target.checked)}
                />
              }
              label="Вариант 1: безусловная часть + спуск от (0,0)"
            />
          )}

          {variant.taskDescription && (
            <Typography variant="caption" color="text.secondary">
              {variant.taskDescription}
            </Typography>
          )}
        </>
      )}

      <Button
        variant="contained"
        size="large"
        startIcon={<PlayArrowIcon />}
        onClick={onSolve}
        disabled={loading}
        fullWidth
      >
        {loading ? 'Расчёт…' : 'Проверить ККТ и найти оптимум'}
      </Button>
    </Stack>
  );
}
