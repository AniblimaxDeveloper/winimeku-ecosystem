import fastify from 'fastify';
import cors from 'fastify-cors';

const server = fastify({ logger: true });

server.register(cors, { origin: '*' });

// Health Check Endpoint
server.get('/api/v1/app/health', async (request, reply) => {
  return { 
    status: 'ok', 
    service: 'Winimeku Core System', 
    timestamp: new Date().toISOString() 
  };
});

// Placeholder untuk Auth & Endpoint lain (Akan diisi di Phase 2)
server.get('/api/v1/app/config', async (request, reply) => {
  return {
    appName: 'Wibinime',
    maintenanceMode: false,
    minimumVersion: '1.0.0'
  };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3000;
    await server.listen({ port, host: '0.0.0.0' });
    console.log(`Winimeku Backend berjalan di port ${port}`);
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
