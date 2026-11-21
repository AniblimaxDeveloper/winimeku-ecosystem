import { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';
import { z } from 'zod';

const prisma = new PrismaClient();

export default async function authRoutes(server: FastifyInstance) {
  // Skema Validasi Input
  const authSchema = z.object({
    email: z.string().email(),
    password: z.string().min(6),
    name: z.string().optional()
  });

  // 1. ENDPOINT: REGISTER
  server.post('/register', async (request, reply) => {
    try {
      const { email, password } = authSchema.parse(request.body);
      
      const existingUser = await prisma.user.findUnique({ where: { email } });
      if (existingUser) return reply.status(400).send({ error: 'Email sudah terdaftar' });

      const passwordHash = await bcrypt.hash(password, 10);
      
      // OWNER BOOTSTRAP LOGIC
      // Jika email sama dengan OWNER_EMAIL di .env, jadikan SUPER_ADMIN
      const isOwner = email === process.env.OWNER_EMAIL;
      const role = isOwner ? 'SUPER_ADMIN' : 'USER';

      const user = await prisma.user.create({
        data: { email, passwordHash, role }
      });

      return reply.status(201).send({ 
        message: 'Registrasi berhasil', 
        user: { id: user.id, email: user.email, role: user.role } 
      });
    } catch (error: any) {
      return reply.status(400).send({ error: error.message || 'Input tidak valid' });
    }
  });

  // 2. ENDPOINT: LOGIN
  server.post('/login', async (request, reply) => {
    try {
      const { email, password } = authSchema.parse(request.body);

      const user = await prisma.user.findUnique({ where: { email } });
      if (!user || !user.passwordHash) {
        return reply.status(401).send({ error: 'Kredensial tidak valid' });
      }

      const isValid = await bcrypt.compare(password, user.passwordHash);
      if (!isValid) return reply.status(401).send({ error: 'Kredensial tidak valid' });

      // Buat Token JWT
      const token = server.jwt.sign(
        { id: user.id, email: user.email, role: user.role },
        { expiresIn: '7d' }
      );

      return reply.send({
        message: 'Login berhasil',
        token,
        user: { id: user.id, email: user.email, role: user.role }
      });
    } catch (error: any) {
      return reply.status(400).send({ error: 'Input tidak valid' });
    }
  });

  // 3. ENDPOINT: GET ME (Cek Profil Sendiri)
  server.get('/me', { preValidation: [server.authenticate] }, async (request, reply) => {
    const user = request.user as any;
    const profile = await prisma.user.findUnique({ 
      where: { id: user.id },
      select: { id: true, email: true, role: true, createdAt: true }
    });
    return reply.send(profile);
  });
}
