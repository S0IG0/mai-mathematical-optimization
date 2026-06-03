import { createTheme } from '@mui/material/styles';

export const scrollbarSx = {
  scrollbarWidth: 'thin',
  scrollbarColor: '#484f58 transparent',
  '&::-webkit-scrollbar': {
    width: 8,
    height: 8,
  },
  '&::-webkit-scrollbar-track': {
    backgroundColor: 'transparent',
    borderRadius: 4,
  },
  '&::-webkit-scrollbar-thumb': {
    backgroundColor: '#30363d',
    borderRadius: 4,
    border: '2px solid transparent',
    backgroundClip: 'content-box',
  },
  '&::-webkit-scrollbar-thumb:hover': {
    backgroundColor: '#484f58',
  },
  '&::-webkit-scrollbar-thumb:active': {
    backgroundColor: '#5c9eff',
  },
  '&::-webkit-scrollbar-corner': {
    backgroundColor: 'transparent',
  },
} as const;

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#5c9eff' },
    secondary: { main: '#7ee8a2' },
    background: {
      default: '#0d1117',
      paper: '#161b22',
    },
  },
  typography: {
    fontFamily: '"Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  },
  shape: { borderRadius: 10 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        '*': {
          scrollbarWidth: 'thin',
          scrollbarColor: '#484f58 transparent',
        },
        '*::-webkit-scrollbar': {
          width: 8,
          height: 8,
        },
        '*::-webkit-scrollbar-track': {
          backgroundColor: 'transparent',
        },
        '*::-webkit-scrollbar-thumb': {
          backgroundColor: '#30363d',
          borderRadius: 4,
          border: '2px solid transparent',
          backgroundClip: 'content-box',
        },
        '*::-webkit-scrollbar-thumb:hover': {
          backgroundColor: '#484f58',
        },
        '*::-webkit-scrollbar-thumb:active': {
          backgroundColor: '#5c9eff',
        },
        '*::-webkit-scrollbar-corner': {
          backgroundColor: 'transparent',
        },
      },
    },
  },
});
