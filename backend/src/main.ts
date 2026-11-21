import fastify from 'fastify';
import cors from 'fastify-cors';
import jwt from '@fastify/jwt';
import authRoutes from './routes/auth';
import { authenticate } from './middleware/auth';

const server = fastify({ logger: true });

server.register(cors, { origin: '*' });

// Daftarkan JWT
server.register(jwt, {
  secret: process.env.JWT_SECRET || 'rahasia-super-panjang-default'
});

// Dekorasi server dengan fungsi authenticate
server.decorate('authenticate', authenticate);

// Health Check
server.get('/api/v1/app/health', async () => ({ status: 'ok', service: 'Winimeku Core' }));

// Daftarkan Rute Auth
server.register(authRoutes, { prefix: '/api/v1/auth' });

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
