import FunctionsIcon from '@mui/icons-material/Functions';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CompareArrowsIcon from '@mui/icons-material/CompareArrows';
import {
  Alert,
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
  onCompareGauss?: () => void;
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
    useOneDimensional: true,
    minimize: true,
  };
}

function getFunctionDef(variant: Variant, functionId: 'F1' | 'F2'): FunctionDefinition {
  return functionId === 'F2' ? variant.f2 : variant.f1;
}

export function OptimizationForm({
  variants,
  params,
  loading,
  onChange,
  onRun,
  onCompareGauss,
}: Props) {
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
      useOneDimensional: v.supportsOneDimensional,
    });
  };

  const handleFunctionChange = (functionId: 'F1' | 'F2') => {
    const f = getFunctionDef(variant, functionId);
    onChange({
      ...params,
      functionId,
      x0: [...f.initialPoints[0].coordinates],
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
              {v.title} — {v.methodLabel}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <Chip label={variant.methodLabel} size="small" color="primary" variant="outlined" sx={{ alignSelf: 'flex-start' }} />

      <FormControl fullWidth size="small">
        <InputLabel>Функция</InputLabel>
        <Select
          label="Функция"
          value={params.functionId}
          onChange={(e) => handleFunctionChange(e.target.value as 'F1' | 'F2')}
        >
          <MenuItem value="F1">F₁(x) — {variant.f1.formula}</MenuItem>
          <MenuItem value="F2">F₂(x) — {variant.f2.formula}</MenuItem>
        </Select>
      </FormControl>

      <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.5 }}>
        {fnDef.formula}
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

      <TextField
        size="small"
        label="Начальный шаг Δ"
        type="number"
        value={params.delta}
        onChange={(e) => onChange({ ...params, delta: Number(e.target.value) })}
        inputProps={{ min: 1e-4, step: 0.05 }}
        fullWidth
        helperText="Для дискретных методов и Гаусса–Зейделя без 1D-оптимизации"
      />

      {variant.supportsOneDimensional && (
        <FormControlLabel
          control={
            <Switch
              checked={params.useOneDimensional}
              disabled={variant.method.includes('DISCRETE') && !variant.method.includes('CONTINUOUS')}
              onChange={(e) => onChange({ ...params, useOneDimensional: e.target.checked })}
            />
          }
          label={
            <Typography variant="body2">
              Одномерная оптимизация (золотое сечение)
            </Typography>
          }
        />
      )}
      {variant.method.includes('GAUSS_SEIDEL') &&
        params.functionId === 'F1' &&
        (variant.id === 5 || variant.id === 16) && (
          <Alert severity="info" sx={{ py: 0.5 }}>
            F₁ — седловая функция (неограничена снизу). Метод может расходиться — это ожидаемо для задания.
            {params.useOneDimensional && (
              <>
                {' '}
                С золотым сечением шаг по направлению больше: на графике линий уровня часто видна только
                X₀, полный путь — на нижней панели.
              </>
            )}
          </Alert>
        )}

      <FormControlLabel
        control={
          <Switch
            checked={params.minimize}
            onChange={(e) => onChange({ ...params, minimize: e.target.checked })}
          />
        }
        label={<Typography variant="body2">Задача MIN</Typography>}
      />

      <Button
        variant="contained"
        startIcon={<PlayArrowIcon />}
        onClick={onRun}
        disabled={loading}
        fullWidth
      >
        Запустить метод
      </Button>

      {variant.supportsOneDimensional && onCompareGauss && (
        <Button
          variant="outlined"
          startIcon={<CompareArrowsIcon />}
          onClick={onCompareGauss}
          disabled={loading}
          fullWidth
        >
          Сравнить с/без 1D-оптимизации
        </Button>
      )}

      <Box sx={{ p: 1.5, bgcolor: 'background.default', borderRadius: 1, border: 1, borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" display="block">
          Метод: <strong>{variant.methodLabel}</strong>
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block" mt={0.5}>
          Размерность: {fnDef.dimension}
          {fnDef.plottable2d ? ' · график: линии уровня (2D)' : ' · график: поверхность и траектория (3D)'}
        </Typography>
      </Box>
    </Stack>
  );
}
