import FunctionsIcon from '@mui/icons-material/Functions';
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
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import type { FunctionDefinition, OptimizationMethod, OptimizationParams, Variant } from '../types';

interface Props {
  variants: Variant[];
  params: OptimizationParams;
  loading: boolean;
  onChange: (params: OptimizationParams) => void;
  onRun: () => void;
  onCompareAll: () => void;
}

function NumberField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <TextField
      label={label}
      type="number"
      size="small"
      fullWidth
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      inputProps={{ step: 'any' }}
    />
  );
}

function getFunctionDef(variant: Variant | undefined, functionId: 'F1' | 'F2'): FunctionDefinition | undefined {
  if (!variant) return undefined;
  return functionId === 'F1' ? variant.f1 : variant.f2;
}

export function OptimizationForm({ variants, params, loading, onChange, onRun, onCompareAll }: Props) {
  const variant = variants.find((v) => v.id === params.variantId);
  const func = getFunctionDef(variant, params.functionId);

  const patch = (part: Partial<OptimizationParams>) => onChange({ ...params, ...part });

  const applyPreset = (index: number) => {
    const preset = func?.presets[index];
    if (!preset) return;
    patch({ a: preset.a, b: preset.b, minimize: preset.minimize });
  };

  return (
    <Box>
      <Stack direction="row" spacing={1} alignItems="center" mb={2}>
        <FunctionsIcon color="primary" />
        <Typography variant="h6">Параметры расчёта</Typography>
      </Stack>

      <Stack spacing={2}>
        <FormControl fullWidth size="small">
          <InputLabel>Вариант задания</InputLabel>
          <Select
            label="Вариант задания"
            value={params.variantId}
            onChange={(e) => {
              const variantId = Number(e.target.value);
              const nextVariant = variants.find((v) => v.id === variantId);
              const nextFunc = nextVariant?.f1;
              const preset = nextFunc?.presets[0];
              patch({
                variantId,
                functionId: 'F1',
                a: preset?.a ?? -1,
                b: preset?.b ?? 1,
                minimize: preset?.minimize ?? nextFunc?.defaultMinimize ?? true,
              });
            }}
          >
            {variants.map((v) => (
              <MenuItem key={v.id} value={v.id}>
                {v.title}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth size="small">
          <InputLabel>Целевая функция</InputLabel>
          <Select
            label="Целевая функция"
            value={params.functionId}
            onChange={(e) => {
              const functionId = e.target.value as 'F1' | 'F2';
              const nextFunc = getFunctionDef(variant, functionId);
              const preset = nextFunc?.presets[0];
              patch({
                functionId,
                a: preset?.a ?? params.a,
                b: preset?.b ?? params.b,
                minimize: preset?.minimize ?? nextFunc?.defaultMinimize ?? true,
              });
            }}
          >
            <MenuItem value="F1">F₁(x): {variant?.f1.formula}</MenuItem>
            <MenuItem value="F2">F₂(x): {variant?.f2.formula}</MenuItem>
          </Select>
        </FormControl>

        {func && (
          <Box>
            <Typography variant="caption" color="text.secondary" display="block" gutterBottom>
              Область определения: [{func.domainFrom}; {func.domainTo}]
            </Typography>
            <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
              {func.presets.map((preset, i) => (
                <Chip
                  key={preset.label}
                  label={preset.label}
                  size="small"
                  variant="outlined"
                  clickable
                  onClick={() => applyPreset(i)}
                  color={
                    params.a === preset.a && params.b === preset.b && params.minimize === preset.minimize
                      ? 'primary'
                      : 'default'
                  }
                />
              ))}
            </Stack>
          </Box>
        )}

        <ToggleButtonGroup
          exclusive
          fullWidth
          size="small"
          value={params.minimize ? 'min' : 'max'}
          onChange={(_, v) => v && patch({ minimize: v === 'min' })}
        >
          <ToggleButton value="min">MIN</ToggleButton>
          <ToggleButton value="max">MAX</ToggleButton>
        </ToggleButtonGroup>

        <Divider />

        <Typography variant="subtitle2" color="text.secondary">
          Интервал неопределённости [a; b]
        </Typography>
        <NumberField label="a" value={params.a} onChange={(a) => patch({ a })} />
        <NumberField label="b" value={params.b} onChange={(b) => patch({ b })} />

        <Typography variant="subtitle2" color="text.secondary">
          Точность
        </Typography>
        <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mb: 1 }}>
          {[0.1, 0.01, 0.001].map((eps) => (
            <Chip
              key={eps}
              label={`ε = ${eps}`}
              size="small"
              variant={params.epsilon === eps ? 'filled' : 'outlined'}
              color={params.epsilon === eps ? 'primary' : 'default'}
              clickable
              onClick={() => patch({ epsilon: eps })}
            />
          ))}
        </Stack>
        <NumberField label="ε — константа различимости" value={params.epsilon} onChange={(epsilon) => patch({ epsilon })} />

        <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mb: 1 }}>
          {[0.1, 0.01].map((len) => (
            <Chip
              key={len}
              label={`l = ${len}`}
              size="small"
              variant={params.l === len ? 'filled' : 'outlined'}
              color={params.l === len ? 'secondary' : 'default'}
              clickable
              onClick={() => patch({ l: len })}
            />
          ))}
        </Stack>
        <NumberField label="l — длина конечного интервала" value={params.l} onChange={(l) => patch({ l })} />

        <Divider />

        <FormControl fullWidth size="small">
          <InputLabel>Метод оптимизации</InputLabel>
          <Select
            label="Метод оптимизации"
            value={params.method}
            onChange={(e) => patch({ method: e.target.value as OptimizationMethod })}
          >
            <MenuItem value="DICHOTOMY">Дихотомический поиск</MenuItem>
            <MenuItem value="GOLDEN_SECTION">Метод золотого сечения</MenuItem>
            <MenuItem value="FIBONACCI">Метод Фибоначчи</MenuItem>
          </Select>
        </FormControl>

        <Stack direction="row" spacing={1}>
          <Button variant="contained" fullWidth disabled={loading} onClick={onRun}>
            Рассчитать
          </Button>
          <Button variant="outlined" fullWidth disabled={loading} onClick={onCompareAll}>
            Сравнить методы
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}

export function buildDefaultParams(variants: Variant[]): OptimizationParams {
  const variant = variants[0];
  const preset = variant?.f1.presets[0];
  return {
    variantId: variant?.id ?? 1,
    functionId: 'F1',
    method: 'GOLDEN_SECTION',
    a: preset?.a ?? -2,
    b: preset?.b ?? 2,
    epsilon: 0.01,
    l: 0.1,
    minimize: preset?.minimize ?? true,
  };
}
