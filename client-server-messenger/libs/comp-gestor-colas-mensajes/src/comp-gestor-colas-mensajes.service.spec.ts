import { Test, TestingModule } from '@nestjs/testing';
import { CompGestorColasMensajesService } from './comp-gestor-colas-mensajes.service.js';

describe('CompGestorColasMensajesService', () => {
  let service: CompGestorColasMensajesService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CompGestorColasMensajesService],
    }).compile();

    service = module.get<CompGestorColasMensajesService>(CompGestorColasMensajesService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
