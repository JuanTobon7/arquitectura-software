import { Test, TestingModule } from '@nestjs/testing';
import { ContratoGestorMensajesService } from './contrato-gestor-mensajes.service.js';

describe('ContratoGestorMensajesService', () => {
  let service: ContratoGestorMensajesService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ContratoGestorMensajesService],
    }).compile();

    service = module.get<ContratoGestorMensajesService>(ContratoGestorMensajesService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
