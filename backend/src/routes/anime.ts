import { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticate, authorize } from '../middleware/auth';

const prisma = new PrismaClient();

export default async function animeRoutes(server: FastifyInstance) {
  // Skema Validasi
  const animeSchema = z.object({
    title: z.string().min(1),
    synopsis: z.string().optional(),
    coverImage: z.string().url().optional(),
    status: z.enum(['ONGOING', 'COMPLETED', 'UPCOMING']).optional(),
    releaseYear: z.number().int().optional(),
  });

  // 1. ENDPOINT PUBLIC: Ambil semua anime (dengan pagination)
  server.get('/', async (request, reply) => {
    const query = request.query as { page?: string, limit?: string };
    const page = parseInt(query.page || '1');
    const limit = parseInt(query.limit || '10');
    const skip = (page - 1) * limit;

    try {
      const [animes, total] = await prisma.$transaction([
        prisma.anime.findMany({
          skip,
          take: limit,
          orderBy: { createdAt: 'desc' },
          include: { episodes: true } // Sertakan episode
        }),
        prisma.anime.count()
      ]);

      return reply.send({
        data: animes,
        meta: { page, limit, total, totalPages: Math.ceil(total / limit) }
      });
    } catch (error: any) {
      return reply.status(500).send({ error: error.message });
    }
  });

  // 2. ENDPOINT PUBLIC: Detail Anime berdasarkan ID
  server.get('/:id', async (request, reply) => {
    const { id } = request.params as { id: string };
    try {
      const anime = await prisma.anime.findUnique({
        where: { id },
        include: { episodes: { orderBy: { episodeNumber: 'asc' } } }
      });
      if (!anime) return reply.status(404).send({ error: 'Anime tidak ditemukan' });
      return reply.send(anime);
    } catch (error: any) {
      return reply.status(500).send({ error: error.message });
    }
  });

  // ==========================================
  // ENDPOINT ADMIN (Dilindungi JWT & RBAC)
  // ==========================================

  // 3. Buat Anime Baru
  server.post('/', { preValidation: [authenticate, authorize(['SUPER_ADMIN', 'ADMIN'])] }, async (request, reply) => {
    try {
      const data = animeSchema.parse(request.body);
      const anime = await prisma.anime.create({ data });
      return reply.status(201).send(anime);
    } catch (error: any) {
      return reply.status(400).send({ error: error.message || 'Input tidak valid' });
    }
  });

  // 4. Tambah Episode ke Anime
  server.post('/:id/episode', { preValidation: [authenticate, authorize(['SUPER_ADMIN', 'ADMIN'])] }, async (request, reply) => {
    const { id } = request.params as { id: string };
    const episodeSchema = z.object({
      episodeNumber: z.number().int(),
      title: z.string().optional(),
      videoUrl: z.string().url()
    });

    try {
      const data = episodeSchema.parse(request.body);
      const episode = await prisma.episode.create({
        data: { ...data, animeId: id }
      });
      return reply.status(201).send(episode);
    } catch (error: any) {
      return reply.status(400).send({ error: error.message || 'Input tidak valid' });
    }
  });
}
