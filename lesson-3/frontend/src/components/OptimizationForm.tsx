import FunctionsIcon from '@mui/icons-material/Functions';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
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
import type { FunctionDefinition, OptimizationParams, Variant } from '../types';

interface Props {
  variants: Variant[];
  params: OptimizationParams;
  loading: boolean;
  onChange: (p: OptimizationParams) => void;
  onRun: () => void;
}

export function buildDefaultParams(variants: Variant[]): OptimizationParams {
  const v = variants[0];
  const fn = v.f1;
  return {
    variantId: v.id,
    functionId: 'F1',
    x0: [...fn.initialPoints[0].coordinates],
    epsilon: 0.01,
    delta: 0.1,
    minimize: fn.minimize,
  };
}

function getFunctionDef(variant: Variant, functionId: 'F1' | 'F2'): FunctionDefinition {
  return functionId === 'F2' ? variant.f2 : variant.f1;
}

export function OptimizationForm({ variants, params, loading, onChange, onRun }: Props) {
  const variant = variants.find((v) => v.id === params.variantId) ?? variants[0];
  const fnDef = getFunctionDef(variant, params.functionId);

  const handleVariantChange = (variantId: number) => {
    const v = variants.find((x) => x.id === variantId)!;
    const f = v.f1;
    onChange({
      ...params,
      variantId,
      functionId: 'F1',
      x0: [...f.initialPoints[0].coordinates],
      minimize: f.minimize,
    });
  };

  const handleFunctionChange = (functionId: 'F1' | 'F2') => {
    const f = getFunctionDef(variant, functionId);
    onChange({
      ...params,
      functionId,
      x0: [...f.initialPoints[0].coordinates],
      minimize: f.minimize,
    });
  };

  const handlePreset = (coords: number[]) => {
    onChange({ ...params, x0: [...coords] });
  };

  const handleCoordChange = (index: number, value: string) => {
    const next = [...params.x0];
    next[index] = Number(value);
    onChange({ ...params, x0: next });
  };

  const needsDelta =
    fnDef.method === 'GRADIENT_FIRST_ORDER' || fnDef.method === 'RAVINE';

  return (
    <Stack spacing={2}>
      <Stack direction="row" alignItems="center" spacing={1}>
        <FunctionsIcon color="primary" fontSize="small" />
        <Typography variant="subtitle1" fontWeight={600}>
          Параметры
        </Typography>
      </Stack>

      <FormControl fullWidth size="small">
        <InputLabel>Вариант задания</InputLabel>
        <Select
          label="Вариант задания"
          value={params.variantId}
          onChange={(e) => handleVariantChange(Number(e.target.value))}
        >
          {variants.map((v) => (
            <MenuItem key={v.id} value={v.id}>
              {v.title}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <FormControl fullWidth size="small">
        <InputLabel>Задача (функция)</InputLabel>
        <Select
          label="Задача (функция)"
          value={params.functionId}
          onChange={(e) => handleFunctionChange(e.target.value as 'F1' | 'F2')}
        >
          <MenuItem value="F1">
            F₁ — {variant.f1.methodLabel} ({variant.f1.minimize ? 'MIN' : 'MAX'})
          </MenuItem>
          <MenuItem value="F2">
            F₂ — {variant.f2.methodLabel} ({variant.f2.minimize ? 'MIN' : 'MAX'})
          </MenuItem>
        </Select>
      </FormControl>

      <Chip
        icon={<TrendingDownIcon />}
        label={fnDef.methodLabel}
        size="small"
        color="primary"
        variant="outlined"
        sx={{ alignSelf: 'flex-start' }}
      />

      <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.5 }}>
        {fnDef.minimize ? 'MIN' : 'MAX'}: {fnDef.formula}
      </Typography>

      <Divider />

      <Typography variant="body2" fontWeight={600}>
        Начальная точка X₀
      </Typography>
      <Stack direction="row" flexWrap="wrap" gap={0.5}>
        {fnDef.initialPoints.map((p) => (
          <Chip
            key={p.label}
            label={p.label}
            size="small"
            variant="outlined"
            onClick={() => handlePreset(p.coordinates)}
            sx={{ cursor: 'pointer' }}
          />
        ))}
      </Stack>
      {params.x0.map((val, i) => (
        <TextField
          key={i}
          size="small"
          label={`x${i + 1}`}
          type="number"
          value={val}
          onChange={(e) => handleCoordChange(i, e.target.value)}
          fullWidth
        />
      ))}

      <Divider />

      <Typography variant="body2" fontWeight={600}>
        Точность ε
      </Typography>
      <Stack direction="row" gap={0.5}>
        {[0.1, 0.01, 0.001].map((eps) => (
          <Chip
            key={eps}
            label={`ε = ${eps}`}
            size="small"
            color={params.epsilon === eps ? 'primary' : 'default'}
            onClick={() => onChange({ ...params, epsilon: eps })}
            sx={{ cursor: 'pointer' }}
          />
        ))}
      </Stack>
      <TextField
        size="small"
        label="ε"
        type="number"
        value={params.epsilon}
        onChange={(e) => onChange({ ...params, epsilon: Number(e.target.value) })}
        inputProps={{ min: 1e-6, step: 0.001 }}
        fullWidth
      />

      {needsDelta && (
        <TextField
          size="small"
          label="Шаг Δ"
          type="number"
          value={params.delta}
          onChange={(e) => onChange({ ...params, delta: Number(e.target.value) })}
          inputProps={{ min: 1e-4, step: 0.05 }}
          fullWidth
          helperText="Фиксированный шаг для метода 1-го порядка и овражного метода"
        />
      )}

      <Box
        sx={{
          p: 1.5,
          bgcolor: 'background.default',
          borderRadius: 1,
          border: 1,
          borderColor: 'divider',
        }}
      >
        <Typography variant="caption" color="text.secondary" display="block">
          Метод: <strong>{fnDef.methodLabel}</strong>
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block" mt={0.5}>
          Задача: <strong>{fnDef.minimize ? 'минимизация' : 'максимизация'}</strong> · размерность:{' '}
          {fnDef.dimension}
          {fnDef.plottable2d ? ' · график: линии уровня' : ' · график: 3D-проекция'}
        </Typography>
      </Box>

      <Button variant="contained" startIcon={<PlayArrowIcon />} onClick={onRun} disabled={loading} fullWidth>
        Запустить метод
      </Button>
    </Stack>
  );
}
