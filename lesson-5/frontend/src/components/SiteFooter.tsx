import { Box, Container, Divider, Typography } from '@mui/material';

interface Props {
  labTitle: string;
}

export function SiteFooter({ labTitle }: Props) {
  return (
    <Box
      component="footer"
      sx={{ mt: 'auto', py: 2, bgcolor: 'background.paper', borderTop: 1, borderColor: 'divider' }}
    >
      <Container maxWidth="xl">
        <Divider sx={{ mb: 2 }} />
        <Typography variant="body2" color="text.secondary" align="center" gutterBottom>
          {labTitle}
        </Typography>
        <Typography variant="body2" color="text.secondary" align="center">
          Выполнено студентом Чибиток Д.С., группа М30-108-СВ
        </Typography>
      </Container>
    </Box>
  );
}
